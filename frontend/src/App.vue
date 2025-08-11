<template>
  <div id="app">
    <Toast />
    <ConfirmDialog />
    
    <!-- Navigation Bar -->
    <nav class="navbar" v-if="$store.getters.isAuthenticated">
      <div class="navbar-content">
        <div class="navbar-brand">
          <router-link to="/dashboard" class="brand-link">
            <i class="pi pi-shield"></i>
            <span class="brand-text">PKI System</span>
          </router-link>
        </div>
        
        <div class="navbar-menu">
          <router-link to="/dashboard" class="nav-link" active-class="active">
            <i class="pi pi-home"></i> Dashboard
          </router-link>
          
          <router-link to="/certificates" class="nav-link" active-class="active">
            <i class="pi pi-file"></i> Certificates
          </router-link>
          
          <router-link 
            to="/templates" 
            class="nav-link" 
            active-class="active"
            v-if="userRole === 'ADMIN' || userRole === 'CA_USER'">
            <i class="pi pi-clone"></i> Templates
          </router-link>
          
          <router-link to="/passwords" class="nav-link" active-class="active">
            <i class="pi pi-lock"></i> Passwords
          </router-link>
          
          <router-link 
            to="/admin" 
            class="nav-link" 
            active-class="active"
            v-if="userRole === 'ADMIN'">
            <i class="pi pi-cog"></i> Admin
          </router-link>
        </div>
        
        <div class="navbar-user">
          <Menu ref="userMenu" :model="userMenuItems" :popup="true" />
          <Button 
            @click="toggleUserMenu"
            class="p-button-text p-button-plain user-button"
            :label="userName"
            icon="pi pi-user" />
        </div>
      </div>
    </nav>

    <!-- Main Content -->
    <div class="main-content" :class="{ 'with-navbar': $store.getters.isAuthenticated }">
      <router-view />
    </div>
  </div>
</template>

<script>
export default {
  name: 'App',
  computed: {
    userRole() {
      return this.$store.getters.userRole
    },
    userName() {
      const user = this.$store.getters.currentUser
      return user ? `${user.firstName} ${user.lastName}` : 'User'
    },
    userMenuItems() {
      return [
        {
          label: 'Profile',
          icon: 'pi pi-user',
          command: () => this.$router.push('/profile')
        },
        {
          label: 'Sessions',
          icon: 'pi pi-desktop',
          command: () => this.$router.push('/sessions')
        },
        {
          separator: true
        },
        {
          label: 'Logout',
          icon: 'pi pi-sign-out',
          command: () => this.logout()
        }
      ]
    }
  },
  methods: {
    toggleUserMenu(event) {
      this.$refs.userMenu.toggle(event)
    },
    async logout() {
      try {
        await this.$store.dispatch('logout')
        this.$router.push('/login')
        this.$toast.add({
          severity: 'success',
          summary: 'Success',
          detail: 'Logged out successfully',
          life: 3000
        })
      } catch (error) {
        console.error('Logout error:', error)
      }
    }
  },
  async created() {
    // Initialize authentication state
    if (this.$store.getters.isAuthenticated) {
      try {
        await this.$store.dispatch('fetchUserProfile')
      } catch (error) {
        console.error('Failed to fetch user profile:', error)
        this.$store.dispatch('logout')
        this.$router.push('/login')
      }
    }
  }
}
</script>

<style lang="scss">
// Global styles
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  background-color: #f8f9fa;
  color: #333;
}

#app {
  min-height: 100vh;
}

// Navbar styles
.navbar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  height: 60px;
}

.navbar-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 2rem;
  height: 100%;
  max-width: 1200px;
  margin: 0 auto;
}

.navbar-brand {
  .brand-link {
    display: flex;
    align-items: center;
    text-decoration: none;
    color: white;
    font-size: 1.5rem;
    font-weight: bold;
    
    i {
      margin-right: 0.5rem;
      font-size: 1.8rem;
    }
  }
}

.navbar-menu {
  display: flex;
  gap: 1rem;
  
  .nav-link {
    display: flex;
    align-items: center;
    padding: 0.5rem 1rem;
    color: rgba(255, 255, 255, 0.9);
    text-decoration: none;
    border-radius: 6px;
    transition: all 0.3s ease;
    
    i {
      margin-right: 0.5rem;
    }
    
    &:hover {
      background-color: rgba(255, 255, 255, 0.1);
      color: white;
    }
    
    &.active {
      background-color: rgba(255, 255, 255, 0.2);
      color: white;
    }
  }
}

.navbar-user {
  .user-button {
    color: white !important;
    
    &:hover {
      background-color: rgba(255, 255, 255, 0.1) !important;
    }
  }
}

// Main content styles
.main-content {
  padding: 2rem;
  
  &.with-navbar {
    margin-top: 60px;
    min-height: calc(100vh - 60px);
  }
}

// Card styles
.custom-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
  border: 1px solid #e9ecef;
  
  .p-card-header {
    background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
    border-bottom: 1px solid #dee2e6;
    border-radius: 12px 12px 0 0;
  }
}

// Button styles
.p-button {
  border-radius: 6px;
  font-weight: 500;
  transition: all 0.3s ease;
  
  &.p-button-primary {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border: none;
    
    &:hover {
      transform: translateY(-1px);
      box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
    }
  }
  
  &.p-button-success {
    background: linear-gradient(135deg, #56ab2f 0%, #a8e6cf 100%);
    border: none;
    
    &:hover {
      transform: translateY(-1px);
      box-shadow: 0 4px 12px rgba(86, 171, 47, 0.4);
    }
  }
  
  &.p-button-danger {
    background: linear-gradient(135deg, #ff416c 0%, #ff4b2b 100%);
    border: none;
    
    &:hover {
      transform: translateY(-1px);
      box-shadow: 0 4px 12px rgba(255, 65, 108, 0.4);
    }
  }
}

// Form styles
.form-group {
  margin-bottom: 1.5rem;
  
  label {
    display: block;
    margin-bottom: 0.5rem;
    font-weight: 500;
    color: #495057;
  }
  
  .p-inputtext,
  .p-password input,
  .p-dropdown,
  .p-calendar input,
  .p-inputtextarea {
    border-radius: 6px;
    border: 1px solid #ced4da;
    transition: all 0.3s ease;
    
    &:focus {
      border-color: #667eea;
      box-shadow: 0 0 0 0.2rem rgba(102, 126, 234, 0.25);
    }
  }
}

// Table styles
.p-datatable {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
  
  .p-datatable-header {
    background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
    border-bottom: 1px solid #dee2e6;
  }
  
  .p-datatable-thead > tr > th {
    background-color: #6c757d;
    color: white;
    font-weight: 600;
    border: none;
  }
  
  .p-datatable-tbody > tr {
    transition: all 0.2s ease;
    
    &:hover {
      background-color: #f8f9fa;
    }
  }
}

// Responsive design
@media (max-width: 768px) {
  .navbar-content {
    padding: 0 1rem;
  }
  
  .navbar-menu {
    display: none; // Hide menu on mobile, implement hamburger menu if needed
  }
  
  .main-content {
    padding: 1rem;
  }
}

// Animation utilities
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from, .fade-leave-to {
  opacity: 0;
}

.slide-enter-active, .slide-leave-active {
  transition: transform 0.3s ease;
}

.slide-enter-from {
  transform: translateX(-100%);
}

.slide-leave-to {
  transform: translateX(100%);
}
</style>