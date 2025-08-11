<template>
  <div class="dashboard">
    <div class="dashboard-header">
      <h1>
        <i class="pi pi-home"></i>
        Dashboard
      </h1>
      <p>Welcome back, {{ userName }}!</p>
    </div>

    <div class="stats-grid">
      <Card class="stat-card">
        <template #content>
          <div class="stat-content">
            <div class="stat-icon certificates">
              <i class="pi pi-file"></i>
            </div>
            <div class="stat-info">
              <h3>{{ stats.certificates }}</h3>
              <p>Certificates</p>
            </div>
          </div>
        </template>
      </Card>

      <Card class="stat-card">
        <template #content>
          <div class="stat-content">
            <div class="stat-icon active">
              <i class="pi pi-check-circle"></i>
            </div>
            <div class="stat-info">
              <h3>{{ stats.activeCertificates }}</h3>
              <p>Active Certificates</p>
            </div>
          </div>
        </template>
      </Card>

      <Card class="stat-card">
        <template #content>
          <div class="stat-content">
            <div class="stat-icon expiring">
              <i class="pi pi-exclamation-triangle"></i>
            </div>
            <div class="stat-info">
              <h3>{{ stats.expiringCertificates }}</h3>
              <p>Expiring Soon</p>
            </div>
          </div>
        </template>
      </Card>

      <Card class="stat-card">
        <template #content>
          <div class="stat-content">
            <div class="stat-icon passwords">
              <i class="pi pi-lock"></i>
            </div>
            <div class="stat-info">
              <h3>{{ stats.passwordEntries }}</h3>
              <p>Saved Passwords</p>
            </div>
          </div>
        </template>
      </Card>
    </div>

    <div class="dashboard-content">
      <div class="content-row">
        <div class="content-col-8">
          <Card class="recent-certificates">
            <template #header>
              <div class="card-header">
                <h3>
                  <i class="pi pi-file"></i>
                  Recent Certificates
                </h3>
                <Button
                  label="View All"
                  class="p-button-text"
                  @click="$router.push('/certificates')"
                />
              </div>
            </template>
            <template #content>
              <DataTable
                :value="recentCertificates"
                :loading="loadingCertificates"
                responsiveLayout="scroll"
                class="dashboard-table"
              >
                <Column field="commonName" header="Common Name" sortable></Column>
                <Column field="certificateType" header="Type" sortable>
                  <template #body="{ data }">
                    <Tag
                      :value="formatCertificateType(data.certificateType).label"
                      :icon="formatCertificateType(data.certificateType).icon"
                    />
                  </template>
                </Column>
                <Column field="validTo" header="Expires" sortable>
                  <template #body="{ data }">
                    {{ formatDate(data.validTo) }}
                  </template>
                </Column>
                <Column field="status" header="Status">
                  <template #body="{ data }">
                    <Tag
                      :value="formatCertificateStatus(data).label"
                      :severity="formatCertificateStatus(data).severity"
                    />
                  </template>
                </Column>
                <Column header="Actions">
                  <template #body="{ data }">
                    <Button
                      icon="pi pi-eye"
                      class="p-button-text p-button-sm"
                      @click="viewCertificate(data.id)"
                      v-tooltip="'View Details'"
                    />
                  </template>
                </Column>
              </DataTable>
            </template>
          </Card>
        </div>

        <div class="content-col-4">
          <Card class="quick-actions">
            <template #header>
              <h3>
                <i class="pi pi-plus"></i>
                Quick Actions
              </h3>
            </template>
            <template #content>
              <div class="action-buttons">
                <Button
                  v-if="userRole === 'ADMIN' || userRole === 'CA_USER'"
                  label="Issue Certificate"
                  icon="pi pi-plus"
                  class="action-button"
                  @click="$router.push('/certificates/issue')"
                />
                
                <Button
                  label="Upload CSR"
                  icon="pi pi-upload"
                  class="action-button p-button-secondary"
                  @click="uploadCSR"
                />
                
                <Button
                  label="Add Password"
                  icon="pi pi-lock"
                  class="action-button p-button-success"
                  @click="$router.push('/passwords')"
                />
                
                <Button
                  v-if="userRole === 'ADMIN'"
                  label="Create CA User"
                  icon="pi pi-user-plus"
                  class="action-button p-button-warning"
                  @click="$router.push('/admin')"
                />
              </div>
            </template>
          </Card>

          <Card class="system-status" v-if="userRole === 'ADMIN'">
            <template #header>
              <h3>
                <i class="pi pi-cog"></i>
                System Status
              </h3>
            </template>
            <template #content>
              <div class="status-items">
                <div class="status-item">
                  <span class="status-label">System Health</span>
                  <Tag value="Online" severity="success" />
                </div>
                <div class="status-item">
                  <span class="status-label">Active Users</span>
                  <span class="status-value">{{ stats.activeUsers }}</span>
                </div>
                <div class="status-item">
                  <span class="status-label">Total Users</span>
                  <span class="status-value">{{ stats.totalUsers }}</span>
                </div>
              </div>
            </template>
          </Card>
        </div>
      </div>
    </div>

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
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import certificateService from '@/services/certificateService'
import passwordService from '@/services/passwordService'
import adminService from '@/services/adminService'
import { formatUtils } from '@/utils/formatting'

export default {
  name: 'Dashboard',
  data() {
    return {
      stats: {
        certificates: 0,
        activeCertificates: 0,
        expiringCertificates: 0,
        passwordEntries: 0,
        activeUsers: 0,
        totalUsers: 0
      },
      recentCertificates: [],
      caCertificates: [],
      loadingCertificates: false,
      showUploadDialog: false,
      uploadingCSR: false,
      uploadForm: {
        file: null,
        issuerId: null,
        validityDays: 365
      }
    }
  },
  computed: {
    ...mapGetters(['currentUser', 'userRole']),
    userName() {
      return this.currentUser ? `${this.currentUser.firstName} ${this.currentUser.lastName}` : 'User'
    }
  },
  methods: {
    formatDate(date) {
      return formatUtils.formatDate(date, 'MMM DD, YYYY')
    },
    
    formatCertificateType(type) {
      return formatUtils.formatCertificateType(type)
    },
    
    formatCertificateStatus(certificate) {
      return formatUtils.formatCertificateStatus(certificate)
    },

    async loadDashboardData() {
      try {
        await Promise.all([
          this.loadCertificateStats(),
          this.loadPasswordStats(),
          this.loadRecentCertificates(),
          this.loadCACertificates()
        ])

        if (this.userRole === 'ADMIN') {
          await this.loadUserStats()
        }
      } catch (error) {
        console.error('Error loading dashboard data:', error)
        this.$toast.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to load dashboard data',
          life: 3000
        })
      }
    },

    async loadCertificateStats() {
      try {
        const response = await certificateService.getCertificates()
        const certificates = response.data || []
        
        this.stats.certificates = certificates.length
        this.stats.activeCertificates = certificates.filter(cert => 
          !cert.revoked && new Date(cert.validTo) > new Date()
        ).length
        
        // Certificates expiring in next 30 days
        const thirtyDaysFromNow = new Date()
        thirtyDaysFromNow.setDate(thirtyDaysFromNow.getDate() + 30)
        
        this.stats.expiringCertificates = certificates.filter(cert => 
          !cert.revoked && 
          new Date(cert.validTo) > new Date() && 
          new Date(cert.validTo) <= thirtyDaysFromNow
        ).length
      } catch (error) {
        console.error('Error loading certificate stats:', error)
      }
    },

    async loadPasswordStats() {
      try {
        const response = await passwordService.getPasswordEntries()
        this.stats.passwordEntries = response.data?.length || 0
      } catch (error) {
        console.error('Error loading password stats:', error)
      }
    },

    async loadRecentCertificates() {
      this.loadingCertificates = true
      try {
        const response = await certificateService.getCertificates()
        const certificates = response.data || []
        
        // Sort by creation date and take first 5
        this.recentCertificates = certificates
          .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
          .slice(0, 5)
      } catch (error) {
        console.error('Error loading recent certificates:', error)
      } finally {
        this.loadingCertificates = false
      }
    },

    async loadCACertificates() {
      try {
        const response = await certificateService.getCertificates()
        const certificates = response.data || []
        
        this.caCertificates = certificates.filter(cert => 
          (cert.certificateType === 'ROOT' || cert.certificateType === 'INTERMEDIATE') &&
          !cert.revoked &&
          new Date(cert.validTo) > new Date()
        )
      } catch (error) {
        console.error('Error loading CA certificates:', error)
      }
    },

    async loadUserStats() {
      try {
        const response = await adminService.getAllUsers()
        const users = response.data || []
        
        this.stats.totalUsers = users.length
        this.stats.activeUsers = users.filter(user => user.activated).length
      } catch (error) {
        console.error('Error loading user stats:', error)
      }
    },

    viewCertificate(id) {
      this.$router.push(`/certificates/${id}`)
    },

    uploadCSR() {
      this.showUploadDialog = true
      this.uploadForm = {
        file: null,
        issuerId: null,
        validityDays: 365
      }
    },

    onFileSelect(event) {
      this.uploadForm.file = event.files[0]
    },

    async handleCSRUpload() {
      if (!this.uploadForm.file || !this.uploadForm.issuerId) {
        return
      }

      this.uploadingCSR = true
      try {
        const formData = new FormData()
        formData.append('csr', this.uploadForm.file)
        formData.append('issuerId', this.uploadForm.issuerId)
        formData.append('validityDays', this.uploadForm.validityDays)

        const response = await certificateService.uploadCSR(formData)
        
        this.$toast.add({
          severity: 'success',
          summary: 'Success',
          detail: 'Certificate issued successfully from CSR',
          life: 3000
        })

        this.showUploadDialog = false
        await this.loadDashboardData() // Refresh data
      } catch (error) {
        this.$toast.add({
          severity: 'error',
          summary: 'Error',
          detail: error.response?.data?.message || 'Failed to issue certificate from CSR',
          life: 5000
        })
      } finally {
        this.uploadingCSR = false
      }
    }
  },

  async mounted() {
    await this.loadDashboardData()
  }
}
</script>

<style lang="scss" scoped>
.dashboard {
  max-width: 1200px;
  margin: 0 auto;
}

.dashboard-header {
  margin-bottom: 2rem;
  
  h1 {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    margin: 0 0 0.5rem;
    color: #333;
    font-size: 2rem;
    font-weight: 600;
    
    i {
      color: #667eea;
    }
  }
  
  p {
    margin: 0;
    color: #6c757d;
    font-size: 1.1rem;
  }
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
  
  .stat-card {
    .p-card-content {
      padding: 1.5rem;
    }
  }
  
  .stat-content {
    display: flex;
    align-items: center;
    gap: 1rem;
  }
  
  .stat-icon {
    width: 60px;
    height: 60px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 1.5rem;
    color: white;
    
    &.certificates {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    }
    
    &.active {
      background: linear-gradient(135deg, #56ab2f 0%, #a8e6cf 100%);
    }
    
    &.expiring {
      background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
    }
    
    &.passwords {
      background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
    }
  }
  
  .stat-info {
    h3 {
      margin: 0 0 0.25rem;
      font-size: 2rem;
      font-weight: 700;
      color: #333;
    }
    
    p {
      margin: 0;
      color: #6c757d;
      font-weight: 500;
    }
  }
}

.dashboard-content {
  .content-row {
    display: grid;
    grid-template-columns: 2fr 1fr;
    gap: 2rem;
  }
  
  .content-col-8,
  .content-col-4 {
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
  }
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1rem 1.5rem;
  
  h3 {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    margin: 0;
    color: #333;
    font-size: 1.25rem;
    font-weight: 600;
    
    i {
      color: #667eea;
    }
  }
}

.dashboard-table {
  .p-datatable-table {
    border-collapse: separate;
    border-spacing: 0;
  }
  
  .p-datatable-tbody > tr {
    transition: background-color 0.2s ease;
    
    &:hover {
      background-color: #f8f9fa;
    }
  }
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  
  .action-button {
    justify-content: flex-start;
    text-align: left;
    padding: 0.75rem 1rem;
    border-radius: 8px;
    font-weight: 500;
    
    .pi {
      margin-right: 0.5rem;
    }
  }
}

.system-status {
  .status-items {
    display: flex;
    flex-direction: column;
    gap: 1rem;
  }
  
  .status-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    
    .status-label {
      color: #6c757d;
      font-weight: 500;
    }
    
    .status-value {
      font-weight: 600;
      color: #333;
    }
  }
}

.upload-form {
  .form-group {
    margin-bottom: 1.5rem;
    
    label {
      display: block;
      margin-bottom: 0.5rem;
      font-weight: 500;
      color: #495057;
    }
  }
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .content-row {
    grid-template-columns: 1fr !important;
  }
  
  .dashboard-header {
    h1 {
      font-size: 1.5rem;
    }
  }
}
</style>