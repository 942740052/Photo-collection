<template>
  <div class="page-container upload-page">
    <div class="page-header">
      <h2>上传照片</h2>
    </div>

    <div
      class="upload-zone"
      :class="{ dragover: isDragover }"
      @dragover.prevent="isDragover = true"
      @dragleave="isDragover = false"
      @drop.prevent="handleDrop"
      @click="triggerFileInput"
    >
      <el-icon :size="48"><Upload /></el-icon>
      <h3>拖拽文件到此处上传</h3>
      <p>或点击选择文件</p>
      <p style="font-size: 12px; color: #c0c4cc">
        支持 JPEG、PNG、WebP、HEIC、BMP、GIF、TIFF 格式，单文件最大 100MB
      </p>
      <input
        ref="fileInput"
        type="file"
        multiple
        accept="image/*"
        style="display: none"
        @change="handleFileSelect"
      />
    </div>

    <div v-if="uploadQueue.length > 0" class="upload-queue">
      <div class="queue-header">
        <h3>上传队列 ({{ uploadQueue.length }} 个文件)</h3>
        <div class="queue-actions">
          <el-button size="small" @click="clearCompleted">清除已完成</el-button>
          <el-button size="small" type="danger" @click="clearQueue">清空队列</el-button>
        </div>
      </div>

      <div class="queue-stats">
        <el-progress :percentage="overallProgress" :stroke-width="8" />
        <span class="stats-text">{{ completedCount }} / {{ uploadQueue.length }} 已完成</span>
      </div>

      <div class="queue-list">
        <div v-for="item in uploadQueue" :key="item.id" class="queue-item">
          <div class="item-preview">
            <img v-if="item.preview" :src="item.preview" alt="" />
            <el-icon v-else :size="24"><Picture /></el-icon>
          </div>
          <div class="item-info">
            <div class="item-name">{{ item.name }}</div>
            <div class="item-size">{{ formatSize(item.size) }}</div>
            <el-progress
              v-if="item.status === 'uploading'"
              :percentage="item.progress"
              :stroke-width="4"
              :show-text="false"
            />
            <el-tag v-else-if="item.status === 'done'" type="success" size="small">已完成</el-tag>
            <el-tag v-else-if="item.status === 'error'" type="danger" size="small">失败</el-tag>
            <el-tag v-else-if="item.status === 'waiting'" type="info" size="small">等待中</el-tag>
          </div>
          <div class="item-actions">
            <el-button
              v-if="item.status === 'error'"
              size="small"
              type="primary"
              @click="retryUpload(item)"
            >
              重试
            </el-button>
            <el-button
              size="small"
              :icon="Close"
              circle
              @click="removeFromQueue(item.id)"
            />
          </div>
        </div>
      </div>
    </div>

    <el-card style="margin-top: 20px">
      <template #header>
        <span>上传设置</span>
      </template>
      <el-form label-width="100px">
        <el-form-item label="目标相册">
          <el-select v-model="uploadSettings.album" placeholder="选择相册（可选）" clearable style="width: 300px">
            <el-option v-for="album in photoStore.albums" :key="album.id" :label="album.name" :value="album.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="自动标签">
          <el-switch v-model="uploadSettings.autoTag" />
          <span style="margin-left: 8px; font-size: 13px; color: #909399">AI 自动识别并添加标签</span>
        </el-form-item>
        <el-form-item label="去重检测">
          <el-switch v-model="uploadSettings.dedup" />
          <span style="margin-left: 8px; font-size: 13px; color: #909399">自动检测重复文件</span>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { usePhotoStore } from '../stores/photo'
import { ElMessage } from 'element-plus'
import { Close } from '@element-plus/icons-vue'
import dayjs from 'dayjs'

const photoStore = usePhotoStore()

const fileInput = ref(null)
const isDragover = ref(false)
const uploadQueue = ref([])

const uploadSettings = reactive({
  album: '',
  autoTag: true,
  dedup: true
})

const overallProgress = computed(() => {
  if (uploadQueue.value.length === 0) return 0
  const total = uploadQueue.value.reduce((sum, item) => sum + item.progress, 0)
  return Math.round(total / uploadQueue.value.length)
})

const completedCount = computed(() => uploadQueue.value.filter(i => i.status === 'done').length)

const triggerFileInput = () => {
  fileInput.value?.click()
}

const handleFileSelect = (e) => {
  addFiles(e.target.files)
  e.target.value = ''
}

const handleDrop = (e) => {
  isDragover.value = false
  addFiles(e.dataTransfer.files)
}

const addFiles = (files) => {
  if (!files || files.length === 0) return

  Array.from(files).forEach(file => {
    if (!file.type.startsWith('image/')) {
      ElMessage.warning(`不支持的文件格式: ${file.name}`)
      return
    }

    const item = {
      id: Date.now() + Math.random(),
      name: file.name,
      size: file.size,
      file,
      preview: null,
      progress: 0,
      status: 'waiting'
    }

    const reader = new FileReader()
    reader.onload = (e) => {
      item.preview = e.target.result
    }
    reader.readAsDataURL(file)

    uploadQueue.value.push(item)
  })

  startUpload()
}

const startUpload = () => {
  const waiting = uploadQueue.value.filter(i => i.status === 'waiting')
  waiting.forEach(item => simulateUpload(item))
}

const simulateUpload = (item) => {
  item.status = 'uploading'
  item.progress = 0

  const interval = setInterval(() => {
    item.progress += Math.random() * 15 + 5
    if (item.progress >= 100) {
      item.progress = 100
      item.status = 'done'
      clearInterval(interval)

      photoStore.addPhoto({
        id: `photo-upload-${item.id}`,
        user_id: 'user-1',
        file_name: item.name,
        file_size: item.size,
        file_hash: `sha256_${item.id}`,
        mime_type: item.file.type,
        width: 3000,
        height: 2000,
        orientation: 1,
        date_taken: dayjs().format('YYYY-MM-DD HH:mm:ss'),
        date_uploaded: dayjs().format('YYYY-MM-DD HH:mm:ss'),
        description: '',
        rating: 0,
        is_favorite: false,
        latitude: null,
        longitude: null,
        altitude: null,
        location_name: null,
        camera_make: '',
        camera_model: '',
        lens_model: '',
        focal_length: 0,
        aperture: '0',
        shutter_speed: '',
        iso: 0,
        thumbnail_small: item.preview || 'https://picsum.photos/seed/new/150/150',
        thumbnail_medium: item.preview || 'https://picsum.photos/seed/new/400/400',
        thumbnail_large: item.preview || 'https://picsum.photos/seed/new/1200/1200',
        is_archived: false,
        is_deleted: false,
        deleted_at: null,
        tags: [],
        created_at: dayjs().format('YYYY-MM-DD HH:mm:ss'),
        updated_at: dayjs().format('YYYY-MM-DD HH:mm:ss')
      })
    }
  }, 200)
}

const retryUpload = (item) => {
  item.status = 'waiting'
  item.progress = 0
  simulateUpload(item)
}

const removeFromQueue = (id) => {
  uploadQueue.value = uploadQueue.value.filter(i => i.id !== id)
}

const clearCompleted = () => {
  uploadQueue.value = uploadQueue.value.filter(i => i.status !== 'done')
}

const clearQueue = () => {
  uploadQueue.value = []
}

function formatSize(bytes) {
  if (!bytes) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(1024))
  return (bytes / Math.pow(1024, i)).toFixed(1) + ' ' + units[i]
}
</script>

<style scoped>
.upload-page {
  max-width: 1000px;
  margin: 0 auto;
}

.upload-zone {
  border: 2px dashed #dcdfe6;
  border-radius: 12px;
  padding: 60px 20px;
  text-align: center;
  cursor: pointer;
  transition: border-color 0.3s, background-color 0.3s;
  margin-bottom: 20px;
}

.upload-zone:hover,
.upload-zone.dragover {
  border-color: #409eff;
  background-color: rgba(64, 158, 255, 0.05);
}

.upload-zone h3 {
  margin: 16px 0 8px;
  color: #303133;
}

.upload-queue {
  background: #fff;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  padding: 16px;
}

.queue-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.queue-header h3 {
  margin: 0;
  font-size: 16px;
}

.queue-stats {
  margin-bottom: 16px;
}

.stats-text {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  display: block;
}

.queue-list {
  max-height: 400px;
  overflow-y: auto;
}

.queue-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px;
  border-bottom: 1px solid #f0f0f0;
}

.queue-item:last-child {
  border-bottom: none;
}

.item-preview {
  width: 48px;
  height: 48px;
  border-radius: 6px;
  overflow: hidden;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.item-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.item-info {
  flex: 1;
  min-width: 0;
}

.item-name {
  font-size: 14px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-size {
  font-size: 12px;
  color: #909399;
  margin: 2px 0;
}

.item-actions {
  display: flex;
  gap: 4px;
  flex-shrink: 0;
}
</style>
