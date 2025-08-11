<template>
  <div class="auth-container">
    <div class="auth-card">
      <Card class="activation-card">
        <template #header>
          <div class="auth-header">
            <i :class="iconClass" class="auth-icon"></i>
            <h2>{{ title }}</h2>
            <p>{{ message }}</p>
          </div>
        </template>
        
        <template #content>
          <div class="activation-content">
            <ProgressBar v-if="loading" mode="indeterminate" />
            
            <div v-if="!loading && success" class="success-content">
              <p>Your account has been successfully activated. You can now sign in to your account.</p>
              <Button
                label="Go to Login"
                class="auth-button"
                @click="$router.push('/login')"
              />
            </div>

            <div v-if="!loading && !success" class="error-content">
              <p>{{ errorMessage }}</p>
              <Button
                label="Go to Login"
                class="p-button-secondary auth-button"
                @click="$router.push('/login')"
              />
            </div>
          </div>
        </template>
      </Card>
    </div>
  </div>
</template>

<script>
import authService from '@/services/authService'

export default {
  name: 'Activate',
  data() {
    return {
      loading: true,
      success: false,
      errorMessage: ''
    }
  },
  computed: {
    title() {
      if (this.loading) return 'Activating Account'
      return this.success ? 'Account Activated' : 'Activation Failed'
    },
    message() {
      if (this.loading) return 'Please wait while we activate your account...'
      return this.success ? 'Welcome to PKI System!' : 'There was a problem activating your account'
    },
    iconClass() {
      if (this.loading) return 'pi pi-spin pi-spinner'
      return this.success ? 'pi pi-check-circle' : 'pi pi-times-circle'
    }
  },
  async mounted() {
    await this.activateAccount()
  },
  methods: {
    async activateAccount() {
      const token = this.$route.params.token
      
      if (!token) {
        this.loading = false
        this.errorMessage = 'Invalid activation link'
        return
      }

      try {
        await authService.activateAccount(token)
        this.success = true
        
        this.$toast.add({
          severity: 'success',
          summary: 'Success',
          detail: 'Account activated successfully',
          life: 3000
        })
      } catch (error) {
        this.success = false
        this.errorMessage = error.response?.data?.message || 'Activation failed'
        
        this.$toast.add({
          severity: 'error',
          summary: 'Activation Failed',
          detail: this.errorMessage,
          life: 5000
        })
      } finally {
        this.loading = false
      }
    }
  }
}
</script>