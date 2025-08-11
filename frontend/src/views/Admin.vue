<template>
  <div class="admin">
    <h1>System Administration</h1>
    <p>Manage users, system settings, and security policies.</p>
    <Card>
      <template #title>Admin Panel</template>
      <template #content>
        <p>Administration features will be implemented here.</p>
      </template>
    </Card>
  </div>
</template>

<script>
export default {
  name: 'Admin'
}
</script>

<style scoped>
.admin {
  padding: 2rem;
}
</style>