import axios, { type AxiosInstance, type AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import router from '@/router'

const request: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 15000
})

request.interceptors.request.use((config) => {
  const user = useUserStore()
  if (user.token) config.headers.Authorization = `Bearer ${user.token}`
  config.headers['X-Trace-Id'] = Math.random().toString(36).slice(2, 12)
  return config
})

request.interceptors.response.use(
  (resp: AxiosResponse) => {
    const data = resp.data
    if (data && typeof data === 'object' && 'code' in data) {
      if (data.code === 0) return data.data
      ElMessage.error(data.message || '请求失败')
      return Promise.reject(new Error(data.message || 'Error'))
    }
    return data
  },
  (err) => {
    const status = err.response?.status
    const msg = err.response?.data?.message || err.message
    if (status === 401) {
      const user = useUserStore()
      user.logout()
      router.push('/login')
      ElMessage.error('登录已过期，请重新登录')
    } else {
      ElMessage.error(msg || '网络异常')
    }
    return Promise.reject(err)
  }
)

export default request
