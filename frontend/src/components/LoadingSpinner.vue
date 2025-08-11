<template>
  <div class="loading-spinner" :class="{ 'full-page': fullPage }">
    <div class="spinner-container">
      <div class="spinner"></div>
      <p v-if="message" class="loading-message">{{ message }}</p>
    </div>
  </div>
</template>

<script>
export default {
  name: 'LoadingSpinner',
  props: {
    message: {
      type: String,
      default: 'Loading...'
    },
    fullPage: {
      type: Boolean,
      default: false
    }
  }
}
</script>

<style lang="scss" scoped>
.loading-spinner {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  
  &.full-page {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-color: rgba(255, 255, 255, 0.9);
    z-index: 9999;
  }
}

.spinner-container {
  text-align: center;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 1rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.loading-message {
  margin: 0;
  color: #6c757d;
  font-weight: 500;
}
</style>