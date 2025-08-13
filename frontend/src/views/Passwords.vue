<template>
  <div class="passwords">
    <div class="passwords-header">
      <h1>Password Manager</h1>
      <div class="header-actions">
        <Button 
          icon="pi pi-refresh" 
          label="Refresh"
          @click="loadPasswords"
          :loading="isLoading"
          size="small"
          severity="secondary"
        />
        <Button 
          icon="pi pi-plus" 
          label="New Password"
          @click="showCreateDialog = true"
        />
      </div>
    </div>

    <!-- Private Key Upload Notice -->
    <Card class="notice-card" v-if="!hasPrivateKey">
      <template #content>
        <div class="notice-content">
          <i class="pi pi-info-circle"></i>
          <div>
            <h3>Private Key Required</h3>
            <p>To decrypt and view your passwords, please upload your private key.</p>
            <Button 
              label="Upload Private Key" 
              icon="pi pi-upload"
              @click="showKeyUploadDialog = true"
              size="small"
            />
          </div>
        </div>
      </template>
    </Card>

    <!-- Passwords Grid -->
    <div class="passwords-grid" v-if="allPasswords.length > 0">
      <Card 
        v-for="password in allPasswords" 
        :key="password.id"
        class="password-card"
      >
        <template #header>
          <div class="password-header">
            <div class="site-info">
              <div class="site-icon">
                <i class="pi pi-globe"></i>
              </div>
              <div>
                <h3>{{ password.siteName }}</h3>
                <p v-if="password.siteUrl">{{ password.siteUrl }}</p>
              </div>
            </div>
            <div class="password-actions">
              <Button 
                icon="pi pi-eye" 
                size="small"
                text
                @click="viewPassword(password)"
                v-tooltip.top="'View Details'"
              />
              <Button 
                icon="pi pi-pencil" 
                size="small"
                text
                @click="editPassword(password)"
                v-if="password.owner.id === currentUser?.id"
                v-tooltip.top="'Edit'"
              />
              <Button 
                icon="pi pi-share-alt" 
                size="small"
                text
                @click="sharePassword(password)"
                v-if="password.owner.id === currentUser?.id"
                v-tooltip.top="'Share'"
              />
              <Button 
                icon="pi pi-trash" 
                size="small"
                text
                severity="danger"
                @click="deletePassword(password)"
                v-if="password.owner.id === currentUser?.id"
                v-tooltip.top="'Delete'"
              />
            </div>
          </div>
        </template>
        
        <template #content>
          <div class="password-content">
            <div class="password-field">
              <label>Username</label>
              <div class="field-value">
                <span>{{ password.username }}</span>
                <Button 
                  icon="pi pi-copy" 
                  size="small"
                  text
                  @click="copyToClipboard(password.username)"
                />
              </div>
            </div>
            
            <div class="password-field">
              <label>Password</label>
              <div class="field-value">
                <span v-if="!showPasswords[password.id]">••••••••</span>
                <span v-else class="password-text">{{ decryptedPasswords[password.id] || 'Loading...' }}</span>
                <div class="password-actions-inline">
                  <Button 
                    :icon="showPasswords[password.id] ? 'pi pi-eye-slash' : 'pi pi-eye'" 
                    size="small"
                    text
                    @click="togglePasswordVisibility(password)"
                  />
                  <Button 
                    icon="pi pi-copy" 
                    size="small"
                    text
                    @click="copyPassword(password)"
                    :disabled="!decryptedPasswords[password.id]"
                  />
                </div>
              </div>
            </div>
            
            <div class="password-meta">
              <div class="meta-item">
                <i class="pi pi-user"></i>
                <span>{{ password.owner.firstName }} {{ password.owner.lastName }}</span>
              </div>
              <div class="meta-item" v-if="password.shares.length > 1">
                <i class="pi pi-users"></i>
                <span>Shared with {{ password.shares.length - 1 }} others</span>
              </div>
              <div class="meta-item">
                <i class="pi pi-calendar"></i>
                <span>{{ formatDate(password.createdAt) }}</span>
              </div>
            </div>
          </div>
        </template>
      </Card>
    </div>

    <!-- Empty State -->
    <Card v-else-if="!isLoading" class="empty-state-card">
      <template #content>
        <div class="empty-state">
          <i class="pi pi-lock" style="font-size: 4rem; color: #ccc;"></i>
          <h3>No Passwords Saved</h3>
          <p>Start by creating your first password entry</p>
          <Button 
            label="Create Password Entry" 
            icon="pi pi-plus"
            @click="showCreateDialog = true"
          />
        </div>
      </template>
    </Card>

    <!-- Create/Edit Password Dialog -->
    <Dialog 
      v-model:visible="showCreateDialog" 
      modal 
      :header="editingPassword ? 'Edit Password' : 'New Password Entry'"
      :style="{ width: '500px' }"
      @hide="resetForm"
    >
      <form @submit.prevent="handleSavePassword">
        <div class="field">
          <label for="siteName">Site Name</label>
          <InputText 
            id="siteName"
            v-model="passwordForm.siteName" 
            placeholder="e.g., Gmail, GitHub"
            class="w-full"
            required
          />
        </div>

        <div class="field">
          <label for="siteUrl">Site URL (Optional)</label>
          <InputText 
            id="siteUrl"
            v-model="passwordForm.siteUrl" 
            placeholder="https://example.com"
            class="w-full"
          />
        </div>

        <div class="field">
          <label for="username">Username/Email</label>
          <InputText 
            id="username"
            v-model="passwordForm.username" 
            placeholder="Enter username or email"
            class="w-full"
            required
          />
        </div>

        <div class="field">
          <label for="password">Password</label>
          <Password 
            id="password"
            v-model="passwordForm.password" 
            placeholder="Enter password"
            class="w-full"
            :feedback="false"
            toggleMask
            required
          />
        </div>

        <div class="field">
          <label for="description">Description (Optional)</label>
          <Textarea 
            id="description"
            v-model="passwordForm.description" 
            placeholder="Additional notes..."
            :rows="3"
            class="w-full"
          />
        </div>
        
        <div class="dialog-footer">
          <Button 
            type="button" 
            label="Cancel" 
            severity="secondary"
            @click="showCreateDialog = false"
          />
          <Button 
            type="submit" 
            :label="editingPassword ? 'Update' : 'Save'"
            :loading="saveLoading"
          />
        </div>
      </form>
    </Dialog>

    <!-- Private Key Upload Dialog -->
    <Dialog 
      v-model:visible="showKeyUploadDialog" 
      modal 
      header="Upload Private Key"
      :style="{ width: '500px' }"
    >
      <div class="key-upload-content">
        <p>Upload your private key to decrypt and view passwords. The key is stored securely in your browser session only.</p>
        
        <div class="field">
          <FileUpload 
            mode="basic" 
            accept=".pem,.key"
            :maxFileSize="1000000"
            @select="onPrivateKeySelect"
            chooseLabel="Upload Private Key"
            class="w-full"
          />
        </div>
        
        <div class="field">
          <label>Or Paste Private Key</label>
          <Textarea 
            v-model="privateKeyText"
            placeholder="-----BEGIN PRIVATE KEY-----
...
-----END PRIVATE KEY-----"
            :rows="10"
            class="w-full"
          />
        </div>
        
        <div class="dialog-footer">
          <Button 
            type="button" 
            label="Cancel" 
            severity="secondary"
            @click="showKeyUploadDialog = false"
          />
          <Button 
            label="Import Key" 
            @click="importPrivateKey"
            :loading="keyImportLoading"
          />
        </div>
      </div>
    </Dialog>

    <!-- Share Password Dialog -->
    <Dialog 
      v-model:visible="showShareDialog" 
      modal 
      header="Share Password"
      :style="{ width: '600px' }"
    >
      <div v-if="selectedPassword" class="share-content">
        <div class="share-info">
          <h4>{{ selectedPassword.siteName }}</h4>
          <p>Select users to share this password with:</p>
        </div>
        
        <div class="current-shares" v-if="selectedPassword.shares.length > 1">
          <h5>Currently Shared With:</h5>
          <div class="shares-list">
            <div 
              v-for="share in selectedPassword.shares.filter(s => s.user.id !== currentUser?.id)" 
              :key="share.id"
              class="share-item"
            >
              <div class="user-info">
                <i class="pi pi-user"></i>
                <span>{{ share.user.firstName }} {{ share.user.lastName }}</span>
                <small>({{ share.user.email }})</small>
              </div>
              <Button 
                icon="pi pi-times" 
                size="small"
                text
                severity="danger"
                @click="unsharePassword(share.user.id)"
                v-tooltip.top="'Remove Access'"
              />
            </div>
          </div>
        </div>
        
        <div class="new-shares">
          <h5>Share With New Users:</h5>
          <div class="field">
            <Dropdown 
              v-model="selectedUserToShare" 
              :options="availableUsers"
              optionLabel="displayName"
              optionValue="id"
              placeholder="Select user to share with"
              class="w-full"
              :filter="true"
            />
          </div>
          <Button 
            label="Share" 
            icon="pi pi-share-alt"
            @click="handleSharePassword"
            :disabled="!selectedUserToShare"
            :loading="shareLoading"
            size="small"
          />
        </div>
      </div>
      
      <template #footer>
        <Button 
          label="Close" 
          @click="showShareDialog = false"
        />
      </template>
    </Dialog>
  </div>
</template>

<script>
import { mapGetters, mapActions } from 'vuex'
import cryptoService from '@/services/cryptoService'
import moment from 'moment'

export default {
  name: 'Passwords',
  data() {
    return {
      showCreateDialog: false,
      showKeyUploadDialog: false,
      showShareDialog: false,
      editingPassword: null,
      selectedPassword: null,
      saveLoading: false,
      shareLoading: false,
      keyImportLoading: false,
      hasPrivateKey: false,
      privateKey: null,
      privateKeyText: '',
      selectedUserToShare: null,
      availableUsers: [],
      passwordForm: {
        siteName: '',
        siteUrl: '',
        username: '',
        password: '',
        description: ''
      },
      showPasswords: {},
      decryptedPasswords: {}
    }
  },
  computed: {
    ...mapGetters('auth', ['currentUser']),
    ...mapGetters('passwords', ['allPasswords', 'isLoading'])
  },
  methods: {
    ...mapActions('passwords', [
      'fetchPasswords', 
      'createPassword', 
      'updatePassword', 
      'deletePassword', 
      'sharePassword'
    ]),

    async loadPasswords() {
      try {
        await this.fetchPasswords()
      } catch (error) {
        this.$toast.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to load passwords',
          life: 3000
        })
      }
    },

    async loadAvailableUsers() {
      try {
        // This would be an API call to get eligible users
        // For now, we'll simulate it
        this.availableUsers = [
          { id: 1, firstName: 'John', lastName: 'Doe', email: 'john@example.com', displayName: 'John Doe (john@example.com)' },
          { id: 2, firstName: 'Jane', lastName: 'Smith', email: 'jane@example.com', displayName: 'Jane Smith (jane@example.com)' }
        ]
      } catch (error) {
        console.error('Failed to load users:', error)
      }
    },

    async handleSavePassword() {
      if (!this.hasPrivateKey) {
        this.$toast.add({
          severity: 'warn',
          summary: 'Warning',
          detail: 'Please upload your private key first',
          life: 3000
        })
        return
      }

      try {
        this.saveLoading = true

        // Get current user's public key (this would come from their certificate)
        const publicKeyPem = await this.getCurrentUserPublicKey()
        const publicKey = await cryptoService.importPublicKey(publicKeyPem)
        
        // Encrypt password with user's public key
        const encryptedPassword = await cryptoService.encryptText(this.passwordForm.password, publicKey)

        const passwordData = {
          siteName: this.passwordForm.siteName,
          siteUrl: this.passwordForm.siteUrl,
          username: this.passwordForm.username,
          description: this.passwordForm.description,
          encryptedPassword
        }

        if (this.editingPassword) {
          await this.updatePassword({ id: this.editingPassword.id, passwordData })
          this.$toast.add({
            severity: 'success',
            summary: 'Success',
            detail: 'Password updated successfully',
            life: 3000
          })
        } else {
          await this.createPassword(passwordData)
          this.$toast.add({
            severity: 'success',
            summary: 'Success',
            detail: 'Password created successfully',
            life: 3000
          })
        }

        this.showCreateDialog = false
        this.resetForm()
      } catch (error) {
        this.$toast.add({
          severity: 'error',
          summary: 'Error',
          detail: error.response?.data?.message || 'Failed to save password',
          life: 5000
        })
      } finally {
        this.saveLoading = false
      }
    },

    async togglePasswordVisibility(password) {
      const passwordId = password.id
      
      if (this.showPasswords[passwordId]) {
        // Hide password
        this.$set(this.showPasswords, passwordId, false)
        this.$delete(this.decryptedPasswords, passwordId)
      } else {
        // Show password - decrypt it
        if (!this.hasPrivateKey) {
          this.$toast.add({
            severity: 'warn',
            summary: 'Warning',
            detail: 'Please upload your private key to view passwords',
            life: 3000
          })
          return
        }

        try {
          // Find the encrypted password for current user
          const userShare = password.shares.find(share => share.user.id === this.currentUser?.id)
          if (!userShare) {
            throw new Error('Password not accessible')
          }

          const decryptedPassword = await cryptoService.decryptText(userShare.encryptedPassword, this.privateKey)
          this.$set(this.decryptedPasswords, passwordId, decryptedPassword)
          this.$set(this.showPasswords, passwordId, true)
        } catch (error) {
          this.$toast.add({
            severity: 'error',
            summary: 'Error',
            detail: 'Failed to decrypt password',
            life: 3000
          })
        }
      }
    },

    async copyPassword(password) {
      if (!this.decryptedPasswords[password.id]) {
        await this.togglePasswordVisibility(password)
      }
      
      const decryptedPassword = this.decryptedPasswords[password.id]
      if (decryptedPassword) {
        await this.copyToClipboard(decryptedPassword)
      }
    },

    async copyToClipboard(text) {
      try {
        await navigator.clipboard.writeText(text)
        this.$toast.add({
          severity: 'success',
          summary: 'Copied',
          detail: 'Copied to clipboard',
          life: 2000
        })
      } catch (error) {
        this.$toast.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to copy to clipboard',
          life: 3000
        })
      }
    },

    viewPassword(password) {
      this.selectedPassword = password
      // Could show a detailed view dialog here
    },

    editPassword(password) {
      this.editingPassword = password
      this.passwordForm = {
        siteName: password.siteName,
        siteUrl: password.siteUrl || '',
        username: password.username,
        password: '', // Password needs to be decrypted first
        description: password.description || ''
      }
      this.showCreateDialog = true
    },

    async sharePassword(password) {
      this.selectedPassword = password
      await this.loadAvailableUsers()
      this.showShareDialog = true
    },

    async handleSharePassword() {
      if (!this.selectedUserToShare || !this.selectedPassword) return

      try {
        this.shareLoading = true

        // Get the target user's public key
        const targetUser = this.availableUsers.find(u => u.id === this.selectedUserToShare)
        const targetPublicKeyPem = await this.getUserPublicKey(targetUser.id)
        const targetPublicKey = await cryptoService.importPublicKey(targetPublicKeyPem)

        // Decrypt password with current user's private key
        const userShare = this.selectedPassword.shares.find(share => share.user.id === this.currentUser?.id)
        const decryptedPassword = await cryptoService.decryptText(userShare.encryptedPassword, this.privateKey)

        // Encrypt with target user's public key
        const encryptedForTarget = await cryptoService.encryptText(decryptedPassword, targetPublicKey)

        // Share the password
        await this.sharePassword({
          id: this.selectedPassword.id,
          shareData: {
            userId: this.selectedUserToShare,
            encryptedPassword: encryptedForTarget
          }
        })

        this.$toast.add({
          severity: 'success',
          summary: 'Success',
          detail: `Password shared with ${targetUser.firstName} ${targetUser.lastName}`,
          life: 3000
        })

        this.selectedUserToShare = null
        await this.loadPasswords() // Refresh to show updated shares
      } catch (error) {
        this.$toast.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to share password',
          life: 3000
        })
      } finally {
        this.shareLoading = false
      }
    },

    async deletePassword(password) {
      this.$confirm.require({
        message: `Are you sure you want to delete the password for ${password.siteName}?`,
        header: 'Confirm Deletion',
        icon: 'pi pi-exclamation-triangle',
        accept: async () => {
          try {
            await this.deletePassword(password.id)
            this.$toast.add({
              severity: 'success',
              summary: 'Success',
              detail: 'Password deleted successfully',
              life: 3000
            })
          } catch (error) {
            this.$toast.add({
              severity: 'error',
              summary: 'Error',
              detail: 'Failed to delete password',
              life: 3000
            })
          }
        }
      })
    },

    async onPrivateKeySelect(event) {
      const file = event.files[0]
      if (file) {
        try {
          const reader = new FileReader()
          reader.onload = (e) => {
            this.privateKeyText = e.target.result
          }
          reader.readAsText(file)
        } catch (error) {
          this.$toast.add({
            severity: 'error',
            summary: 'Error',
            detail: 'Failed to read private key file',
            life: 3000
          })
        }
      }
    },

    async importPrivateKey() {
      if (!this.privateKeyText.trim()) {
        this.$toast.add({
          severity: 'warn',
          summary: 'Warning',
          detail: 'Please provide a private key',
          life: 3000
        })
        return
      }

      try {
        this.keyImportLoading = true
        
        // Import and validate private key
        this.privateKey = await cryptoService.importPrivateKey(this.privateKeyText)
        this.hasPrivateKey = true
        
        this.$toast.add({
          severity: 'success',
          summary: 'Success',
          detail: 'Private key imported successfully',
          life: 3000
        })
        
        this.showKeyUploadDialog = false
        this.privateKeyText = ''
      } catch (error) {
        this.$toast.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Invalid private key format',
          life: 3000
        })
      } finally {
        this.keyImportLoading = false
      }
    },

    resetForm() {
      this.passwordForm = {
        siteName: '',
        siteUrl: '',
        username: '',
        password: '',
        description: ''
      }
      this.editingPassword = null
    },

    formatDate(dateTime) {
      return moment(dateTime).format('MMM DD, YYYY')
    },

    // Mock methods - these would be real API calls
    async getCurrentUserPublicKey() {
      // This would get the current user's public key from their certificate
      return '-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA...\n-----END PUBLIC KEY-----'
    },

    async getUserPublicKey(userId) {
      // This would get a specific user's public key
      return '-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA...\n-----END PUBLIC KEY-----'
    }
  },

  async mounted() {
    await this.loadPasswords()
  }
}
</script>

<style scoped>
.passwords {
  padding: 2rem;
  max-width: 1200px;
  margin: 0 auto;
}

.passwords-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.passwords-header h1 {
  margin: 0;
  color: #2c3e50;
}

.header-actions {
  display: flex;
  gap: 1rem;
}

.notice-card {
  margin-bottom: 2rem;
  border-left: 4px solid #3498db;
}

.notice-content {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.notice-content i {
  font-size: 2rem;
  color: #3498db;
}

.notice-content h3 {
  margin: 0 0 0.5rem;
  color: #2c3e50;
}

.notice-content p {
  margin: 0 0 1rem;
  color: #7f8c8d;
}

.passwords-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 1.5rem;
}

.password-card {
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s;
}

.password-card:hover {
  transform: translateY(-2px);
}

.password-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 1.5rem 1.5rem 0;
}

.site-info {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.site-icon {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1.2rem;
}

.site-info h3 {
  margin: 0 0 0.25rem;
  color: #2c3e50;
  font-size: 1.1rem;
}

.site-info p {
  margin: 0;
  color: #7f8c8d;
  font-size: 0.875rem;
}

.password-actions {
  display: flex;
  gap: 0.25rem;
}

.password-content {
  padding: 0 1.5rem 1.5rem;
}

.password-field {
  margin-bottom: 1rem;
}

.password-field:last-of-type {
  margin-bottom: 1.5rem;
}

.password-field label {
  display: block;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 0.5rem;
  font-size: 0.875rem;
}

.field-value {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.5rem;
  background: #f8f9fa;
  border-radius: 4px;
}

.password-text {
  font-family: 'Courier New', monospace;
  font-size: 0.875rem;
}

.password-actions-inline {
  display: flex;
  gap: 0.25rem;
}

.password-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  padding-top: 1rem;
  border-top: 1px solid #f0f0f0;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #7f8c8d;
  font-size: 0.875rem;
}

.meta-item i {
  font-size: 0.75rem;
}

.empty-state-card {
  text-align: center;
  margin-top: 3rem;
}

.empty-state {
  padding: 3rem;
}

.empty-state h3 {
  margin: 1rem 0 0.5rem;
  color: #2c3e50;
}

.empty-state p {
  margin: 0 0 2rem;
  color: #7f8c8d;
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

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
  margin-top: 2rem;
  padding-top: 1rem;
  border-top: 1px solid #e9ecef;
}

.key-upload-content p {
  margin-bottom: 1.5rem;
  color: #7f8c8d;
  line-height: 1.5;
}

.share-content {
  padding: 0;
}

.share-info {
  margin-bottom: 2rem;
}

.share-info h4 {
  margin: 0 0 0.5rem;
  color: #2c3e50;
}

.share-info p {
  margin: 0;
  color: #7f8c8d;
}

.current-shares {
  margin-bottom: 2rem;
}

.current-shares h5 {
  margin: 0 0 1rem;
  color: #2c3e50;
  font-size: 0.875rem;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.shares-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.share-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem;
  background: #f8f9fa;
  border-radius: 4px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.user-info i {
  color: #7f8c8d;
}

.user-info small {
  color: #95a5a6;
  margin-left: 0.5rem;
}

.new-shares h5 {
  margin: 0 0 1rem;
  color: #2c3e50;
  font-size: 0.875rem;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.new-shares .field {
  margin-bottom: 1rem;
}

@media (max-width: 768px) {
  .passwords {
    padding: 1rem;
  }
  
  .passwords-header {
    flex-direction: column;
    gap: 1rem;
    align-items: stretch;
  }
  
  .header-actions {
    justify-content: center;
  }
  
  .passwords-grid {
    grid-template-columns: 1fr;
  }
  
  .password-header {
    flex-direction: column;
    gap: 1rem;
  }
  
  .password-actions {
    align-self: flex-end;
  }
  
  .password-meta {
    flex-direction: column;
    gap: 0.5rem;
  }
}
</style>