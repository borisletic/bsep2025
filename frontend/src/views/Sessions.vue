<template>
  <div class="sessions-container">
    <div class="page-header">
      <h1>Active Sessions</h1>
      <p>Manage your active login sessions across different devices and browsers</p>
    </div>

    <Card class="sessions-card">
      <template #title>
        <div class="card-title">
          <i class="pi pi-shield"></i>
          Session Management
        </div>
      </template>
      
      <template #content>
        <div v-if="loading" class="loading-state">
          <ProgressSpinner />
          <p>Loading sessions...</p>
        </div>

        <div v-else-if="sessions.length === 0" class="empty-state">
          <i class="pi pi-info-circle"></i>
          <p>No active sessions found</p>
        </div>

        <div v-else class="sessions-content">
          <!-- Current Session -->
          <div class="current-session-section">
            <h3>Current Session</h3>
            <div v-for="session in currentSessions" :key="session.id" class="session-card current">
              <div class="session-icon">
                <i :class="getDeviceIcon(session.deviceType)"></i>
              </div>
              <div class="session-info">
                <div class="session-primary">
                  <span class="device-name">{{ getDeviceName(session) }}</span>
                  <Tag severity="success" value="Current" class="current-tag" />
                </div>
                <div class="session-details">
                  <span class="location">{{ session.location || session.ipAddress }}</span>
                  <span class="separator">•</span>
                  <span class="last-activity">Active now</span>
                </div>
                <div class="session-meta">
                  <small>Started {{ formatDate(session.createdAt) }}</small>
                </div>
              </div>
            </div>
          </div>

          <!-- Other Sessions -->
          <div v-if="otherSessions.length > 0" class="other-sessions-section">
            <div class="section-header">
              <h3>Other Sessions</h3>
              <Button 
                label="Log Out All Others" 
                icon="pi pi-sign-out"
                severity="danger"
                size="small"
                @click="confirmLogoutAllOthers"
                :disabled="otherSessions.length === 0"
              />
            </div>
            
            <div v-for="session in otherSessions" :key="session.id" class="session-card">
              <div class="session-icon">
                <i :class="getDeviceIcon(session.deviceType)"></i>
              </div>
              <div class="session-info">
                <div class="session-primary">
                  <span class="device-name">{{ getDeviceName(session) }}</span>
                  <Button 
                    icon="pi pi-sign-out"
                    size="small"
                    severity="danger"
                    outlined
                    @click="confirmLogoutSession(session)"
                    title="Log out this session"
                  />
                </div>
                <div class="session-details">
                  <span class="location">{{ session.location || session.ipAddress }}</span>
                  <span class="separator">•</span>
                  <span class="last-activity">{{ formatLastActivity(session.lastActivity) }}</span>
                </div>
                <div class="session-meta">
                  <small>Started {{ formatDate(session.createdAt) }}</small>
                </div>
              </div>
            </div>
          </div>
        </div>
      </template>
    </Card>

    <!-- Confirmation Dialogs -->
    <ConfirmDialog 
      v-model:visible="showLogoutDialog"
      :header="logoutDialogData.header"
      :message="logoutDialogData.message"
      icon="pi pi-exclamation-triangle"
      acceptClass="p-button-danger"
      @accept="handleLogoutConfirm"
    />
  </div>
</template>

<script>
import sessionsService from '@/services/sessionsService'
import { formatDistanceToNow, format } from 'date-fns'

export default {
  name: 'Sessions',
  data() {
    return {
      sessions: [],
      loading: true,
      showLogoutDialog: false,
      logoutDialogData: {
        header: '',
        message: '',
        action: null,
        sessionId: null
      }
    }
  },
  computed: {
    currentSessions() {
      return this.sessions.filter(session => session.current)
    },
    otherSessions() {
      return this.sessions.filter(session => !session.current)
    }
  },
  methods: {
    async loadSessions() {
      this.loading = true
      try {
        const response = await sessionsService.getActiveSessions()
        this.sessions = response.data || []
      } catch (error) {
        this.$toast.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to load sessions',
          life: 5000
        })
      } finally {
        this.loading = false
      }
    },

    getDeviceIcon(deviceType) {
      switch (deviceType?.toLowerCase()) {
        case 'mobile':
          return 'pi pi-mobile'
        case 'tablet':
          return 'pi pi-tablet'
        default:
          return 'pi pi-desktop'
      }
    },

    getDeviceName(session) {
      const browser = session.browser || 'Unknown Browser'
      const os = session.operatingSystem || 'Unknown OS'
      
      if (session.deviceType === 'Mobile') {
        return `${browser} on ${os} (Mobile)`
      } else if (session.deviceType === 'Tablet') {
        return `${browser} on ${os} (Tablet)`
      } else {
        return `${browser} on ${os}`
      }
    },

    formatDate(dateString) {
      if (!dateString) return 'Unknown'
      return format(new Date(dateString), 'MMM dd, yyyy at HH:mm')
    },

    formatLastActivity(dateString) {
      if (!dateString) return 'Unknown'
      return formatDistanceToNow(new Date(dateString), { addSuffix: true })
    },

    confirmLogoutSession(session) {
      this.logoutDialogData = {
        header: 'Log Out Session',
        message: `Are you sure you want to log out from "${this.getDeviceName(session)}"? This will end that session immediately.`,
        action: 'logout-session',
        sessionId: session.tokenId
      }
      this.showLogoutDialog = true
    },

    confirmLogoutAllOthers() {
      this.logoutDialogData = {
        header: 'Log Out All Other Sessions',
        message: `Are you sure you want to log out from all other devices? This will end ${this.otherSessions.length} session(s).`,
        action: 'logout-all-others',
        sessionId: null
      }
      this.showLogoutDialog = true
    },

    async handleLogoutConfirm() {
      try {
        if (this.logoutDialogData.action === 'logout-session') {
          await sessionsService.revokeSession(this.logoutDialogData.sessionId)
          this.$toast.add({
            severity: 'success',
            summary: 'Success',
            detail: 'Session logged out successfully',
            life: 3000
          })
        } else if (this.logoutDialogData.action === 'logout-all-others') {
          await sessionsService.revokeAllOtherSessions()
          this.$toast.add({
            severity: 'success',
            summary: 'Success',
            detail: 'All other sessions logged out successfully',
            life: 3000
          })
        }
        
        // Reload sessions
        await this.loadSessions()
      } catch (error) {
        this.$toast.add({
          severity: 'error',
          summary: 'Error',
          detail: error.response?.data?.message || 'Failed to log out session',
          life: 5000
        })
      }
    }
  },

  async mounted() {
    await this.loadSessions()
  }
}
</script>

<style lang="scss" scoped>
.sessions-container {
  padding: 2rem;
  max-width: 1000px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 2rem;
  
  h1 {
    color: #2c3e50;
    margin-bottom: 0.5rem;
  }
  
  p {
    color: #6c757d;
    margin: 0;
  }
}

.sessions-card {
  .card-title {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    color: #2c3e50;
    
    i {
      color: #28a745;
    }
  }
}

.loading-state {
  text-align: center;
  padding: 3rem;
  
  p {
    margin-top: 1rem;
    color: #6c757d;
  }
}

.empty-state {
  text-align: center;
  padding: 3rem;
  color: #6c757d;
  
  i {
    font-size: 3rem;
    margin-bottom: 1rem;
    display: block;
  }
}

.sessions-content {
  .current-session-section, 
  .other-sessions-section {
    margin-bottom: 2rem;
    
    &:last-child {
      margin-bottom: 0;
    }
  }
  
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 1rem;
    
    h3 {
      margin: 0;
      color: #2c3e50;
    }
  }
  
  h3 {
    margin-bottom: 1rem;
    color: #2c3e50;
    font-size: 1.2rem;
  }
}

.session-card {
  display: flex;
  align-items: flex-start;
  padding: 1.5rem;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  margin-bottom: 1rem;
  transition: all 0.3s ease;
  
  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    border-color: #667eea;
  }
  
  &.current {
    background: linear-gradient(135deg, #f8fff8 0%, #e8f5e8 100%);
    border-color: #28a745;
  }
  
  &:last-child {
    margin-bottom: 0;
  }
}

.session-icon {
  margin-right: 1rem;
  
  i {
    font-size: 2rem;
    color: #667eea;
    width: 48px;
    height: 48px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(102, 126, 234, 0.1);
    border-radius: 50%;
  }
}

.session-info {
  flex: 1;
  
  .session-primary {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 0.5rem;
    
    .device-name {
      font-weight: 600;
      color: #2c3e50;
      font-size: 1.1rem;
    }
    
    .current-tag {
      font-size: 0.75rem;
    }
  }
  
  .session-details {
    display: flex;
    align-items: center;
    color: #6c757d;
    font-size: 0.9rem;
    margin-bottom: 0.25rem;
    
    .separator {
      margin: 0 0.5rem;
    }
  }
  
  .session-meta {
    color: #adb5bd;
    font-size: 0.8rem;
  }
}

@media (max-width: 768px) {
  .sessions-container {
    padding: 1rem;
  }
  
  .session-card {
    padding: 1rem;
    
    .session-primary {
      flex-direction: column;
      align-items: flex-start;
      gap: 0.5rem;
    }
  }
  
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }
}
</style>