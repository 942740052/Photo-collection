<template>
  <div class="page-container settings-page">
    <el-tabs v-model="activeTab" tab-position="left" style="min-height: 500px">
      <el-tab-pane label="个人资料" name="profile">
        <el-card>
          <template #header><span>个人资料</span></template>
          <el-form :model="profileForm" label-width="100px" style="max-width: 500px">
            <el-form-item label="头像">
              <el-avatar :size="64">{{ userStore.currentUser?.display_name?.[0] || 'U' }}</el-avatar>
            </el-form-item>
            <el-form-item label="用户名">
              <el-input :model-value="userStore.currentUser?.username" disabled />
            </el-form-item>
            <el-form-item label="显示名称">
              <el-input v-model="profileForm.displayName" />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="profileForm.email" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveProfile">保存</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="修改密码" name="password">
        <el-card>
          <template #header><span>修改密码</span></template>
          <el-form :model="passwordForm" label-width="100px" style="max-width: 500px">
            <el-form-item label="当前密码">
              <el-input v-model="passwordForm.currentPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="新密码">
              <el-input v-model="passwordForm.newPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="确认密码">
              <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="changePassword">修改密码</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="存储管理" name="storage">
        <el-card>
          <template #header><span>存储管理</span></template>
          <div class="storage-info">
            <el-progress
              :percentage="storagePercentage"
              :color="storageColor"
              :stroke-width="16"
              style="margin-bottom: 16px"
            />
            <div class="storage-detail">
              <span>已用 {{ storageUsed }}</span>
              <span>总配额 {{ storageTotal }}</span>
            </div>
          </div>
          <el-descriptions :column="2" border style="margin-top: 20px">
            <el-descriptions-item label="照片数量">{{ photoStore.activePhotos.length }}</el-descriptions-item>
            <el-descriptions-item label="相册数量">{{ photoStore.albums.length }}</el-descriptions-item>
            <el-descriptions-item label="回收站">{{ photoStore.deletedPhotos.length }} 张</el-descriptions-item>
            <el-descriptions-item label="收藏">{{ photoStore.favoritePhotos.length }} 张</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="偏好设置" name="preferences">
        <el-card>
          <template #header><span>偏好设置</span></template>
          <el-form label-width="140px" style="max-width: 500px">
            <el-form-item label="默认上传可见性">
              <el-select v-model="preferences.defaultVisibility" style="width: 200px">
                <el-option label="私有" value="private" />
                <el-option label="公开" value="public" />
              </el-select>
            </el-form-item>
            <el-form-item label="默认照片排序">
              <el-select v-model="preferences.defaultSort" style="width: 200px">
                <el-option label="拍摄时间（新→旧）" value="date_taken-desc" />
                <el-option label="拍摄时间（旧→新）" value="date_taken-asc" />
                <el-option label="上传时间" value="date_uploaded-desc" />
              </el-select>
            </el-form-item>
            <el-form-item label="回收站保留天数">
              <el-input-number v-model="preferences.trashRetentionDays" :min="1" :max="365" />
            </el-form-item>
            <el-form-item label="界面语言">
              <el-select v-model="preferences.language" style="width: 200px">
                <el-option label="简体中文" value="zh-CN" />
                <el-option label="English" value="en" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="savePreferences">保存设置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useUserStore } from '../stores/user'
import { usePhotoStore } from '../stores/photo'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const photoStore = usePhotoStore()

const activeTab = ref('profile')

const profileForm = reactive({
  displayName: userStore.currentUser?.display_name || '',
  email: userStore.currentUser?.email || ''
})

const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const preferences = reactive({
  defaultVisibility: 'private',
  defaultSort: 'date_taken-desc',
  trashRetentionDays: 30,
  language: 'zh-CN'
})

const storageUsed = computed(() => formatSize(userStore.currentUser?.storage_used || 0))
const storageTotal = computed(() => formatSize(userStore.currentUser?.storage_quota || 0))
const storagePercentage = computed(() => {
  const used = userStore.currentUser?.storage_used || 0
  const total = userStore.currentUser?.storage_quota || 1
  return Math.round((used / total) * 100)
})
const storageColor = computed(() => {
  const p = storagePercentage.value
  if (p > 90) return '#f56c6c'
  if (p > 70) return '#e6a23c'
  return '#409eff'
})

const saveProfile = () => {
  userStore.updateProfile({
    display_name: profileForm.displayName,
    email: profileForm.email
  })
  ElMessage.success('个人资料已更新')
}

const changePassword = () => {
  if (!passwordForm.currentPassword || !passwordForm.newPassword) {
    ElMessage.warning('请填写完整')
    return
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.error('两次密码不一致')
    return
  }
  ElMessage.success('密码已修改')
  passwordForm.currentPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
}

const savePreferences = () => {
  ElMessage.success('偏好设置已保存')
}

function formatSize(bytes) {
  if (!bytes) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(1024))
  return (bytes / Math.pow(1024, i)).toFixed(1) + ' ' + units[i]
}
</script>

<style scoped>
.settings-page {
  max-width: 1000px;
  margin: 0 auto;
}

.storage-detail {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  color: #606266;
}
</style>
