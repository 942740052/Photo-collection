<template>
  <div class="page-container trash-page">
    <div class="page-header">
      <h2>回收站</h2>
      <div class="header-actions">
        <span class="trash-info">{{ photoStore.deletedPhotos.length }} 张照片 · {{ formatSize(trashSize) }}</span>
        <el-button
          v-if="photoStore.deletedPhotos.length > 0"
          type="danger"
          @click="emptyTrash"
        >
          清空回收站
        </el-button>
      </div>
    </div>

    <el-alert
      v-if="photoStore.deletedPhotos.length > 0"
      title="回收站中的照片将在30天后自动永久删除"
      type="warning"
      :closable="false"
      show-icon
      style="margin-bottom: 16px"
    />

    <div v-if="selectedPhotos.length > 0" class="batch-bar">
      <span>已选择 {{ selectedPhotos.length }} 张照片</span>
      <el-button size="small" type="primary" @click="restoreSelected">恢复</el-button>
      <el-button size="small" type="danger" @click="permanentDeleteSelected">永久删除</el-button>
      <el-button size="small" @click="selectedPhotos = []">取消选择</el-button>
    </div>

    <div class="photo-grid medium" v-if="photoStore.deletedPhotos.length > 0">
      <div
        v-for="photo in photoStore.deletedPhotos"
        :key="photo.id"
        class="photo-item"
        :class="{ selected: selectedPhotos.includes(photo.id) }"
        @click="toggleSelect(photo.id)"
      >
        <div class="photo-select">
          <el-checkbox :model-value="selectedPhotos.includes(photo.id)" />
        </div>
        <img :src="photo.thumbnail_medium" :alt="photo.file_name" loading="lazy" />
        <div class="photo-overlay">
          <span class="photo-name">{{ photo.file_name }}</span>
          <span class="photo-date">删除于 {{ formatDate(photo.deleted_at) }}</span>
        </div>
        <div class="photo-actions" @click.stop>
          <el-button size="small" type="primary" @click="restorePhoto(photo.id)">恢复</el-button>
          <el-button size="small" type="danger" @click="permanentDelete(photo.id)">永久删除</el-button>
        </div>
      </div>
    </div>

    <div v-else class="empty-state">
      <el-icon :size="64"><Delete /></el-icon>
      <p>回收站是空的</p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { usePhotoStore } from '../stores/photo'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'

const photoStore = usePhotoStore()
const selectedPhotos = ref([])

const trashSize = computed(() =>
  photoStore.deletedPhotos.reduce((sum, p) => sum + p.file_size, 0)
)

const toggleSelect = (id) => {
  const idx = selectedPhotos.value.indexOf(id)
  if (idx === -1) selectedPhotos.value.push(id)
  else selectedPhotos.value.splice(idx, 1)
}

const restorePhoto = (id) => {
  photoStore.restorePhotos([id])
  ElMessage.success('照片已恢复')
}

const permanentDelete = async (id) => {
  try {
    await ElMessageBox.confirm('永久删除后无法恢复，确定要删除吗？', '确认永久删除', { type: 'warning' })
    photoStore.permanentDeletePhotos([id])
    ElMessage.success('照片已永久删除')
  } catch {}
}

const restoreSelected = () => {
  photoStore.restorePhotos(selectedPhotos.value)
  selectedPhotos.value = []
  ElMessage.success('照片已恢复')
}

const permanentDeleteSelected = async () => {
  try {
    await ElMessageBox.confirm(`永久删除 ${selectedPhotos.value.length} 张照片后无法恢复，确定吗？`, '确认永久删除', { type: 'warning' })
    photoStore.permanentDeletePhotos(selectedPhotos.value)
    selectedPhotos.value = []
    ElMessage.success('照片已永久删除')
  } catch {}
}

const emptyTrash = async () => {
  try {
    await ElMessageBox.confirm('清空回收站后所有照片将无法恢复，确定吗？', '确认清空', { type: 'warning' })
    photoStore.emptyTrash()
    ElMessage.success('回收站已清空')
  } catch {}
}

function formatSize(bytes) {
  if (!bytes) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(1024))
  return (bytes / Math.pow(1024, i)).toFixed(1) + ' ' + units[i]
}

function formatDate(date) {
  return dayjs(date).format('YYYY/MM/DD')
}
</script>

<style scoped>
.trash-page {
  max-width: 1600px;
  margin: 0 auto;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.trash-info {
  font-size: 14px;
  color: #909399;
}

.photo-item.selected {
  outline: 3px solid #409eff;
  outline-offset: -3px;
  border-radius: 8px;
}

.photo-actions {
  position: absolute;
  bottom: 8px;
  right: 8px;
  display: flex;
  gap: 4px;
  opacity: 0;
  transition: opacity 0.2s;
}

.photo-item:hover .photo-actions {
  opacity: 1;
}

.batch-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: #ecf5ff;
  border-radius: 8px;
  margin-bottom: 16px;
  font-size: 14px;
  color: #409eff;
}
</style>
