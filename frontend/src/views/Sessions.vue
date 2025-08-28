<template>
  <div class="sessions-container">
    <Card>
      <template #header>
        <div class="session-header">
          <h2>Active Sessions</h2>
          <Button 
            label="Refresh" 
            icon="pi pi-refresh" 
            @click="loadSessions"
            :loading="loading"
            class="p-button-outlined"
          />
        </div>
      </template>
      
      <template #content>
        <DataTable 
          :value="sessions" 
          :loading="loading"
          paginator 
          :rows="10"
          dataKey="id"
          :globalFilterFields="['browser', 'location', 'ipAddress']"
          responsiveLayout="scroll"
        >
          <Column field="browser" header="Browser" sortable>
            <template #body="slotProps">
              <div class="session-browser">
                <i class="pi pi-globe mr-2"></i>
                {{ slotProps.data.browser }}
              </div>
            </template>
          </Column>
          
          <Column field="location" header="Location" sortable>
            <template #body="slotProps">
              <div class="session-location">
                <i class="pi pi-map-marker mr-2"></i>
                {{ slotProps.data.location }}
              </div>
            </template>
          </Column>
          
          <Column field="ipAddress" header="IP Address" sortable></Column>
          
          <Column field="lastActive" header="Last Active" sortable>
            <template #body="slotProps">
              {{ formatDate(slotProps.data.lastActive) }}
            </template>
          </Column>
          
          <Column field="current" header="Current" sortable>
            <template #body="slotProps">
              <Tag 
                :value="slotProps.data.current ? 'Current' : 'Active'" 
                :severity="slotProps.data.current ? 'success' : 'info'"
              />
            </template>
          </Column>
          
          <Column header="Actions">
            <template #body="slotProps">
              <Button 
                v-if="!slotProps.data.current"
                icon="pi pi-times" 
                class="p-button-rounded p-button-danger p-button-outlined"
                @click="terminateSession(slotProps.data)"
                v-tooltip="'Terminate Session'"
              />
            </template>
          </Column>
        </DataTable>
      </template>
    </Card>
  </div>
</template>

<script>
import { mapGetters, mapActions } from 'vuex'

export default {
  name: 'Sessions',
  data() {
    return {
      sessions: [
        {
          id: 1,
          browser: 'Chrome on Windows',
          location: 'Belgrade, Serbia',
          ipAddress: '192.168.1.100',
          lastActive: new Date(),
          current: true
        },
        {
          id: 2,
          browser: 'Firefox on Linux',
          location: 'Novi Sad, Serbia',
          ipAddress: '10.0.0.50',
          lastActive: new Date(Date.now() - 3600000),
          current: false
        }
      ],
      loading: false
    }
  },
  computed: {
    ...mapGetters('auth', ['user'])
  },
  methods: {
    ...mapActions('auth', ['logout']),
    
    async loadSessions() {
      this.loading = true
      try {
        // TODO: Implement API call to fetch actual sessions
        // const response = await api.get('/api/users/sessions')
        // this.sessions = response.data.data
        
        // Mock delay
        await new Promise(resolve => setTimeout(resolve, 1000))
      } catch (error) {
        this.$toast.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to load sessions',
          life: 3000
        })
      } finally {
        this.loading = false
      }
    },
    
    async terminateSession(session) {
      this.$confirm.require({
        message: `Are you sure you want to terminate the session from ${session.location}?`,
        header: 'Terminate Session',
        icon: 'pi pi-exclamation-triangle',
        accept: async () => {
          try {
            // TODO: Implement API call to terminate session
            // await api.delete(`/api/users/sessions/${session.id}`)
            
            this.sessions = this.sessions.filter(s => s.id !== session.id)
            
            this.$toast.add({
              severity: 'success',
              summary: 'Success',
              detail: 'Session terminated successfully',
              life: 3000
            })
          } catch (error) {
            this.$toast.add({
              severity: 'error',
              summary: 'Error',
              detail: 'Failed to terminate session',
              life: 3000
            })
          }
        }
      })
    },
    
    formatDate(date) {
      return new Intl.DateTimeFormat('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      }).format(new Date(date))
    }
  },
  
  mounted() {
    this.loadSessions()
  }
}
</script>

<style scoped>
.sessions-container {
  padding: 1rem;
}

.session-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
}

.session-browser,
.session-location {
  display: flex;
  align-items: center;
}
</style>