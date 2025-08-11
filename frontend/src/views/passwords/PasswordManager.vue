<template>
  <div class="password-manager">
    <div class="page-header">
      <div class="header-content">
        <h1>
          <i class="pi pi-lock"></i>
          Password Manager
        </h1>
        <p>Securely store and share your passwords</p>
      </div>
      <div class="header-actions">
        <Button
          label="Add Password"
          icon="pi pi-plus"
          @click="showAddDialog = true"
        />
      </div>
    </div>

    <!-- Search -->
    <Card class="search-card">
      <template #content>
        <div class="search-container">
          <InputText
            v-model="searchQuery"
            placeholder="Search passwords..."
            @input="searchPasswords"
            class="search-input"
          />
        </div>
      </template>
    </Card>

    <!-- Password Entries -->
    <div class="passwords-grid">
      <Card
        v-for="entry in passwordEntries"
        :key="entry.id"
        class="password-card"
        @click="viewPassword(entry)"
      >
        <template #content>
          <div class="password-content">
            <div class="password-info">
              <div class="site-info">
                <h3>{{ entry.siteName }}</h3>
                <p class="username">{{ entry.username }}</p>
                <p v-if="entry.siteUrl" class="url">{{ entry.siteUrl }}</p>
              </div>
              <div class="password-meta">
                <small class="owner">
                  {{ entry.isOwner ? 'Owned' : `Shared by ${entry.ownerEmail}` }}
                </small>
                <small class="date">
                  {{ formatRelativeTime(entry.updatedAt) }}
                </small>
              </div>
            </div>
            <div class="password-actions">
              <Button
                icon="pi pi-eye"
                class="p-button-text p-button-sm"
                @click.stop="revealPassword(entry)"
                v-tooltip="'View Password'"
              />
              <Button
                v-if="entry.isOwner"
                icon="pi pi-share-alt"
                class="p-button-text p-button-sm"
                @click.stop="sharePassword(entry)"
                v-tooltip="'Share'"
              />
              <Button
                v-if="entry.isOwner"
                icon="pi pi-pencil"
                class="p-button-text p-button-sm"
                @click.stop="editPassword(entry)"
                v-tooltip="'Edit'"
              />
            </div>
          </div>
        </template>
      </Card>
    </div>

    <!-- Empty State -->
    <EmptyState
      v-if="passwordEntries.length === 0 && !loading"
      icon="pi pi-lock"
      title="No Passwords Found"
      description="Start by adding your first password entry"
      actionLabel="Add Password"
      @action="showAddDialog = true"
    />

    <!-- Add/Edit Password Dialog -->
    <Dialog
      v-model:visible="showAddDialog"
      modal
      :header="editingEntry ? 'Edit Password' : 'Add New Password'"
      :style="{ width: '600px' }"
    >
      <div class="password-form">
        <div class="form-group">
          <label>Site Name *</label>
          <InputText
            v-model="passwordForm.siteName"
            placeholder="Enter site name"
            class="w-full"
          />
        </div>

        <div class="form-group">
          <label>Site URL</label>
          <InputText
            v-model="passwordForm.siteUrl"
            placeholder="https://example.com"
            class="w-full"
          />
        </div>

        <div class="form-group">
          <label>Username *</label>
          <InputText
            v-model="passwordForm.username"
            placeholder="Enter username"
            class="w-full"
          />
        </div>

        <div class="form-group">
          <label>Password *</label>
          <div class="password-input-group">
            <Password
              v-model="passwordForm.password"
              placeholder="Enter password"
              :feedback="false"
              toggleMask
              class="password-input"
            />
            <Button
              icon="pi pi-refresh"
              class="p-button-secondary"
              @click="generatePassword"
              v-tooltip="'Generate Password'"
            />
          </div>
        </div>

        <div class="form-group">
          <label>Description</label>
          <Textarea
            v-model="passwordForm.description"
            rows="3"
            placeholder="Optional description..."
            class="w-full"
          />
        </div>
      </div>

      <template #footer>
        <Button
          label="Cancel"
          class="p-button-text"
          @click="closePasswordDialog"
        />
        <Button
          :label="editingEntry ? 'Update' : 'Save'"
          @click="savePassword"
          :loading="savingPassword"
          :disabled="!isPasswordFormValid"
        />
      </template>
    </Dialog>

    <!-- View Password Dialog -->
    <Dialog
      v-model:visible="showViewDialog"
      modal
      header="Password Details"
      :style="{ width: '500px' }"
    >
      <div v-if="selectedEntry" class="view-password-content">
        <div class="password-details">
          <div class="detail-item">
            <label>Site Name</label>
            <span>{{ selectedEntry.siteName }}</span>
          </div>
          <div class="detail-item" v-if="selectedEntry.siteUrl">
            <label>Site URL</label>
            <a :href="selectedEntry.siteUrl" target="_blank">{{ selectedEntry.siteUrl }}</a>
          </div>
          <div class="detail-item">
            <label>Username</label>
            <span>{{ selectedEntry.username }}</span>
          </div>
          <div class="detail-item">
            <label>Password</label>
            <div class="password-reveal">
              <span v-if="!passwordRevealed">••••••••••••</span>
              <span v-else class="revealed-password">{{ decryptedPassword }}</span>
              <Button
                :icon="passwordRevealed ? 'pi pi-eye-slash' : 'pi pi-eye'"
                class="p-button-text p-button-sm"
                @click="togglePasswordReveal"
              />
            </div>
          </div>
          <div class="detail-item" v-if="selectedEntry.description">
            <label>Description</label>
            <p>{{ selectedEntry.description }}</p>
          </div>
        </div>

        <div v-if="selectedEntry.sharedWith?.length > 0" class="shared-info">
          <h4>Shared With</h4>
          <div class="shared-users">
            <Chip
              v-for="user in selectedEntry.sharedWith"
              :key="user.id"
              :label="user.email"
              removable
              @remove="removeShare(user.email)"
            />
          </div>
        </div>
      </div>
    </Dialog>

    <!-- Share Password Dialog -->
    <Dialog
      v-model:visible="showShareDialog"
      modal
      header="Share Password"
      :style="{ width: '400px' }"
    >
      <div class="share-form">
        <p>Share "<strong>{{ selectedEntry?.siteName }}</strong>" with:</p>
        
        <div class="form-group">
          <label>User Email</label>
          <InputText
            v-model="shareForm.userEmail"
            type="email"
            placeholder="Enter user email"
            class="w-full"
          />
        </div>
      </div>

      <template #footer>
        <Button
          label="Cancel"
          class="p-button-text"
          @click="showShareDialog = false"
        />
        <Button
          label="Share"
          @click="handleSharePassword"
          :loading="sharingPassword"
          :disabled="!shareForm.userEmail"
        />
      </template>
    </Dialog>
  </div>
</template>

<script>
import { mapGetters, mapActions } from 'vuex'
import EmptyState from '@/components/EmptyState.vue'
import { cryptoUtils } from '@/utils/crypto'
import { formatUtils } from '@/utils/formatting'

export default {
  name: 'PasswordManager',
  components: {
    EmptyState
  },
  data() {
    return {
      searchQuery: '',
      showAddDialog: false,
      showViewDialog: false,
      showShareDialog: false,
      editingEntry: null,
      selectedEntry: null,
      passwordRevealed: false,
      decryptedPassword: '',
      savingPassword: false,
      sharingPassword: false,
      userKeyPair: null,
      passwordForm: {
        siteName: '',
        siteUrl: '',
        username: '',
        password: '',
        description: ''
      },
      shareForm: {
        userEmail: ''
      }
    }
  },
  computed: {
    ...mapGetters('passwords', ['allPasswordEntries', 'isLoading']),
    loading() {
      return this.isLoading
    },
    passwordEntries() {
      return this.allPasswordEntries
    },
    isPasswordFormValid() {
      return this.passwordForm.siteName && 
             this.passwordForm.username && 
             this.passwordForm.password
    }
  },
  methods: {
    ...mapActions('passwords', [
      'fetchPasswordEntries',
      'createPasswordEntry',
      'updatePasswordEntry',
      'deletePasswordEntry',
      'sharePassword',
      'removePasswordShare',
      'searchPasswordEntries'
    ]),

    formatRelativeTime(date) {
      return formatUtils.formatRelativeTime(date)
    },

    async initializeKeyPair() {
      // Check if user has stored key pair
      const storedPrivateKey = localStorage.getItem('userPrivateKey')
      const storedPublicKey = localStorage.getItem('userPublicKey')

      if (storedPrivateKey && storedPublicKey) {
        try {
          const privateKey = await cryptoUtils.importPrivateKeyFromPEM(storedPrivateKey)
          const publicKey = await cryptoUtils.importPublicKeyFromPEM(storedPublicKey)
          this.userKeyPair = { privateKey, publicKey }
        } catch (error) {
          console.error('Failed to import stored keys:', error)
          await this.generateNewKeyPair()
        }
      } else {
        await this.generateNewKeyPair()
      }
    },

    async generateNewKeyPair() {
      try {
        this.userKeyPair = await cryptoUtils.generateKeyPair()
        
        // Store keys in localStorage
        const privateKeyPEM = await cryptoUtils.exportPrivateKeyToPEM(this.userKeyPair.privateKey)
        const publicKeyPEM = await cryptoUtils.exportPublicKeyToPEM(this.userKeyPair.publicKey)
        
        localStorage.setItem('userPrivateKey', privateKeyPEM)
        localStorage.setItem('userPublicKey', publicKeyPEM)
      } catch (error) {
        console.error('Failed to generate key pair:', error)
        this.$toast.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to initialize encryption keys',
          life: 5000
        })
      }
    },

    generatePassword() {
      this.passwordForm.password = cryptoUtils.generateSecurePassword(16)
    },

    async searchPasswords() {
      if (this.searchQuery.trim()) {
        await this.searchPasswordEntries(this.searchQuery)
      } else {
        await this.fetchPasswordEntries()
      }
    },

    async viewPassword(entry) {
      this.selectedEntry = entry
      this.passwordRevealed = false
      this.decryptedPassword = ''
      this.showViewDialog = true
    },

    async togglePasswordReveal() {
      if (!this.passwordRevealed) {
        try {
          this.decryptedPassword = await cryptoUtils.decryptWithPrivateKey(
            this.selectedEntry.encryptedPassword,
            this.userKeyPair.privateKey
          )
          this.passwordRevealed = true
        } catch (error) {
          console.error('Failed to decrypt password:', error)
          this.$toast.add({
            severity: 'error',
            summary: 'Error',
            detail: 'Failed to decrypt password',
            life: 3000
          })
        }
      } else {
        this.passwordRevealed = false
        this.decryptedPassword = ''
      }
    },

    editPassword(entry) {
      this.editingEntry = entry
      this.passwordForm = {
        siteName: entry.siteName,
        siteUrl: entry.siteUrl || '',
        username: entry.username,
        password: '', // Will be decrypted when dialog opens
        description: entry.description || ''
      }
      this.showAddDialog = true
      this.decryptPasswordForEdit(entry)
    },

    async decryptPasswordForEdit(entry) {
      try {
        this.passwordForm.password = await cryptoUtils.decryptWithPrivateKey(
          entry.encryptedPassword,
          this.userKeyPair.privateKey
        )
      } catch (error) {
        console.error('Failed to decrypt password for editing:', error)
      }
    },

    sharePassword(entry) {
      this.selectedEntry = entry
      this.shareForm.userEmail = ''
      this.showShareDialog = true
    },

    async savePassword() {
      if (!this.isPasswordFormValid) return

      this.savingPassword = true
      try {
        const encryptedPassword = await cryptoUtils.encryptWithPublicKey(
          this.passwordForm.password,
          this.userKeyPair.publicKey
        )

        const entryData = {
          siteName: this.passwordForm.siteName,
          siteUrl: this.passwordForm.siteUrl,
          username: this.passwordForm.username,
          encryptedPassword,
          description: this.passwordForm.description
        }

        if (this.editingEntry) {
          await this.updatePasswordEntry({
            id: this.editingEntry.id,
            entryData
          })
          this.$toast.add({
            severity: 'success',
            summary: 'Success',
            detail: 'Password updated successfully',
            life: 3000
          })
        } else {
          await this.createPasswordEntry(entryData)
          this.$toast.add({
            severity: 'success',
            summary: 'Success',
            detail: 'Password saved successfully',
            life: 3000
          })
        }

        this.closePasswordDialog()
      } catch (error) {
        this.$toast.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to save password',
          life: 5000
        })
      } finally {
        this.savingPassword = false
      }
    },

    async handleSharePassword() {
      if (!this.shareForm.userEmail) return

      this.sharingPassword = true
      try {
        // In a real implementation, you would fetch the target user's public key
        // For now, we'll simulate this
        const targetUserPublicKey = this.userKeyPair.publicKey // Placeholder

        const decryptedPassword = await cryptoUtils.decryptWithPrivateKey(
          this.selectedEntry.encryptedPassword,
          this.userKeyPair.privateKey
        )

        const reEncryptedPassword = await cryptoUtils.encryptWithPublicKey(
          decryptedPassword,
          targetUserPublicKey
        )

        await this.sharePassword({
          id: this.selectedEntry.id,
          shareData: {
            userEmail: this.shareForm.userEmail,
            encryptedPassword: reEncryptedPassword
          }
        })

        this.$toast.add({
          severity: 'success',
          summary: 'Success',
          detail: 'Password shared successfully',
          life: 3000
        })

        this.showShareDialog = false
      } catch (error) {
        this.$toast.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to share password',
          life: 5000
        })
      } finally {
        this.sharingPassword = false
      }
    },

    async removeShare(userEmail) {
      try {
        await this.removePasswordShare({
          id: this.selectedEntry.id,
          userEmail
        })
        
        this.$toast.add({
          severity: 'success',
          summary: 'Success',
          detail: 'Share removed successfully',
          life: 3000
        })
      } catch (error) {
        this.$toast.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to remove share',
          life: 5000
        })
      }
    },

    closePasswordDialog() {
      this.showAddDialog = false
      this.editingEntry = null
      this.passwordForm = {
        siteName: '',
        siteUrl: '',
        username: '',
        password: '',
        description: ''
      }
    }
  },

  async mounted() {
    await this.initializeKeyPair()
    await this.fetchPasswordEntries()
  }
}
</script>

<style lang="scss" scoped>
.password-manager {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1rem;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 2rem;
  
  .header-content {
    h1 {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      margin: 0 0 0.5rem;
      color: #333;
      font-size: 2rem;
      font-weight: 600;
      
      i {
        color: #667eea;
      }
    }
    
    p {
      margin: 0;
      color: #6c757d;
      font-size: 1.1rem;
    }
  }
}

.search-card {
  margin-bottom: 2rem;
  
  .search-container {
    .search-input {
      width: 100%;
      max-width: 400px;
    }
  }
}

.passwords-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 1.5rem;
  
  .password-card {
    cursor: pointer;
    transition: all 0.3s ease;
    
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
    }
    
    .password-content {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      
      .password-info {
        flex: 1;
        
        .site-info {
          h3 {
            margin: 0 0 0.25rem;
            color: #333;
            font-size: 1.1rem;
            font-weight: 600;
          }
          
          .username {
            margin: 0 0 0.25rem;
            color: #667eea;
            font-weight: 500;
          }
          
          .url {
            margin: 0;
            color: #6c757d;
            font-size: 0.85rem;
          }
        }
        
        .password-meta {
          margin-top: 0.75rem;
          
          small {
            display: block;
            color: #6c757d;
            font-size: 0.8rem;
            
            &.owner {
              font-weight: 500;
            }
          }
        }
      }
      
      .password-actions {
        display: flex;
        gap: 0.25rem;
      }
    }
  }
}

.password-form {
  .form-group {
    margin-bottom: 1.5rem;
    
    label {
      display: block;
      margin-bottom: 0.5rem;
      font-weight: 500;
      color: #495057;
    }
    
    .password-input-group {
      display: flex;
      gap: 0.5rem;
      
      .password-input {
        flex: 1;
      }
    }
  }
}

.view-password-content {
  .password-details {
    .detail-item {
      margin-bottom: 1.5rem;
      
      label {
        display: block;
        font-weight: 600;
        color: #495057;
        margin-bottom: 0.25rem;
        font-size: 0.9rem;
      }
      
      span, a, p {
        color: #333;
        
        &.revealed-password {
          font-family: 'Courier New', monospace;
          background-color: #f8f9fa;
          padding: 0.25rem 0.5rem;
          border-radius: 4px;
          border: 1px solid #e9ecef;
        }
      }
      
      .password-reveal {
        display: flex;
        align-items: center;
        gap: 0.5rem;
      }
    }
  }
  
  .shared-info {
    margin-top: 2rem;
    padding-top: 1.5rem;
    border-top: 1px solid #e9ecef;
    
    h4 {
      margin: 0 0 1rem;
      color: #333;
      font-size: 1rem;
      font-weight: 600;
    }
    
    .shared-users {
      display: flex;
      flex-wrap: wrap;
      gap: 0.5rem;
    }
  }
}

.share-form {
  p {
    margin-bottom: 1.5rem;
    color: #495057;
  }
  
  .form-group {
    label {
      display: block;
      margin-bottom: 0.5rem;
      font-weight: 500;
      color: #495057;
    }
  }
}

@media (max-width: 768px) {
  .passwords-grid {
    grid-template-columns: 1fr;
  }
  
  .page-header {
    flex-direction: column;
    gap: 1rem;
    
    .header-actions {
      width: 100%;
    }
  }
}
</style>