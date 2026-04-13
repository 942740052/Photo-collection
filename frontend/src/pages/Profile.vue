<template>
  <div class="profile-page">
    <div class="page-header">
      <h2>{{ t('profile.title') }}</h2>
    </div>

    <el-row :gutter="20">
      <el-col :span="8">
        <el-card class="profile-card">
          <div class="avatar-section">
            <el-avatar :size="120" :src="userStore.user?.avatar" class="avatar">
              {{ userStore.user?.nickname?.charAt(0) || 'U' }}
            </el-avatar>
            <el-button type="primary" link @click="handleUploadAvatar">
              <el-icon><Camera /></el-icon>
              {{ t('profile.changeAvatar') }}
            </el-button>
          </div>
          <div class="user-info">
            <h3>{{ userStore.user?.nickname }}</h3>
            <p class="username">@{{ userStore.user?.username }}</p>
            <p class="email">{{ userStore.user?.email }}</p>
          </div>
          <div class="stats">
            <div class="stat-item">
              <span class="stat-value">{{ stats.photoCount }}</span>
              <span class="stat-label">{{ currentLang === 'zh-CN' ? '照片' : 'Photos' }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">{{ stats.albumCount }}</span>
              <span class="stat-label">{{ currentLang === 'zh-CN' ? '相册' : 'Albums' }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">{{ stats.shareCount }}</span>
              <span class="stat-label">{{ currentLang === 'zh-CN' ? '分享' : 'Shares' }}</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="16">
        <el-card class="info-card">
          <template #header>
            <div class="card-header">
              <span>{{ t('profile.basicInfo') }}</span>
            </div>
          </template>
          <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px">
            <el-form-item :label="t('login.username')">
              <el-input :value="userStore.user?.username" disabled />
            </el-form-item>
            <el-form-item :label="t('login.email')">
              <el-input :value="userStore.user?.email" disabled />
            </el-form-item>
            <el-form-item :label="t('login.nickname')" prop="nickname">
              <el-input v-model="formData.nickname" :placeholder="t('login.nicknamePlaceholder')" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="loading" @click="handleUpdateProfile">
                {{ t('common.save') }}
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card class="password-card">
          <template #header>
            <div class="card-header">
              <span>{{ t('profile.changePassword') }}</span>
            </div>
          </template>
          <el-form ref="passwordFormRef" :model="passwordData" :rules="passwordRules" label-width="100px">
            <el-form-item :label="t('profile.oldPassword')" prop="oldPassword">
              <el-input v-model="passwordData.oldPassword" type="password" show-password />
            </el-form-item>
            <el-form-item :label="t('profile.newPassword')" prop="newPassword">
              <el-input v-model="passwordData.newPassword" type="password" show-password />
            </el-form-item>
            <el-form-item :label="t('profile.confirmPassword')" prop="confirmPassword">
              <el-input v-model="passwordData.confirmPassword" type="password" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="passwordLoading" @click="handleUpdatePassword">
                {{ t('profile.changePassword') }}
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog
      v-model="avatarDialogVisible"
      :title="t('profile.changeAvatar')"
      width="400px"
    >
      <el-upload
        class="avatar-uploader"
        drag
        action="#"
        :show-file-list="false"
        :before-upload="beforeAvatarUpload"
      >
        <el-avatar v-if="tempAvatar" :src="tempAvatar" :size="150" />
        <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
      </el-upload>
      <template #footer>
        <el-button @click="avatarDialogVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="avatarLoading" @click="handleConfirmAvatar">
          {{ t('common.confirm') }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules, UploadProps } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { authApi } from '@/api/auth'

const { t, locale } = useI18n()
const router = useRouter()
const userStore = useUserStore()

const currentLang = computed(() => locale.value)

if (!userStore.user) {
  router.push('/login')
}

const formRef = ref<FormInstance>()
const passwordFormRef = ref<FormInstance>()
const loading = ref(false)
const passwordLoading = ref(false)
const avatarDialogVisible = ref(false)
const avatarLoading = ref(false)
const tempAvatar = ref('')

const formData = reactive({
  nickname: '',
  avatar: '',
})

const passwordData = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const stats = reactive({
  photoCount: 0,
  albumCount: 0,
  shareCount: 0,
})

const rules: FormRules = {
  nickname: [
    { required: true, message: t('login.nicknamePlaceholder'), trigger: 'blur' },
  ],
}

const validateConfirmPassword = (rule: any, value: any, callback: any) => {
  if (value !== passwordData.newPassword) {
    callback(new Error(t('profile.passwordMismatch')))
  } else {
    callback()
  }
}

const passwordRules: FormRules = {
  oldPassword: [
    { required: true, message: t('profile.oldPassword'), trigger: 'blur' },
  ],
  newPassword: [
    { required: true, message: t('profile.newPassword'), trigger: 'blur' },
    { min: 6, max: 20, message: '6-20 characters', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: t('profile.confirmPassword'), trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' },
  ],
}

const handleUploadAvatar = () => {
  avatarDialogVisible.value = true
  tempAvatar.value = formData.avatar || ''
}

const beforeAvatarUpload: UploadProps['beforeUpload'] = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('Please upload an image file')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('Image size should be less than 2MB')
    return false
  }

  const reader = new FileReader()
  reader.onload = (e) => {
    tempAvatar.value = e.target?.result as string
  }
  reader.readAsDataURL(file)
  return false
}

const handleConfirmAvatar = async () => {
  if (!tempAvatar.value) {
    ElMessage.warning('Please select an image')
    return
  }

  avatarLoading.value = true
  try {
    await userStore.updateProfile({ avatar: tempAvatar.value })
    formData.avatar = tempAvatar.value
    avatarDialogVisible.value = false
    ElMessage.success(t('profile.updateSuccess'))
  } catch (error) {
    ElMessage.error(t('profile.updateFailed'))
  } finally {
    avatarLoading.value = false
  }
}

const handleUpdateProfile = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await userStore.updateProfile({
          nickname: formData.nickname,
          avatar: formData.avatar,
        })
        ElMessage.success(t('profile.updateSuccess'))
      } catch (error) {
        ElMessage.error(t('profile.updateFailed'))
      } finally {
        loading.value = false
      }
    }
  })
}

const handleUpdatePassword = async () => {
  if (!passwordFormRef.value) return

  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      passwordLoading.value = true
      try {
        await authApi.updatePassword(passwordData.oldPassword, passwordData.newPassword)
        ElMessage.success(t('profile.passwordSuccess'))
        passwordData.oldPassword = ''
        passwordData.newPassword = ''
        passwordData.confirmPassword = ''
      } catch (error) {
        ElMessage.error(t('profile.passwordFailed'))
      } finally {
        passwordLoading.value = false
      }
    }
  })
}

const loadStats = async () => {
  try {
    const res = await authApi.getUserStats()
    stats.photoCount = res.data.photoCount || 0
    stats.albumCount = res.data.albumCount || 0
    stats.shareCount = res.data.shareCount || 0
  } catch (error) {
    console.error('Failed to load stats')
  }
}

onMounted(() => {
  if (userStore.user) {
    formData.nickname = userStore.user.nickname
    formData.avatar = userStore.user.avatar || ''
  }
  loadStats()
})
</script>

<style scoped lang="scss">
.profile-page {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 20px;

  h2 {
    margin: 0;
    color: #333;
    font-size: 24px;
  }
}

.profile-card {
  text-align: center;

  .avatar-section {
    padding: 20px 0;

    .avatar {
      margin-bottom: 15px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      font-size: 48px;
      color: #fff;

      .default-avatar-img {
        width: 100%;
        height: 100%;
        object-fit: cover;
        border-radius: 50%;
      }
    }
  }

  .user-info {
    padding: 20px 0;
    border-top: 1px solid #eee;
    border-bottom: 1px solid #eee;

    h3 {
      margin: 0 0 8px;
      font-size: 20px;
      color: #333;
    }

    .username {
      margin: 0 0 5px;
      color: #666;
      font-size: 14px;
    }

    .email {
      margin: 0;
      color: #999;
      font-size: 13px;
    }
  }

  .stats {
    display: flex;
    justify-content: space-around;
    padding: 20px 0;

    .stat-item {
      text-align: center;

      .stat-value {
        display: block;
        font-size: 24px;
        font-weight: 600;
        color: #409eff;
      }

      .stat-label {
        display: block;
        font-size: 13px;
        color: #999;
        margin-top: 5px;
      }
    }
  }
}

.info-card,
.password-card {
  margin-bottom: 20px;

  .card-header {
    font-size: 16px;
    font-weight: 500;
  }
}

.avatar-uploader {
  display: flex;
  justify-content: center;

  :deep(.el-upload-dragger) {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 200px;
    height: 200px;
    border-radius: 50%;
  }

  .avatar-uploader-icon {
    font-size: 40px;
    color: #999;
  }
}
</style>
