import { saveAs } from 'file-saver'

export const downloadUtils = {
  // Download certificate
  downloadCertificate(certificateData, filename) {
    const blob = new Blob([certificateData], { type: 'application/x-x509-ca-cert' })
    saveAs(blob, filename)
  },

  // Download text file
  downloadTextFile(content, filename) {
    const blob = new Blob([content], { type: 'text/plain;charset=utf-8' })
    saveAs(blob, filename)
  },

  // Download JSON file
  downloadJSON(data, filename) {
    const content = JSON.stringify(data, null, 2)
    const blob = new Blob([content], { type: 'application/json;charset=utf-8' })
    saveAs(blob, filename)
  },

  // Generate and download CSR template
  generateCSRTemplate() {
    const template = `# Certificate Signing Request (CSR) Generation Guide

## Using OpenSSL to generate CSR:

1. Generate private key:
   openssl genrsa -out private.key 2048

2. Generate CSR:
   openssl req -new -key private.key -out certificate.csr

3. Provide the following information when prompted:
   - Country Name (2 letter code)
   - State or Province Name
   - City or Locality Name
   - Organization Name
   - Organizational Unit Name
   - Common Name (FQDN)
   - Email Address

## Example CSR generation command:
openssl req -new -key private.key -out certificate.csr -subj "/C=RS/ST=Vojvodina/L=Novi Sad/O=Your Organization/OU=IT Department/CN=example.com/emailAddress=admin@example.com"

## Notes:
- Keep your private key secure and never share it
- The CSR file can be safely shared with the CA
- Common Name should match the domain name for SSL certificates
`
    this.downloadTextFile(template, 'csr-generation-guide.txt')
  }
}