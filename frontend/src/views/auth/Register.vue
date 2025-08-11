<template>
  <div class="auth-container">
    <div class="auth-card">
      <Card class="register-card">
        <template #header>
          <div class="auth-header">
            <i class="pi pi-user-plus auth-icon"></i>
            <h2>Create Account</h2>
            <p>Join the PKI System</p>
          </div>
        </template>
        
        <template #content>
          <form @submit.prevent="handleRegister" class="auth-form">
            <div class="form-row">
              <div class="form-group">
                <label for="firstName">First Name</label>
                <InputText
                  id="firstName"
                  v-model="form.firstName"
                  placeholder="Enter your first name"
                  :class="{ 'p-invalid': errors.firstName }"
                  required
                />
                <small v-if="errors.firstName" class="p-error">{{ errors.firstName }}</small>
              </div>

              <div class="form-group">
                <label for="lastName">Last Name</label>
                <InputText
                  id="lastName"
                  v-model="form.lastName"
                  placeholder="Enter your last name"
                  :class="{ 'p-invalid': errors.lastName }"
                  required
                />
                <small v-if="errors.lastName" class="p-error">{{ errors.lastName }}</small>
              </div>
            </div>

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

            <div class="form-group">
              <label for="organization">Organization</label>
              <InputText
                id="organization"
                v-model="form.organization"
                placeholder="Enter your organization name"
                :class="{ 'p-invalid': errors.organization }"
                required
              />
              <small v-if="errors.organization" class="p-error">{{ errors.organization }}</small>
            </div>

            <div class="form-group">
              <label for="password">Password</label>
              <Password
                id="password"
                v-model="form.password"
                placeholder="Enter your password"
                toggleMask
                :feedback="true"
                :class="{ 'p-invalid': errors.password }"
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
              <small v-if="errors.password" class="p-error">{{ errors.password }}</small>
            </div>

            <div class="form-group">
              <label for="confirmPassword">Confirm Password</label>
              <Password
                id="confirmPassword"
                v-model="form.confirmPassword"
                placeholder="Confirm your password"
                :feedback="false"
                toggleMask
                :class="{ 'p-invalid': errors.confirmPassword }"
                required
              />
              <small v-if="errors.confirmPassword" class="p-error">{{ errors.confirmPassword }}</small>
            </div>

            <Button
              type="submit"
              label="Create Account"
              class="auth-button"
              :loading="loading"
              :disabled="!isFormValid"
            />
          </form>
        </template>

        <template #footer>
          <div class="auth-footer">
            <p>
              Already have an account?
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
import { mapActions } from 'vuex'
import { validationUtils } from '@/utils/validation'
import { cryptoUtils } from '@/utils/crypto'
import { CONSTANTS } from '@/utils/constants'

export default {
  name: 'Register',
  data() {
    return {
      form: {
        firstName: '',
        lastName: '',
        email: '',
        organization: '',
        password: '',
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
        validationUtils.isRequired(this.form.firstName) &&
        validationUtils.isRequired(this.form.lastName) &&
        validationUtils.isValidEmail(this.form.email) &&
        validationUtils.isRequired(this.form.organization) &&
        this.passwordStrength.isValid &&
        this.form.password === this.form.confirmPassword &&
        Object.keys(this.errors).length === 0
      )
    }
  },
  methods: {
    ...mapActions(['register']),

    checkPasswordStrength() {
      this.passwordStrength = cryptoUtils.checkPasswordStrength(this.form.password)
    },

    getStrengthColor(level) {
      return CONSTANTS.PASSWORD_STRENGTH_COLORS[level] || '#6c757d'
    },

    validateForm() {
      this.errors = {}

      if (!validationUtils.isRequired(this.form.firstName)) {
        this.errors.firstName = 'First name is required'
      }

      if (!validationUtils.isRequired(this.form.lastName)) {
        this.errors.lastName = 'Last name is required'
      }

      if (!validationUtils.isValidEmail(this.form.email)) {
        this.errors.email = 'Please enter a valid email address'
      }

      if (!validationUtils.isRequired(this.form.organization)) {
        this.errors.organization = 'Organization is required'
      }

      if (!this.passwordStrength.isValid) {
        this.errors.password = this.passwordStrength.feedback || 'Password is too weak'
      }

      if (this.form.password !== this.form.confirmPassword) {
        this.errors.confirmPassword = 'Passwords do not match'
      }

      return Object.keys(this.errors).length === 0
    },

    async handleRegister() {
      if (!this.validateForm()) return

      this.loading = true
      try {
        await this.register({
          firstName: this.form.firstName,
          lastName: this.form.lastName,
          email: this.form.email,
          organization: this.form.organization,
          password: this.form.password,
          confirmPassword: this.form.confirmPassword
        })

        this.$toast.add({
          severity: 'success',
          summary: 'Registration Successful',
          detail: 'Please check your email to activate your account',
          life: 5000
        })

        this.$router.push('/login')
      } catch (error) {
        this.$toast.add({
          severity: 'error',
          summary: 'Registration Failed',
          detail: error.response?.data?.message || 'Registration failed',
          life: 5000
        })
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.auth-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 2rem;
}

.auth-card {
  width: 100%;
  max-width: 500px;
  
  .p-card {
    border-radius: 16px;
    box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
    border: none;
    overflow: hidden;
  }
  
  .p-card-header {
    background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
    border-bottom: none;
    padding: 2rem 2rem 1rem;
  }
  
  .p-card-content {
    padding: 2rem;
  }
  
  .p-card-footer {
    background-color: #f8f9fa;
    border-top: 1px solid #dee2e6;
    padding: 1.5rem 2rem;
  }
}

.auth-header {
  text-align: center;
  
  .auth-icon {
    font-size: 3rem;
    color: #667eea;
    margin-bottom: 1rem;
  }
  
  h2 {
    margin: 0 0 0.5rem;
    color: #333;
    font-weight: 600;
  }
  
  p {
    margin: 0;
    color: #6c757d;
  }
}

.auth-form {
  .form-row {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 1rem;
  }
  
  .form-group {
    margin-bottom: 1.5rem;
    
    label {
      display: block;
      margin-bottom: 0.5rem;
      font-weight: 500;
      color: #495057;
    }
  }
  
  .auth-button {
    width: 100%;
    padding: 0.75rem;
    font-size: 1rem;
    font-weight: 600;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border: none;
    
    &:hover {
      transform: translateY(-1px);
      box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4);
    }
  }
}

.captcha-container {
  .captcha-challenge {
    display: flex;
    align-items: center;
    background-color: #f8f9fa;
    border: 1px solid #ced4da;
    border-radius: 6px;
    padding: 0.75rem;
    margin-bottom: 0.5rem;
    
    span {
      flex: 1;
      font-weight: 600;
      font-size: 1.1rem;
      color: #495057;
    }
  }
}

.password-strength {
  margin-top: 0.5rem;
  
  .strength-bar {
    height: 4px;
    background-color: #e9ecef;
    border-radius: 2px;
    overflow: hidden;
    margin-bottom: 0.25rem;
    
    .strength-fill {
      height: 100%;
      transition: all 0.3s ease;
    }
  }
}

.auth-footer {
  text-align: center;
  
  p {
    margin: 0.5rem 0;
    color: #6c757d;
    font-size: 0.9rem;
  }
  
  .auth-link {
    color: #667eea;
    text-decoration: none;
    font-weight: 500;
    
    &:hover {
      text-decoration: underline;
    }
  }
}

.activation-content,
.success-content,
.error-content {
  text-align: center;
  
  p {
    margin-bottom: 1.5rem;
    color: #495057;
    line-height: 1.5;
  }
}

@media (max-width: 600px) {
  .auth-container {
    padding: 1rem;
  }
  
  .auth-card {
    .p-card-header,
    .p-card-content,
    .p-card-footer {
      padding-left: 1.5rem;
      padding-right: 1.5rem;
    }
  }
  
  .form-row {
    grid-template-columns: 1fr !important;
  }
}
</style>