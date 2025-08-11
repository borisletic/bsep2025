import api from './api'

export default {
  async login(credentials) {
    const response = await api.post('/auth/login', credentials)
    return response.data
  },

  async register(userData) {
    const response = await api.post('/auth/register', userData)
    return response.data
  },

  async activateAccount(token) {
    const response = await api.get(`/auth/activate/${token}`)
    return response.data
  },

  async requestPasswordReset(email) {
    const response = await api.post('/auth/password-reset', { email })
    return response.data
  },

  async changePassword(data) {
    const response = await api.post('/auth/password-change', data)
    return response.data
  }
}