import api from './api'

export default {
  async getProfile() {
    const response = await api.get('/users/profile')
    return response.data
  },

  async getActiveTokens() {
    const response = await api.get('/users/tokens')
    return response.data
  },

  async revokeToken(tokenId) {
    const response = await api.post(`/users/tokens/${tokenId}/revoke`)
    return response.data
  },

  async revokeAllTokens() {
    const response = await api.post('/users/tokens/revoke-all')
    return response.data
  }
}