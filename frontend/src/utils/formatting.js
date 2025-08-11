import moment from 'moment'

export const formatUtils = {
  // Date formatting
  formatDate(date, format = 'YYYY-MM-DD HH:mm:ss') {
    return moment(date).format(format)
  },

  // Relative time formatting
  formatRelativeTime(date) {
    return moment(date).fromNow()
  },

  // Certificate status formatting
  formatCertificateStatus(certificate) {
    if (certificate.revoked) {
      return { label: 'Revoked', severity: 'danger' }
    }
    
    const now = moment()
    const validTo = moment(certificate.validTo)
    const validFrom = moment(certificate.validFrom)
    
    if (now.isBefore(validFrom)) {
      return { label: 'Not Yet Valid', severity: 'warning' }
    }
    
    if (now.isAfter(validTo)) {
      return { label: 'Expired', severity: 'danger' }
    }
    
    const daysUntilExpiry = validTo.diff(now, 'days')
    if (daysUntilExpiry <= 30) {
      return { label: 'Expiring Soon', severity: 'warning' }
    }
    
    return { label: 'Valid', severity: 'success' }
  },

  // Certificate type formatting
  formatCertificateType(type) {
    const types = {
      ROOT: { label: 'Root CA', icon: 'pi-shield' },
      INTERMEDIATE: { label: 'Intermediate CA', icon: 'pi-sitemap' },
      END_ENTITY: { label: 'End Entity', icon: 'pi-user' }
    }
    return types[type] || { label: type, icon: 'pi-file' }
  },

  // User role formatting
  formatUserRole(role) {
    const roles = {
      ADMIN: { label: 'Administrator', severity: 'danger' },
      CA_USER: { label: 'CA User', severity: 'warning' },
      END_USER: { label: 'End User', severity: 'info' }
    }
    return roles[role] || { label: role, severity: 'secondary' }
  },

  // File size formatting
  formatFileSize(bytes) {
    if (bytes === 0) return '0 Bytes'
    const k = 1024
    const sizes = ['Bytes', 'KB', 'MB', 'GB']
    const i = Math.floor(Math.log(bytes) / Math.log(k))
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
  },

  // Truncate text
  truncateText(text, maxLength = 50) {
    if (!text) return ''
    if (text.length <= maxLength) return text
    return text.substring(0, maxLength) + '...'
  }
}