import api from '@/services/api'

const state = {
  passwords: [],
  loading: false,
  error: null
}

const getters = {
  allPasswords: state => state.passwords,
  isLoading: state => state.loading,
  error: state => state.error,
  getPasswordById: state => id => state.passwords.find(pass => pass.id === id)
}

const mutations = {
  SET_LOADING(state, loading) {
    state.loading = loading
  },
  SET_ERROR(state, error) {
    state.error = error
  },
  SET_PASSWORDS(state, passwords) {
    state.passwords = passwords
  },
  ADD_PASSWORD(state, password) {
    state.passwords.push(password)
  },
  UPDATE_PASSWORD(state, updatedPassword) {
    const index = state.passwords.findIndex(pass => pass.id === updatedPassword.id)
    if (index !== -1) {
      state.passwords.splice(index, 1, updatedPassword)
    }
  },
  REMOVE_PASSWORD(state, passwordId) {
    state.passwords = state.passwords.filter(pass => pass.id !== passwordId)
  }
}

const actions = {
  async fetchPasswords({ commit }) {
    try {
      commit('SET_LOADING', true)
      commit('SET_ERROR', null)
      
      const response = await api.get('/passwords')
      commit('SET_PASSWORDS', response.data.data)
      return response.data.data
    } catch (error) {
      const message = error.response?.data?.message || 'Failed to fetch passwords'
      commit('SET_ERROR', message)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },

  async createPassword({ commit }, passwordData) {
    try {
      commit('SET_LOADING', true)
      commit('SET_ERROR', null)
      
      const response = await api.post('/passwords', passwordData)
      commit('ADD_PASSWORD', response.data.data)
      return response.data.data
    } catch (error) {
      const message = error.response?.data?.message || 'Failed to create password entry'
      commit('SET_ERROR', message)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },

  async updatePassword({ commit }, { id, passwordData }) {
    try {
      commit('SET_LOADING', true)
      
      const response = await api.put(`/passwords/${id}`, passwordData)
      commit('UPDATE_PASSWORD', response.data.data)
      return response.data.data
    } catch (error) {
      const message = error.response?.data?.message || 'Failed to update password entry'
      commit('SET_ERROR', message)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },

  async deletePassword({ commit }, passwordId) {
    try {
      commit('SET_LOADING', true)
      
      await api.delete(`/passwords/${passwordId}`)
      commit('REMOVE_PASSWORD', passwordId)
      return true
    } catch (error) {
      const message = error.response?.data?.message || 'Failed to delete password entry'
      commit('SET_ERROR', message)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },

  async sharePassword({ commit }, { id, shareData }) {
    try {
      commit('SET_LOADING', true)
      
      const response = await api.post(`/passwords/${id}/share`, shareData)
      commit('UPDATE_PASSWORD', response.data.data)
      return response.data.data
    } catch (error) {
      const message = error.response?.data?.message || 'Failed to share password'
      commit('SET_ERROR', message)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },

  clearError({ commit }) {
    commit('SET_ERROR', null)
  }
}

export default {
  namespaced: true,
  state,
  getters,
  mutations,
  actions
}