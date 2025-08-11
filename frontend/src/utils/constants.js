export const CONSTANTS = {
  CERTIFICATE_TYPES: [
    { label: 'Root CA', value: 'ROOT' },
    { label: 'Intermediate CA', value: 'INTERMEDIATE' },
    { label: 'End Entity', value: 'END_ENTITY' }
  ],

  REVOCATION_REASONS: [
    { label: 'Unspecified', value: 'UNSPECIFIED' },
    { label: 'Key Compromise', value: 'KEY_COMPROMISE' },
    { label: 'CA Compromise', value: 'CA_COMPROMISE' },
    { label: 'Affiliation Changed', value: 'AFFILIATION_CHANGED' },
    { label: 'Superseded', value: 'SUPERSEDED' },
    { label: 'Cessation of Operation', value: 'CESSATION_OF_OPERATION' },
    { label: 'Certificate Hold', value: 'CERTIFICATE_HOLD' },
    { label: 'Remove from CRL', value: 'REMOVE_FROM_CRL' },
    { label: 'Privilege Withdrawn', value: 'PRIVILEGE_WITHDRAWN' },
    { label: 'AA Compromise', value: 'AA_COMPROMISE' }
  ],

  USER_ROLES: [
    { label: 'Administrator', value: 'ADMIN' },
    { label: 'CA User', value: 'CA_USER' },
    { label: 'End User', value: 'END_USER' }
  ],

  COUNTRIES: [
    { label: 'Serbia', value: 'RS' },
    { label: 'United States', value: 'US' },
    { label: 'Germany', value: 'DE' },
    { label: 'United Kingdom', value: 'GB' },
    { label: 'France', value: 'FR' },
    { label: 'Italy', value: 'IT' },
    { label: 'Spain', value: 'ES' },
    { label: 'Netherlands', value: 'NL' },
    { label: 'Canada', value: 'CA' },
    { label: 'Australia', value: 'AU' }
  ],

  KEY_USAGE_OPTIONS: [
    { label: 'Digital Signature', value: 'digitalSignature' },
    { label: 'Key Encipherment', value: 'keyEncipherment' },
    { label: 'Key Agreement', value: 'keyAgreement' },
    { label: 'Certificate Sign', value: 'keyCertSign' },
    { label: 'CRL Sign', value: 'cRLSign' },
    { label: 'Non Repudiation', value: 'nonRepudiation' }
  ],

  EXTENDED_KEY_USAGE_OPTIONS: [
    { label: 'Server Authentication', value: 'serverAuth' },
    { label: 'Client Authentication', value: 'clientAuth' },
    { label: 'Code Signing', value: 'codeSigning' },
    { label: 'Email Protection', value: 'emailProtection' },
    { label: 'Time Stamping', value: 'timeStamping' },
    { label: 'OCSP Signing', value: 'OCSPSigning' }
  ],

  PASSWORD_STRENGTH_COLORS: {
    weak: '#dc3545',
    medium: '#ffc107',
    strong: '#28a745'
  }
}