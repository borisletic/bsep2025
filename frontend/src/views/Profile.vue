<template>
  <div class="profile">
    <h1>User Profile</h1>
    <p>Manage your account settings and preferences.</p>
    <Card>
      <template #title>Profile Settings</template>
      <template #content>
        <p>Profile management features will be implemented here.</p>
      </template>
    </Card>
  </div>
</template>

<script>
export default {
  name: 'Profile'
}
</script>

<style scoped>
.profile {
  padding: 2rem;
}
</style>