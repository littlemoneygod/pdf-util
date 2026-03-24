// API 配置
const config = {
  // 开发环境
  development: {
    baseURL: 'http://localhost:9898'
  },
  // 生产环境
  production: {
    baseURL: 'http://localhost:9898' // 或者你的服务器IP
  }
}

// 根据环境变量选择配置
const env = process.env.NODE_ENV || 'development'
export const API_BASE_URL = config[env].baseURL

// 创建配置好的 axios 实例
import axios from 'axios'

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 60000,
  headers: {
    'Content-Type': 'application/json'
  }
})

export default apiClient