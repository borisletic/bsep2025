import api from './api'

export default {
  async generateCaptcha() {
    const response = await api.get('/captcha/generate')
    return response.data
  },

  async verifyCaptcha(sessionId, answer) {
    const response = await api.post('/captcha/verify', {
      sessionId,
      answer
    })
    return response.data
  }
}