import api from '@/services/api'

const state = {
  user: null,
  token: localStorage.getItem('token'),
  isAuthenticated: !!localStorage.getItem('token'),
  loading: false,
  error: null
}

const getters = {
  isAuthenticated: state => state.isAuthenticated,
  currentUser: state => state.user,
  userRole: state => state.user?.role || null,
  token: state => state.token,
  isLoading: state => state.loading,
  error: state => state.error
}

const mutations = {
  SET_LOADING(state, loading) {
    state.loading = loading
  },
  SET_ERROR(state, error) {
    state.error = error
  },
  SET_AUTH_SUCCESS(state, { token, user }) {
    state.token = token
    state.user = user
    state.isAuthenticated = true
    state.error = null
    localStorage.setItem('token', token)
  },
  SET_USER(state, user) {
    state.user = user
  },
  CLEAR_AUTH(state) {
    state.token = null
    state.user = null
    state.isAuthenticated = false
    state.error = null
    localStorage.removeItem('token')
  }
}

const actions = {
  async login({ commit }, credentials) {
    try {
      commit('SET_LOADING', true)
      commit('SET_ERROR', null)
      
      const response = await api.post('/auth/login', credentials)
      const { token, user } = response.data.data
      
      commit('SET_AUTH_SUCCESS', { token, user })
      return response.data
    } catch (error) {
      const message = error.response?.data?.message || 'Login failed'
      commit('SET_ERROR', message)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },

  async register({ commit }, userData) {
    try {
      commit('SET_LOADING', true)
      commit('SET_ERROR', null)
      
      const response = await api.post('/auth/register', userData)
      return response.data
    } catch (error) {
      const message = error.response?.data?.message || 'Registration failed'
      commit('SET_ERROR', message)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },

  async fetchUserProfile({ commit }) {
    try {
      const response = await api.get('/users/profile')
      commit('SET_USER', response.data.data)
      return response.data.data
    } catch (error) {
      commit('CLEAR_AUTH')
      throw error
    }
  },

  async logout({ commit }) {
    commit('CLEAR_AUTH')
  },

  async requestPasswordReset({ commit }, email) {
    try {
      commit('SET_LOADING', true)
      const response = await api.post('/auth/password-reset', { email })
      return response.data
    } catch (error) {
      const message = error.response?.data?.message || 'Password reset failed'
      commit('SET_ERROR', message)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },

  async changePassword({ commit }, passwordData) {
    try {
      commit('SET_LOADING', true)
      const response = await api.post('/auth/password-change', passwordData)
      return response.data
    } catch (error) {
      const message = error.response?.data?.message || 'Password change failed'
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