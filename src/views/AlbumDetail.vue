<template>
  <div class="page-container album-detail-page">
    <div class="page-header">
      <div class="header-left">
        <el-button :icon="Back" @click="$router.push('/albums')">返回相册</el-button>
        <h2>{{ album?.name || '相册' }}</h2>
        <el-tag v-if="album?.type === 'smart'" size="small" type="warning">智能相册</el-tag>
      </div>
      <div class="header-actions">
        <el-button :icon="Plus" @click="showAddPhoto = true">添加照片</el-button>
        <el-button :icon="Share">分享</el-button>
        <el-button :icon="Setting" @click="editAlbum">编辑</el-button>
      </div>
    </div>

    <p v-if="album?.description" class="album-desc">{{ album.description }}</p>

    <div class="photo-grid medium" v-if="albumPhotos.length > 0">
      <div
        v-for="photo in albumPhotos"
        :key="photo.id"
        class="photo-item"
        @click="$router.push(`/photos/${photo.id}`)"
      >
        <div class="photo-favorite" :class="{ active: photo.is_favorite }" @click.stop="photoStore.toggleFavorite(photo.id)">
          <el-icon><Star /></el-icon>
        </div>
        <img :src="photo.thumbnail_medium" :alt="photo.file_name" loading="lazy" />
        <div class="photo-overlay">
          <span class="photo-name">{{ photo.file_name }}</span>
        </div>
      </div>
    </div>

    <div v-else class="empty-state">
      <el-icon :size="64"><Picture /></el-icon>
      <p>相册中还没有照片</p>
      <el-button type="primary" :icon="Plus" @click="showAddPhoto = true">添加照片</el-button>
    </div>

    <el-dialog v-model="showAddPhoto" title="添加照片到相册" width="600px">
      <div class="photo-grid small" style="max-height: 400px; overflow-y: auto">
        <div
          v-for="photo in photoStore.activePhotos"
          :key="photo.id"
          class="photo-item"
          :class="{ selected: selectedPhotos.includes(photo.id) }"
          @click="toggleSelectPhoto(photo.id)"
        >
          <img :src="photo.thumbnail_small" :alt="photo.file_name" loading="lazy" />
        </div>
      </div>
      <template #footer>
        <span>已选择 {{ selectedPhotos.length }} 张</span>
        <el-button @click="showAddPhoto = false">取消</el-button>
        <el-button type="primary" @click="confirmAddPhotos">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { usePhotoStore } from '../stores/photo'
import { ElMessage } from 'element-plus'
import { Plus, Back } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const photoStore = usePhotoStore()

const showAddPhoto = ref(false)
const selectedPhotos = ref([])

const album = computed(() => photoStore.getAlbum(route.params.id))
const albumPhotos = computed(() => photoStore.getAlbumPhotos(route.params.id))

const toggleSelectPhoto = (photoId) => {
  const idx = selectedPhotos.value.indexOf(photoId)
  if (idx === -1) {
    selectedPhotos.value.push(photoId)
  } else {
    selectedPhotos.value.splice(idx, 1)
  }
}

const confirmAddPhotos = () => {
  if (selectedPhotos.value.length === 0) {
    ElMessage.warning('请选择照片')
    return
  }
  if (album.value) {
    album.value.photo_count += selectedPhotos.value.length
  }
  ElMessage.success(`已添加 ${selectedPhotos.value.length} 张照片`)
  showAddPhoto.value = false
  selectedPhotos.value = []
}

const editAlbum = () => {
  router.push('/albums')
}
</script>

<style scoped>
.album-detail-page {
  max-width: 1600px;
  margin: 0 auto;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-left h2 {
  margin: 0;
}

.album-desc {
  color: #909399;
  font-size: 14px;
  margin: 0 0 16px;
}

.photo-item.selected {
  outline: 3px solid #409eff;
  outline-offset: -3px;
  border-radius: 8px;
}
</style>
