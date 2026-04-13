<template>
  <div class="trash-page">
    <div class="page-header">
      <h2>{{ t('menu.trash') }}</h2>
      <div class="header-actions">
        <el-button v-if="selectedPhotos.length > 0" type="success" @click="handleBatchRestore">
          <el-icon><RefreshRight /></el-icon>
          {{ currentLang === 'zh-CN' ? '恢复选中' : 'Restore Selected' }} ({{ selectedPhotos.length }})
        </el-button>
        <el-button type="danger" @click="handleClearAll" :disabled="photos.length === 0">
          <el-icon><Delete /></el-icon>
          {{ currentLang === 'zh-CN' ? '清空回收站' : 'Empty Trash' }}
        </el-button>
      </div>
    </div>

    <el-empty v-if="!loading && photos.length === 0" :description="currentLang === 'zh-CN' ? '回收站为空' : 'Trash is empty'" />

    <div v-else v-loading="loading" class="photo-masonry">
      <div
        v-for="photo in photos"
        :key="photo.id"
        class="photo-card"
        :class="{ 'selected': selectedPhotos.includes(photo.id) }"
        @click="toggleSelect(photo.id)"
      >
        <div class="select-checkbox" @click.stop="toggleSelect(photo.id)">
          <el-checkbox :model-value="selectedPhotos.includes(photo.id)" />
        </div>
        <el-image
          :src="getPhotoUrl(photo.thumbnailPath)"
          fit="cover"
          class="photo-image"
        >
          <template #error>
            <div class="image-error">
              <el-icon :size="40"><Picture /></el-icon>
            </div>
          </template>
        </el-image>
        <div class="photo-overlay">
          <div class="overlay-content">
            <div class="overlay-title">{{ photo.title || (currentLang === 'zh-CN' ? '未命名照片' : 'Untitled') }}</div>
            <div class="overlay-meta" v-if="photo.deletedAt">
              <span>{{ currentLang === 'zh-CN' ? '删除于' : 'Deleted' }}: {{ formatDate(photo.deletedAt) }}</span>
            </div>
            <div class="overlay-actions">
              <el-button circle size="small" type="success" @click.stop="handleRestore(photo)">
                <el-icon><RefreshRight /></el-icon>
              </el-button>
              <el-button circle size="small" type="danger" @click.stop="handlePermanentDelete(photo)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <el-pagination
      v-if="total > 0"
      v-model:current-page="pageNum"
      v-model:page-size="pageSize"
      :total="total"
      :page-sizes="[20, 40, 60, 100]"
      layout="total, sizes, prev, pager, next"
      @size-change="fetchTrash"
      @current-change="fetchTrash"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { photoApi } from '@/api/photo'
import type { Photo } from '@/types/photo'

const { t, locale } = useI18n()

const currentLang = computed(() => locale.value)

const loading = ref(false)
const photos = ref<Photo[]>([])
const selectedPhotos = ref<number[]>([])
const pageNum = ref(1)
const pageSize = ref(20)
const total = ref(0)

const API_BASE = 'http://localhost:8080/api'

const getPhotoUrl = (path: string | undefined) => {
  if (!path) return ''
  if (path.startsWith('http')) return path
  return `${API_BASE}${path}`
}

const fetchTrash = async () => {
  loading.value = true
  try {
    const result = await photoApi.getList({ 
      pageNum: pageNum.value, 
      pageSize: pageSize.value,
      isDeleted: 1 
    })
    photos.value = result.data?.records || []
    total.value = result.data?.total || 0
  } catch (error) {
    ElMessage.error(currentLang.value === 'zh-CN' ? '获取回收站数据失败' : 'Failed to load trash')
  } finally {
    loading.value = false
  }
}

const toggleSelect = (photoId: number) => {
  const index = selectedPhotos.value.indexOf(photoId)
  if (index > -1) {
    selectedPhotos.value.splice(index, 1)
  } else {
    selectedPhotos.value.push(photoId)
  }
}

const handleRestore = async (photo: Photo) => {
  try {
    await photoApi.restore(photo.id)
    ElMessage.success(currentLang.value === 'zh-CN' ? '恢复成功' : 'Restored successfully')
    await fetchTrash()
  } catch (error) {
    ElMessage.error(currentLang.value === 'zh-CN' ? '恢复失败' : 'Restore failed')
  }
}

const handleBatchRestore = async () => {
  if (selectedPhotos.value.length === 0) {
    ElMessage.warning(currentLang.value === 'zh-CN' ? '请选择要恢复的照片' : 'Please select photos to restore')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      currentLang.value === 'zh-CN' 
        ? `确定要恢复选中的 ${selectedPhotos.value.length} 张照片吗？` 
        : `Restore ${selectedPhotos.value.length} selected photos?`,
      currentLang.value === 'zh-CN' ? '提示' : 'Confirm',
      {
        confirmButtonText: currentLang.value === 'zh-CN' ? '确定' : 'Confirm',
        cancelButtonText: currentLang.value === 'zh-CN' ? '取消' : 'Cancel',
        type: 'info',
      }
    )
    await photoApi.batchRestore(selectedPhotos.value)
    ElMessage.success(currentLang.value === 'zh-CN' ? '批量恢复成功' : 'Batch restore successful')
    selectedPhotos.value = []
    await fetchTrash()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(currentLang.value === 'zh-CN' ? '批量恢复失败' : 'Batch restore failed')
    }
  }
}

const handlePermanentDelete = async (photo: Photo) => {
  try {
    await ElMessageBox.confirm(
      currentLang.value === 'zh-CN' ? '永久删除后无法恢复，确定要删除吗？' : 'This cannot be undone. Are you sure?',
      currentLang.value === 'zh-CN' ? '警告' : 'Warning',
      {
        confirmButtonText: currentLang.value === 'zh-CN' ? '确定' : 'Confirm',
        cancelButtonText: currentLang.value === 'zh-CN' ? '取消' : 'Cancel',
        type: 'warning',
      }
    )
    await photoApi.permanentDelete(photo.id)
    ElMessage.success(currentLang.value === 'zh-CN' ? '永久删除成功' : 'Permanently deleted')
    await fetchTrash()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(currentLang.value === 'zh-CN' ? '删除失败' : 'Delete failed')
    }
  }
}

const handleClearAll = async () => {
  try {
    await ElMessageBox.confirm(
      currentLang.value === 'zh-CN' ? '确定要清空回收站吗？清空后所有照片将永久删除且无法恢复！' : 'Empty trash? All photos will be permanently deleted!',
      currentLang.value === 'zh-CN' ? '警告' : 'Warning',
      {
        confirmButtonText: currentLang.value === 'zh-CN' ? '确定' : 'Confirm',
        cancelButtonText: currentLang.value === 'zh-CN' ? '取消' : 'Cancel',
        type: 'warning',
      }
    )
    await photoApi.emptyTrash()
    ElMessage.success(currentLang.value === 'zh-CN' ? '清空成功' : 'Trash emptied')
    await fetchTrash()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(currentLang.value === 'zh-CN' ? '清空失败' : 'Failed to empty trash')
    }
  }
}

const formatDate = (date: string) => {
  if (!date) return ''
  return new Date(date).toLocaleDateString()
}

onMounted(() => {
  fetchTrash()
})
</script>

<style scoped lang="scss">
.trash-page {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;

  h2 {
    margin: 0;
    color: #333;
  }
}

.header-actions {
  display: flex;
  gap: 10px;
}

.photo-masonry {
  flex: 1;
  column-count: 4;
  column-gap: 20px;
  overflow-y: auto;
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
  border-radius: 16px;
  
  @media (max-width: 1400px) {
    column-count: 3;
  }
  
  @media (max-width: 1000px) {
    column-count: 2;
    column-gap: 16px;
  }
  
  @media (max-width: 600px) {
    column-count: 2;
    column-gap: 12px;
  }
}

.photo-card {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  break-inside: avoid;
  margin-bottom: 20px;
  
  &.selected {
    box-shadow: 0 0 0 3px #67c23a, 0 12px 32px rgba(103, 194, 58, 0.3);
    transform: scale(1.02);
  }
  
  &:hover {
    transform: translateY(-12px) scale(1.03);
    box-shadow: 0 20px 40px rgba(0, 0, 0, 0.18);
    
    .photo-overlay {
      opacity: 1;
    }
    
    .select-checkbox {
      opacity: 1;
      transform: scale(1.1);
    }
    
    .photo-image {
      transform: scale(1.08);
    }
  }
}

.select-checkbox {
  position: absolute;
  top: 12px;
  left: 12px;
  z-index: 10;
  opacity: 0;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: rgba(255, 255, 255, 0.95);
  border-radius: 8px;
  padding: 6px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  backdrop-filter: blur(10px);
}

.photo-image {
  width: 100%;
  display: block;
  background: #f5f7fa;
  transition: transform 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}

.image-error {
  width: 100%;
  height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
  color: #c0c4cc;
}

.photo-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(
    to bottom,
    rgba(0, 0, 0, 0) 0%,
    rgba(0, 0, 0, 0.2) 40%,
    rgba(0, 0, 0, 0.75) 100%
  );
  display: flex;
  align-items: flex-end;
  opacity: 0;
  transition: opacity 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.overlay-content {
  width: 100%;
  padding: 20px;
  color: white;
  transform: translateY(10px);
  transition: transform 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  
  .photo-card:hover & {
    transform: translateY(0);
  }
}

.overlay-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 10px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.overlay-meta {
  font-size: 13px;
  margin-bottom: 14px;
  opacity: 0.9;
  
  span {
    display: flex;
    align-items: center;
    gap: 6px;
    text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
  }
}

.overlay-actions {
  display: flex;
  gap: 10px;
  
  .el-button {
    backdrop-filter: blur(12px);
    background: rgba(255, 255, 255, 0.15);
    border-color: rgba(255, 255, 255, 0.25);
    color: white;
    transition: all 0.3s ease;
    
    &:hover {
      background: rgba(255, 255, 255, 0.25);
      border-color: rgba(255, 255, 255, 0.4);
      transform: scale(1.1);
    }
    
    &.el-button--success {
      background: rgba(103, 194, 58, 0.85);
      border-color: rgba(103, 194, 58, 0.95);
    }
    
    &.el-button--danger {
      background: rgba(245, 108, 108, 0.85);
      border-color: rgba(245, 108, 108, 0.95);
    }
  }
}

.el-pagination {
  margin-top: 20px;
  justify-content: center;
}
</style>
