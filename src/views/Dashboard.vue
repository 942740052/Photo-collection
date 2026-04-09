<template>
  <div class="page-container dashboard">
    <div class="welcome-section">
      <div class="welcome-text">
        <h2>欢迎回来，{{ userStore.currentUser?.display_name || '用户' }}</h2>
        <p>{{ todayStr }}</p>
      </div>
      <el-button type="primary" :icon="Upload" @click="$router.push('/upload')">
        上传照片
      </el-button>
    </div>

    <el-row :gutter="16" class="stats-row">
      <el-col :xs="12" :sm="6">
        <el-card shadow="hover" class="stat-card">
          <el-icon :size="32" color="#409eff"><Picture /></el-icon>
          <div class="stat-value">{{ stats.active }}</div>
          <div class="stat-label">照片总数</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="hover" class="stat-card">
          <el-icon :size="32" color="#e6a23c"><Folder /></el-icon>
          <div class="stat-value">{{ stats.albums }}</div>
          <div class="stat-label">相册</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="hover" class="stat-card">
          <el-icon :size="32" color="#f56c6c"><Star /></el-icon>
          <div class="stat-value">{{ stats.favorites }}</div>
          <div class="stat-label">收藏</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="hover" class="stat-card">
          <el-icon :size="32" color="#67c23a"><Coin /></el-icon>
          <div class="stat-value">{{ storageUsed }}</div>
          <div class="stat-label">已用存储</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="content-row">
      <el-col :xs="24" :lg="16">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>最近上传</span>
              <el-link type="primary" @click="$router.push('/photos')">查看全部</el-link>
            </div>
          </template>
          <div class="photo-grid" v-if="recentPhotos.length > 0">
            <div
              v-for="photo in recentPhotos"
              :key="photo.id"
              class="photo-item"
              @click="$router.push(`/photos/${photo.id}`)"
            >
              <img :src="photo.thumbnail_medium" :alt="photo.file_name" loading="lazy" />
              <div class="photo-overlay">
                <span class="photo-name">{{ photo.file_name }}</span>
              </div>
            </div>
          </div>
          <div v-else class="empty-state">
            <el-icon :size="48"><Picture /></el-icon>
            <p>还没有照片，快来上传吧！</p>
            <el-button type="primary" @click="$router.push('/upload')">上传照片</el-button>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="8">
        <el-card shadow="hover" class="memories-card">
          <template #header>
            <div class="card-header">
              <span><el-icon><Clock /></el-icon> 回忆</span>
            </div>
          </template>
          <div v-if="memoryPhotos.length > 0" class="memories-list">
            <div
              v-for="photo in memoryPhotos"
              :key="photo.id"
              class="memory-item"
              @click="$router.push(`/photos/${photo.id}`)"
            >
              <img :src="photo.thumbnail_small" :alt="photo.file_name" loading="lazy" />
              <div class="memory-info">
                <div class="memory-date">{{ formatDate(photo.date_taken) }}</div>
                <div class="memory-location">{{ photo.location_name || '未知地点' }}</div>
              </div>
            </div>
          </div>
          <div v-else class="empty-state" style="padding: 30px">
            <el-icon :size="36"><Clock /></el-icon>
            <p>暂无回忆</p>
          </div>
        </el-card>

        <el-card shadow="hover" style="margin-top: 16px">
          <template #header>
            <div class="card-header">
              <span>存储使用</span>
            </div>
          </template>
          <div class="storage-info">
            <el-progress
              :percentage="storagePercentage"
              :color="storageColor"
              :stroke-width="12"
            />
            <div class="storage-detail">
              <span>已用 {{ storageUsed }}</span>
              <span>共 {{ storageTotal }}</span>
            </div>
          </div>
        </el-card>

        <el-card shadow="hover" style="margin-top: 16px">
          <template #header>
            <div class="card-header">
              <span>快捷入口</span>
            </div>
          </template>
          <div class="quick-links">
            <el-button @click="$router.push('/photos')">
              <el-icon><Picture /></el-icon>浏览照片
            </el-button>
            <el-button @click="$router.push('/albums')">
              <el-icon><Folder /></el-icon>我的相册
            </el-button>
            <el-button @click="$router.push('/favorites')">
              <el-icon><Star /></el-icon>收藏夹
            </el-button>
            <el-button @click="$router.push('/trash')">
              <el-icon><Delete /></el-icon>回收站
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useUserStore } from '../stores/user'
import { usePhotoStore } from '../stores/photo'
import { Upload } from '@element-plus/icons-vue'
import dayjs from 'dayjs'

const userStore = useUserStore()
const photoStore = usePhotoStore()

const todayStr = dayjs().format('YYYY年MM月DD日 dddd')

const stats = computed(() => photoStore.photoStats)

const recentPhotos = computed(() => {
  return photoStore.activePhotos
    .slice(0, 12)
    .sort((a, b) => dayjs(b.date_uploaded).unix() - dayjs(a.date_uploaded).unix())
})

const memoryPhotos = computed(() => {
  const today = dayjs()
  return photoStore.activePhotos.filter(p => {
    const taken = dayjs(p.date_taken)
    return taken.month() === today.month() && taken.date() === today.date() && taken.year() !== today.year()
  }).slice(0, 5)
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

function formatSize(bytes) {
  if (bytes === 0) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(1024))
  return (bytes / Math.pow(1024, i)).toFixed(1) + ' ' + units[i]
}

function formatDate(date) {
  return dayjs(date).format('YYYY年MM月DD日')
}
</script>

<style scoped>
.dashboard {
  max-width: 1400px;
  margin: 0 auto;
}

.welcome-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.welcome-text h2 {
  margin: 0 0 4px;
  font-size: 24px;
  color: #303133;
}

.welcome-text p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.stats-row {
  margin-bottom: 20px;
}

.stats-row .el-col {
  margin-bottom: 12px;
}

.stat-card {
  text-align: center;
  padding: 8px;
}

.stat-card .stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  margin: 8px 0 4px;
}

.stat-card .stat-label {
  font-size: 13px;
  color: #909399;
}

.content-row .el-col {
  margin-bottom: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}

.card-header .el-icon {
  margin-right: 4px;
  vertical-align: middle;
}

.memories-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.memory-item {
  display: flex;
  gap: 12px;
  cursor: pointer;
  padding: 8px;
  border-radius: 8px;
  transition: background 0.2s;
}

.memory-item:hover {
  background: #f5f7fa;
}

.memory-item img {
  width: 64px;
  height: 64px;
  object-fit: cover;
  border-radius: 8px;
  flex-shrink: 0;
}

.memory-info {
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.memory-date {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.memory-location {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.storage-info {
  padding: 4px 0;
}

.storage-detail {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

.quick-links {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}

.quick-links .el-button {
  width: 100%;
}
</style>
