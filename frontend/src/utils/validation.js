export const validationUtils = {
  // Email validation
  isValidEmail(email) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    return emailRegex.test(email)
  },

  // Password validation
  isValidPassword(password) {
    return password && password.length >= 8
  },

  // Common Name validation for certificates
  isValidCommonName(cn) {
    const cnRegex = /^[a-zA-Z0-9\-\.\*]+$/
    return cnRegex.test(cn)
  },

  // Organization validation
  isValidOrganization(org) {
    return org && org.trim().length > 0
  },

  // Country code validation (2-letter ISO codes)
  isValidCountryCode(country) {
    const countryRegex = /^[A-Z]{2}$/
    return countryRegex.test(country)
  },

  // Required field validation
  isRequired(value) {
    return value !== null && value !== undefined && value.toString().trim() !== ''
  },

  // Number validation
  isValidNumber(value, min = null, max = null) {
    const num = Number(value)
    if (isNaN(num)) return false
    if (min !== null && num < min) return false
    if (max !== null && num > max) return false
    return true
  },

  // URL validation
  isValidURL(url) {
    try {
      new URL(url)
      return true
    } catch {
      return false
    }
  }
}