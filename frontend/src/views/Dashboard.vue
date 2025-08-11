<template>
  <div class="dashboard">
    <div class="dashboard-header">
      <h1>PKI System Dashboard</h1>
      <p>Welcome to your certificate management system</p>
    </div>

    <div class="dashboard-grid">
      <Card class="dashboard-card">
        <template #title>
          <i class="pi pi-file"></i>
          Certificates
        </template>
        <template #content>
          <div class="card-stats">
            <div class="stat">
              <span class="stat-number">{{ totalCertificates }}</span>
              <span class="stat-label">Total</span>
            </div>
            <div class="stat">
              <span class="stat-number text-green">{{ activeCertificates }}</span>
              <span class="stat-label">Active</span>
            </div>
            <div class="stat">
              <span class="stat-number text-orange">{{ expiringCertificates }}</span>
              <span class="stat-label">Expiring</span>
            </div>
          </div>
        </template>
        <template #footer>
          <Button label="Manage Certificates" @click="$router.push('/certificates')" class="p-button-text" />
        </template>
      </Card>

      <Card class="dashboard-card">
        <template #title>
          <i class="pi pi-lock"></i>
          Password Manager
        </template>
        <template #content>
          <div class="card-stats">
            <div class="stat">
              <span class="stat-number">{{ totalPasswords }}</span>
              <span class="stat-label">Stored</span>
            </div>
            <div class="stat">
              <span class="stat-number text-blue">{{ sharedPasswords }}</span>
              <span class="stat-label">Shared</span>
            </div>
          </div>
        </template>
        <template #footer>
          <Button label="Manage Passwords" @click="$router.push('/passwords')" class="p-button-text" />
        </template>
      </Card>

      <Card class="dashboard-card" v-if="canManageTemplates">
        <template #title>
          <i class="pi pi-clone"></i>
          Certificate Templates
        </template>
        <template #content>
          <div class="card-stats">
            <div class="stat">
              <span class="stat-number">{{ totalTemplates }}</span>
              <span class="stat-label">Templates</span>
            </div>
          </div>
        </template>
        <template #footer>
          <Button label="Manage Templates" @click="$router.push('/templates')" class="p-button-text" />
        </template>
      </Card>

      <Card class="dashboard-card" v-if="isAdmin">
        <template #title>
          <i class="pi pi-cog"></i>
          System Administration
        </template>
        <template #content>
          <div class="card-stats">
            <div class="stat">
              <span class="stat-number">{{ totalUsers }}</span>
              <span class="stat-label">Users</span>
            </div>
          </div>
        </template>
        <template #footer>
          <Button label="Admin Panel" @click="$router.push('/admin')" class="p-button-text" />
        </template>
      </Card>
    </div>

    <div class="recent-activity">
      <Card>
        <template #title>Recent Activity</template>
        <template #content>
          <div v-if="recentActivities.length === 0" class="text-center p-4">
            <i class="pi pi-info-circle text-4xl text-muted"></i>
            <p class="mt-2">No recent activities</p>
          </div>
          <div v-else>
            <div v-for="activity in recentActivities" :key="activity.id" class="activity-item">
              <div class="activity-icon">
                <i :class="getActivityIcon(activity.type)"></i>
              </div>
              <div class="activity-content">
                <p>{{ activity.description }}</p>
                <small class="text-muted">{{ formatDate(activity.timestamp) }}</small>
              </div>
            </div>
          </div>
        </template>
      </Card>
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'

export default {
  name: 'Dashboard',
  data() {
    return {
      totalCertificates: 0,
      activeCertificates: 0,
      expiringCertificates: 0,
      totalPasswords: 0,
      sharedPasswords: 0,
      totalTemplates: 0,
      totalUsers: 0,
      recentActivities: []
    }
  },
  computed: {
    ...mapGetters(['userRole', 'currentUser']),
    canManageTemplates() {
      return this.userRole === 'ADMIN' || this.userRole === 'CA_USER'
    },
    isAdmin() {
      return this.userRole === 'ADMIN'
    }
  },
  methods: {
    async loadDashboardData() {
      try {
        // Load certificate stats
        // const certResponse = await this.$store.dispatch('fetchCertificates')
        // this.totalCertificates = certResponse.length
        
        // Load password stats
        // const passResponse = await this.$store.dispatch('fetchPasswordEntries')
        // this.totalPasswords = passResponse.length
        
        // For now, use mock data
        this.totalCertificates = 12
        this.activeCertificates = 8
        this.expiringCertificates = 2
        this.totalPasswords = 15
        this.sharedPasswords = 3
        this.totalTemplates = 5
        this.totalUsers = 25
      } catch (error) {
        console.error('Failed to load dashboard data:', error)
      }
    },
    getActivityIcon(type) {
      const icons = {
        certificate: 'pi pi-file',
        password: 'pi pi-lock',
        login: 'pi pi-sign-in',
        user: 'pi pi-user'
      }
      return icons[type] || 'pi pi-info-circle'
    },
    formatDate(date) {
      return new Date(date).toLocaleDateString()
    }
  },
  async mounted() {
    await this.loadDashboardData()
  }
}
</script>

<style lang="scss" scoped>
.dashboard {
  padding: 2rem;
  max-width: 1200px;
  margin: 0 auto;
}

.dashboard-header {
  text-align: center;
  margin-bottom: 2rem;
  
  h1 {
    color: #333;
    margin-bottom: 0.5rem;
  }
  
  p {
    color: #666;
  }
}

.dashboard-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.dashboard-card {
  .p-card-title {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    color: #495057;
    
    i {
      font-size: 1.2rem;
    }
  }
}

.card-stats {
  display: flex;
  justify-content: space-around;
  margin: 1rem 0;
}

.stat {
  text-align: center;
  
  .stat-number {
    display: block;
    font-size: 2rem;
    font-weight: bold;
    color: #495057;
    
    &.text-green { color: #28a745; }
    &.text-orange { color: #fd7e14; }
    &.text-blue { color: #007bff; }
  }
  
  .stat-label {
    display: block;
    font-size: 0.875rem;
    color: #6c757d;
    margin-top: 0.25rem;
  }
}

.recent-activity {
  .activity-item {
    display: flex;
    align-items: center;
    padding: 1rem 0;
    border-bottom: 1px solid #dee2e6;
    
    &:last-child {
      border-bottom: none;
    }
  }
  
  .activity-icon {
    width: 40px;
    height: 40px;
    background: #f8f9fa;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 1rem;
    
    i {
      color: #6c757d;
    }
  }
  
  .activity-content {
    flex: 1;
    
    p {
      margin: 0 0 0.25rem;
      color: #495057;
    }
    
    small {
      color: #6c757d;
    }
  }
}

.text-center {
  text-align: center;
}

.text-muted {
  color: #6c757d !important;
}
</style>