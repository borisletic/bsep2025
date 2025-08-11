<template>
  <div class="auth-container">
    <div class="auth-card">
      <Card class="password-reset-card">
        <template #header>
          <div class="auth-header">
            <i class="pi pi-key auth-icon"></i>
            <h2>Reset Password</h2>
            <p>Enter your email address to receive reset instructions</p>
          </div>
        </template>
        
        <template #content>
          <form @submit.prevent="handlePasswordReset" class="auth-form">
            <div class="form-group">
              <label for="email">Email Address</label>
              <InputText
                id="email"
                v-model="form.email"
                type="email"
                placeholder="Enter your email address"
                :class="{ 'p-invalid': errors.email }"
                required
              />
              <small v-if="errors.email" class="p-error">{{ errors.email }}</small>
            </div>

            <Button
              type="submit"
              label="Send Reset Email"
              class="auth-button"
              :loading="loading"
              :disabled="!isFormValid"
            />
          </form>
        </template>

        <template #footer>
          <div class="auth-footer">
            <p>
              Remember your password?
              <router-link to="/login" class="auth-link">
                Sign in here
              </router-link>
            </p>
          </div>
        </template>
      </Card>
    </div>
  </div>
</template>

<script>
import authService from '@/services/authService'
import { validationUtils } from '@/utils/validation'

export default {
  name: 'PasswordReset',
  data() {
    return {
      form: {
        email: ''
      },
      errors: {},
      loading: false
    }
  },
  computed: {
    isFormValid() {
      return validationUtils.isValidEmail(this.form.email) && Object.keys(this.errors).length === 0
    }
  },
  methods: {
    validateForm() {
      this.errors = {}

      if (!validationUtils.isValidEmail(this.form.email)) {
        this.errors.email = 'Please enter a valid email address'
      }

      return Object.keys(this.errors).length === 0
    },

    async handlePasswordReset() {
      if (!this.validateForm()) return

      this.loading = true
      try {
        await authService.requestPasswordReset(this.form.email)

        this.$toast.add({
          severity: 'success',
          summary: 'Email Sent',
          detail: 'Password reset instructions have been sent to your email',
          life: 5000
        })

        this.$router.push('/login')
      } catch (error) {
        this.$toast.add({
          severity: 'error',
          summary: 'Error',
          detail: error.response?.data?.message || 'Failed to send reset email',
          life: 5000
        })
      } finally {
        this.loading = false
      }
    }
  }
}
</script>
