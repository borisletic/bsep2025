<template>
  <div class="login-container">
    <Card class="login-card">
      <template #header>
        <div class="login-header">
          <i class="pi pi-shield" style="font-size: 3rem; color: var(--primary-color);"></i>
          <h2>PKI System Login</h2>
        </div>
      </template>
      
      <template #content>
        <form @submit.prevent="handleLogin" class="login-form">
          <div class="field">
            <label for="email">Email</label>
            <InputText 
              id="email"
              v-model="loginForm.email" 
              type="email"
              placeholder="Enter your email"
              :class="{ 'p-invalid': errors.email }"
              autocomplete="email"
              required
            />
            <small class="p-error" v-if="errors.email">{{ errors.email }}</small>
          </div>

          <div class="field">
            <label for="password">Password</label>
            <Password 
              id="password"
              v-model="loginForm.password" 
              placeholder="Enter your password"
              :class="{ 'p-invalid': errors.password }"
              :feedback="false"
              autocomplete="current-password"
              toggleMask
              required
            />
            <small class="p-error" v-if="errors.password">{{ errors.password }}</small>
          </div>

          <!-- CAPTCHA -->
          <div class="field" v-if="captcha">
            <label for="captcha">Security Check</label>
            <div class="captcha-container">
              <div class="captcha-question">{{ captcha.question }}</div>
              <InputText 
                id="captcha"
                v-model="loginForm.captchaAnswer" 
                placeholder="Enter answer"
                :class="{ 'p-invalid': errors.captcha }"
                autocomplete="off"
                required
              />
              <Button 
                type="button" 
                icon="pi pi-refresh" 
                class="p-button-outlined p-button-sm"
                @click="generateCaptcha"
                :loading="captchaLoading"
              />
            </div>
            <small class="p-error" v-if="errors.captcha">{{ errors.captcha }}</small>
          </div>

          <div class="login-actions">
            <Button 
              type="submit" 
              label="Login" 
              icon="pi pi-sign-in"
              :loading="isLoading"
              class="w-full"
            />
            
            <div class="login-links">
              <router-link to="/register" class="link">Create Account</router-link>
              <button type="button" @click="showForgotPassword = true" class="link-button">
                Forgot Password?
              </button>
            </div>
          </div>
        </form>
      </template>
    </Card>

    <!-- Forgot Password Dialog -->
    <Dialog 
      v-model:visible="showForgotPassword" 
      modal 
      header="Reset Password"
      :style="{ width: '400px' }"
    >
      <form @submit.prevent="handleForgotPassword">
        <div class="field">
          <label for="resetEmail">Email Address</label>
          <InputText 
            id="resetEmail"
            v-model="resetEmail" 
            type="email"
            placeholder="Enter your email"
            class="w-full"
            required
          />
        </div>
        
        <div class="flex justify-content-end gap-2">
          <Button 
            type="button" 
            label="Cancel" 
            severity="secondary"
            @click="showForgotPassword = false"
          />
          <Button 
            type="submit" 
            label="Send Reset Link" 
            :loading="resetLoading"
          />
        </div>
      </form>
    </Dialog>
  </div>
</template>

<script>
import { mapGetters, mapActions } from 'vuex'
import api from '@/services/api'

export default {
  name: 'Login',
  data() {
    return {
      loginForm: {
        email: '',
        password: '',
        captchaToken: '',
        captchaAnswer: ''
      },
      captcha: null,
      captchaLoading: false,
      errors: {},
      showForgotPassword: false,
      resetEmail: '',
      resetLoading: false
    }
  },
  computed: {
    ...mapGetters('auth', ['isLoading', 'error'])
  },
  methods: {
    ...mapActions('auth', ['login', 'requestPasswordReset', 'clearError']),
    
    async generateCaptcha() {
      try {
        this.captchaLoading = true
        const response = await api.get('/captcha/generate')
        this.captcha = response.data.data
        this.loginForm.captchaToken = this.captcha.token
        this.loginForm.captchaAnswer = ''
      } catch (error) {
        this.$toast.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to generate CAPTCHA',
          life: 3000
        })
      } finally {
        this.captchaLoading = false
      }
    },

    async handleLogin() {
      this.errors = {}
      
      if (!this.validateForm()) {
        return
      }

      try {
        await this.login(this.loginForm)
        
        this.$toast.add({
          severity: 'success',
          summary: 'Success',
          detail: 'Login successful',
          life: 3000
        })
        
        this.$router.push('/dashboard')
      } catch (error) {
        // Error is handled by the store and displayed via toast
        this.$toast.add({
          severity: 'error',
          summary: 'Login Failed',
          detail: this.error || 'Invalid credentials',
          life: 5000
        })
        
        // Regenerate CAPTCHA on failed login
        this.generateCaptcha()
      }
    },

    async handleForgotPassword() {
      if (!this.resetEmail) {
        this.$toast.add({
          severity: 'warn',
          summary: 'Warning',
          detail: 'Please enter your email address',
          life: 3000
        })
        return
      }

      try {
        this.resetLoading = true
        await this.requestPasswordReset(this.resetEmail)
        
        this.$toast.add({
          severity: 'success',
          summary: 'Success',
          detail: 'Password reset link sent to your email',
          life: 5000
        })
        
        this.showForgotPassword = false
        this.resetEmail = ''
      } catch (error) {
        this.$toast.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to send password reset link',
          life: 3000
        })
      } finally {
        this.resetLoading = false
      }
    },

    validateForm() {
      const errors = {}
      
      if (!this.loginForm.email) {
        errors.email = 'Email is required'
      } else if (!/\S+@\S+\.\S+/.test(this.loginForm.email)) {
        errors.email = 'Email is invalid'
      }
      
      if (!this.loginForm.password) {
        errors.password = 'Password is required'
      }
      
      if (this.captcha && !this.loginForm.captchaAnswer) {
        errors.captcha = 'Please solve the security check'
      }
      
      this.errors = errors
      return Object.keys(errors).length === 0
    }
  },
  async mounted() {
    await this.generateCaptcha()
  },
  beforeUnmount() {
    this.clearError()
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 2rem;
}

.login-card {
  width: 100%;
  max-width: 400px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
}

.login-header {
  text-align: center;
  padding: 2rem 0 1rem;
}

.login-header h2 {
  margin: 1rem 0 0;
  color: #2c3e50;
}

.login-form {
  padding: 0 2rem 2rem;
}

.field {
  margin-bottom: 1.5rem;
}

.field label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 600;
  color: #2c3e50;
}

.captcha-container {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.captcha-question {
  font-weight: bold;
  color: #2c3e50;
  padding: 0.5rem;
  background: #f8f9fa;
  border-radius: 4px;
  min-width: 80px;
  text-align: center;
}

.login-actions {
  margin-top: 2rem;
}

.login-links {
  display: flex;
  justify-content: space-between;
  margin-top: 1rem;
}

.link {
  color: var(--primary-color);
  text-decoration: none;
  font-size: 0.9rem;
}

.link:hover {
  text-decoration: underline;
}

.link-button {
  background: none;
  border: none;
  color: var(--primary-color);
  text-decoration: none;
  font-size: 0.9rem;
  cursor: pointer;
}

.link-button:hover {
  text-decoration: underline;
}
</style>