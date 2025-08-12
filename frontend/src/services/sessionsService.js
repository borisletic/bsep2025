import api from './api'

export default {
  async getActiveSessions() {
    const response = await api.get('/tokens/active')
    return response.data
  },

  async revokeSession(tokenId) {
    const response = await api.delete(`/tokens/${tokenId}`)
    return response.data
  },

  async revokeAllOtherSessions() {
    const response = await api.delete('/tokens/all-others')
    return response.data
  }
}