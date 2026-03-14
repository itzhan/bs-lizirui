import axios from 'axios'

const http = axios.create({ baseURL: '/api', timeout: 15000 })

http.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

http.interceptors.response.use(
  res => {
    const { code, message, data } = res.data
    if (code === 401) {
      localStorage.removeItem('token')
      window.location.hash = '#/login'
      return Promise.reject(new Error(message))
    }
    if (code !== 200) return Promise.reject(new Error(message || '请求失败'))
    return res.data
  },
  err => {
    if (err.response?.status === 401) {
      localStorage.removeItem('token')
      window.location.hash = '#/login'
    }
    return Promise.reject(err)
  }
)

export default http
