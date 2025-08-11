<template>
  <div class="auth-container">
    <div class="auth-card">
      <Card class="login-card">
        <template #header>
          <div class="auth-header">
            <i class="pi pi-shield auth-icon"></i>
            <h2>PKI System Login</h2>
            <p>Sign in to your account</p>
          </div>
        </template>
        
        <template #content>
          <form @submit.prevent="handleLogin" class="auth-form">
            <div class="form-group">
              <label for="email">Email Address</label>
              <InputText
                id="email"
                v-model="form.email"
                type="email"
                placeholder="Enter your email"
                :class="{ 'p-invalid': errors.email }"
                required
              />
              <small v-if="errors.email" class="p-error">{{ errors.email }}</small>
            </div>

            <div class="form-group">
              <label for="password">Password</label>
              <Password
                id="password"
                v-model="form.password"
                placeholder="Enter your password"
                :feedback="false"
                toggleMask
                :class="{ 'p-invalid': errors.password }"
                required
              />
              <small v-if="errors.password" class="p-error">{{ errors.password }}</small>
            </div>

            <!-- CAPTCHA -->
            <div class="form-group" v-if="captcha.challenge">
              <label>Security Check</label>
              <div class="captcha-container">
                <div class="captcha-challenge">
                  <span>{{ captcha.challenge }}</span>
                  <Button
                    icon="pi pi-refresh"
                    class="p-button-text p-button-sm"
                    @click="loadCaptcha"
                    type="button"
                  />
                </div>
                <InputText
                  v-model="form.captchaAnswer"
                  placeholder="Enter the answer"
                  :class="{ 'p-invalid': errors.captcha }"
                  required
                />
              </div>
              <small v-if="errors.captcha" class="p-error">{{ errors.captcha }}</small>
            </div>

            <Button
              type="submit"
              label="Sign In"
              class="auth-button"
              :loading="loading"
              :disabled="!isFormValid"
            />
          </form>
        </template>

        <template #footer>
          <div class="auth-footer">
            <p>
              <router-link to="/password-reset" class="auth-link">
                Forgot your password?
              </router-link>
            </p>
            <p>
              Don't have an account?
              <router-link to="/register" class="auth-link">
                Sign up here
              </router-link>
            </p>
          </div>
        </template>
      </Card>
    </div>
  </div>
</template>

<script>
import { mapGetters, mapActions } from 'vuex'
import captchaService from '@/services/captchaService'
import { validationUtils } from '@/utils/validation'

export default {
  name: 'Login',
  data() {
    return {
      form: {
        email: '',
        password: '',
        captchaAnswer: '',
        captchaToken: ''
      },
      errors: {},
      loading: false,
      captcha: {
        challenge: '',
        sessionId: ''
      }
    }
  },
  computed: {
    ...mapGetters(['isAuthenticated']),
    isFormValid() {
      return (
        validationUtils.isValidEmail(this.form.email) &&
        this.form.password.length >= 8 &&
        this.form.captchaAnswer.trim() !== '' &&
        Object.keys(this.errors).length === 0
      )
    }
  },
  methods: {
    ...mapActions(['login']),
    
    async loadCaptcha() {
      try {
        const response = await captchaService.generateCaptcha()
        this.captcha.challenge = response.data.challenge
        this.captcha.sessionId = response.data.sessionId
        this.form.captchaToken = response.data.sessionId
        this.form.captchaAnswer = ''
      } catch (error) {
        console.error('Failed to load CAPTCHA:', error)
        this.$toast.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to load security check',
          life: 3000
        })
      }
    },

    validateForm() {
      this.errors = {}

      if (!validationUtils.isValidEmail(this.form.email)) {
        this.errors.email = 'Please enter a valid email address'
      }

      if (!validationUtils.isValidPassword(this.form.password)) {
        this.errors.password = 'Password must be at least 8 characters long'
      }

      if (!this.form.captchaAnswer.trim()) {
        this.errors.captcha = 'Please solve the security check'
      }

      return Object.keys(this.errors).length === 0
    },

    async handleLogin() {
      if (!this.validateForm()) return

      this.loading = true
      try {
        await this.login({
          email: this.form.email,
          password: this.form.password,
          captchaToken: this.form.captchaToken,
          captchaAnswer: this.form.captchaAnswer
        })

        this.$toast.add({
          severity: 'success',
          summary: 'Success',
          detail: 'Login successful',
          life: 3000
        })

        this.$router.push('/dashboard')
      } catch (error) {
        this.$toast.add({
          severity: 'error',
          summary: 'Login Failed',
          detail: error.response?.data?.message || 'Invalid credentials',
          life: 5000
        })
        
        // Reload CAPTCHA after failed login
        await this.loadCaptcha()
      } finally {
        this.loading = false
      }
    }
  },

  async mounted() {
    await this.loadCaptcha()
  }
}
</script>