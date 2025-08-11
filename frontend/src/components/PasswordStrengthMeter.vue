<template>
  <div class="password-strength-meter" v-if="strength.level">
    <div class="strength-bar">
      <div 
        class="strength-fill" 
        :style="{ 
          width: (strength.score / 5) * 100 + '%',
          backgroundColor: getStrengthColor(strength.level)
        }"
      ></div>
    </div>
    <div class="strength-info">
      <small :style="{ color: getStrengthColor(strength.level) }">
        Strength: {{ strength.level.toUpperCase() }}
      </small>
      <small v-if="strength.feedback" class="strength-feedback">
        {{ strength.feedback }}
      </small>
    </div>
  </div>
</template>

<script>
import { cryptoUtils } from '@/utils/crypto'
import { CONSTANTS } from '@/utils/constants'

export default {
  name: 'PasswordStrengthMeter',
  props: {
    password: {
      type: String,
      default: ''
    }
  },
  computed: {
    strength() {
      return this.password ? cryptoUtils.checkPasswordStrength(this.password) : {}
    }
  },
  methods: {
    getStrengthColor(level) {
      return CONSTANTS.PASSWORD_STRENGTH_COLORS[level] || '#6c757d'
    }
  }
}
</script>

<style lang="scss" scoped>
.password-strength-meter {
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
  
  .strength-info {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .strength-feedback {
      color: #6c757d;
      font-style: italic;
    }
  }
}
</style>