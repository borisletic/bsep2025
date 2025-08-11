import { createStore } from 'vuex'
import api from '@/services/api'

// Import modules
import auth from './modules/auth'
import certificates from './modules/certificates'
import passwords from './modules/passwords'

const store = createStore({
  modules: {
    auth,
    certificates,
    passwords
  },
  state: {
    loading: false,
    error: null
  },
  getters: {
    isLoading: state => state.loading,
    error: state => state.error,
    // Auth getters (forwarded from auth module)
    isAuthenticated: state => state.auth.isAuthenticated,
    currentUser: state => state.auth.user,
    userRole: state => state.auth.user?.role || null,
    token: state => state.auth.token
  },
  mutations: {
    SET_LOADING(state, loading) {
      state.loading = loading
    },
    SET_ERROR(state, error) {
      state.error = error
    },
    CLEAR_ERROR(state) {
      state.error = null
    }
  },
  actions: {
    // Global actions can be added here
    clearError({ commit }) {
      commit('CLEAR_ERROR')
    }
  }
})

export default store