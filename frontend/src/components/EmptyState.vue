<template>
  <div class="empty-state">
    <div class="empty-icon">
      <i :class="icon"></i>
    </div>
    <h3>{{ title }}</h3>
    <p>{{ description }}</p>
    <Button
      v-if="actionLabel"
      :label="actionLabel"
      @click="$emit('action')"
      class="empty-action"
    />
  </div>
</template>

<script>
export default {
  name: 'EmptyState',
  props: {
    icon: {
      type: String,
      default: 'pi pi-inbox'
    },
    title: {
      type: String,
      required: true
    },
    description: {
      type: String,
      required: true
    },
    actionLabel: {
      type: String,
      default: null
    }
  },
  emits: ['action']
}
</script>

<style lang="scss" scoped>
.empty-state {
  text-align: center;
  padding: 3rem 2rem;
  
  .empty-icon {
    margin-bottom: 1.5rem;
    
    i {
      font-size: 4rem;
      color: #ced4da;
    }
  }
  
  h3 {
    margin: 0 0 1rem;
    color: #495057;
    font-weight: 600;
  }
  
  p {
    margin: 0 0 2rem;
    color: #6c757d;
    line-height: 1.5;
    max-width: 400px;
    margin-left: auto;
    margin-right: auto;
  }
  
  .empty-action {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border: none;
    
    &:hover {
      transform: translateY(-1px);
      box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
    }
  }
}
</style>