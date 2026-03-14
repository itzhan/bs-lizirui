package com.babyshop.service;

import com.babyshop.entity.Product;
import com.babyshop.entity.UserBehavior;
import com.babyshop.mapper.UserBehaviorMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 协同过滤推荐服务
 * 基于物品的协同过滤算法（Item-Based Collaborative Filtering）
 *
 * 核心思路：
 * 1. 构建用户-商品评分矩阵（基于用户行为数据）
 * 2. 计算商品之间的余弦相似度
 * 3. 根据用户历史行为，推荐与其偏好商品相似的其他商品
 * 4. 冷启动降级：无行为数据时推荐热销商品
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendService {

    private final UserBehaviorMapper userBehaviorMapper;
    private final ProductService productService;

    /**
     * 获取个性化推荐商品（协同过滤主入口）
     *
     * @param userId 当前用户ID（可为 null，表示未登录）
     * @param size   推荐数量
     * @return 推荐的商品列表
     */
    public List<Product> getRecommendations(Long userId, int size) {
        // 1. 未登录或无行为数据时，降级为热销推荐
        if (userId == null) {
            log.info("用户未登录，降级为热销商品推荐");
            return getHotProducts(size);
        }

        // 2. 获取该用户的所有行为记录
        List<UserBehavior> userBehaviors = userBehaviorMapper.selectList(
                new LambdaQueryWrapper<UserBehavior>()
                        .eq(UserBehavior::getUserId, userId)
        );

        if (userBehaviors.isEmpty()) {
            log.info("用户[{}]无行为数据，降级为热销商品推荐", userId);
            return getHotProducts(size);
        }

        // 3. 获取用户已交互的商品及对应的最高评分
        Map<Long, Double> userItemScores = new HashMap<>();
        for (UserBehavior b : userBehaviors) {
            userItemScores.merge(b.getProductId(), b.getScore(), Math::max);
        }

        // 4. 构建全局用户-商品评分矩阵
        Map<Long, Map<Long, Double>> userItemMatrix = buildUserItemMatrix();

        // 5. 计算商品之间的相似度矩阵
        Map<Long, Map<Long, Double>> itemSimilarityMatrix = computeItemSimilarityMatrix(userItemMatrix);

        // 6. 为用户生成推荐评分
        Map<Long, Double> recommendScores = new HashMap<>();
        Set<Long> userInteractedItems = userItemScores.keySet();

        for (Map.Entry<Long, Double> entry : userItemScores.entrySet()) {
            Long itemId = entry.getKey();
            double userScore = entry.getValue();

            // 获取该商品的相似商品
            Map<Long, Double> similarities = itemSimilarityMatrix.getOrDefault(itemId, Collections.emptyMap());

            for (Map.Entry<Long, Double> simEntry : similarities.entrySet()) {
                Long candidateItemId = simEntry.getKey();
                double similarity = simEntry.getValue();

                // 跳过用户已交互过的商品
                if (userInteractedItems.contains(candidateItemId)) {
                    continue;
                }

                // 累加推荐评分：∑(相似度 × 用户对已交互商品的评分)
                recommendScores.merge(candidateItemId, similarity * userScore, Double::sum);
            }
        }

        if (recommendScores.isEmpty()) {
            log.info("用户[{}]协同过滤无推荐结果，降级为热销商品推荐", userId);
            return getHotProducts(size);
        }

        // 7. 按推荐评分排序，取 Top-N
        List<Long> recommendedIds = recommendScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(size)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // 8. 查询商品详情并保持排序
        List<Product> products = productService.listByIds(recommendedIds);
        // 只返回上架商品
        products = products.stream()
                .filter(p -> p.getStatus() == 1)
                .collect(Collectors.toList());

        // 保持推荐评分排序
        Map<Long, Integer> idOrder = new HashMap<>();
        for (int i = 0; i < recommendedIds.size(); i++) {
            idOrder.put(recommendedIds.get(i), i);
        }
        products.sort(Comparator.comparingInt(p -> idOrder.getOrDefault(p.getId(), Integer.MAX_VALUE)));

        // 如果推荐数量不足，用热销商品补充
        if (products.size() < size) {
            Set<Long> existingIds = products.stream().map(Product::getId).collect(Collectors.toSet());
            existingIds.addAll(userInteractedItems);
            List<Product> hotProducts = getHotProducts(size - products.size() + 10);
            for (Product hp : hotProducts) {
                if (!existingIds.contains(hp.getId()) && products.size() < size) {
                    products.add(hp);
                    existingIds.add(hp.getId());
                }
            }
        }

        log.info("用户[{}]协同过滤推荐成功，返回{}个商品", userId, products.size());
        return products.stream().limit(size).collect(Collectors.toList());
    }

    /**
     * 获取与指定商品相似的商品
     *
     * @param productId 商品ID
     * @param size      推荐数量
     * @return 相似商品列表
     */
    public List<Product> getSimilarProducts(Long productId, int size) {
        // 1. 构建用户-商品评分矩阵
        Map<Long, Map<Long, Double>> userItemMatrix = buildUserItemMatrix();

        if (userItemMatrix.isEmpty()) {
            // 无行为数据，退化为同分类商品
            return getFallbackSimilarProducts(productId, size);
        }

        // 2. 计算该商品与所有其他商品的相似度
        Map<Long, Double> similarities = computeItemSimilarities(productId, userItemMatrix);

        if (similarities.isEmpty()) {
            return getFallbackSimilarProducts(productId, size);
        }

        // 3. 按相似度排序取 Top-N
        List<Long> similarIds = similarities.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(size)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        List<Product> products = productService.listByIds(similarIds);
        products = products.stream()
                .filter(p -> p.getStatus() == 1)
                .collect(Collectors.toList());

        // 保持相似度排序
        Map<Long, Integer> idOrder = new HashMap<>();
        for (int i = 0; i < similarIds.size(); i++) {
            idOrder.put(similarIds.get(i), i);
        }
        products.sort(Comparator.comparingInt(p -> idOrder.getOrDefault(p.getId(), Integer.MAX_VALUE)));

        // 不足时用同分类商品补充
        if (products.size() < size) {
            Set<Long> existingIds = products.stream().map(Product::getId).collect(Collectors.toSet());
            existingIds.add(productId);
            List<Product> fallback = getFallbackSimilarProducts(productId, size);
            for (Product fp : fallback) {
                if (!existingIds.contains(fp.getId()) && products.size() < size) {
                    products.add(fp);
                    existingIds.add(fp.getId());
                }
            }
        }

        return products.stream().limit(size).collect(Collectors.toList());
    }

    // ==================== 协同过滤核心算法 ====================

    /**
     * 构建用户-商品评分矩阵
     * 结构: Map<userId, Map<productId, score>>
     * 对于同一用户的同一商品的多次行为，取最高评分
     */
    private Map<Long, Map<Long, Double>> buildUserItemMatrix() {
        List<UserBehavior> allBehaviors = userBehaviorMapper.selectList(null);

        Map<Long, Map<Long, Double>> matrix = new HashMap<>();
        for (UserBehavior b : allBehaviors) {
            matrix.computeIfAbsent(b.getUserId(), k -> new HashMap<>())
                    .merge(b.getProductId(), b.getScore(), Math::max);
        }

        log.debug("构建用户-商品评分矩阵完成：{}个用户", matrix.size());
        return matrix;
    }

    /**
     * 计算全局商品相似度矩阵
     * 结构: Map<itemA, Map<itemB, similarity>>
     */
    private Map<Long, Map<Long, Double>> computeItemSimilarityMatrix(
            Map<Long, Map<Long, Double>> userItemMatrix) {

        // 反转矩阵：商品 -> 对该商品有行为的用户及评分
        Map<Long, Map<Long, Double>> itemUserMatrix = new HashMap<>();
        for (Map.Entry<Long, Map<Long, Double>> userEntry : userItemMatrix.entrySet()) {
            Long userId = userEntry.getKey();
            for (Map.Entry<Long, Double> itemEntry : userEntry.getValue().entrySet()) {
                itemUserMatrix.computeIfAbsent(itemEntry.getKey(), k -> new HashMap<>())
                        .put(userId, itemEntry.getValue());
            }
        }

        // 计算每一对商品的余弦相似度
        Map<Long, Map<Long, Double>> similarityMatrix = new HashMap<>();
        List<Long> itemIds = new ArrayList<>(itemUserMatrix.keySet());

        for (int i = 0; i < itemIds.size(); i++) {
            Long itemA = itemIds.get(i);
            Map<Long, Double> usersA = itemUserMatrix.get(itemA);

            for (int j = i + 1; j < itemIds.size(); j++) {
                Long itemB = itemIds.get(j);
                Map<Long, Double> usersB = itemUserMatrix.get(itemB);

                double similarity = cosineSimilarity(usersA, usersB);

                if (similarity > 0) {
                    similarityMatrix.computeIfAbsent(itemA, k -> new HashMap<>()).put(itemB, similarity);
                    similarityMatrix.computeIfAbsent(itemB, k -> new HashMap<>()).put(itemA, similarity);
                }
            }
        }

        log.debug("计算商品相似度矩阵完成：{}个商品", itemIds.size());
        return similarityMatrix;
    }

    /**
     * 计算指定商品与所有其他商品的相似度
     */
    private Map<Long, Double> computeItemSimilarities(Long targetItemId,
            Map<Long, Map<Long, Double>> userItemMatrix) {

        // 反转矩阵
        Map<Long, Map<Long, Double>> itemUserMatrix = new HashMap<>();
        for (Map.Entry<Long, Map<Long, Double>> userEntry : userItemMatrix.entrySet()) {
            Long userId = userEntry.getKey();
            for (Map.Entry<Long, Double> itemEntry : userEntry.getValue().entrySet()) {
                itemUserMatrix.computeIfAbsent(itemEntry.getKey(), k -> new HashMap<>())
                        .put(userId, itemEntry.getValue());
            }
        }

        Map<Long, Double> targetUsers = itemUserMatrix.get(targetItemId);
        if (targetUsers == null || targetUsers.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Long, Double> similarities = new HashMap<>();
        for (Map.Entry<Long, Map<Long, Double>> entry : itemUserMatrix.entrySet()) {
            Long itemId = entry.getKey();
            if (itemId.equals(targetItemId)) continue;

            double similarity = cosineSimilarity(targetUsers, entry.getValue());
            if (similarity > 0) {
                similarities.put(itemId, similarity);
            }
        }

        return similarities;
    }

    /**
     * 余弦相似度计算
     *
     * cos(A, B) = (A · B) / (||A|| × ||B||)
     *
     * 其中 A、B 为两个商品的用户评分向量
     *
     * @param vectorA 向量A: Map<userId, score>
     * @param vectorB 向量B: Map<userId, score>
     * @return 余弦相似度值 [0, 1]
     */
    private double cosineSimilarity(Map<Long, Double> vectorA, Map<Long, Double> vectorB) {
        // 计算点积（仅计算两个向量都有值的维度）
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        // 找共同用户
        Set<Long> commonUsers = new HashSet<>(vectorA.keySet());
        commonUsers.retainAll(vectorB.keySet());

        if (commonUsers.isEmpty()) {
            return 0.0;
        }

        for (Long userId : commonUsers) {
            double a = vectorA.get(userId);
            double b = vectorB.get(userId);
            dotProduct += a * b;
        }

        // 计算向量模长
        for (double val : vectorA.values()) {
            normA += val * val;
        }
        for (double val : vectorB.values()) {
            normB += val * val;
        }

        normA = Math.sqrt(normA);
        normB = Math.sqrt(normB);

        if (normA == 0 || normB == 0) {
            return 0.0;
        }

        return dotProduct / (normA * normB);
    }

    // ==================== 降级策略 ====================

    /**
     * 热销商品推荐（冷启动降级）
     */
    private List<Product> getHotProducts(int size) {
        return productService.listHotProducts(1, size).getRecords();
    }

    /**
     * 同分类商品推荐（相似商品的降级）
     */
    private List<Product> getFallbackSimilarProducts(Long productId, int size) {
        Product product = productService.getById(productId);
        if (product == null || product.getCategoryId() == null) {
            return getHotProducts(size);
        }
        return productService.listProducts(1, size + 1, product.getCategoryId(),
                null, null, 1, "sales", "desc").getRecords()
                .stream()
                .filter(p -> !p.getId().equals(productId))
                .limit(size)
                .collect(Collectors.toList());
    }
}
