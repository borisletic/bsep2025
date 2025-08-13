class PasswordStrengthService {
  
  checkStrength(password) {
    let score = 0
    let feedback = []
    
    if (!password) {
      return { score: 0, level: 'Very Weak', feedback: ['Password is required'] }
    }

    // Length check
    if (password.length >= 8) score += 1
    else feedback.push('Use at least 8 characters')

    if (password.length >= 12) score += 1

    // Character variety checks
    if (/[a-z]/.test(password)) score += 1
    else feedback.push('Include lowercase letters')

    if (/[A-Z]/.test(password)) score += 1
    else feedback.push('Include uppercase letters')

    if (/[0-9]/.test(password)) score += 1
    else feedback.push('Include numbers')

    if (/[^A-Za-z0-9]/.test(password)) score += 1
    else feedback.push('Include special characters')

    // Pattern checks
    if (!/(.)\1{2,}/.test(password)) score += 1
    else feedback.push('Avoid repeating characters')

    if (!/012|123|234|345|456|567|678|789|890|abc|bcd|cde|def|efg|fgh|ghi|hij|ijk|jkl|klm|lmn|mno|nop|opq|pqr|qrs|rst|stu|tuv|uvw|vwx|wxy|xyz/i.test(password)) score += 1
    else feedback.push('Avoid common sequences')

    // Common passwords check
    const commonPasswords = ['password', '123456', 'qwerty', 'abc123', 'letmein', 'monkey', 'password123']
    if (!commonPasswords.includes(password.toLowerCase())) score += 1
    else feedback.push('Avoid common passwords')

    // Determine level
    let level
    if (score <= 2) level = 'Very Weak'
    else if (score <= 4) level = 'Weak'
    else if (score <= 6) level = 'Medium'
    else if (score <= 8) level = 'Strong'
    else level = 'Very Strong'

    return {
      score,
      level,
      feedback: feedback.length > 0 ? feedback : ['Password looks good!'],
      percentage: Math.min(100, (score / 9) * 100)
    }
  }

  getStrengthColor(level) {
    switch (level) {
      case 'Very Weak': return '#d73027'
      case 'Weak': return '#fc8d59'
      case 'Medium': return '#fee08b'
      case 'Strong': return '#d9ef8b'
      case 'Very Strong': return '#91bfdb'
      default: return '#cccccc'
    }
  }
}

export default new PasswordStrengthService()