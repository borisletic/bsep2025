<template>
  <div class="certificates">
    <div class="certificates-header">
      <h1>Certificates</h1>
      <div class="header-actions">
        <Button 
          icon="pi pi-refresh" 
          label="Refresh"
          @click="loadCertificates"
          :loading="isLoading"
          size="small"
          severity="secondary"
        />
        <Button 
          icon="pi pi-plus" 
          label="New Certificate"
          @click="showCreateDialog = true"
          v-if="userRole === 'ADMIN' || userRole === 'CA_USER'"
        />
      </div>
    </div>

    <!-- Certificates Table -->
    <Card class="certificates-card">
      <template #content>
        <DataTable 
          :value="allCertificates" 
          :loading="isLoading"
          :paginator="true"
          :rows="10"
          :rowsPerPageOptions="[5, 10, 25, 50]"
          stripedRows
          showGridlines
          sortMode="multiple"
          removableSort
          filterDisplay="menu"
          v-model:filters="filters"
          :globalFilterFields="['subjectDN', 'issuerDN', 'serialNumber']"
        >
          <template #header>
            <div class="table-header">
              <span class="p-input-icon-left">
                <i class="pi pi-search" />
                <InputText 
                  v-model="filters['global'].value" 
                  placeholder="Search certificates..." 
                />
              </span>
            </div>
          </template>

          <Column field="subjectDN" header="Subject" sortable>
            <template #body="{ data }">
              <div class="subject-cell">
                <div class="subject-cn">{{ formatSubjectDN(data.subjectDN) }}</div>
                <div class="subject-full">{{ data.subjectDN }}</div>
              </div>
            </template>
          </Column>

          <Column field="type" header="Type" sortable>
            <template #body="{ data }">
              <Tag 
                :value="data.type" 
                :severity="getCertTypeSeverity(data.type)"
              />
            </template>
          </Column>

          <Column field="status" header="Status" sortable>
            <template #body="{ data }">
              <Tag 
                :value="data.status" 
                :severity="getCertStatusSeverity(data.status)"
              />
            </template>
          </Column>

          <Column field="validTo" header="Expires" sortable>
            <template #body="{ data }">
              <div class="expiry-cell">
                <div>{{ formatDate(data.validTo) }}</div>
                <div class="expiry-warning" v-if="isExpiringSoon(data.validTo)">
                  <i class="pi pi-exclamation-triangle"></i>
                  Expires soon
                </div>
              </div>
            </template>
          </Column>

          <Column field="owner.organization" header="Organization" sortable>
            <template #body="{ data }">
              {{ data.owner?.organization }}
            </template>
          </Column>

          <Column header="Actions" :exportable="false">
            <template #body="{ data }">
              <div class="action-buttons">
                <Button 
                  icon="pi pi-eye" 
                  size="small"
                  text
                  @click="viewCertificate(data)"
                  v-tooltip.top="'View Details'"
                />
                <Button 
                  icon="pi pi-download" 
                  size="small"
                  text
                  @click="downloadCertificate(data.id)"
                  v-tooltip.top="'Download'"
                />
                <Button 
                  icon="pi pi-ban" 
                  size="small"
                  text
                  severity="danger"
                  @click="revokeCertificate(data)"
                  v-if="data.status === 'ACTIVE' && canRevoke(data)"
                  v-tooltip.top="'Revoke'"
                />
              </div>
            </template>
          </Column>

          <template #empty>
            <div class="empty-state">
              <i class="pi pi-file" style="font-size: 3rem; color: #ccc;"></i>
              <p>No certificates found</p>
            </div>
          </template>
        </DataTable>
      </template>
    </Card>

    <!-- Create Certificate Dialog -->
    <Dialog 
      v-model:visible="showCreateDialog" 
      modal 
      header="Create New Certificate"
      :style="{ width: '600px' }"
      @hide="resetCreateForm"
    >
      <form @submit.prevent="handleCreateCertificate">
        <div class="form-grid">
          <div class="field">
            <label for="certType">Certificate Type</label>
            <Dropdown 
              id="certType"
              v-model="createForm.type" 
              :options="certificateTypes"
              optionLabel="label"
              optionValue="value"
              placeholder="Select certificate type"
              class="w-full"
              required
            />
          </div>

          <div class="field" v-if="createForm.type !== 'ROOT'">
            <label for="issuer">Issuer Certificate</label>
            <Dropdown 
              id="issuer"
              v-model="createForm.issuerId" 
              :options="caCertificates"
              optionLabel="subjectDN"
              optionValue="id"
              placeholder="Select issuer certificate"
              class="w-full"
              :filter="true"
              required
            />
          </div>

          <div class="field">
            <label for="subjectDN">Subject Distinguished Name</label>
            <InputText 
              id="subjectDN"
              v-model="createForm.subjectDN" 
              placeholder="CN=example.com,O=Organization,C=US"
              class="w-full"
              required
            />
            <small class="field-help">
              Format: CN=CommonName,O=Organization,OU=Unit,L=City,ST=State,C=Country
            </small>
          </div>

          <div class="field">
            <label for="validFrom">Valid From</label>
            <Calendar 
              id="validFrom"
              v-model="createForm.validFrom" 
              showTime
              hourFormat="24"
              class="w-full"
              :minDate="new Date()"
              required
            />
          </div>

          <div class="field">
            <label for="validTo">Valid To</label>
            <Calendar 
              id="validTo"
              v-model="createForm.validTo" 
              showTime
              hourFormat="24"
              class="w-full"
              :minDate="createForm.validFrom"
              required
            />
          </div>

          <!-- CSR Upload for End Entity Certificates -->
          <div class="field" v-if="createForm.type === 'END_ENTITY'">
            <label>Certificate Signing Request (Optional)</label>
            <FileUpload 
              mode="basic" 
              accept=".csr,.pem"
              :maxFileSize="1000000"
              @select="onCSRSelect"
              @clear="createForm.csrData = null"
              chooseLabel="Upload CSR"
              class="w-full"
            />
            <small class="field-help">
              Upload a CSR file if you have one, otherwise a new key pair will be generated
            </small>
          </div>

          <!-- Extensions Configuration -->
          <div class="field" v-if="createForm.type !== 'END_ENTITY'">
            <label>CA Certificate</label>
            <div class="checkbox-group">
              <div class="field-checkbox">
                <Checkbox 
                  id="basicConstraintsCA" 
                  v-model="createForm.basicConstraintsCA" 
                  :binary="true"
                />
                <label for="basicConstraintsCA">Is CA Certificate</label>
              </div>
              <div class="field" v-if="createForm.basicConstraintsCA">
                <label for="pathLength">Path Length Constraint</label>
                <InputText 
                  id="pathLength"
                  v-model.number="createForm.pathLengthConstraint" 
                  type="number"
                  placeholder="Optional"
                  class="w-full"
                />
              </div>
            </div>
          </div>
        </div>
        
        <div class="dialog-footer">
          <Button 
            type="button" 
            label="Cancel" 
            severity="secondary"
            @click="showCreateDialog = false"
          />
          <Button 
            type="submit" 
            label="Create Certificate" 
            :loading="createLoading"
          />
        </div>
      </form>
    </Dialog>

    <!-- Certificate Details Dialog -->
    <Dialog 
      v-model:visible="showDetailsDialog" 
      modal 
      header="Certificate Details"
      :style="{ width: '800px' }"
    >
      <div v-if="selectedCertificate" class="certificate-details">
        <TabView>
          <TabPanel header="General">
            <div class="details-grid">
              <div class="detail-item">
                <label>Serial Number</label>
                <span class="serial-number">{{ selectedCertificate.serialNumber }}</span>
              </div>
              
              <div class="detail-item">
                <label>Subject</label>
                <span>{{ selectedCertificate.subjectDN }}</span>
              </div>
              
              <div class="detail-item">
                <label>Issuer</label>
                <span>{{ selectedCertificate.issuerDN }}</span>
              </div>
              
              <div class="detail-item">
                <label>Type</label>
                <Tag 
                  :value="selectedCertificate.type" 
                  :severity="getCertTypeSeverity(selectedCertificate.type)"
                />
              </div>
              
              <div class="detail-item">
                <label>Status</label>
                <Tag 
                  :value="selectedCertificate.status" 
                  :severity="getCertStatusSeverity(selectedCertificate.status)"
                />
              </div>
              
              <div class="detail-item">
                <label>Valid From</label>
                <span>{{ formatDateTime(selectedCertificate.validFrom) }}</span>
              </div>
              
              <div class="detail-item">
                <label>Valid To</label>
                <span>{{ formatDateTime(selectedCertificate.validTo) }}</span>
              </div>
              
              <div class="detail-item">
                <label>Owner</label>
                <span>{{ selectedCertificate.owner?.firstName }} {{ selectedCertificate.owner?.lastName }}</span>
              </div>
              
              <div class="detail-item">
                <label>Organization</label>
                <span>{{ selectedCertificate.owner?.organization }}</span>
              </div>
              
              <div class="detail-item" v-if="selectedCertificate.revocationReason">
                <label>Revocation Reason</label>
                <span>{{ selectedCertificate.revocationReason }}</span>
              </div>
            </div>
          </TabPanel>
          
          <TabPanel header="Certificate Data">
            <div class="certificate-data">
              <Textarea 
                :value="formatCertificateData(selectedCertificate.certificateData)"
                :rows="15"
                class="w-full"
                readonly
              />
              <div class="data-actions">
                <Button 
                  icon="pi pi-copy" 
                  label="Copy to Clipboard"
                  @click="copyCertificateData"
                  size="small"
                />
              </div>
            </div>
          </TabPanel>
        </TabView>
      </div>
      
      <template #footer>
        <Button 
          label="Close" 
          @click="showDetailsDialog = false"
        />
      </template>
    </Dialog>

    <!-- Revoke Certificate Dialog -->
    <Dialog 
      v-model:visible="showRevokeDialog" 
      modal 
      header="Revoke Certificate"
      :style="{ width: '500px' }"
    >
      <form @submit.prevent="handleRevokeCertificate">
        <div class="revoke-warning">
          <i class="pi pi-exclamation-triangle"></i>
          <p>Are you sure you want to revoke this certificate? This action cannot be undone.</p>
        </div>
        
        <div class="field">
          <label for="revocationReason">Revocation Reason</label>
          <Dropdown 
            id="revocationReason"
            v-model="revokeForm.reason" 
            :options="revocationReasons"
            optionLabel="label"
            optionValue="value"
            placeholder="Select reason"
            class="w-full"
            required
          />
        </div>
        
        <div class="dialog-footer">
          <Button 
            type="button" 
            label="Cancel" 
            severity="secondary"
            @click="showRevokeDialog = false"
          />
          <Button 
            type="submit" 
            label="Revoke Certificate" 
            severity="danger"
            :loading="revokeLoading"
          />
        </div>
      </form>
    </Dialog>
  </div>
</template>

<script>
import { mapGetters, mapActions } from 'vuex'
import { FilterMatchMode } from 'primevue/api'
import moment from 'moment'

export default {
  name: 'Certificates',
  data() {
    return {
      filters: {
        global: { value: null, matchMode: FilterMatchMode.CONTAINS }
      },
      showCreateDialog: false,
      showDetailsDialog: false,
      showRevokeDialog: false,
      selectedCertificate: null,
      createLoading: false,
      revokeLoading: false,
      createForm: {
        type: null,
        issuerId: null,
        subjectDN: '',
        validFrom: new Date(),
        validTo: new Date(Date.now() + 365 * 24 * 60 * 60 * 1000), // 1 year from now
        basicConstraintsCA: false,
        pathLengthConstraint: null,
        csrData: null
      },
      revokeForm: {
        reason: null
      },
      certificateTypes: [
        { label: 'Root Certificate', value: 'ROOT' },
        { label: 'Intermediate Certificate', value: 'INTERMEDIATE' },
        { label: 'End Entity Certificate', value: 'END_ENTITY' }
      ],
      revocationReasons: [
        { label: 'Key Compromise', value: 'Key Compromise' },
        { label: 'CA Compromise', value: 'CA Compromise' },
        { label: 'Affiliation Changed', value: 'Affiliation Changed' },
        { label: 'Superseded', value: 'Superseded' },
        { label: 'Cessation of Operation', value: 'Cessation of Operation' },
        { label: 'Certificate Hold', value: 'Certificate Hold' },
        { label: 'Privilege Withdrawn', value: 'Privilege Withdrawn' },
        { label: 'AA Compromise', value: 'AA Compromise' }
      ]
    }
  },
  computed: {
    ...mapGetters('auth', ['currentUser', 'userRole']),
    ...mapGetters('certificates', ['allCertificates', 'caCertificates', 'isLoading'])
  },
  methods: {
    ...mapActions('certificates', [
      'fetchCertificates', 
      'fetchCACertificates', 
      'createCertificate', 
      'revokeCertificate', 
      'downloadCertificate'
    ]),

    async loadCertificates() {
      try {
        await this.fetchCertificates()
        if (this.userRole === 'ADMIN' || this.userRole === 'CA_USER') {
          await this.fetchCACertificates()
        }
      } catch (error) {
        this.$toast.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to load certificates',
          life: 3000
        })
      }
    },

    async handleCreateCertificate() {
      try {
        this.createLoading = true
        
        const certificateData = {
          ...this.createForm,
          validFrom: this.createForm.validFrom.toISOString(),
          validTo: this.createForm.validTo.toISOString()
        }

        await this.createCertificate(certificateData)
        
        this.$toast.add({
          severity: 'success',
          summary: 'Success',
          detail: 'Certificate created successfully',
          life: 3000
        })
        
        this.showCreateDialog = false
        this.resetCreateForm()
      } catch (error) {
        this.$toast.add({
          severity: 'error',
          summary: 'Error',
          detail: error.response?.data?.message || 'Failed to create certificate',
          life: 5000
        })
      } finally {
        this.createLoading = false
      }
    },

    async handleRevokeCertificate() {
      try {
        this.revokeLoading = true
        
        await this.revokeCertificate({
          id: this.selectedCertificate.id,
          reason: this.revokeForm.reason
        })
        
        this.$toast.add({
          severity: 'success',
          summary: 'Success',
          detail: 'Certificate revoked successfully',
          life: 3000
        })
        
        this.showRevokeDialog = false
        this.selectedCertificate = null
        this.revokeForm.reason = null
      } catch (error) {
        this.$toast.add({
          severity: 'error',
          summary: 'Error',
          detail: error.response?.data?.message || 'Failed to revoke certificate',
          life: 5000
        })
      } finally {
        this.revokeLoading = false
      }
    },

    viewCertificate(certificate) {
      this.selectedCertificate = certificate
      this.showDetailsDialog = true
    },

    revokeCertificate(certificate) {
      this.selectedCertificate = certificate
      this.showRevokeDialog = true
    },

    async onCSRSelect(event) {
      const file = event.files[0]
      if (file) {
        try {
          const reader = new FileReader()
          reader.onload = (e) => {
            this.createForm.csrData = btoa(e.target.result)
          }
          reader.readAsText(file)
        } catch (error) {
          this.$toast.add({
            severity: 'error',
            summary: 'Error',
            detail: 'Failed to read CSR file',
            life: 3000
          })
        }
      }
    },

    resetCreateForm() {
      this.createForm = {
        type: null,
        issuerId: null,
        subjectDN: '',
        validFrom: new Date(),
        validTo: new Date(Date.now() + 365 * 24 * 60 * 60 * 1000),
        basicConstraintsCA: false,
        pathLengthConstraint: null,
        csrData: null
      }
    },

    formatSubjectDN(dn) {
      const cnMatch = dn.match(/CN=([^,]+)/)
      return cnMatch ? cnMatch[1] : dn
    },

    formatDate(dateTime) {
      return moment(dateTime).format('MMM DD, YYYY')
    },

    formatDateTime(dateTime) {
      return moment(dateTime).format('MMM DD, YYYY HH:mm:ss')
    },

    formatCertificateData(data) {
      if (!data) return ''
      
      try {
        // Decode base64 and format as PEM
        const binaryData = atob(data)
        const base64Data = btoa(binaryData)
        const formatted = base64Data.match(/.{1,64}/g).join('\n')
        return `-----BEGIN CERTIFICATE-----\n${formatted}\n-----END CERTIFICATE-----`
      } catch (error) {
        return data
      }
    },

    copyCertificateData() {
      if (this.selectedCertificate?.certificateData) {
        const formattedData = this.formatCertificateData(this.selectedCertificate.certificateData)
        navigator.clipboard.writeText(formattedData).then(() => {
          this.$toast.add({
            severity: 'success',
            summary: 'Copied',
            detail: 'Certificate data copied to clipboard',
            life: 2000
          })
        })
      }
    },

    isExpiringSoon(validTo) {
      const expiryDate = moment(validTo)
      const daysUntilExpiry = expiryDate.diff(moment(), 'days')
      return daysUntilExpiry <= 30 && daysUntilExpiry > 0
    },

    getCertTypeSeverity(type) {
      switch (type) {
        case 'ROOT': return 'danger'
        case 'INTERMEDIATE': return 'warning'
        case 'END_ENTITY': return 'info'
        default: return 'secondary'
      }
    },

    getCertStatusSeverity(status) {
      switch (status) {
        case 'ACTIVE': return 'success'
        case 'REVOKED': return 'danger'
        case 'EXPIRED': return 'warning'
        case 'SUSPENDED': return 'secondary'
        default: return 'secondary'
      }
    },

    canRevoke(certificate) {
      if (this.userRole === 'ADMIN') return true
      if (this.userRole === 'CA_USER' && certificate.owner?.id === this.currentUser?.id) return true
      if (certificate.owner?.id === this.currentUser?.id) return true
      return false
    }
  },

  async mounted() {
    await this.loadCertificates()
  }
}
</script>

<style scoped>
.certificates {
  padding: 2rem;
  max-width: 1200px;
  margin: 0 auto;
}

.certificates-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.certificates-header h1 {
  margin: 0;
  color: #2c3e50;
}

.header-actions {
  display: flex;
  gap: 1rem;
}

.certificates-card {
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 0;
}

.subject-cell {
  max-width: 250px;
}

.subject-cn {
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 0.25rem;
}

.subject-full {
  font-size: 0.875rem;
  color: #7f8c8d;
  word-break: break-all;
}

.expiry-cell {
  min-width: 120px;
}

.expiry-warning {
  color: #e74c3c;
  font-size: 0.75rem;
  margin-top: 0.25rem;
}

.expiry-warning i {
  margin-right: 0.25rem;
}

.action-buttons {
  display: flex;
  gap: 0.5rem;
}

.empty-state {
  text-align: center;
  padding: 3rem;
  color: #7f8c8d;
}

.form-grid {
  display: grid;
  gap: 1.5rem;
}

.field {
  margin-bottom: 1rem;
}

.field label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 600;
  color: #2c3e50;
}

.field-help {
  color: #7f8c8d;
  font-size: 0.875rem;
  margin-top: 0.25rem;
}

.checkbox-group {
  margin-top: 0.5rem;
}

.field-checkbox {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
  margin-top: 2rem;
  padding-top: 1rem;
  border-top: 1px solid #e9ecef;
}

.certificate-details {
  padding: 0;
}

.details-grid {
  display: grid;
  gap: 1rem;
  padding: 1rem 0;
}

.detail-item {
  display: grid;
  grid-template-columns: 150px 1fr;
  gap: 1rem;
  align-items: center;
  padding: 0.75rem 0;
  border-bottom: 1px solid #f8f9fa;
}

.detail-item:last-child {
  border-bottom: none;
}

.detail-item label {
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
}

.serial-number {
  font-family: 'Courier New', monospace;
  font-size: 0.875rem;
  word-break: break-all;
}

.certificate-data {
  padding: 1rem 0;
}

.data-actions {
  margin-top: 1rem;
  text-align: right;
}

.revoke-warning {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  background: #fff3cd;
  border: 1px solid #ffeaa7;
  border-radius: 4px;
  margin-bottom: 1.5rem;
}

.revoke-warning i {
  color: #856404;
  font-size: 1.5rem;
}

.revoke-warning p {
  margin: 0;
  color: #856404;
}

@media (max-width: 768px) {
  .certificates {
    padding: 1rem;
  }
  
  .certificates-header {
    flex-direction: column;
    gap: 1rem;
    align-items: stretch;
  }
  
  .header-actions {
    justify-content: center;
  }
  
  .detail-item {
    grid-template-columns: 1fr;
    gap: 0.5rem;
  }
  
  .subject-cell {
    max-width: none;
  }
}
</style>