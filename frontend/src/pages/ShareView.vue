<template>
  <div class="share-view-page">
    <div v-if="loading" class="loading-container">
      <el-icon class="is-loading" :size="40"><Loading /></el-icon>
      <p>{{ currentLang === 'zh-CN' ? '加载中...' : 'Loading...' }}</p>
    </div>

    <div v-else-if="needPassword" class="password-container">
      <el-card>
        <h2>{{ currentLang === 'zh-CN' ? '请输入访问密码' : 'Enter Password' }}</h2>
        <el-form @submit.prevent="handleVerify">
          <el-form-item>
            <el-input
              v-model="password"
              type="password"
              :placeholder="currentLang === 'zh-CN' ? '请输入访问密码' : 'Enter access password'"
              show-password
              size="large"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" size="large" @click="handleVerify" style="width: 100%">
              {{ currentLang === 'zh-CN' ? '确认' : 'Confirm' }}
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <div v-else-if="shareContent" class="content-container">
      <div class="header">
        <h2>{{ shareContent.title || (currentLang === 'zh-CN' ? '分享内容' : 'Shared Content') }}</h2>
        <p class="share-meta" v-if="shareContent.expiresAt">
          {{ currentLang === 'zh-CN' ? '有效期至' : 'Expires at' }}: {{ formatDateTime(shareContent.expiresAt) }}
        </p>
      </div>

      <div v-if="shareContent.type === 'photo'" class="photo-container">
        <el-image
          :src="getPhotoUrl(shareContent.content?.thumbnailPath || shareContent.content?.filePath)"
          fit="contain"
          class="photo-image"
          :preview-src-list="[getPhotoUrl(shareContent.content?.filePath)]"
        >
          <template #error>
            <div class="image-error">
              <el-icon :size="60"><Picture /></el-icon>
            </div>
          </template>
        </el-image>
        <div class="photo-info">
          <h3>{{ shareContent.content?.title || (currentLang === 'zh-CN' ? '未命名照片' : 'Untitled') }}</h3>
          <p v-if="shareContent.content?.description">{{ shareContent.content?.description }}</p>
        </div>
      </div>

      <div v-else-if="shareContent.type === 'album'" class="album-container">
        <div class="album-info" v-if="shareContent.album">
          <h3>{{ shareContent.album.name }}</h3>
          <p v-if="shareContent.album.description">{{ shareContent.album.description }}</p>
          <p class="photo-count">{{ currentLang === 'zh-CN' ? `${shareContent.photos?.length || 0} 张照片` : `${shareContent.photos?.length || 0} photos` }}</p>
        </div>
        <div class="photo-grid">
          <div
            v-for="photo in shareContent.photos"
            :key="photo.id"
            class="photo-card"
          >
            <el-image
              :src="getPhotoUrl(photo.thumbnailPath)"
              fit="cover"
              class="photo-image"
              :preview-src-list="[getPhotoUrl(photo.filePath)]"
            >
              <template #error>
                <div class="image-error">
                  <el-icon :size="30"><Picture /></el-icon>
                </div>
              </template>
            </el-image>
            <div class="photo-title">{{ photo.title || (currentLang === 'zh-CN' ? '未命名' : 'Untitled') }}</div>
          </div>
        </div>
      </div>

      <el-empty v-else :description="currentLang === 'zh-CN' ? '分享内容不存在' : 'Share content not found'" />
    </div>

    <el-empty v-else :description="currentLang === 'zh-CN' ? '分享不存在或已过期' : 'Share not found or expired'" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { useShareStore } from '@/stores/share'
import type { ShareContent } from '@/types/share'

const route = useRoute()
const { locale } = useI18n()
const shareStore = useShareStore()

const currentLang = computed(() => locale.value)

const API_BASE = 'http://localhost:8080/api'

const loading = ref(true)
const needPassword = ref(false)
const password = ref('')
const shareContent = ref<ShareContent | null>(null)

const getPhotoUrl = (path: string | undefined) => {
  if (!path) return ''
  if (path.startsWith('http')) return path
  return `${API_BASE}${path}`
}

const fetchShare = async () => {
  const code = route.params.code as string
  loading.value = true
  needPassword.value = false

  try {
    shareContent.value = await shareStore.getShareContent(code)
  } catch (error: any) {
    if (error.response?.data?.message?.includes('密码') || error.message?.includes('密码')) {
      needPassword.value = true
    } else {
      ElMessage.error(error.response?.data?.message || error.message || (currentLang.value === 'zh-CN' ? '获取分享内容失败' : 'Failed to load share'))
    }
  } finally {
    loading.value = false
  }
}

const handleVerify = async () => {
  if (!password.value) {
    ElMessage.warning(currentLang.value === 'zh-CN' ? '请输入访问密码' : 'Please enter password')
    return
  }

  const code = route.params.code as string
  loading.value = true

  try {
    shareContent.value = await shareStore.verifyShare(code, password.value)
    needPassword.value = false
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || (currentLang.value === 'zh-CN' ? '密码错误' : 'Incorrect password'))
  } finally {
    loading.value = false
  }
}

const formatDateTime = (date: string) => {
  if (!date) return ''
  return new Date(date).toLocaleString()
}

onMounted(() => {
  fetchShare()
})
</script>

<style scoped lang="scss">
.share-view-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 20px;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;

  p {
    margin-top: 20px;
    color: #666;
  }
}

.password-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;

  .el-card {
    width: 400px;
    text-align: center;

    h2 {
      margin-bottom: 20px;
      color: #333;
    }
  }
}

.content-container {
  max-width: 1200px;
  margin: 0 auto;
}

.header {
  margin-bottom: 20px;
  text-align: center;

  h2 {
    color: #333;
    margin-bottom: 8px;
  }
  
  .share-meta {
    color: #909399;
    font-size: 14px;
  }
}

.photo-container {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  text-align: center;

  .photo-image {
    max-width: 100%;
    max-height: 70vh;
  }

  .photo-info {
    margin-top: 20px;
    text-align: left;

    h3 {
      margin: 0 0 10px;
      color: #333;
    }

    p {
      color: #666;
      margin: 0;
    }
  }
}

.album-container {
  .album-info {
    background: #fff;
    border-radius: 8px;
    padding: 20px;
    margin-bottom: 20px;
    
    h3 {
      margin: 0 0 10px;
      color: #333;
    }
    
    p {
      color: #666;
      margin: 0 0 8px;
    }
    
    .photo-count {
      color: #909399;
      font-size: 14px;
    }
  }

  .photo-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 16px;
  }

  .photo-card {
    background: #fff;
    border-radius: 8px;
    overflow: hidden;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    cursor: pointer;
    transition: transform 0.2s;
    
    &:hover {
      transform: translateY(-4px);
    }
  }

  .photo-image {
    width: 100%;
    height: 200px;
  }
  
  .image-error {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #f5f5f5;
    color: #999;
  }

  .photo-title {
    padding: 10px;
    font-size: 14px;
    color: #333;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}
</style>
