import api from './api'

export default {
  async getCertificates() {
    const response = await api.get('/certificates')
    return response.data
  },

  async getCertificate(id) {
    const response = await api.get(`/certificates/${id}`)
    return response.data
  },

  async issueCertificate(certificateData) {
    const response = await api.post('/certificates/issue', certificateData)
    return response.data
  },

  async uploadCSR(formData) {
    const response = await api.post('/certificates/upload-csr', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    return response.data
  },

  async revokeCertificate(id, revocationData) {
    const response = await api.post(`/certificates/${id}/revoke`, revocationData)
    return response.data
  },

  async downloadCertificate(id) {
    const response = await api.get(`/certificates/${id}/download`, {
      responseType: 'blob'
    })
    return response
  }
}