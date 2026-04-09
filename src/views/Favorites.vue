<template>
  <div class="page-container favorites-page">
    <div class="page-header">
      <h2>收藏</h2>
      <span class="photo-count">{{ photoStore.favoritePhotos.length }} 张照片</span>
    </div>

    <div class="photo-grid medium" v-if="photoStore.favoritePhotos.length > 0">
      <div
        v-for="photo in photoStore.favoritePhotos"
        :key="photo.id"
        class="photo-item"
        @click="$router.push(`/photos/${photo.id}`)"
      >
        <div class="photo-favorite active" @click.stop="photoStore.toggleFavorite(photo.id)">
          <el-icon><Star /></el-icon>
        </div>
        <img :src="photo.thumbnail_medium" :alt="photo.file_name" loading="lazy" />
        <div class="photo-overlay">
          <span class="photo-name">{{ photo.file_name }}</span>
        </div>
      </div>
    </div>

    <div v-else class="empty-state">
      <el-icon :size="64"><Star /></el-icon>
      <p>还没有收藏的照片</p>
      <el-button type="primary" @click="$router.push('/photos')">浏览照片</el-button>
    </div>
  </div>
</template>

<script setup>
import { usePhotoStore } from '../stores/photo'
const photoStore = usePhotoStore()
</script>

<style scoped>
.favorites-page {
  max-width: 1600px;
  margin: 0 auto;
}

.photo-count {
  color: #909399;
  font-size: 14px;
}
</style>
