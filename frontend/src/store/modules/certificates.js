import api from '@/services/api'

const state = {
  certificates: [],
  caCertificates: [],
  loading: false,
  error: null
}

const getters = {
  allCertificates: state => state.certificates,
  caCertificates: state => state.caCertificates,
  isLoading: state => state.loading,
  error: state => state.error,
  getCertificateById: state => id => state.certificates.find(cert => cert.id === id)
}

const mutations = {
  SET_LOADING(state, loading) {
    state.loading = loading
  },
  SET_ERROR(state, error) {
    state.error = error
  },
  SET_CERTIFICATES(state, certificates) {
    state.certificates = certificates
  },
  SET_CA_CERTIFICATES(state, certificates) {
    state.caCertificates = certificates
  },
  ADD_CERTIFICATE(state, certificate) {
    state.certificates.push(certificate)
  },
  UPDATE_CERTIFICATE(state, updatedCertificate) {
    const index = state.certificates.findIndex(cert => cert.id === updatedCertificate.id)
    if (index !== -1) {
      state.certificates.splice(index, 1, updatedCertificate)
    }
  },
  REMOVE_CERTIFICATE(state, certificateId) {
    state.certificates = state.certificates.filter(cert => cert.id !== certificateId)
  }
}

const actions = {
  async fetchCertificates({ commit }) {
    try {
      commit('SET_LOADING', true)
      commit('SET_ERROR', null)
      
      const response = await api.get('/certificates')
      commit('SET_CERTIFICATES', response.data.data)
      return response.data.data
    } catch (error) {
      const message = error.response?.data?.message || 'Failed to fetch certificates'
      commit('SET_ERROR', message)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },

  async fetchCACertificates({ commit }) {
    try {
      const response = await api.get('/certificates/ca')
      commit('SET_CA_CERTIFICATES', response.data.data)
      return response.data.data
    } catch (error) {
      const message = error.response?.data?.message || 'Failed to fetch CA certificates'
      commit('SET_ERROR', message)
      throw error
    }
  },

  async createCertificate({ commit }, certificateData) {
    try {
      commit('SET_LOADING', true)
      commit('SET_ERROR', null)
      
      const response = await api.post('/certificates', certificateData)
      commit('ADD_CERTIFICATE', response.data.data)
      return response.data.data
    } catch (error) {
      const message = error.response?.data?.message || 'Failed to create certificate'
      commit('SET_ERROR', message)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },

  async revokeCertificate({ commit }, { id, reason }) {
    try {
      commit('SET_LOADING', true)
      
      const response = await api.post(`/certificates/${id}/revoke`, { reason })
      commit('UPDATE_CERTIFICATE', response.data.data)
      return response.data.data
    } catch (error) {
      const message = error.response?.data?.message || 'Failed to revoke certificate'
      commit('SET_ERROR', message)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },

  async downloadCertificate({ commit }, certificateId) {
    try {
      const response = await api.get(`/certificates/${certificateId}/download`, {
        responseType: 'blob'
      })
      
      // Create download link
      const url = window.URL.createObjectURL(new Blob([response.data]))
      const link = document.createElement('a')
      link.href = url
      link.setAttribute('download', `certificate_${certificateId}.crt`)
      document.body.appendChild(link)
      link.click()
      link.remove()
      window.URL.revokeObjectURL(url)
      
      return true
    } catch (error) {
      const message = error.response?.data?.message || 'Failed to download certificate'
      commit('SET_ERROR', message)
      throw error
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