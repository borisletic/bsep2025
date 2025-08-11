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
    // Only keep global getters, remove duplicates
    isLoading: state => state.loading,
    globalError: state => state.error, // Renamed to avoid conflict
    
    // Auth getters are now accessed directly from auth module
    // These are removed to prevent duplicates:
    // isAuthenticated: state => state.auth.isAuthenticated,
    // currentUser: state => state.auth.user,
    // userRole: state => state.auth.user?.role || null,
    // token: state => state.auth.token
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