<template>
  <div class="shared-content-page">
    <div class="shared-header">
      <el-icon :size="32" color="#409eff"><Camera /></el-icon>
      <h1>照片收藏系统</h1>
    </div>

    <div v-if="!shareData" class="shared-empty">
      <el-result icon="warning" title="分享链接无效" sub-title="该分享链接不存在或已过期">
        <template #extra>
          <el-button type="primary" @click="$router.push('/login')">返回首页</el-button>
        </template>
      </el-result>
    </div>

    <div v-else-if="needPassword" class="shared-password">
      <el-card style="max-width: 400px; margin: 0 auto">
        <h3>此分享需要密码访问</h3>
        <el-input v-model="password" placeholder="请输入访问密码" type="password" show-password style="margin-top: 16px" />
        <el-button type="primary" style="width: 100%; margin-top: 12px" @click="verifyPassword">确认</el-button>
      </el-card>
    </div>

    <div v-else class="shared-gallery">
      <div class="gallery-header">
        <h2>{{ shareData.resource_name }}</h2>
        <div class="gallery-info">
          <span>{{ shareData.resource_type === 'album' ? '相册' : '照片' }}</span>
          <span>·</span>
          <span>访问 {{ shareData.view_count }} 次</span>
        </div>
        <el-button v-if="shareData.allow_download" :icon="Download" @click="handleDownload">下载</el-button>
      </div>

      <div class="photo-grid medium">
        <div v-for="photo in sharedPhotos" :key="photo.id" class="photo-item">
          <img :src="photo.thumbnail_medium" :alt="photo.file_name" loading="lazy" />
          <div class="photo-overlay">
            <span class="photo-name">{{ photo.file_name }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useShareStore } from '../stores/share'
import { usePhotoStore } from '../stores/photo'
import { ElMessage } from 'element-plus'
import { Download } from '@element-plus/icons-vue'

const route = useRoute()
const shareStore = useShareStore()
const photoStore = usePhotoStore()

const password = ref('')
const needPassword = ref(false)
const passwordVerified = ref(false)

const shareData = computed(() => {
  const token = route.params.token
  return shareStore.getShareByToken(token)
})

const sharedPhotos = computed(() => {
  if (!shareData.value) return []
  if (shareData.value.resource_type === 'album') {
    return photoStore.getAlbumPhotos(shareData.value.resource_id)
  }
  const photo = photoStore.getPhoto(shareData.value.resource_id)
  return photo ? [photo] : []
})

onMounted(() => {
  if (shareData.value?.password) {
    needPassword.value = true
  }
})

const verifyPassword = () => {
  if (password.value === shareData.value.password) {
    needPassword.value = false
    passwordVerified.value = true
  } else {
    ElMessage.error('密码错误')
  }
}

const handleDownload = () => {
  ElMessage.info('下载功能')
}
</script>

<style scoped>
.shared-content-page {
  min-height: 100vh;
  background: #f5f7fa;
  padding: 20px;
}

.shared-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
}

.shared-header h1 {
  margin: 0;
  font-size: 20px;
  color: #303133;
}

.shared-empty {
  display: flex;
  justify-content: center;
  padding-top: 100px;
}

.shared-password {
  padding-top: 100px;
}

.shared-gallery {
  max-width: 1200px;
  margin: 0 auto;
}

.gallery-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.gallery-header h2 {
  margin: 0;
  font-size: 22px;
}

.gallery-info {
  color: #909399;
  font-size: 14px;
}
</style>
