import api from './api'

export default {
  async createCAUser(userData) {
    const response = await api.post('/admin/ca-users', userData)
    return response.data
  },

  async getAllUsers() {
    const response = await api.get('/admin/users')
    return response.data
  },

  async getUser(id) {
    const response = await api.get(`/admin/users/${id}`)
    return response.data
  },

  async deleteUser(id) {
    const response = await api.delete(`/admin/users/${id}`)
    return response.data
  }
}