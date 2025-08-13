<template>
  <div class="dashboard">
    <div class="dashboard-header">
      <h1>Dashboard</h1>
      <p>Welcome back, {{ currentUser?.firstName }} {{ currentUser?.lastName }}</p>
    </div>

    <div class="dashboard-stats">
      <div class="stats-grid">
        <Card class="stat-card">
          <template #content>
            <div class="stat-content">
              <div class="stat-icon certificates">
                <i class="pi pi-file"></i>
              </div>
              <div class="stat-info">
                <h3>{{ certificateStats.total }}</h3>
                <p>Total Certificates</p>
                <small>{{ certificateStats.active }} active</small>
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
                <h3>{{ passwordStats.total }}</h3>
                <p>Saved Passwords</p>
                <small>{{ passwordStats.shared }} shared</small>
              </div>
            </div>
          </template>
        </Card>

        <Card class="stat-card" v-if="userRole === 'ADMIN'">
          <template #content>
            <div class="stat-content">
              <div class="stat-icon users">
                <i class="pi pi-users"></i>
              </div>
              <div class="stat-info">
                <h3>{{ userStats.total }}</h3>
                <p>Total Users</p>
                <small>{{ userStats.active }} active</small>
              </div>
            </div>
          </template>
        </Card>

        <Card class="stat-card">
          <template #content>
            <div class="stat-content">
              <div class="stat-icon sessions">
                <i class="pi pi-desktop"></i>
              </div>
              <div class="stat-info">
                <h3>{{ sessionStats.active }}</h3>
                <p>Active Sessions</p>
                <small>{{ sessionStats.devices }} devices</small>
              </div>
            </div>
          </template>
        </Card>
      </div>
    </div>

    <div class="dashboard-content">
      <div class="content-grid">
        <!-- Recent Certificates -->
        <Card class="content-card">
          <template #header>
            <div class="card-header">
              <h3>Recent Certificates</h3>
              <Button 
                icon="pi pi-plus" 
                label="New Certificate"
                @click="$router.push('/certificates')"
                size="small"
                v-if="userRole === 'ADMIN' || userRole === 'CA_USER'"
              />
            </div>
          </template>
          
          <template #content>
            <div v-if="recentCertificates.length > 0">
              <div 
                v-for="cert in recentCertificates" 
                :key="cert.id"
                class="certificate-item"
              >
                <div class="cert-info">
                  <div class="cert-subject">{{ formatSubjectDN(cert.subjectDN) }}</div>
                  <div class="cert-details">
                    <Tag 
                      :value="cert.type" 
                      :severity="getCertTypeSeverity(cert.type)"
                      class="cert-type"
                    />
                    <Tag 
                      :value="cert.status" 
                      :severity="getCertStatusSeverity(cert.status)"
                      class="cert-status"
                    />
                  </div>
                </div>
                <div class="cert-actions">
                  <Button 
                    icon="pi pi-eye" 
                    size="small"
                    text
                    @click="viewCertificate(cert.id)"
                  />
                </div>
              </div>
            </div>
            <div v-else class="empty-state">
              <i class="pi pi-file" style="font-size: 3rem; color: #ccc;"></i>
              <p>No certificates found</p>
            </div>
          </template>
        </Card>

        <!-- Recent Password Entries -->
        <Card class="content-card">
          <template #header>
            <div class="card-header">
              <h3>Recent Passwords</h3>
              <Button 
                icon="pi pi-plus" 
                label="New Password"
                @click="$router.push('/passwords')"
                size="small"
              />
            </div>
          </template>
          
          <template #content>
            <div v-if="recentPasswords.length > 0">
              <div 
                v-for="password in recentPasswords" 
                :key="password.id"
                class="password-item"
              >
                <div class="password-info">
                  <div class="password-site">{{ password.siteName }}</div>
                  <div class="password-username">{{ password.username }}</div>
                </div>
                <div class="password-actions">
                  <Button 
                    icon="pi pi-eye" 
                    size="small"
                    text
                    @click="viewPassword(password.id)"
                  />
                </div>
              </div>
            </div>
            <div v-else class="empty-state">
              <i class="pi pi-lock" style="font-size: 3rem; color: #ccc;"></i>
              <p>No passwords saved</p>
            </div>
          </template>
        </Card>

        <!-- System Activity -->
        <Card class="content-card full-width" v-if="userRole === 'ADMIN'">
          <template #header>
            <div class="card-header">
              <h3>Recent Activity</h3>
              <Button 
                icon="pi pi-refresh" 
                size="small"
                text
                @click="refreshActivity"
                :loading="activityLoading"
              />
            </div>
          </template>
          
          <template #content>
            <div v-if="recentActivity.length > 0">
              <div 
                v-for="activity in recentActivity" 
                :key="activity.id"
                class="activity-item"
              >
                <div class="activity-icon">
                  <i :class="getActivityIcon(activity.eventType)"></i>
                </div>
                <div class="activity-info">
                  <div class="activity-description">{{ activity.description }}</div>
                  <div class="activity-details">
                    <span class="activity-user">{{ activity.userEmail || 'System' }}</span>
                    <span class="activity-time">{{ formatDateTime(activity.timestamp) }}</span>
                  </div>
                </div>
              </div>
            </div>
            <div v-else class="empty-state">
              <i class="pi pi-clock" style="font-size: 3rem; color: #ccc;"></i>
              <p>No recent activity</p>
            </div>
          </template>
        </Card>
      </div>
    </div>
  </div>
</template>

<script>
import { mapGetters, mapActions } from 'vuex'
import moment from 'moment'

export default {
  name: 'Dashboard',
  data() {
    return {
      certificateStats: {
        total: 0,
        active: 0
      },
      passwordStats: {
        total: 0,
        shared: 0
      },
      userStats: {
        total: 0,
        active: 0
      },
      sessionStats: {
        active: 0,
        devices: 0
      },
      recentCertificates: [],
      recentPasswords: [],
      recentActivity: [],
      activityLoading: false
    }
  },
  computed: {
    ...mapGetters('auth', ['currentUser', 'userRole']),
    ...mapGetters('certificates', ['allCertificates']),
    ...mapGetters('passwords', ['allPasswords'])
  },
  methods: {
    ...mapActions('certificates', ['fetchCertificates']),
    ...mapActions('passwords', ['fetchPasswords']),

    async loadDashboardData() {
      try {
        // Load certificates
        await this.fetchCertificates()
        this.calculateCertificateStats()

        // Load passwords  
        await this.fetchPasswords()
        this.calculatePasswordStats()

        // Load recent activity if admin
        if (this.userRole === 'ADMIN') {
          await this.loadRecentActivity()
        }
      } catch (error) {
        console.error('Failed to load dashboard data:', error)
      }
    },

    calculateCertificateStats() {
      const certificates = this.allCertificates
      this.certificateStats = {
        total: certificates.length,
        active: certificates.filter(cert => cert.status === 'ACTIVE').length
      }
      
      // Get recent certificates (last 5)
      this.recentCertificates = certificates
        .slice()
        .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
        .slice(0, 5)
    },

    calculatePasswordStats() {
      const passwords = this.allPasswords
      this.passwordStats = {
        total: passwords.length,
        shared: passwords.filter(pass => pass.shares && pass.shares.length > 1).length
      }
      
      // Get recent passwords (last 5)
      this.recentPasswords = passwords
        .slice()
        .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
        .slice(0, 5)
    },

    async loadRecentActivity() {
      try {
        this.activityLoading = true
        // Implementation would depend on your audit log API
        // const response = await api.get('/admin/activity?limit=10')
        // this.recentActivity = response.data.data
        this.recentActivity = [] // Placeholder
      } catch (error) {
        console.error('Failed to load activity:', error)
      } finally {
        this.activityLoading = false
      }
    },

    formatSubjectDN(dn) {
      // Extract CN from DN
      const cnMatch = dn.match(/CN=([^,]+)/)
      return cnMatch ? cnMatch[1] : dn
    },

    formatDateTime(dateTime) {
      return moment(dateTime).fromNow()
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
        default: return 'secondary'
      }
    },

    getActivityIcon(eventType) {
      switch (eventType) {
        case 'CERTIFICATE_CREATED': return 'pi pi-plus-circle'
        case 'CERTIFICATE_REVOKED': return 'pi pi-minus-circle'
        case 'LOGIN_SUCCESS': return 'pi pi-sign-in'
        case 'LOGIN_FAILED': return 'pi pi-exclamation-triangle'
        case 'USER_CREATED': return 'pi pi-user-plus'
        default: return 'pi pi-info-circle'
      }
    },

    viewCertificate(id) {
      this.$router.push(`/certificates?view=${id}`)
    },

    viewPassword(id) {
      this.$router.push(`/passwords?view=${id}`)
    },

    refreshActivity() {
      this.loadRecentActivity()
    }
  },

  async mounted() {
    await this.loadDashboardData()
  }
}
</script>

<style scoped>
.dashboard {
  padding: 2rem;
  max-width: 1200px;
  margin: 0 auto;
}

.dashboard-header {
  margin-bottom: 2rem;
}

.dashboard-header h1 {
  margin: 0 0 0.5rem;
  color: #2c3e50;
}

.dashboard-header p {
  margin: 0;
  color: #7f8c8d;
  font-size: 1.1rem;
}

.dashboard-stats {
  margin-bottom: 2rem;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1rem;
}

.stat-card .p-card-content {
  padding: 1.5rem;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  color: white;
}

.stat-icon.certificates {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-icon.passwords {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-icon.users {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-icon.sessions {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stat-info h3 {
  margin: 0 0 0.5rem;
  font-size: 2rem;
  font-weight: bold;
  color: #2c3e50;
}

.stat-info p {
  margin: 0 0 0.25rem;
  color: #7f8c8d;
  font-weight: 600;
}

.stat-info small {
  color: #95a5a6;
  font-size: 0.875rem;
}

.content-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 1.5rem;
}

.content-card.full-width {
  grid-column: 1 / -1;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 1.5rem;
  border-bottom: 1px solid #e9ecef;
}

.card-header h3 {
  margin: 0;
  color: #2c3e50;
}

.certificate-item,
.password-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  border-bottom: 1px solid #f8f9fa;
}

.certificate-item:last-child,
.password-item:last-child {
  border-bottom: none;
}

.cert-info,
.password-info {
  flex: 1;
}

.cert-subject,
.password-site {
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 0.25rem;
}

.password-username {
  color: #7f8c8d;
  font-size: 0.875rem;
}

.cert-details {
  display: flex;
  gap: 0.5rem;
  margin-top: 0.5rem;
}

.activity-item {
  display: flex;
  align-items: flex-start;
  gap: 1rem;
  padding: 1rem;
  border-bottom: 1px solid #f8f9fa;
}

.activity-item:last-child {
  border-bottom: none;
}

.activity-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #f8f9fa;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #6c757d;
  flex-shrink: 0;
}

.activity-info {
  flex: 1;
}

.activity-description {
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 0.25rem;
}

.activity-details {
  display: flex;
  justify-content: space-between;
  font-size: 0.875rem;
  color: #7f8c8d;
}

.empty-state {
  text-align: center;
  padding: 3rem 1rem;
  color: #7f8c8d;
}

.empty-state p {
  margin: 1rem 0 0;
  font-size: 1.1rem;
}

@media (max-width: 768px) {
  .dashboard {
    padding: 1rem;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .content-grid {
    grid-template-columns: 1fr;
  }
  
  .card-header {
    flex-direction: column;
    gap: 1rem;
    align-items: stretch;
  }
  
  .activity-details {
    flex-direction: column;
    gap: 0.25rem;
  }
}
</style>