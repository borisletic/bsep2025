<template>
  <div class="passwords">
    <h1>Password Manager</h1>
    <p>Securely store and manage your passwords.</p>
    <Card>
      <template #title>Password Entries</template>
      <template #content>
        <p>Password management features will be implemented here.</p>
      </template>
    </Card>
  </div>
</template>

<script>
export default {
  name: 'Passwords'
}
</script>

<style scoped>
.passwords {
  padding: 2rem;
}
</style>