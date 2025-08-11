import passwordService from '@/services/passwordService'

const state = {
  passwordEntries: [],
  currentEntry: null,
  loading: false,
  error: null
}

const getters = {
  allPasswordEntries: state => state.passwordEntries,
  currentEntry: state => state.currentEntry,
  ownedEntries: state => state.passwordEntries.filter(entry => entry.isOwner),
  sharedEntries: state => state.passwordEntries.filter(entry => !entry.isOwner),
  isLoading: state => state.loading,
  error: state => state.error
}

const mutations = {
  SET_PASSWORD_ENTRIES(state, entries) {
    state.passwordEntries = entries
  },
  SET_CURRENT_ENTRY(state, entry) {
    state.currentEntry = entry
  },
  ADD_PASSWORD_ENTRY(state, entry) {
    state.passwordEntries.unshift(entry)
  },
  UPDATE_PASSWORD_ENTRY(state, updatedEntry) {
    const index = state.passwordEntries.findIndex(entry => entry.id === updatedEntry.id)
    if (index !== -1) {
      state.passwordEntries.splice(index, 1, updatedEntry)
    }
  },
  REMOVE_PASSWORD_ENTRY(state, entryId) {
    state.passwordEntries = state.passwordEntries.filter(entry => entry.id !== entryId)
  },
  SET_LOADING(state, loading) {
    state.loading = loading
  },
  SET_ERROR(state, error) {
    state.error = error
  }
}

const actions = {
  async fetchPasswordEntries({ commit }) {
    commit('SET_LOADING', true)
    commit('SET_ERROR', null)
    try {
      const response = await passwordService.getPasswordEntries()
      commit('SET_PASSWORD_ENTRIES', response.data)
      return response.data
    } catch (error) {
      commit('SET_ERROR', error.response?.data?.message || 'Failed to fetch password entries')
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },

  async fetchPasswordEntry({ commit }, id) {
    commit('SET_LOADING', true)
    commit('SET_ERROR', null)
    try {
      const response = await passwordService.getPasswordEntry(id)
      commit('SET_CURRENT_ENTRY', response.data)
      return response.data
    } catch (error) {
      commit('SET_ERROR', error.response?.data?.message || 'Failed to fetch password entry')
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },

  async createPasswordEntry({ commit }, entryData) {
    commit('SET_LOADING', true)
    commit('SET_ERROR', null)
    try {
      const response = await passwordService.createPasswordEntry(entryData)
      commit('ADD_PASSWORD_ENTRY', response.data)
      return response.data
    } catch (error) {
      commit('SET_ERROR', error.response?.data?.message || 'Failed to create password entry')
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },

  async updatePasswordEntry({ commit }, { id, entryData }) {
    commit('SET_LOADING', true)
    commit('SET_ERROR', null)
    try {
      const response = await passwordService.updatePasswordEntry(id, entryData)
      commit('UPDATE_PASSWORD_ENTRY', response.data)
      return response.data
    } catch (error) {
      commit('SET_ERROR', error.response?.data?.message || 'Failed to update password entry')
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },

  async deletePasswordEntry({ commit }, id) {
    commit('SET_LOADING', true)
    commit('SET_ERROR', null)
    try {
      await passwordService.deletePasswordEntry(id)
      commit('REMOVE_PASSWORD_ENTRY', id)
    } catch (error) {
      commit('SET_ERROR', error.response?.data?.message || 'Failed to delete password entry')
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },

  async sharePassword({ commit }, { id, shareData }) {
    commit('SET_LOADING', true)
    commit('SET_ERROR', null)
    try {
      await passwordService.sharePassword(id, shareData)
      // Refresh the entry to get updated share information
      const response = await passwordService.getPasswordEntry(id)
      commit('UPDATE_PASSWORD_ENTRY', response.data)
      return response.data
    } catch (error) {
      commit('SET_ERROR', error.response?.data?.message || 'Failed to share password')
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },

  async removePasswordShare({ commit }, { id, userEmail }) {
    commit('SET_LOADING', true)
    commit('SET_ERROR', null)
    try {
      await passwordService.removePasswordShare(id, userEmail)
      // Refresh the entry to get updated share information
      const response = await passwordService.getPasswordEntry(id)
      commit('UPDATE_PASSWORD_ENTRY', response.data)
      return response.data
    } catch (error) {
      commit('SET_ERROR', error.response?.data?.message || 'Failed to remove password share')
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },

  async searchPasswordEntries({ commit }, query) {
    commit('SET_LOADING', true)
    commit('SET_ERROR', null)
    try {
      const response = await passwordService.searchPasswordEntries(query)
      commit('SET_PASSWORD_ENTRIES', response.data)
      return response.data
    } catch (error) {
      commit('SET_ERROR', error.response?.data?.message || 'Failed to search password entries')
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  }
}

export default {
  namespaced: true,
  state,
  getters,
  mutations,
  actions
}