<template>
  <div class="album-detail-page">
    <div class="album-cover-section" v-if="album">
      <div class="cover-preview" @click="showCoverDialog = true">
        <el-image 
          v-if="currentCoverPhoto" 
          :src="getPhotoUrl(currentCoverPhoto.filePath)" 
          fit="cover"
          class="cover-image"
        >
          <template #error>
            <div class="cover-placeholder">
              <el-icon :size="60"><Picture /></el-icon>
              <span>{{ currentLang === 'zh-CN' ? '点击设置封面' : 'Click to set cover' }}</span>
            </div>
          </template>
        </el-image>
        <div v-else class="cover-placeholder">
          <el-icon :size="60"><Picture /></el-icon>
          <span>{{ currentLang === 'zh-CN' ? '点击设置封面' : 'Click to set cover' }}</span>
        </div>
        <div class="cover-overlay">
          <el-icon :size="24"><Edit /></el-icon>
          <span>{{ currentLang === 'zh-CN' ? '更换封面' : 'Change Cover' }}</span>
        </div>
      </div>
      <div class="album-header-info">
        <h1>{{ album.name }}</h1>
        <p class="album-description" v-if="album.description">{{ album.description }}</p>
        <div class="album-stats">
          <span><el-icon><Picture /></el-icon> {{ photos.length }} {{ currentLang === 'zh-CN' ? '张照片' : 'photos' }}</span>
          <span v-if="album.createdAt"><el-icon><Calendar /></el-icon> {{ formatDate(album.createdAt) }}</span>
        </div>
      </div>
    </div>

    <div class="page-header">
      <div class="header-left">
        <el-button @click="handleBack">
          <el-icon><ArrowLeft /></el-icon>
          {{ currentLang === 'zh-CN' ? '返回' : 'Back' }}
        </el-button>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="showUploadDialog = true">
          <el-icon><Upload /></el-icon>
          {{ currentLang === 'zh-CN' ? '添加照片' : 'Add Photos' }}
        </el-button>
        <el-button @click="showEditDialog = true" v-if="album">
          <el-icon><Edit /></el-icon>
          {{ currentLang === 'zh-CN' ? '编辑相册' : 'Edit' }}
        </el-button>
        <el-button type="primary" @click="handleShare" v-if="album">
          <el-icon><Share /></el-icon>
          {{ currentLang === 'zh-CN' ? '分享相册' : 'Share' }}
        </el-button>
      </div>
    </div>

    <el-empty v-if="!loading && photos.length === 0" :description="currentLang === 'zh-CN' ? '相册暂无照片' : 'No photos in this album'">
      <el-button type="primary" @click="showUploadDialog = true">
        <el-icon><Upload /></el-icon>
        {{ currentLang === 'zh-CN' ? '添加照片' : 'Add Photos' }}
      </el-button>
    </el-empty>

    <div v-else v-loading="loading" class="photo-grid">
      <div
        v-for="photo in photos"
        :key="photo.id"
        class="photo-card"
        :class="{ 'selected': selectedPhotos.includes(photo.id) }"
        @click="handleViewPhoto(photo)"
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
        <div class="photo-info">
          <div class="photo-title">{{ photo.title || (currentLang === 'zh-CN' ? '未命名照片' : 'Untitled') }}</div>
          <div class="photo-actions">
            <el-tooltip :content="currentLang === 'zh-CN' ? '设为封面' : 'Set as Cover'" placement="top">
              <el-icon :class="{ 'is-cover': album?.coverPhotoId === photo.id }" @click.stop="handleSetCover(photo)">
                <Picture />
              </el-icon>
            </el-tooltip>
            <el-icon :class="{ 'is-favorite': photo.isFavorite === 1 }" @click.stop="handleToggleFavorite(photo)">
              <Star />
            </el-icon>
            <el-tooltip :content="currentLang === 'zh-CN' ? '从相册移除' : 'Remove from Album'" placement="top">
              <el-icon @click.stop="handleRemoveFromAlbum(photo)">
                <Close />
              </el-icon>
            </el-tooltip>
            <el-tooltip :content="currentLang === 'zh-CN' ? '删除照片' : 'Delete Photo'" placement="top">
              <el-icon class="delete-icon" @click.stop="handleDeletePhoto(photo)">
                <Delete />
              </el-icon>
            </el-tooltip>
          </div>
        </div>
      </div>
    </div>

    <div class="batch-actions" v-if="selectedPhotos.length > 0">
      <el-button size="small" @click="selectedPhotos = []">{{ currentLang === 'zh-CN' ? '取消选择' : 'Cancel' }}</el-button>
      <el-button size="small" type="warning" @click="handleBatchRemove">
        {{ currentLang === 'zh-CN' ? '从相册移除' : 'Remove from Album' }} ({{ selectedPhotos.length }})
      </el-button>
      <el-button size="small" type="danger" @click="handleBatchDelete">
        {{ currentLang === 'zh-CN' ? '删除选中' : 'Delete Selected' }} ({{ selectedPhotos.length }})
      </el-button>
    </div>

    <el-dialog v-model="showUploadDialog" :title="currentLang === 'zh-CN' ? '添加照片' : 'Add Photos'" width="500px">
      <el-upload
        ref="uploadRef"
        v-model:file-list="uploadFileList"
        action="#"
        :auto-upload="false"
        accept="image/*"
        list-type="picture-card"
        multiple
      >
        <el-icon><Plus /></el-icon>
      </el-upload>
      <template #footer>
        <el-button @click="showUploadDialog = false">{{ currentLang === 'zh-CN' ? '取消' : 'Cancel' }}</el-button>
        <el-button type="primary" :loading="uploadLoading" @click="handleUpload">{{ currentLang === 'zh-CN' ? '上传' : 'Upload' }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showEditDialog" :title="currentLang === 'zh-CN' ? '编辑相册' : 'Edit Album'" width="500px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item :label="currentLang === 'zh-CN' ? '名称' : 'Name'">
          <el-input v-model="editForm.name" />
        </el-form-item>
        <el-form-item :label="currentLang === 'zh-CN' ? '描述' : 'Description'">
          <el-input v-model="editForm.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">{{ currentLang === 'zh-CN' ? '取消' : 'Cancel' }}</el-button>
        <el-button type="primary" :loading="editLoading" @click="handleEdit">{{ currentLang === 'zh-CN' ? '保存' : 'Save' }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showCoverDialog" :title="currentLang === 'zh-CN' ? '选择封面' : 'Select Cover'" width="800px">
      <div class="cover-selection-info">
        <el-icon><InfoFilled /></el-icon>
        <span>{{ currentLang === 'zh-CN' ? '点击照片设置为相册封面' : 'Click a photo to set as album cover' }}</span>
      </div>
      <div class="cover-selection-grid">
        <div 
          v-for="photo in photos" 
          :key="photo.id" 
          class="cover-option"
          :class="{ 'is-selected': album?.coverPhotoId === photo.id }"
          @click="handleSelectCover(photo)"
        >
          <el-image :src="getPhotoUrl(photo.thumbnailPath)" fit="cover" class="cover-option-image">
            <template #error>
              <div class="image-error"><el-icon :size="30"><Picture /></el-icon></div>
            </template>
          </el-image>
          <div class="cover-option-check" v-if="album?.coverPhotoId === photo.id">
            <el-icon><Check /></el-icon>
          </div>
          <div class="cover-option-title">{{ photo.title || (currentLang === 'zh-CN' ? '未命名' : 'Untitled') }}</div>
        </div>
      </div>
      <template #footer>
        <el-button @click="showCoverDialog = false">{{ currentLang === 'zh-CN' ? '关闭' : 'Close' }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="previewDialogVisible" :title="currentPhoto?.title || (currentLang === 'zh-CN' ? '照片详情' : 'Photo Detail')" width="800px">
      <div v-if="currentPhoto" class="photo-detail">
        <el-image :src="getPhotoUrl(currentPhoto.filePath)" fit="contain" style="width: 100%; max-height: 500px;">
          <template #error>
            <div class="image-error"><el-icon :size="60"><Picture /></el-icon></div>
          </template>
        </el-image>
        <div class="detail-info">
          <p v-if="currentPhoto.description"><strong>{{ currentLang === 'zh-CN' ? '描述' : 'Description' }}:</strong> {{ currentPhoto.description }}</p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { UploadFile } from 'element-plus'
import { useAlbumStore } from '@/stores/album'
import { usePhotoStore } from '@/stores/photo'
import { photoApi } from '@/api/photo'
import type { Album } from '@/types/album'
import type { Photo } from '@/types/photo'

const route = useRoute()
const router = useRouter()
const { locale } = useI18n()
const albumStore = useAlbumStore()
const photoStore = usePhotoStore()

const currentLang = computed(() => locale.value)

const API_BASE = 'http://localhost:8080/api'

const loading = ref(false)
const album = ref<Album | null>(null)
const photos = ref<Photo[]>([])
const selectedPhotos = ref<number[]>([])

const showUploadDialog = ref(false)
const uploadLoading = ref(false)
const uploadFileList = ref<UploadFile[]>([])

const showEditDialog = ref(false)
const editLoading = ref(false)
const showCoverDialog = ref(false)
const editForm = reactive({
  name: '',
  description: ''
})

const previewDialogVisible = ref(false)
const currentPhoto = ref<Photo | null>(null)

const currentCoverPhoto = computed(() => {
  if (!album.value?.coverPhotoId) return null
  return photos.value.find(p => p.id === album.value!.coverPhotoId)
})

const getPhotoUrl = (path: string | undefined) => {
  if (!path) return ''
  if (path.startsWith('http')) return path
  return `${API_BASE}${path}`
}

const fetchAlbum = async () => {
  const id = Number(route.params.id)
  try {
    const result = await albumStore.fetchAlbumById(id)
    album.value = result
    if (result) {
      editForm.name = result.name
      editForm.description = result.description || ''
    }
  } catch (error) {
    ElMessage.error(currentLang.value === 'zh-CN' ? '获取相册失败' : 'Failed to load album')
  }
}

const fetchPhotos = async () => {
  const id = Number(route.params.id)
  loading.value = true
  try {
    const result = await photoStore.fetchPhotos({ pageNum: 1, pageSize: 100, albumId: id })
    photos.value = result.records
  } catch (error) {
    ElMessage.error(currentLang.value === 'zh-CN' ? '获取照片失败' : 'Failed to load photos')
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

const handleViewPhoto = (photo: Photo) => {
  currentPhoto.value = photo
  previewDialogVisible.value = true
}

const handleToggleFavorite = async (photo: Photo) => {
  try {
    await photoStore.toggleFavorite(photo.id, photo.isFavorite === 1)
    photo.isFavorite = photo.isFavorite === 1 ? 0 : 1
    ElMessage.success(photo.isFavorite === 1 
      ? (currentLang.value === 'zh-CN' ? '已收藏' : 'Added to favorites')
      : (currentLang.value === 'zh-CN' ? '已取消收藏' : 'Removed from favorites'))
  } catch (error) {
    ElMessage.error(currentLang.value === 'zh-CN' ? '操作失败' : 'Operation failed')
  }
}

const handleSetCover = async (photo: Photo) => {
  try {
    await albumStore.updateAlbum(album.value!.id, { coverPhotoId: photo.id })
    album.value!.coverPhotoId = photo.id
    ElMessage.success(currentLang.value === 'zh-CN' ? '封面设置成功' : 'Cover set successfully')
  } catch (error) {
    ElMessage.error(currentLang.value === 'zh-CN' ? '设置封面失败' : 'Failed to set cover')
  }
}

const handleSelectCover = async (photo: Photo) => {
  await handleSetCover(photo)
  showCoverDialog.value = false
}

const formatDate = (date: string) => {
  if (!date) return ''
  const d = new Date(date)
  return d.toLocaleDateString(currentLang.value === 'zh-CN' ? 'zh-CN' : 'en-US', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

const handleRemoveFromAlbum = async (photo: Photo) => {
  try {
    await ElMessageBox.confirm(
      currentLang.value === 'zh-CN' ? '确定要从相册中移除这张照片吗？照片仍会保留在照片管理中' : 'Remove this photo from album? The photo will still be in Photos.',
      currentLang.value === 'zh-CN' ? '提示' : 'Confirm',
      { type: 'warning' }
    )
    await photoApi.update(photo.id, {
      id: photo.id,
      albumId: null
    })
    ElMessage.success(currentLang.value === 'zh-CN' ? '已从相册移除，照片仍保留在照片管理中' : 'Removed from album, photo is still in Photos')
    await fetchPhotos()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(currentLang.value === 'zh-CN' ? '移除失败' : 'Failed to remove')
    }
  }
}

const handleDeletePhoto = async (photo: Photo) => {
  try {
    await ElMessageBox.confirm(
      currentLang.value === 'zh-CN' ? '确定要删除这张照片吗？删除后可在回收站找回' : 'Delete this photo? You can restore it from trash.',
      currentLang.value === 'zh-CN' ? '提示' : 'Confirm',
      { type: 'warning' }
    )
    await photoApi.delete(photo.id)
    ElMessage.success(currentLang.value === 'zh-CN' ? '删除成功，已移入回收站' : 'Deleted successfully')
    await fetchPhotos()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(currentLang.value === 'zh-CN' ? '删除失败' : 'Failed to delete')
    }
  }
}

const handleBatchRemove = async () => {
  try {
    await ElMessageBox.confirm(
      currentLang.value === 'zh-CN' 
        ? `确定要从相册中移除选中的 ${selectedPhotos.value.length} 张照片吗？照片仍会保留在照片管理中` 
        : `Remove ${selectedPhotos.value.length} selected photos from album? Photos will still be in Photos.`,
      currentLang.value === 'zh-CN' ? '提示' : 'Confirm',
      { type: 'warning' }
    )
    await photoApi.batchUpdate({
      photoIds: selectedPhotos.value,
      albumId: null
    })
    ElMessage.success(currentLang.value === 'zh-CN' ? '批量移除成功，照片仍保留在照片管理中' : 'Batch remove successful, photos are still in Photos')
    selectedPhotos.value = []
    await fetchPhotos()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(currentLang.value === 'zh-CN' ? '移除失败' : 'Failed to remove')
    }
  }
}

const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(
      currentLang.value === 'zh-CN' 
        ? `确定要删除选中的 ${selectedPhotos.value.length} 张照片吗？删除后可在回收站找回` 
        : `Delete ${selectedPhotos.value.length} selected photos? You can restore them from trash.`,
      currentLang.value === 'zh-CN' ? '提示' : 'Confirm',
      { type: 'warning' }
    )
    await photoApi.batchOperation({ ids: selectedPhotos.value, operation: 'delete' })
    ElMessage.success(currentLang.value === 'zh-CN' ? '批量删除成功，已移入回收站' : 'Batch delete successful')
    selectedPhotos.value = []
    await fetchPhotos()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(currentLang.value === 'zh-CN' ? '删除失败' : 'Failed to delete')
    }
  }
}

const handleUpload = async () => {
  if (uploadFileList.value.length === 0) {
    ElMessage.warning(currentLang.value === 'zh-CN' ? '请选择照片' : 'Please select photos')
    return
  }
  uploadLoading.value = true
  try {
    const files = uploadFileList.value.map(f => f.raw).filter(Boolean) as File[]
    for (const file of files) {
      await photoApi.uploadPhoto(file, album.value?.id)
    }
    ElMessage.success(currentLang.value === 'zh-CN' ? '上传成功' : 'Upload successful')
    showUploadDialog.value = false
    uploadFileList.value = []
    await fetchPhotos()
  } catch (error) {
    ElMessage.error(currentLang.value === 'zh-CN' ? '上传失败' : 'Upload failed')
  } finally {
    uploadLoading.value = false
  }
}

const handleEdit = async () => {
  if (!editForm.name) {
    ElMessage.warning(currentLang.value === 'zh-CN' ? '请输入相册名称' : 'Please enter album name')
    return
  }
  editLoading.value = true
  try {
    await albumStore.updateAlbum(album.value!.id, editForm)
    ElMessage.success(currentLang.value === 'zh-CN' ? '更新成功' : 'Updated successfully')
    showEditDialog.value = false
    await fetchAlbum()
  } catch (error) {
    ElMessage.error(currentLang.value === 'zh-CN' ? '更新失败' : 'Update failed')
  } finally {
    editLoading.value = false
  }
}

const handleShare = () => {
  router.push(`/albums?share=${album.value!.id}`)
}

const handleBack = () => {
  router.push('/albums')
}

onMounted(() => {
  fetchAlbum()
  fetchPhotos()
})
</script>

<style scoped lang="scss">
.album-detail-page {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.album-cover-section {
  display: flex;
  gap: 24px;
  padding: 24px;
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  border-radius: 16px;
  margin-bottom: 20px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
}

.cover-preview {
  width: 280px;
  height: 180px;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  position: relative;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  
  &:hover {
    transform: scale(1.02);
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
    
    .cover-overlay {
      opacity: 1;
    }
  }
}

.cover-image {
  width: 100%;
  height: 100%;
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  background: linear-gradient(135deg, #e8eaed 0%, #d1d5db 100%);
  color: #909399;
  
  span {
    font-size: 14px;
  }
}

.cover-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: white;
  opacity: 0;
  transition: opacity 0.3s ease;
  
  span {
    font-size: 14px;
    font-weight: 500;
  }
}

.album-header-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  
  h1 {
    margin: 0 0 12px;
    font-size: 28px;
    font-weight: 700;
    color: #303133;
  }
  
  .album-description {
    margin: 0 0 16px;
    font-size: 15px;
    color: #606266;
    line-height: 1.6;
  }
  
  .album-stats {
    display: flex;
    gap: 20px;
    font-size: 14px;
    color: #909399;
    
    span {
      display: flex;
      align-items: center;
      gap: 6px;
    }
  }
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.header-left {
  display: flex;
  align-items: flex-start;
  gap: 20px;
}

.album-info {
  h2 {
    margin: 0 0 8px;
    color: #333;
  }
  
  .album-desc {
    margin: 0 0 4px;
    color: #666;
    font-size: 14px;
  }
  
  .album-meta {
    margin: 0;
    color: #999;
    font-size: 13px;
  }
}

.header-actions {
  display: flex;
  gap: 10px;
}

.photo-grid {
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
    box-shadow: 0 0 0 3px #409eff, 0 12px 32px rgba(64, 158, 255, 0.3);
    transform: scale(1.02);
  }

  &:hover {
    transform: translateY(-12px) scale(1.03);
    box-shadow: 0 20px 40px rgba(0, 0, 0, 0.18);

    .photo-actions {
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

.photo-info {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 16px;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.7) 0%, rgba(0, 0, 0, 0) 100%);
  color: white;
  opacity: 0;
  transition: opacity 0.3s;
  
  .photo-card:hover & {
    opacity: 1;
  }
}

.photo-title {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.photo-actions {
  display: flex;
  gap: 12px;
  
  .el-icon {
    cursor: pointer;
    font-size: 18px;
    color: white;
    transition: all 0.3s ease;
    
    &:hover {
      transform: scale(1.2);
    }
    
    &.is-favorite {
      color: #f56c6c;
    }
    
    &.is-cover {
      color: #67c23a;
    }
    
    &.delete-icon {
      &:hover {
        color: #f56c6c;
      }
    }
  }
}

.batch-actions {
  display: flex;
  gap: 10px;
  padding: 10px 0;
  border-top: 1px solid #ebeef5;
  margin-top: 10px;
}

.photo-detail {
  .detail-info {
    margin-top: 20px;
    padding: 16px;
    background: #f5f7fa;
    border-radius: 8px;
    p { margin: 8px 0; color: #606266; }
  }
}

.cover-selection-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: #ecf5ff;
  border-radius: 8px;
  margin-bottom: 20px;
  color: #409eff;
  font-size: 14px;
}

.cover-selection-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 16px;
  max-height: 500px;
  overflow-y: auto;
  padding: 4px;
}

.cover-option {
  position: relative;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  border: 3px solid transparent;
  transition: all 0.3s ease;
  
  &:hover {
    transform: scale(1.05);
    border-color: #409eff;
  }
  
  &.is-selected {
    border-color: #67c23a;
    
    .cover-option-check {
      opacity: 1;
    }
  }
}

.cover-option-image {
  width: 100%;
  height: 150px;
}

.cover-option-check {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 24px;
  height: 24px;
  background: #67c23a;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 14px;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.cover-option-title {
  padding: 8px;
  background: rgba(0, 0, 0, 0.6);
  color: white;
  font-size: 12px;
  text-align: center;
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
