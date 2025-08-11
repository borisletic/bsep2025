const state = {
  sidebarVisible: false,
  theme: localStorage.getItem('theme') || 'light',
  notifications: [],
  globalLoading: false
}

const getters = {
  sidebarVisible: state => state.sidebarVisible,
  currentTheme: state => state.theme,
  notifications: state => state.notifications,
  isGlobalLoading: state => state.globalLoading
}

const mutations = {
  TOGGLE_SIDEBAR(state) {
    state.sidebarVisible = !state.sidebarVisible
  },
  SET_SIDEBAR_VISIBLE(state, visible) {
    state.sidebarVisible = visible
  },
  SET_THEME(state, theme) {
    state.theme = theme
    localStorage.setItem('theme', theme)
  },
  ADD_NOTIFICATION(state, notification) {
    state.notifications.push({
      id: Date.now(),
      ...notification,
      timestamp: new Date()
    })
  },
  REMOVE_NOTIFICATION(state, notificationId) {
    state.notifications = state.notifications.filter(n => n.id !== notificationId)
  },
  CLEAR_NOTIFICATIONS(state) {
    state.notifications = []
  },
  SET_GLOBAL_LOADING(state, loading) {
    state.globalLoading = loading
  }
}

const actions = {
  toggleSidebar({ commit }) {
    commit('TOGGLE_SIDEBAR')
  },
  
  setSidebarVisible({ commit }, visible) {
    commit('SET_SIDEBAR_VISIBLE', visible)
  },
  
  setTheme({ commit }, theme) {
    commit('SET_THEME', theme)
  },
  
  addNotification({ commit }, notification) {
    commit('ADD_NOTIFICATION', notification)
    
    // Auto-remove notification after specified time
    if (notification.autoRemove !== false) {
      setTimeout(() => {
        commit('REMOVE_NOTIFICATION', notification.id)
      }, notification.duration || 5000)
    }
  },
  
  removeNotification({ commit }, notificationId) {
    commit('REMOVE_NOTIFICATION', notificationId)
  },
  
  clearNotifications({ commit }) {
    commit('CLEAR_NOTIFICATIONS')
  },
  
  setGlobalLoading({ commit }, loading) {
    commit('SET_GLOBAL_LOADING', loading)
  }
}

export default {
  namespaced: true,
  state,
  getters,
  mutations,
  actions
}