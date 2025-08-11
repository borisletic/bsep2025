<template>
  <Tag
    :value="status.label"
    :severity="status.severity"
    :icon="status.icon"
  />
</template>

<script>
import { formatUtils } from '@/utils/formatting'

export default {
  name: 'CertificateStatusBadge',
  props: {
    certificate: {
      type: Object,
      required: true
    }
  },
  computed: {
    status() {
      return formatUtils.formatCertificateStatus(this.certificate)
    }
  }
}
</script>