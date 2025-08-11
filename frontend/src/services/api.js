import axios from 'axios'
import router from '@/router'
import store from '@/store'

// Use Vite's environment variables instead of process.env
const API_BASE_URL = import.meta.env.VITE_API_URL || 'https://localhost:8080/api'

const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  },
  // Handle self-signed certificates in development
  httpsAgent: false
})

// Request interceptor
api.interceptors.request.use(
  (config) => {
    const token = store.getters.token || localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Response interceptor
api.interceptors.response.use(
  (response) => {
    return response
  },
  (error) => {
    if (error.response?.status === 401) {
      store.dispatch('logout')
      router.push('/login')
    }
    return Promise.reject(error)
  }
)

export default api