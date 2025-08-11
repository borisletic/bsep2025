<template>
  <div class="auth-container">
    <div class="auth-card">
      <Card class="password-change-card">
        <template #header>
          <div class="auth-header">
            <i class="pi pi-lock auth-icon"></i>
            <h2>Change Password</h2>
            <p>Enter your new password</p>
          </div>
        </template>
        
        <template #content>
          <form @submit.prevent="handlePasswordChange" class="auth-form">
            <div class="form-group">
              <label for="newPassword">New Password</label>
              <Password
                id="newPassword"
                v-model="form.newPassword"
                placeholder="Enter new password"
                toggleMask
                :feedback="true"
                :class="{ 'p-invalid': errors.newPassword }"
                @input="checkPasswordStrength"
                required
              />
              <div v-if="passwordStrength.level" class="password-strength">
                <div class="strength-bar">
                  <div 
                    class="strength-fill" 
                    :style="{ 
                      width: (passwordStrength.score / 5) * 100 + '%',
                      backgroundColor: getStrengthColor(passwordStrength.level)
                    }"
                  ></div>
                </div>
                <small :style="{ color: getStrengthColor(passwordStrength.level) }">
                  Password strength: {{ passwordStrength.level.toUpperCase() }}
                </small>
              </div>
              <small v-if="errors.newPassword" class="p-error">{{ errors.newPassword }}</small>
            </div>

            <div class="form-group">
              <label for="confirmPassword">Confirm New Password</label>
              <Password
                id="confirmPassword"
                v-model="form.confirmPassword"
                placeholder="Confirm new password"
                :feedback="false"
                toggleMask
                :class="{ 'p-invalid': errors.confirmPassword }"
                required
              />
              <small v-if="errors.confirmPassword" class="p-error">{{ errors.confirmPassword }}</small>
            </div>

            <Button
              type="submit"
              label="Change Password"
              class="auth-button"
              :loading="loading"
              :disabled="!isFormValid"
            />
          </form>
        </template>

        <template #footer>
          <div class="auth-footer">
            <p>
              <router-link to="/login" class="auth-link">
                Back to Login
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
import { cryptoUtils } from '@/utils/crypto'
import { CONSTANTS } from '@/utils/constants'

export default {
  name: 'PasswordChange',
  data() {
    return {
      form: {
        newPassword: '',
        confirmPassword: ''
      },
      errors: {},
      loading: false,
      passwordStrength: {}
    }
  },
  computed: {
    isFormValid() {
      return (
        this.passwordStrength.isValid &&
        this.form.newPassword === this.form.confirmPassword &&
        Object.keys(this.errors).length === 0
      )
    }
  },
  methods: {
    checkPasswordStrength() {
      this.passwordStrength = cryptoUtils.checkPasswordStrength(this.form.newPassword)
    },

    getStrengthColor(level) {
      return CONSTANTS.PASSWORD_STRENGTH_COLORS[level] || '#6c757d'
    },

    validateForm() {
      this.errors = {}

      if (!this.passwordStrength.isValid) {
        this.errors.newPassword = this.passwordStrength.feedback || 'Password is too weak'
      }

      if (this.form.newPassword !== this.form.confirmPassword) {
        this.errors.confirmPassword = 'Passwords do not match'
      }

      return Object.keys(this.errors).length === 0
    },

    async handlePasswordChange() {
      if (!this.validateForm()) return

      const token = this.$route.params.token
      if (!token) {
        this.$toast.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Invalid password reset link',
          life: 5000
        })
        return
      }

      this.loading = true
      try {
        await authService.changePassword({
          token,
          newPassword: this.form.newPassword,
          confirmPassword: this.form.confirmPassword
        })

        this.$toast.add({
          severity: 'success',
          summary: 'Success',
          detail: 'Password changed successfully. Please log in with your new password.',
          life: 5000
        })

        this.$router.push('/login')
      } catch (error) {
        this.$toast.add({
          severity: 'error',
          summary: 'Error',
          detail: error.response?.data?.message || 'Failed to change password',
          life: 5000
        })
      } finally {
        this.loading = false
      }
    }
  }
}
</script>
