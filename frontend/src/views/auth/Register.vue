<template>
  <div class="register-container">
    <Card class="register-card">
      <template #header>
        <div class="register-header">
          <i class="pi pi-user-plus" style="font-size: 3rem; color: var(--primary-color);"></i>
          <h2>Create Account</h2>
          <p>Join the PKI System</p>
        </div>
      </template>
      
      <template #content>
        <form @submit.prevent="handleRegister" class="register-form">
          <div class="form-grid">
            <div class="field">
              <label for="firstName">First Name</label>
              <InputText 
                id="firstName"
                v-model="registerForm.firstName" 
                placeholder="Enter your first name"
                :class="{ 'p-invalid': errors.firstName }"
                required
              />
              <small class="p-error" v-if="errors.firstName">{{ errors.firstName }}</small>
            </div>

            <div class="field">
              <label for="lastName">Last Name</label>
              <InputText 
                id="lastName"
                v-model="registerForm.lastName" 
                placeholder="Enter your last name"
                :class="{ 'p-invalid': errors.lastName }"
                required
              />
              <small class="p-error" v-if="errors.lastName">{{ errors.lastName }}</small>
            </div>
          </div>

          <div class="field">
            <label for="email">Email Address</label>
            <InputText 
              id="email"
              v-model="registerForm.email" 
              type="email"
              placeholder="Enter your email"
              :class="{ 'p-invalid': errors.email }"
              autocomplete="email"
              required
            />
            <small class="p-error" v-if="errors.email">{{ errors.email }}</small>
          </div>

          <div class="field">
            <label for="organization">Organization</label>
            <InputText 
              id="organization"
              v-model="registerForm.organization" 
              placeholder="Enter your organization"
              :class="{ 'p-invalid': errors.organization }"
              required
            />
            <small class="p-error" v-if="errors.organization">{{ errors.organization }}</small>
          </div>

          <div class="field">
            <label for="password">Password</label>
            <Password 
              id="password"
              v-model="registerForm.password" 
              placeholder="Create a password"
              :class="{ 'p-invalid': errors.password }"
              toggleMask
              :feedback="false"
              autocomplete="new-password"
              required
            />
            
            <!-- Password Strength Indicator -->
            <div class="password-strength" v-if="registerForm.password">
              <div class="strength-bar">
                <div 
                  class="strength-fill" 
                  :style="{ 
                    width: passwordStrength.percentage + '%',
                    backgroundColor: getStrengthColor(passwordStrength.level)
                  }"
                ></div>
              </div>
              <div class="strength-info">
                <span class="strength-level" :style="{ color: getStrengthColor(passwordStrength.level) }">
                  {{ passwordStrength.level }}
                </span>
                <small class="strength-tips">
                  <div v-for="tip in passwordStrength.feedback" :key="tip">{{ tip }}</div>
                </small>
              </div>
            </div>
            
            <small class="p-error" v-if="errors.password">{{ errors.password }}</small>
          </div>

          <div class="field">
            <label for="confirmPassword">Confirm Password</label>
            <Password 
              id="confirmPassword"
              v-model="registerForm.confirmPassword" 
              placeholder="Confirm your password"
              :class="{ 'p-invalid': errors.confirmPassword }"
              :feedback="false"
              toggleMask
              autocomplete="new-password"
              required
            />
            <small class="p-error" v-if="errors.confirmPassword">{{ errors.confirmPassword }}</small>
          </div>

          <div class="register-actions">
            <Button 
              type="submit" 
              label="Create Account" 
              icon="pi pi-user-plus"
              :loading="isLoading"
              class="w-full"
            />
            
            <div class="register-links">
              <router-link to="/login" class="link">Already have an account? Sign in</router-link>
            </div>
          </div>
        </form>
      </template>
    </Card>
  </div>
</template>

<script>
import { mapGetters, mapActions } from 'vuex'
import passwordStrengthService from '@/services/passwordStrengthService'

export default {
  name: 'Register',
  data() {
    return {
      registerForm: {
        firstName: '',
        lastName: '',
        email: '',
        organization: '',
        password: '',
        confirmPassword: ''
      },
      errors: {}
    }
  },
  computed: {
    ...mapGetters('auth', ['isLoading', 'error']),
    
    passwordStrength() {
      return passwordStrengthService.checkStrength(this.registerForm.password)
    }
  },
  methods: {
    ...mapActions('auth', ['register', 'clearError']),
    
    getStrengthColor(level) {
      return passwordStrengthService.getStrengthColor(level)
    },

    async handleRegister() {
      this.errors = {}
      
      if (!this.validateForm()) {
        return
      }

      try {
        await this.register(this.registerForm)
        
        this.$toast.add({
          severity: 'success',
          summary: 'Registration Successful',
          detail: 'Please check your email to activate your account',
          life: 10000
        })
        
        this.$router.push('/login')
      } catch (error) {
        this.$toast.add({
          severity: 'error',
          summary: 'Registration Failed',
          detail: this.error || 'Registration failed. Please try again.',
          life: 5000
        })
      }
    },

    validateForm() {
      const errors = {}
      
      if (!this.registerForm.firstName?.trim()) {
        errors.firstName = 'First name is required'
      }
      
      if (!this.registerForm.lastName?.trim()) {
        errors.lastName = 'Last name is required'
      }
      
      if (!this.registerForm.email?.trim()) {
        errors.email = 'Email is required'
      } else if (!/\S+@\S+\.\S+/.test(this.registerForm.email)) {
        errors.email = 'Email is invalid'
      }
      
      if (!this.registerForm.organization?.trim()) {
        errors.organization = 'Organization is required'
      }
      
      if (!this.registerForm.password) {
        errors.password = 'Password is required'
      } else if (this.registerForm.password.length < 8) {
        errors.password = 'Password must be at least 8 characters long'
      }
      
      if (!this.registerForm.confirmPassword) {
        errors.confirmPassword = 'Password confirmation is required'
      } else if (this.registerForm.password !== this.registerForm.confirmPassword) {
        errors.confirmPassword = 'Passwords do not match'
      }
      
      this.errors = errors
      return Object.keys(errors).length === 0
    }
  },
  beforeUnmount() {
    this.clearError()
  }
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 2rem;
}

.register-card {
  width: 100%;
  max-width: 500px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
}

.register-header {
  text-align: center;
  padding: 2rem 0 1rem;
}

.register-header h2 {
  margin: 1rem 0 0.5rem;
  color: #2c3e50;
}

.register-header p {
  color: #7f8c8d;
  margin: 0;
}

.register-form {
  padding: 0 2rem 2rem;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
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

.password-strength {
  margin-top: 0.5rem;
}

.strength-bar {
  height: 4px;
  background: #e9ecef;
  border-radius: 2px;
  overflow: hidden;
  margin-bottom: 0.5rem;
}

.strength-fill {
  height: 100%;
  transition: all 0.3s ease;
}

.strength-info {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 1rem;
}

.strength-level {
  font-weight: 600;
  font-size: 0.875rem;
}

.strength-tips {
  flex: 1;
  color: #6c757d;
  font-size: 0.75rem;
  text-align: right;
}

.register-actions {
  margin-top: 2rem;
}

.register-links {
  text-align: center;
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

@media (max-width: 768px) {
  .form-grid {
    grid-template-columns: 1fr;
  }
  
  .strength-info {
    flex-direction: column;
    gap: 0.5rem;
  }
  
  .strength-tips {
    text-align: left;
  }
}
</style>