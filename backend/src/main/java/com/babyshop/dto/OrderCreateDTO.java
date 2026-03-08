package com.babyshop.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class OrderCreateDTO {
    @NotNull(message = "地址ID不能为空")
    private Long addressId;
    private String remark;
    /** 购物车商品ID列表（从购物车下单时传），为空则结算全部已选中商品 */
    private List<Long> cartIds;
}
