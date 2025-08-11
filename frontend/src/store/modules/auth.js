import api from '@/services/api'

const state = {
  user: null,
  token: localStorage.getItem('token') || null,
  isAuthenticated: !!localStorage.getItem('token'),
  loading: false,
  error: null
}

const getters = {
  currentUser: state => state.user,
  token: state => state.token,
  isAuthenticated: state => state.isAuthenticated,
  userRole: state => state.user?.role || null,
  isLoading: state => state.loading,
  error: state => state.error
}

const mutations = {
  SET_USER(state, user) {
    state.user = user
  },
  SET_TOKEN(state, token) {
    state.token = token
    state.isAuthenticated = !!token
    if (token) {
      localStorage.setItem('token', token)
    } else {
      localStorage.removeItem('token')
    }
  },
  SET_LOADING(state, loading) {
    state.loading = loading
  },
  SET_ERROR(state, error) {
    state.error = error
  },
  CLEAR_AUTH(state) {
    state.user = null
    state.token = null
    state.isAuthenticated = false
    localStorage.removeItem('token')
  }
}

const actions = {
  async login({ commit }, credentials) {
    commit('SET_LOADING', true)
    commit('SET_ERROR', null)
    
    try {
      const response = await api.post('/auth/login', credentials)
      const { token, user } = response.data.data
      
      commit('SET_TOKEN', token)
      commit('SET_USER', user)
      
      return response.data
    } catch (error) {
      commit('SET_ERROR', error.response?.data?.message || 'Login failed')
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },

  async register({ commit }, userData) {
    commit('SET_LOADING', true)
    commit('SET_ERROR', null)
    
    try {
      const response = await api.post('/auth/register', userData)
      return response.data
    } catch (error) {
      commit('SET_ERROR', error.response?.data?.message || 'Registration failed')
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },

  async fetchUserProfile({ commit }) {
    commit('SET_LOADING', true)
    
    try {
      const response = await api.get('/auth/profile')
      commit('SET_USER', response.data.data)
      return response.data
    } catch (error) {
      commit('SET_ERROR', error.response?.data?.message || 'Failed to fetch profile')
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },

  async logout({ commit }) {
    commit('SET_LOADING', true)
    
    try {
      await api.post('/auth/logout')
    } catch (error) {
      console.error('Logout API call failed:', error)
    } finally {
      commit('CLEAR_AUTH')
      commit('SET_LOADING', false)
    }
  },

  // Initialize auth state on app load
  initializeAuth({ commit, dispatch }) {
    const token = localStorage.getItem('token')
    if (token) {
      commit('SET_TOKEN', token)
      // Optionally fetch user profile
      dispatch('fetchUserProfile').catch(() => {
        // If profile fetch fails, clear auth
        commit('CLEAR_AUTH')
      })
    }
  }
}

export default {
  namespaced: false, // Keep global for backward compatibility
  state,
  getters,
  mutations,
  actions
}