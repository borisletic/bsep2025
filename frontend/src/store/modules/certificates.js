import certificateService from '@/services/certificateService'

const state = {
  certificates: [],
  currentCertificate: null,
  caCertificates: [],
  loading: false,
  error: null
}

const getters = {
  allCertificates: state => state.certificates,
  currentCertificate: state => state.currentCertificate,
  caCertificates: state => state.caCertificates,
  activeCertificates: state => state.certificates.filter(cert => 
    !cert.revoked && new Date(cert.validTo) > new Date()
  ),
  expiringCertificates: state => {
    const thirtyDaysFromNow = new Date()
    thirtyDaysFromNow.setDate(thirtyDaysFromNow.getDate() + 30)
    return state.certificates.filter(cert => 
      !cert.revoked && 
      new Date(cert.validTo) > new Date() && 
      new Date(cert.validTo) <= thirtyDaysFromNow
    )
  },
  revokedCertificates: state => state.certificates.filter(cert => cert.revoked),
  isLoading: state => state.loading,
  error: state => state.error
}

const mutations = {
  SET_CERTIFICATES(state, certificates) {
    state.certificates = certificates
  },
  SET_CURRENT_CERTIFICATE(state, certificate) {
    state.currentCertificate = certificate
  },
  SET_CA_CERTIFICATES(state, certificates) {
    state.caCertificates = certificates
  },
  ADD_CERTIFICATE(state, certificate) {
    state.certificates.unshift(certificate)
  },
  UPDATE_CERTIFICATE(state, updatedCertificate) {
    const index = state.certificates.findIndex(cert => cert.id === updatedCertificate.id)
    if (index !== -1) {
      state.certificates.splice(index, 1, updatedCertificate)
    }
  },
  REMOVE_CERTIFICATE(state, certificateId) {
    state.certificates = state.certificates.filter(cert => cert.id !== certificateId)
  },
  SET_LOADING(state, loading) {
    state.loading = loading
  },
  SET_ERROR(state, error) {
    state.error = error
  }
}

const actions = {
  async fetchCertificates({ commit }) {
    commit('SET_LOADING', true)
    commit('SET_ERROR', null)
    try {
      const response = await certificateService.getCertificates()
      commit('SET_CERTIFICATES', response.data)
      
      // Filter CA certificates for dropdown usage
      const caCertificates = response.data.filter(cert => 
        (cert.certificateType === 'ROOT' || cert.certificateType === 'INTERMEDIATE') &&
        !cert.revoked &&
        new Date(cert.validTo) > new Date()
      )
      commit('SET_CA_CERTIFICATES', caCertificates)
      
      return response.data
    } catch (error) {
      commit('SET_ERROR', error.response?.data?.message || 'Failed to fetch certificates')
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },

  async fetchCertificate({ commit }, id) {
    commit('SET_LOADING', true)
    commit('SET_ERROR', null)
    try {
      const response = await certificateService.getCertificate(id)
      commit('SET_CURRENT_CERTIFICATE', response.data)
      return response.data
    } catch (error) {
      commit('SET_ERROR', error.response?.data?.message || 'Failed to fetch certificate')
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },

  async issueCertificate({ commit }, certificateData) {
    commit('SET_LOADING', true)
    commit('SET_ERROR', null)
    try {
      const response = await certificateService.issueCertificate(certificateData)
      commit('ADD_CERTIFICATE', response.data)
      return response.data
    } catch (error) {
      commit('SET_ERROR', error.response?.data?.message || 'Failed to issue certificate')
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },

  async uploadCSR({ commit }, formData) {
    commit('SET_LOADING', true)
    commit('SET_ERROR', null)
    try {
      const response = await certificateService.uploadCSR(formData)
      commit('ADD_CERTIFICATE', response.data)
      return response.data
    } catch (error) {
      commit('SET_ERROR', error.response?.data?.message || 'Failed to upload CSR')
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },

  async revokeCertificate({ commit }, { id, revocationData }) {
    commit('SET_LOADING', true)
    commit('SET_ERROR', null)
    try {
      const response = await certificateService.revokeCertificate(id, revocationData)
      commit('UPDATE_CERTIFICATE', response.data)
      return response.data
    } catch (error) {
      commit('SET_ERROR', error.response?.data?.message || 'Failed to revoke certificate')
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },

  async downloadCertificate({ commit }, id) {
    commit('SET_LOADING', true)
    commit('SET_ERROR', null)
    try {
      const response = await certificateService.downloadCertificate(id)
      return response
    } catch (error) {
      commit('SET_ERROR', error.response?.data?.message || 'Failed to download certificate')
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