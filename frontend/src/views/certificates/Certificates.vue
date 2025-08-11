<template>
  <div class="certificates-page">
    <div class="page-header">
      <div class="header-content">
        <h1>
          <i class="pi pi-file"></i>
          Certificates
        </h1>
        <p>Manage your digital certificates</p>
      </div>
      <div class="header-actions">
        <Button
          v-if="userRole === 'ADMIN' || userRole === 'CA_USER'"
          label="Issue Certificate"
          icon="pi pi-plus"
          @click="$router.push('/certificates/issue')"
        />
        <Button
          label="Upload CSR"
          icon="pi pi-upload"
          class="p-button-secondary"
          @click="showUploadDialog = true"
        />
      </div>
    </div>

    <!-- Filters -->
    <Card class="filters-card">
      <template #content>
        <div class="filters">
          <div class="filter-group">
            <label>Search</label>
            <InputText
              v-model="filters.search"
              placeholder="Search certificates..."
              @input="applyFilters"
            />
          </div>
          <div class="filter-group">
            <label>Type</label>
            <Dropdown
              v-model="filters.type"
              :options="certificateTypes"
              optionLabel="label"
              optionValue="value"
              placeholder="All Types"
              @change="applyFilters"
            />
          </div>
          <div class="filter-group">
            <label>Status</label>
            <Dropdown
              v-model="filters.status"
              :options="statusOptions"
              optionLabel="label"
              optionValue="value"
              placeholder="All Statuses"
              @change="applyFilters"
            />
          </div>
          <div class="filter-group">
            <Button
              label="Reset"
              class="p-button-text"
              @click="resetFilters"
            />
          </div>
        </div>
      </template>
    </Card>

    <!-- Certificates Table -->
    <Card class="certificates-table-card">
      <template #content>
        <DataTable
          :value="filteredCertificates"
          :loading="loading"
          responsiveLayout="scroll"
          paginator
          :rows="10"
          :rowsPerPageOptions="[10, 25, 50]"
          currentPageReportTemplate="Showing {first} to {last} of {totalRecords} certificates"
          paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport RowsPerPageDropdown"
          class="certificates-table"
        >
          <Column field="commonName" header="Common Name" sortable>
            <template #body="{ data }">
              <div class="certificate-name">
                <strong>{{ data.commonName }}</strong>
                <br>
                <small class="text-muted">{{ data.organization }}</small>
              </div>
            </template>
          </Column>
          
          <Column field="certificateType" header="Type" sortable>
            <template #body="{ data }">
              <Tag
                :value="formatCertificateType(data.certificateType).label"
                :icon="formatCertificateType(data.certificateType).icon"
              />
            </template>
          </Column>
          
          <Column field="validFrom" header="Valid From" sortable>
            <template #body="{ data }">
              {{ formatDate(data.validFrom) }}
            </template>
          </Column>
          
          <Column field="validTo" header="Valid To" sortable>
            <template #body="{ data }">
              <div class="expiry-info">
                {{ formatDate(data.validTo) }}
                <br>
                <small class="text-muted">{{ formatRelativeTime(data.validTo) }}</small>
              </div>
            </template>
          </Column>
          
          <Column field="status" header="Status" sortable>
            <template #body="{ data }">
              <CertificateStatusBadge :certificate="data" />
            </template>
          </Column>
          
          <Column header="Actions" :exportable="false">
            <template #body="{ data }">
              <div class="action-buttons">
                <Button
                  icon="pi pi-eye"
                  class="p-button-text p-button-sm"
                  @click="viewCertificate(data)"
                  v-tooltip="'View Details'"
                />
                <Button
                  icon="pi pi-download"
                  class="p-button-text p-button-sm"
                  @click="downloadCertificate(data)"
                  v-tooltip="'Download'"
                />
                <Button
                  v-if="!data.revoked && canRevokeCertificate(data)"
                  icon="pi pi-times"
                  class="p-button-text p-button-sm p-button-danger"
                  @click="showRevokeDialog(data)"
                  v-tooltip="'Revoke'"
                />
              </div>
            </template>
          </Column>
        </DataTable>
      </template>
    </Card>

    <!-- Upload CSR Dialog -->
    <Dialog
      v-model:visible="showUploadDialog"
      modal
      header="Upload Certificate Signing Request"
      :style="{ width: '600px' }"
    >
      <div class="upload-form">
        <div class="form-group">
          <label>Select CA Certificate</label>
          <Dropdown
            v-model="uploadForm.issuerId"
            :options="caCertificates"
            optionLabel="commonName"
            optionValue="id"
            placeholder="Select CA for signing"
            class="w-full"
          />
        </div>

        <div class="form-group">
          <label>Validity Period (days)</label>
          <InputText
            v-model="uploadForm.validityDays"
            type="number"
            placeholder="365"
            min="1"
            max="3650"
          />
        </div>

        <div class="form-group">
          <label>CSR File</label>
          <FileUpload
            ref="fileUpload"
            mode="basic"
            accept=".csr,.pem"
            :maxFileSize="1000000"
            @select="onFileSelect"
            chooseLabel="Select CSR File"
            class="w-full"
          />
        </div>
      </div>

      <template #footer>
        <Button
          label="Cancel"
          class="p-button-text"
          @click="showUploadDialog = false"
        />
        <Button
          label="Upload & Issue"
          @click="handleCSRUpload"
          :loading="uploadingCSR"
          :disabled="!uploadForm.file || !uploadForm.issuerId"
        />
      </template>
    </Dialog>

    <!-- Revoke Certificate Dialog -->
    <Dialog
      v-model:visible="showRevokeDialogVisible"
      modal
      header="Revoke Certificate"
      :style="{ width: '500px' }"
    >
      <div class="revoke-form">
        <p>
          Are you sure you want to revoke the certificate for 
          <strong>{{ selectedCertificate?.commonName }}</strong>?
        </p>
        
        <div class="form-group">
          <label>Revocation Reason</label>
          <Dropdown
            v-model="revokeForm.reason"
            :options="revocationReasons"
            optionLabel="label"
            optionValue="value"
            placeholder="Select reason"
            class="w-full"
          />
        </div>

        <div class="form-group">
          <label>Description (Optional)</label>
          <Textarea
            v-model="revokeForm.description"
            rows="3"
            placeholder="Additional information about revocation..."
            class="w-full"
          />
        </div>
      </div>

      <template #footer>
        <Button
          label="Cancel"
          class="p-button-text"
          @click="showRevokeDialogVisible = false"
        />
        <Button
          label="Revoke Certificate"
          class="p-button-danger"
          @click="handleRevokeCertificate"
          :loading="revokingCertificate"
          :disabled="!revokeForm.reason"
        />
      </template>
    </Dialog>
  </div>
</template>

<script>
import { mapGetters, mapActions } from 'vuex'
import CertificateStatusBadge from '@/components/CertificateStatusBadge.vue'
import { formatUtils } from '@/utils/formatting'
import { downloadUtils } from '@/utils/download'
import { CONSTANTS } from '@/utils/constants'

export default {
  name: 'Certificates',
  components: {
    CertificateStatusBadge
  },
  data() {
    return {
      filters: {
        search: '',
        type: null,
        status: null
      },
      filteredCertificates: [],
      showUploadDialog: false,
      showRevokeDialogVisible: false,
      selectedCertificate: null,
      uploadingCSR: false,
      revokingCertificate: false,
      uploadForm: {
        file: null,
        issuerId: null,
        validityDays: 365
      },
      revokeForm: {
        reason: null,
        description: ''
      },
      certificateTypes: CONSTANTS.CERTIFICATE_TYPES,
      revocationReasons: CONSTANTS.REVOCATION_REASONS,
      statusOptions: [
        { label: 'Valid', value: 'valid' },
        { label: 'Expired', value: 'expired' },
        { label: 'Expiring Soon', value: 'expiring' },
        { label: 'Revoked', value: 'revoked' }
      ]
    }
  },
  computed: {
    ...mapGetters(['userRole']),
    ...mapGetters('certificates', ['allCertificates', 'caCertificates', 'isLoading']),
    loading() {
      return this.isLoading
    }
  },
  methods: {
    ...mapActions('certificates', [
      'fetchCertificates',
      'uploadCSR',
      'revokeCertificate',
      'downloadCertificate'
    ]),

    formatDate(date) {
      return formatUtils.formatDate(date, 'MMM DD, YYYY')
    },

    formatRelativeTime(date) {
      return formatUtils.formatRelativeTime(date)
    },

    formatCertificateType(type) {
      return formatUtils.formatCertificateType(type)
    },

    applyFilters() {
      let filtered = [...this.allCertificates]

      // Search filter
      if (this.filters.search) {
        const search = this.filters.search.toLowerCase()
        filtered = filtered.filter(cert =>
          cert.commonName.toLowerCase().includes(search) ||
          cert.organization?.toLowerCase().includes(search) ||
          cert.serialNumber.toLowerCase().includes(search)
        )
      }

      // Type filter
      if (this.filters.type) {
        filtered = filtered.filter(cert => cert.certificateType === this.filters.type)
      }

      // Status filter
      if (this.filters.status) {
        filtered = filtered.filter(cert => {
          const status = formatUtils.formatCertificateStatus(cert)
          return this.getStatusValue(status) === this.filters.status
        })
      }

      this.filteredCertificates = filtered
    },

    getStatusValue(status) {
      const statusMap = {
        'Valid': 'valid',
        'Expired': 'expired',
        'Expiring Soon': 'expiring',
        'Revoked': 'revoked'
      }
      return statusMap[status.label] || 'unknown'
    },

    resetFilters() {
      this.filters = {
        search: '',
        type: null,
        status: null
      }
      this.applyFilters()
    },

    viewCertificate(certificate) {
      this.$router.push(`/certificates/${certificate.id}`)
    },

    async downloadCertificate(certificate) {
      try {
        const response = await this.$store.dispatch('certificates/downloadCertificate', certificate.id)
        downloadUtils.downloadCertificate(response.data, `${certificate.commonName}.crt`)
        
        this.$toast.add({
          severity: 'success',
          summary: 'Success',
          detail: 'Certificate downloaded successfully',
          life: 3000
        })
      } catch (error) {
        this.$toast.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to download certificate',
          life: 5000
        })
      }
    },

    canRevokeCertificate(certificate) {
      // Logic to determine if current user can revoke this certificate
      return !certificate.revoked
    },

    showRevokeDialog(certificate) {
      this.selectedCertificate = certificate
      this.revokeForm = {
        reason: null,
        description: ''
      }
      this.showRevokeDialogVisible = true
    },

    onFileSelect(event) {
      this.uploadForm.file = event.files[0]
    },

    async handleCSRUpload() {
      if (!this.uploadForm.file || !this.uploadForm.issuerId) {
        this.$toast.add({
          severity: 'warn',
          summary: 'Warning',
          detail: 'Please select a CSR file and CA certificate',
          life: 5000
        })
        return
      }

      this.uploadingCSR = true
      
      try {
        const formData = new FormData()
        formData.append('csrFile', this.uploadForm.file)
        formData.append('issuerId', this.uploadForm.issuerId)
        formData.append('validityDays', this.uploadForm.validityDays)

        await this.uploadCSR(formData)
        
        this.$toast.add({
          severity: 'success',
          summary: 'Success',
          detail: 'Certificate issued successfully from CSR',
          life: 3000
        })
        
        this.showUploadDialog = false
        this.resetUploadForm()
        await this.fetchCertificates()
      } catch (error) {
        this.$toast.add({
          severity: 'error',
          summary: 'Error',
          detail: error.response?.data?.message || 'Failed to process CSR',
          life: 5000
        })
      } finally {
        this.uploadingCSR = false
      }
    },

    async handleRevokeCertificate() {
      if (!this.revokeForm.reason) {
        this.$toast.add({
          severity: 'warn',
          summary: 'Warning',
          detail: 'Please select a revocation reason',
          life: 5000
        })
        return
      }

      this.revokingCertificate = true
      
      try {
        await this.revokeCertificate({
          certificateId: this.selectedCertificate.id,
          reason: this.revokeForm.reason,
          description: this.revokeForm.description
        })
        
        this.$toast.add({
          severity: 'success',
          summary: 'Success',
          detail: 'Certificate revoked successfully',
          life: 3000
        })
        
        this.showRevokeDialogVisible = false
        await this.fetchCertificates()
      } catch (error) {
        this.$toast.add({
          severity: 'error',
          summary: 'Error',
          detail: error.response?.data?.message || 'Failed to revoke certificate',
          life: 5000
        })
      } finally {
        this.revokingCertificate = false
      }
    },

    resetUploadForm() {
      this.uploadForm = {
        file: null,
        issuerId: null,
        validityDays: 365
      }
      if (this.$refs.fileUpload) {
        this.$refs.fileUpload.clear()
      }
    }
  },

  async mounted() {
    try {
      await this.fetchCertificates()
      this.applyFilters()
    } catch (error) {
      this.$toast.add({
        severity: 'error',
        summary: 'Error',
        detail: 'Failed to load certificates',
        life: 5000
      })
    }
  },

  watch: {
    allCertificates: {
      handler() {
        this.applyFilters()
      },
      immediate: true
    }
  }
}
</script>

<style scoped>
.certificates-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 30px;
  flex-wrap: wrap;
  gap: 20px;
}

.header-content h1 {
  margin: 0;
  color: var(--primary-color);
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-content p {
  margin: 5px 0 0 0;
  color: var(--text-color-secondary);
}

.header-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.filters-card {
  margin-bottom: 20px;
}

.filters {
  display: flex;
  gap: 20px;
  align-items: end;
  flex-wrap: wrap;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 5px;
  min-width: 150px;
}

.filter-group label {
  font-weight: 600;
  color: var(--text-color);
}

.certificates-table-card {
  margin-bottom: 20px;
}

.certificates-table .certificate-name strong {
  color: var(--text-color);
}

.certificates-table .text-muted {
  color: var(--text-color-secondary);
}

.expiry-info {
  line-height: 1.4;
}

.action-buttons {
  display: flex;
  gap: 5px;
}

.upload-form,
.revoke-form {
  padding: 20px 0;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 600;
  color: var(--text-color);
}

.w-full {
  width: 100%;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: stretch;
  }

  .header-actions {
    justify-content: stretch;
  }

  .header-actions .p-button {
    flex: 1;
  }

  .filters {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-group {
    min-width: auto;
  }

  .action-buttons {
    justify-content: center;
  }
}

@media (max-width: 480px) {
  .certificates-page {
    padding: 10px;
  }

  .filters {
    gap: 15px;
  }
}
</style>