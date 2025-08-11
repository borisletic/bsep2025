import CryptoJS from 'crypto-js'

export const cryptoUtils = {
  // Generate RSA key pair using Web Crypto API
  async generateKeyPair() {
    try {
      const keyPair = await window.crypto.subtle.generateKey(
        {
          name: 'RSA-OAEP',
          modulusLength: 2048,
          publicExponent: new Uint8Array([1, 0, 1]),
          hash: 'SHA-256'
        },
        true,
        ['encrypt', 'decrypt']
      )
      return keyPair
    } catch (error) {
      console.error('Error generating key pair:', error)
      throw error
    }
  },

  // Export public key to PEM format
  async exportPublicKeyToPEM(publicKey) {
    try {
      const exported = await window.crypto.subtle.exportKey('spki', publicKey)
      const exportedAsString = this.ab2str(exported)
      const exportedAsBase64 = window.btoa(exportedAsString)
      const pemExported = `-----BEGIN PUBLIC KEY-----\n${exportedAsBase64}\n-----END PUBLIC KEY-----`
      return pemExported
    } catch (error) {
      console.error('Error exporting public key:', error)
      throw error
    }
  },

  // Export private key to PEM format
  async exportPrivateKeyToPEM(privateKey) {
    try {
      const exported = await window.crypto.subtle.exportKey('pkcs8', privateKey)
      const exportedAsString = this.ab2str(exported)
      const exportedAsBase64 = window.btoa(exportedAsString)
      const pemExported = `-----BEGIN PRIVATE KEY-----\n${exportedAsBase64}\n-----END PRIVATE KEY-----`
      return pemExported
    } catch (error) {
      console.error('Error exporting private key:', error)
      throw error
    }
  },

  // Import public key from PEM format
  async importPublicKeyFromPEM(pemString) {
    try {
      const pemHeader = '-----BEGIN PUBLIC KEY-----'
      const pemFooter = '-----END PUBLIC KEY-----'
      const pemContents = pemString.substring(pemHeader.length, pemString.length - pemFooter.length)
      const binaryDerString = window.atob(pemContents)
      const binaryDer = this.str2ab(binaryDerString)

      const publicKey = await window.crypto.subtle.importKey(
        'spki',
        binaryDer,
        {
          name: 'RSA-OAEP',
          hash: 'SHA-256'
        },
        true,
        ['encrypt']
      )
      return publicKey
    } catch (error) {
      console.error('Error importing public key:', error)
      throw error
    }
  },

  // Import private key from PEM format
  async importPrivateKeyFromPEM(pemString) {
    try {
      const pemHeader = '-----BEGIN PRIVATE KEY-----'
      const pemFooter = '-----END PRIVATE KEY-----'
      const pemContents = pemString.substring(pemHeader.length, pemString.length - pemFooter.length)
      const binaryDerString = window.atob(pemContents)
      const binaryDer = this.str2ab(binaryDerString)

      const privateKey = await window.crypto.subtle.importKey(
        'pkcs8',
        binaryDer,
        {
          name: 'RSA-OAEP',
          hash: 'SHA-256'
        },
        true,
        ['decrypt']
      )
      return privateKey
    } catch (error) {
      console.error('Error importing private key:', error)
      throw error
    }
  },

  // Encrypt text with public key
  async encryptWithPublicKey(text, publicKey) {
    try {
      const encoded = new TextEncoder().encode(text)
      const encrypted = await window.crypto.subtle.encrypt(
        {
          name: 'RSA-OAEP'
        },
        publicKey,
        encoded
      )
      return window.btoa(this.ab2str(encrypted))
    } catch (error) {
      console.error('Error encrypting with public key:', error)
      throw error
    }
  },

  // Decrypt text with private key
  async decryptWithPrivateKey(encryptedText, privateKey) {
    try {
      const encrypted = this.str2ab(window.atob(encryptedText))
      const decrypted = await window.crypto.subtle.decrypt(
        {
          name: 'RSA-OAEP'
        },
        privateKey,
        encrypted
      )
      return new TextDecoder().decode(decrypted)
    } catch (error) {
      console.error('Error decrypting with private key:', error)
      throw error
    }
  },

  // Helper functions
  ab2str(buf) {
    return String.fromCharCode.apply(null, new Uint8Array(buf))
  },

  str2ab(str) {
    const buf = new ArrayBuffer(str.length)
    const bufView = new Uint8Array(buf)
    for (let i = 0, strLen = str.length; i < strLen; i++) {
      bufView[i] = str.charCodeAt(i)
    }
    return buf
  },

  // Generate secure random password
  generateSecurePassword(length = 12) {
    const charset = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*'
    let password = ''
    for (let i = 0; i < length; i++) {
      const randomIndex = Math.floor(Math.random() * charset.length)
      password += charset[randomIndex]
    }
    return password
  },

  // Check password strength
  checkPasswordStrength(password) {
    let score = 0
    let feedback = []

    if (password.length >= 8) score++
    else feedback.push('Use at least 8 characters')

    if (/[A-Z]/.test(password)) score++
    else feedback.push('Add uppercase letters')

    if (/[a-z]/.test(password)) score++
    else feedback.push('Add lowercase letters')

    if (/\d/.test(password)) score++
    else feedback.push('Add numbers')

    if (/[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(password)) score++
    else feedback.push('Add special characters')

    let level
    if (score <= 2) level = 'weak'
    else if (score <= 4) level = 'medium'
    else level = 'strong'

    return {
      score,
      level,
      feedback: feedback.join(', '),
      isValid: score >= 3
    }
  }
}