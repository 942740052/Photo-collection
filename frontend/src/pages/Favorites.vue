<template>
  <div class="favorites-page">
    <div class="page-header">
      <h2>{{ t('menu.favorites') }}</h2>
      <span class="count">{{ currentLang === 'zh-CN' ? `共 ${total} 张收藏` : `${total} favorites` }}</span>
    </div>

    <el-empty v-if="!loading && photos.length === 0" :description="currentLang === 'zh-CN' ? '暂无收藏的照片' : 'No favorite photos yet'" />

    <div v-else v-loading="loading" class="photo-masonry">
      <div
        v-for="photo in photos"
        :key="photo.id"
        class="photo-card"
        @click="handleViewPhoto(photo)"
      >
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
            <div class="overlay-meta" v-if="photo.shootTime">
              <span><el-icon><Clock /></el-icon> {{ formatDate(photo.shootTime) }}</span>
            </div>
            <div class="overlay-actions">
              <el-button circle size="small" type="warning" @click.stop="handleUnfavorite(photo)">
                <el-icon><Star /></el-icon>
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <el-pagination
      v-if="total > pageSize"
      v-model:current-page="pageNum"
      v-model:page-size="pageSize"
      :total="total"
      :page-sizes="[20, 40, 60, 100]"
      layout="total, sizes, prev, pager, next"
      @size-change="fetchFavorites"
      @current-change="fetchFavorites"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { photoApi } from '@/api/photo'
import type { Photo } from '@/types/photo'

const { t, locale } = useI18n()

const currentLang = computed(() => locale.value)

const API_BASE = 'http://localhost:8080/api'

const loading = ref(false)
const photos = ref<Photo[]>([])
const pageNum = ref(1)
const pageSize = ref(20)
const total = ref(0)

const getPhotoUrl = (path: string | undefined) => {
  if (!path) return ''
  if (path.startsWith('http')) return path
  return `${API_BASE}${path}`
}

const fetchFavorites = async () => {
  loading.value = true
  try {
    const result = await photoApi.getList({ 
      pageNum: pageNum.value, 
      pageSize: pageSize.value, 
      isFavorite: 1 
    })
    photos.value = result.data?.records || []
    total.value = result.data?.total || 0
  } catch (error) {
    ElMessage.error(currentLang.value === 'zh-CN' ? '获取收藏失败' : 'Failed to load favorites')
  } finally {
    loading.value = false
  }
}

const handleUnfavorite = async (photo: Photo) => {
  try {
    await photoApi.unfavorite(photo.id)
    ElMessage.success(currentLang.value === 'zh-CN' ? '已取消收藏' : 'Removed from favorites')
    await fetchFavorites()
  } catch (error) {
    ElMessage.error(currentLang.value === 'zh-CN' ? '操作失败' : 'Operation failed')
  }
}

const formatDate = (date: string) => {
  if (!date) return ''
  return new Date(date).toLocaleDateString()
}

onMounted(() => {
  fetchFavorites()
})
</script>

<style scoped lang="scss">
.favorites-page {
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

  .count {
    color: #909399;
    font-size: 14px;
  }
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
  
  &:hover {
    transform: translateY(-12px) scale(1.03);
    box-shadow: 0 20px 40px rgba(0, 0, 0, 0.18);
    
    .photo-overlay {
      opacity: 1;
    }
    
    .photo-image {
      transform: scale(1.08);
    }
  }
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
    
    &.el-button--warning {
      background: rgba(230, 162, 60, 0.85);
      border-color: rgba(230, 162, 60, 0.95);
    }
  }
}

.el-pagination {
  margin-top: 20px;
  justify-content: center;
}
</style>
