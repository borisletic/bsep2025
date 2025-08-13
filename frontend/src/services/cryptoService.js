class CryptoService {
  
  // Generate RSA key pair
  async generateKeyPair() {
    try {
      const keyPair = await window.crypto.subtle.generateKey(
        {
          name: "RSA-OAEP",
          modulusLength: 2048,
          publicExponent: new Uint8Array([1, 0, 1]),
          hash: "SHA-256",
        },
        true,
        ["encrypt", "decrypt"]
      )
      return keyPair
    } catch (error) {
      throw new Error('Failed to generate key pair: ' + error.message)
    }
  }

  // Export public key to PEM format
  async exportPublicKey(publicKey) {
    try {
      const exported = await window.crypto.subtle.exportKey('spki', publicKey)
      const exportedAsBase64 = this.arrayBufferToBase64(exported)
      const pemExported = `-----BEGIN PUBLIC KEY-----\n${exportedAsBase64}\n-----END PUBLIC KEY-----`
      return pemExported
    } catch (error) {
      throw new Error('Failed to export public key: ' + error.message)
    }
  }

  // Export private key to PEM format
  async exportPrivateKey(privateKey) {
    try {
      const exported = await window.crypto.subtle.exportKey('pkcs8', privateKey)
      const exportedAsBase64 = this.arrayBufferToBase64(exported)
      const pemExported = `-----BEGIN PRIVATE KEY-----\n${exportedAsBase64}\n-----END PRIVATE KEY-----`
      return pemExported
    } catch (error) {
      throw new Error('Failed to export private key: ' + error.message)
    }
  }

  // Import public key from PEM format
  async importPublicKey(pemKey) {
    try {
      const pemHeader = "-----BEGIN PUBLIC KEY-----"
      const pemFooter = "-----END PUBLIC KEY-----"
      const pemContents = pemKey.substring(pemHeader.length, pemKey.length - pemFooter.length)
      const binaryDerString = window.atob(pemContents)
      const binaryDer = this.str2ab(binaryDerString)

      const publicKey = await window.crypto.subtle.importKey(
        'spki',
        binaryDer,
        {
          name: "RSA-OAEP",
          hash: "SHA-256",
        },
        true,
        ["encrypt"]
      )
      return publicKey
    } catch (error) {
      throw new Error('Failed to import public key: ' + error.message)
    }
  }

  // Import private key from PEM format
  async importPrivateKey(pemKey) {
    try {
      const pemHeader = "-----BEGIN PRIVATE KEY-----"
      const pemFooter = "-----END PRIVATE KEY-----"
      const pemContents = pemKey.substring(pemHeader.length, pemKey.length - pemFooter.length)
      const binaryDerString = window.atob(pemContents)
      const binaryDer = this.str2ab(binaryDerString)

      const privateKey = await window.crypto.subtle.importKey(
        'pkcs8',
        binaryDer,
        {
          name: "RSA-OAEP",
          hash: "SHA-256",
        },
        true,
        ["decrypt"]
      )
      return privateKey
    } catch (error) {
      throw new Error('Failed to import private key: ' + error.message)
    }
  }

  // Encrypt text with public key
  async encryptText(text, publicKey) {
    try {
      const encoder = new TextEncoder()
      const data = encoder.encode(text)
      
      const encrypted = await window.crypto.subtle.encrypt(
        {
          name: "RSA-OAEP"
        },
        publicKey,
        data
      )
      
      return this.arrayBufferToBase64(encrypted)
    } catch (error) {
      throw new Error('Failed to encrypt text: ' + error.message)
    }
  }

  // Decrypt text with private key
  async decryptText(encryptedText, privateKey) {
    try {
      const encryptedData = this.base64ToArrayBuffer(encryptedText)
      
      const decrypted = await window.crypto.subtle.decrypt(
        {
          name: "RSA-OAEP"
        },
        privateKey,
        encryptedData
      )
      
      const decoder = new TextDecoder()
      return decoder.decode(decrypted)
    } catch (error) {
      throw new Error('Failed to decrypt text: ' + error.message)
    }
  }

  // Utility functions
  arrayBufferToBase64(buffer) {
    let binary = ''
    const bytes = new Uint8Array(buffer)
    const len = bytes.byteLength
    for (let i = 0; i < len; i++) {
      binary += String.fromCharCode(bytes[i])
    }
    return window.btoa(binary)
  }

  base64ToArrayBuffer(base64) {
    const binaryString = window.atob(base64)
    const len = binaryString.length
    const bytes = new Uint8Array(len)
    for (let i = 0; i < len; i++) {
      bytes[i] = binaryString.charCodeAt(i)
    }
    return bytes.buffer
  }

  str2ab(str) {
    const buf = new ArrayBuffer(str.length)
    const bufView = new Uint8Array(buf)
    for (let i = 0, strLen = str.length; i < strLen; i++) {
      bufView[i] = str.charCodeAt(i)
    }
    return buf
  }
}

export default new CryptoService()