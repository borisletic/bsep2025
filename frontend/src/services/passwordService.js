import api from './api'

export default {
  async getPasswordEntries() {
    const response = await api.get('/passwords')
    return response.data
  },

  async getPasswordEntry(id) {
    const response = await api.get(`/passwords/${id}`)
    return response.data
  },

  async createPasswordEntry(entryData) {
    const response = await api.post('/passwords', entryData)
    return response.data
  },

  async updatePasswordEntry(id, entryData) {
    const response = await api.put(`/passwords/${id}`, entryData)
    return response.data
  },

  async deletePasswordEntry(id) {
    const response = await api.delete(`/passwords/${id}`)
    return response.data
  },

  async sharePassword(id, shareData) {
    const response = await api.post(`/passwords/${id}/share`, shareData)
    return response.data
  },

  async removePasswordShare(id, userEmail) {
    const response = await api.delete(`/passwords/${id}/share/${userEmail}`)
    return response.data
  },

  async searchPasswordEntries(query) {
    const response = await api.get(`/passwords/search?query=${encodeURIComponent(query)}`)
    return response.data
  }
}