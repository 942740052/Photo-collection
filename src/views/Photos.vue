<template>
  <div class="page-container photos-page">
    <div class="page-header">
      <h2>照片</h2>
      <div class="header-actions">
        <el-radio-group v-model="photoStore.viewMode" size="small">
          <el-radio-button value="grid">
            <el-icon><Grid /></el-icon>
          </el-radio-button>
          <el-radio-button value="timeline">
            <el-icon><Timer /></el-icon>
          </el-radio-button>
        </el-radio-group>
        <el-dropdown trigger="click" @command="handleSort">
          <el-button size="small">
            排序 <el-icon><ArrowDown /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="date_taken-desc">拍摄时间（新→旧）</el-dropdown-item>
              <el-dropdown-item command="date_taken-asc">拍摄时间（旧→新）</el-dropdown-item>
              <el-dropdown-item command="date_uploaded-desc">上传时间</el-dropdown-item>
              <el-dropdown-item command="file_name-asc">文件名</el-dropdown-item>
              <el-dropdown-item command="file_size-desc">文件大小</el-dropdown-item>
              <el-dropdown-item command="rating-desc">评分</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <el-dropdown trigger="click" @command="handleGridSize">
          <el-button size="small">
            密度 <el-icon><ArrowDown /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="small">紧凑</el-dropdown-item>
              <el-dropdown-item command="medium">标准</el-dropdown-item>
              <el-dropdown-item command="large">宽松</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <el-button size="small" :icon="Filter" @click="showFilter = !showFilter">
          筛选
        </el-button>
      </div>
    </div>

    <div v-if="showFilter" class="filter-panel">
      <div class="filter-row">
        <span class="filter-label">标签：</span>
        <el-select
          v-model="photoStore.filterTags"
          multiple
          placeholder="选择标签"
          size="small"
          style="flex: 1"
        >
          <el-option
            v-for="tag in photoStore.tags"
            :key="tag.id"
            :label="tag.name"
            :value="tag.name"
          />
        </el-select>
      </div>
      <div class="filter-row">
        <span class="filter-label">日期：</span>
        <el-date-picker
          v-model="photoStore.filterDateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          size="small"
          style="flex: 1"
          value-format="YYYY-MM-DD"
        />
      </div>
      <div class="filter-row">
        <span class="filter-label">相机：</span>
        <el-select
          v-model="photoStore.filterCamera"
          placeholder="选择相机"
          size="small"
          clearable
          style="flex: 1"
        >
          <el-option
            v-for="camera in photoStore.cameraList"
            :key="camera"
            :label="camera"
            :value="camera"
          />
        </el-select>
      </div>
      <div class="filter-row" style="justify-content: flex-end">
        <el-button size="small" @click="photoStore.clearFilters()">清除筛选</el-button>
      </div>
    </div>

    <div v-if="photoStore.selectedPhotos.length > 0" class="batch-bar">
      <span>已选择 {{ photoStore.selectedPhotos.length }} 张照片</span>
      <el-button size="small" type="primary" @click="batchAddToAlbum">加入相册</el-button>
      <el-button size="small" @click="batchAddTags">添加标签</el-button>
      <el-button size="small" @click="batchDownload">下载</el-button>
      <el-button size="small" type="danger" @click="batchDelete">删除</el-button>
      <el-button size="small" @click="photoStore.clearSelection()">取消选择</el-button>
    </div>

    <div v-if="photoStore.viewMode === 'grid'" class="photo-grid" :class="photoStore.gridSize">
      <div
        v-for="photo in photoStore.filteredPhotos"
        :key="photo.id"
        class="photo-item"
        :class="{ selected: photoStore.selectedPhotos.includes(photo.id) }"
        @click="handlePhotoClick(photo)"
      >
        <div class="photo-select" @click.stop="photoStore.toggleSelect(photo.id)">
          <el-checkbox :model-value="photoStore.selectedPhotos.includes(photo.id)" />
        </div>
        <div class="photo-favorite" :class="{ active: photo.is_favorite }" @click.stop="photoStore.toggleFavorite(photo.id)">
          <el-icon><Star /></el-icon>
        </div>
        <img :src="photo.thumbnail_medium" :alt="photo.file_name" loading="lazy" />
        <div class="photo-overlay">
          <span class="photo-name">{{ photo.file_name }}</span>
          <span class="photo-date">{{ formatDate(photo.date_taken) }}</span>
        </div>
      </div>
    </div>

    <div v-else class="timeline-view">
      <div v-for="group in photoStore.timelinePhotos" :key="group.date" class="timeline-group">
        <div class="timeline-group-header">
          <span class="date-label">{{ group.displayDate }}</span>
          <span class="photo-count">{{ group.photos.length }} 张照片</span>
        </div>
        <div class="photo-grid" :class="photoStore.gridSize">
          <div
            v-for="photo in group.photos"
            :key="photo.id"
            class="photo-item"
            :class="{ selected: photoStore.selectedPhotos.includes(photo.id) }"
            @click="handlePhotoClick(photo)"
          >
            <div class="photo-select" @click.stop="photoStore.toggleSelect(photo.id)">
              <el-checkbox :model-value="photoStore.selectedPhotos.includes(photo.id)" />
            </div>
            <div class="photo-favorite" :class="{ active: photo.is_favorite }" @click.stop="photoStore.toggleFavorite(photo.id)">
              <el-icon><Star /></el-icon>
            </div>
            <img :src="photo.thumbnail_medium" :alt="photo.file_name" loading="lazy" />
            <div class="photo-overlay">
              <span class="photo-name">{{ photo.file_name }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="photoStore.filteredPhotos.length === 0" class="empty-state">
      <el-icon :size="64"><Picture /></el-icon>
      <p>没有找到照片</p>
      <el-button type="primary" @click="$router.push('/upload')">上传照片</el-button>
    </div>

    <el-dialog v-model="albumDialogVisible" title="加入相册" width="400px">
      <el-select v-model="selectedAlbum" placeholder="选择相册" style="width: 100%">
        <el-option
          v-for="album in photoStore.albums"
          :key="album.id"
          :label="album.name"
          :value="album.id"
        />
      </el-select>
      <template #footer>
        <el-button @click="albumDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAddToAlbum">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { usePhotoStore } from '../stores/photo'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Filter } from '@element-plus/icons-vue'
import dayjs from 'dayjs'

const router = useRouter()
const photoStore = usePhotoStore()

const showFilter = ref(false)
const albumDialogVisible = ref(false)
const selectedAlbum = ref('')

const handlePhotoClick = (photo) => {
  if (photoStore.selectedPhotos.length > 0) {
    photoStore.toggleSelect(photo.id)
  } else {
    router.push(`/photos/${photo.id}`)
  }
}

const handleSort = (command) => {
  const [field, order] = command.split('-')
  photoStore.setSortBy(field)
  photoStore.setSortOrder(order)
}

const handleGridSize = (size) => {
  photoStore.setGridSize(size)
}

const formatDate = (date) => {
  return dayjs(date).format('YYYY/MM/DD')
}

const batchAddToAlbum = () => {
  albumDialogVisible.value = true
}

const confirmAddToAlbum = () => {
  if (!selectedAlbum.value) {
    ElMessage.warning('请选择相册')
    return
  }
  const album = photoStore.getAlbum(selectedAlbum.value)
  album.photo_count += photoStore.selectedPhotos.length
  ElMessage.success(`已将 ${photoStore.selectedPhotos.length} 张照片加入相册「${album.name}」`)
  photoStore.clearSelection()
  albumDialogVisible.value = false
  selectedAlbum.value = ''
}

const batchAddTags = () => {
  ElMessage.info('批量添加标签功能')
}

const batchDownload = () => {
  ElMessage.info('批量下载功能')
}

const batchDelete = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${photoStore.selectedPhotos.length} 张照片吗？照片将移入回收站。`,
      '确认删除',
      { type: 'warning' }
    )
    photoStore.deletePhotos(photoStore.selectedPhotos)
    ElMessage.success('已移入回收站')
  } catch {}
}
</script>

<style scoped>
.photos-page {
  max-width: 1600px;
  margin: 0 auto;
}

.header-actions {
  display: flex;
  gap: 8px;
  align-items: center;
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

.photo-item.selected {
  outline: 3px solid #409eff;
  outline-offset: -3px;
  border-radius: 8px;
}

.timeline-view {
  max-width: 1600px;
}

@media (max-width: 768px) {
  .header-actions {
    flex-wrap: wrap;
  }
}
</style>
