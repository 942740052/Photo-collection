<template>
  <div class="photos-page">
    <div class="page-header">
      <h2>{{ t('menu.photos') }}</h2>
      <div class="header-actions">
        <el-button v-if="selectedPhotos.length > 0" type="warning" @click="showBatchEditDialog">
          <el-icon><Edit /></el-icon>
          {{ currentLang === 'zh-CN' ? '批量编辑' : 'Batch Edit' }} ({{ selectedPhotos.length }})
        </el-button>
        <el-button type="primary" @click="uploadDialogVisible = true">
          <el-icon><Upload /></el-icon>
          {{ t('photo.upload') }}
        </el-button>
      </div>
    </div>

    <div class="search-container">
      <div class="search-main">
        <el-input
          v-model="filters.keyword"
          :placeholder="currentLang === 'zh-CN' ? '搜索照片名称、描述或标签' : 'Search name, description or tags'"
          prefix-icon="Search"
          clearable
          style="width: 300px"
          @keyup.enter="handleFilter"
        >
          <template #suffix>
            <el-icon class="search-history-btn" @click="showSearchHistory = !showSearchHistory">
              <Clock />
            </el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="handleFilter">
          <el-icon><Search /></el-icon>
          {{ currentLang === 'zh-CN' ? '搜索' : 'Search' }}
        </el-button>
        <el-button @click="handleReset">{{ currentLang === 'zh-CN' ? '重置' : 'Reset' }}</el-button>
        <el-button type="primary" link @click="showAdvancedSearch = !showAdvancedSearch">
          {{ showAdvancedSearch ? (currentLang === 'zh-CN' ? '收起筛选' : 'Hide Filters') : (currentLang === 'zh-CN' ? '高级筛选' : 'Advanced Filters') }}
          <el-icon><component :is="showAdvancedSearch ? 'ArrowUp' : 'ArrowDown'" /></el-icon>
        </el-button>
      </div>

      <el-collapse-transition>
        <div v-show="showAdvancedSearch" class="advanced-search-panel">
          <div class="filter-section">
            <div class="filter-group">
              <div class="filter-label">
                <el-icon><Calendar /></el-icon>
                {{ currentLang === 'zh-CN' ? '拍摄时间' : 'Shoot Time' }}
              </div>
              <div class="filter-content">
                <el-radio-group v-model="shootTimeType" size="small">
                  <el-radio-button label="all">{{ currentLang === 'zh-CN' ? '全部' : 'All' }}</el-radio-button>
                  <el-radio-button label="today">{{ currentLang === 'zh-CN' ? '今天' : 'Today' }}</el-radio-button>
                  <el-radio-button label="week">{{ currentLang === 'zh-CN' ? '本周' : 'This Week' }}</el-radio-button>
                  <el-radio-button label="month">{{ currentLang === 'zh-CN' ? '本月' : 'This Month' }}</el-radio-button>
                  <el-radio-button label="year">{{ currentLang === 'zh-CN' ? '今年' : 'This Year' }}</el-radio-button>
                  <el-radio-button label="custom">{{ currentLang === 'zh-CN' ? '自定义' : 'Custom' }}</el-radio-button>
                </el-radio-group>
                <div v-if="shootTimeType === 'custom'" class="custom-date-range">
                  <el-date-picker
                    v-model="shootDateRange"
                    type="daterange"
                    :range-separator="currentLang === 'zh-CN' ? '至' : 'To'"
                    :start-placeholder="currentLang === 'zh-CN' ? '开始日期' : 'Start'"
                    :end-placeholder="currentLang === 'zh-CN' ? '结束日期' : 'End'"
                    style="width: 280px"
                  />
                </div>
              </div>
            </div>

            <div class="filter-group">
              <div class="filter-label">
                <el-icon><FolderOpened /></el-icon>
                {{ currentLang === 'zh-CN' ? '所属相册' : 'Album' }}
              </div>
              <div class="filter-content">
                <el-select v-model="filters.albumId" :placeholder="currentLang === 'zh-CN' ? '全部相册' : 'All Albums'" clearable style="width: 200px">
                  <el-option v-for="album in albums" :key="album.id" :label="album.name" :value="album.id" />
                </el-select>
              </div>
            </div>

            <div class="filter-group">
              <div class="filter-label">
                <el-icon><PriceTag /></el-icon>
                {{ currentLang === 'zh-CN' ? '标签筛选' : 'Tags' }}
              </div>
              <div class="filter-content">
                <div class="tag-list">
                  <el-tag
                    v-for="tag in availableTags"
                    :key="tag.id"
                    :type="selectedTags.includes(tag.id) ? 'primary' : 'info'"
                    :effect="selectedTags.includes(tag.id) ? 'dark' : 'plain'"
                    class="tag-item"
                    @click="toggleTag(tag.id)"
                  >
                    {{ tag.name }}
                  </el-tag>
                  <span v-if="availableTags.length === 0" class="no-tags">
                    {{ currentLang === 'zh-CN' ? '暂无标签' : 'No tags' }}
                  </span>
                </div>
              </div>
            </div>

            <div class="filter-group">
              <div class="filter-label">
                <el-icon><Location /></el-icon>
                {{ currentLang === 'zh-CN' ? '拍摄地点' : 'Location' }}
              </div>
              <div class="filter-content">
                <el-input
                  v-model="filters.location"
                  :placeholder="currentLang === 'zh-CN' ? '输入地点关键词' : 'Enter location'"
                  clearable
                  style="width: 200px"
                />
                <el-select v-model="filters.locationFilter" :placeholder="currentLang === 'zh-CN' ? '筛选条件' : 'Filter'" style="width: 120px; margin-left: 10px">
                  <el-option :label="currentLang === 'zh-CN' ? '包含' : 'Contains'" value="contains" />
                  <el-option :label="currentLang === 'zh-CN' ? '精确匹配' : 'Exact'" value="exact" />
                </el-select>
              </div>
            </div>

            <div class="filter-group">
              <div class="filter-label">
                <el-icon><Camera /></el-icon>
                {{ currentLang === 'zh-CN' ? '相机设备' : 'Camera Device' }}
              </div>
              <div class="filter-content">
                <el-select v-model="filters.cameraModel" :placeholder="currentLang === 'zh-CN' ? '全部设备' : 'All Devices'" clearable style="width: 200px">
                  <el-option v-for="camera in cameraModels" :key="camera" :label="camera" :value="camera" />
                </el-select>
              </div>
            </div>

            <div class="filter-group">
              <div class="filter-label">
                <el-icon><Sort /></el-icon>
                {{ currentLang === 'zh-CN' ? '排序方式' : 'Sort By' }}
              </div>
              <div class="filter-content">
                <el-select v-model="filters.sortBy" style="width: 140px">
                  <el-option :label="currentLang === 'zh-CN' ? '上传时间' : 'Upload Time'" value="created_at" />
                  <el-option :label="currentLang === 'zh-CN' ? '拍摄时间' : 'Shoot Time'" value="shoot_time" />
                  <el-option :label="currentLang === 'zh-CN' ? '文件大小' : 'File Size'" value="file_size" />
                  <el-option :label="currentLang === 'zh-CN' ? '浏览次数' : 'View Count'" value="view_count" />
                </el-select>
                <el-select v-model="filters.sortOrder" style="width: 100px; margin-left: 10px">
                  <el-option :label="currentLang === 'zh-CN' ? '降序' : 'DESC'" value="DESC" />
                  <el-option :label="currentLang === 'zh-CN' ? '升序' : 'ASC'" value="ASC" />
                </el-select>
              </div>
            </div>
          </div>

          <div class="filter-actions">
            <el-button type="primary" @click="handleFilter">
              <el-icon><Search /></el-icon>
              {{ currentLang === 'zh-CN' ? '应用筛选' : 'Apply Filters' }}
            </el-button>
            <el-button @click="handleReset">{{ currentLang === 'zh-CN' ? '重置全部' : 'Reset All' }}</el-button>
          </div>
        </div>
      </el-collapse-transition>

      <el-collapse-transition>
        <div v-show="showSearchHistory" class="search-history-panel">
          <div class="history-header">
            <span>{{ currentLang === 'zh-CN' ? '搜索历史' : 'Search History' }}</span>
            <el-button type="danger" link size="small" @click="clearSearchHistory">
              <el-icon><Delete /></el-icon>
              {{ currentLang === 'zh-CN' ? '清空' : 'Clear' }}
            </el-button>
          </div>
          <div class="history-list">
            <div
              v-for="(item, index) in searchHistory"
              :key="index"
              class="history-item"
              @click="applyHistorySearch(item)"
            >
              <el-icon><Clock /></el-icon>
              <span class="history-keyword">{{ item.keyword || (currentLang === 'zh-CN' ? '无关键词' : 'No keyword') }}</span>
              <span class="history-time">{{ formatHistoryTime(item.timestamp) }}</span>
              <el-icon class="history-delete" @click.stop="removeHistoryItem(index)"><Close /></el-icon>
            </div>
            <el-empty v-if="searchHistory.length === 0" :description="currentLang === 'zh-CN' ? '暂无搜索历史' : 'No search history'" :image-size="60" />
          </div>
        </div>
      </el-collapse-transition>
    </div>

    <div class="selection-bar" v-if="selectedPhotos.length > 0">
      <el-checkbox v-model="selectAll" @change="handleSelectAll">{{ currentLang === 'zh-CN' ? '全选' : 'Select All' }}</el-checkbox>
      <span class="selected-count">{{ currentLang === 'zh-CN' ? `已选择 ${selectedPhotos.length} 张照片` : `${selectedPhotos.length} photos selected` }}</span>
      <el-button size="small" @click="handleBatchFavorite">{{ currentLang === 'zh-CN' ? '批量收藏' : 'Batch Favorite' }}</el-button>
      <el-button size="small" @click="showBatchMoveDialog">{{ currentLang === 'zh-CN' ? '移动到相册' : 'Move to Album' }}</el-button>
      <el-button size="small" type="danger" @click="handleBatchDelete">{{ currentLang === 'zh-CN' ? '批量删除' : 'Batch Delete' }}</el-button>
      <el-button size="small" @click="selectedPhotos = []">{{ currentLang === 'zh-CN' ? '取消选择' : 'Cancel' }}</el-button>
    </div>

    <el-empty v-if="!loading && photos.length === 0" :description="currentLang === 'zh-CN' ? '暂无照片，点击上方按钮上传' : 'No photos yet'">
      <el-button type="primary" @click="uploadDialogVisible = true">
        <el-icon><Upload /></el-icon>
        {{ t('photo.upload') }}
      </el-button>
    </el-empty>

    <div v-else v-loading="loading" class="photo-timeline-container">
      <div v-for="(group, index) in groupedPhotos" :key="index" class="timeline-group">
        <div class="timeline-header">
          <div class="timeline-date">
            <el-icon><Calendar /></el-icon>
            <span>{{ group.date }}</span>
          </div>
          <div class="timeline-count">{{ group.photos.length }} {{ currentLang === 'zh-CN' ? '张照片' : 'photos' }}</div>
        </div>
        <div class="photo-masonry">
          <div
            v-for="photo in group.photos"
            :key="photo.id"
            class="photo-card"
            :class="{ 'selected': selectedPhotos.includes(photo.id) }"
            @click="handleViewPhoto(photo)"
          >
            <div class="photo-wrapper">
              <div class="select-checkbox" @click.stop="toggleSelect(photo.id)">
                <el-checkbox :model-value="selectedPhotos.includes(photo.id)" />
              </div>
              <el-image 
                :src="getPhotoUrl(photo.thumbnailPath)" 
                fit="cover" 
                class="photo-image"
                :style="{ aspectRatio: getPhotoAspectRatio(photo) }"
              >
                <template #error>
                  <div class="image-error">
                    <el-icon :size="40"><Picture /></el-icon>
                  </div>
                </template>
                <template #placeholder>
                  <div class="image-loading">
                    <el-icon class="is-loading"><Loading /></el-icon>
                  </div>
                </template>
              </el-image>
              <div class="photo-overlay">
                <div class="overlay-content">
                  <div class="overlay-title">{{ photo.title || (currentLang === 'zh-CN' ? '未命名照片' : 'Untitled') }}</div>
                  <div class="overlay-meta">
                    <span v-if="photo.shootTime"><el-icon><Clock /></el-icon> {{ formatDate(photo.shootTime) }}</span>
                    <span v-if="photo.cameraModel"><el-icon><Camera /></el-icon> {{ photo.cameraModel }}</span>
                  </div>
                  <div class="overlay-actions">
                    <el-button 
                      circle 
                      size="small" 
                      :type="photo.isFavorite ? 'warning' : 'default'"
                      @click.stop="handleToggleFavorite(photo)"
                    >
                      <el-icon><Star /></el-icon>
                    </el-button>
                    <el-button 
                      circle 
                      size="small" 
                      type="danger"
                      @click.stop="handleDelete(photo)"
                    >
                      <el-icon><Delete /></el-icon>
                    </el-button>
                  </div>
                </div>
              </div>
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
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="handleSizeChange"
      @current-change="handlePageChange"
    />

    <el-dialog v-model="uploadDialogVisible" :title="t('photo.upload')" width="600px" destroy-on-close>
      <el-form :model="uploadForm" label-width="80px">
        <el-form-item :label="currentLang === 'zh-CN' ? '选择相册' : 'Album'">
          <el-select v-model="uploadForm.albumId" :placeholder="currentLang === 'zh-CN' ? '选择相册（可选）' : 'Select album (optional)'" clearable style="width: 100%">
            <el-option v-for="album in albums" :key="album.id" :label="album.name" :value="album.id" />
          </el-select>
        </el-form-item>
        <el-form-item :label="currentLang === 'zh-CN' ? '上传照片' : 'Photos'">
          <el-upload ref="uploadRef" v-model:file-list="fileList" action="#" :auto-upload="false" :on-change="handleFileChange" :on-remove="handleFileRemove" accept="image/*" list-type="picture-card" multiple>
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="uploadDialogVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="uploadLoading" @click="handleUpload">{{ t('common.confirm') }}</el-button>
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
          <p><strong>{{ currentLang === 'zh-CN' ? '描述' : 'Description' }}:</strong> {{ currentPhoto.description || '-' }}</p>
          <p v-if="currentPhoto.shootTime"><strong>{{ currentLang === 'zh-CN' ? '拍摄时间' : 'Shoot Time' }}:</strong> {{ formatDateTime(currentPhoto.shootTime) }}</p>
          <p v-if="currentPhoto.cameraModel"><strong>{{ currentLang === 'zh-CN' ? '相机型号' : 'Camera' }}:</strong> {{ currentPhoto.cameraModel }}</p>
          <p v-if="currentPhoto.location"><strong>{{ currentLang === 'zh-CN' ? '拍摄地点' : 'Location' }}:</strong> {{ currentPhoto.location }}</p>
          <p v-if="currentPhoto.fileSize"><strong>{{ currentLang === 'zh-CN' ? '文件大小' : 'File Size' }}:</strong> {{ formatFileSize(currentPhoto.fileSize) }}</p>
        </div>
      </div>
    </el-dialog>

    <el-dialog v-model="batchEditDialogVisible" :title="currentLang === 'zh-CN' ? '批量编辑' : 'Batch Edit'" width="500px">
      <el-form :model="batchEditForm" label-width="80px">
        <el-form-item :label="currentLang === 'zh-CN' ? '标题' : 'Title'">
          <el-input v-model="batchEditForm.title" :placeholder="currentLang === 'zh-CN' ? '留空则不修改' : 'Leave empty to skip'" />
        </el-form-item>
        <el-form-item :label="currentLang === 'zh-CN' ? '描述' : 'Description'">
          <el-input v-model="batchEditForm.description" type="textarea" :rows="3" :placeholder="currentLang === 'zh-CN' ? '留空则不修改' : 'Leave empty to skip'" />
        </el-form-item>
        <el-form-item :label="currentLang === 'zh-CN' ? '移动到' : 'Move to'">
          <el-select v-model="batchEditForm.albumId" :placeholder="currentLang === 'zh-CN' ? '选择相册（可选）' : 'Select album (optional)'" clearable style="width: 100%">
            <el-option v-for="album in albums" :key="album.id" :label="album.name" :value="album.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="batchEditDialogVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="batchEditLoading" @click="handleBatchEdit">{{ t('common.confirm') }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="batchMoveDialogVisible" :title="currentLang === 'zh-CN' ? '移动到相册' : 'Move to Album'" width="400px">
      <el-form label-width="80px">
        <el-form-item :label="currentLang === 'zh-CN' ? '选择相册' : 'Album'">
          <el-select v-model="batchMoveAlbumId" :placeholder="currentLang === 'zh-CN' ? '选择相册' : 'Select album'" style="width: 100%">
            <el-option v-for="album in albums" :key="album.id" :label="album.name" :value="album.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="batchMoveDialogVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="batchMoveLoading" @click="handleBatchMove">{{ t('common.confirm') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { UploadFile, UploadInstance } from 'element-plus'
import { usePhotoStore } from '@/stores/photo'
import { useAlbumStore } from '@/stores/album'
import { photoApi } from '@/api/photo'
import type { Photo } from '@/types/photo'

interface SearchHistoryItem {
  keyword: string
  albumId: number | null
  cameraModel: string
  location: string
  shootTimeType: string
  timestamp: number
}

const { t, locale } = useI18n()
const route = useRoute()
const photoStore = usePhotoStore()
const albumStore = useAlbumStore()

const currentLang = computed(() => locale.value)

const groupedPhotos = computed(() => {
  const groups: { date: string; photos: Photo[] }[] = []
  const dateMap = new Map<string, Photo[]>()
  
  photos.value.forEach(photo => {
    const date = photo.shootTime 
      ? new Date(photo.shootTime).toLocaleDateString(currentLang.value === 'zh-CN' ? 'zh-CN' : 'en-US', { 
          year: 'numeric', 
          month: 'long', 
          day: 'numeric' 
        })
      : (currentLang.value === 'zh-CN' ? '未知日期' : 'Unknown Date')
    
    if (!dateMap.has(date)) {
      dateMap.set(date, [])
    }
    dateMap.get(date)!.push(photo)
  })
  
  dateMap.forEach((photoList, date) => {
    groups.push({ date, photos: photoList })
  })
  
  return groups.sort((a, b) => {
    if (a.date === (currentLang.value === 'zh-CN' ? '未知日期' : 'Unknown Date')) return 1
    if (b.date === (currentLang.value === 'zh-CN' ? '未知日期' : 'Unknown Date')) return -1
    return new Date(b.date).getTime() - new Date(a.date).getTime()
  })
})

const getPhotoAspectRatio = (photo: Photo) => {
  if (photo.width && photo.height) {
    return `${photo.width} / ${photo.height}`
  }
  return '4 / 3'
}

const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(20)
const total = ref(0)
const photos = ref<Photo[]>([])
const albums = ref<any[]>([])
const cameraModels = ref<string[]>([])
const availableTags = ref<any[]>([])
const showAdvancedSearch = ref(false)
const showSearchHistory = ref(false)

const filters = reactive({
  keyword: '',
  albumId: null as number | null,
  cameraModel: '',
  location: '',
  locationFilter: 'contains',
  sortBy: 'created_at',
  sortOrder: 'DESC',
})

const shootTimeType = ref('all')
const shootDateRange = ref<[Date, Date] | null>(null)
const selectedTags = ref<number[]>([])

const searchHistory = ref<SearchHistoryItem[]>([])

const selectedPhotos = ref<number[]>([])
const selectAll = ref(false)

const uploadDialogVisible = ref(false)
const uploadLoading = ref(false)
const uploadRef = ref<UploadInstance>()
const fileList = ref<UploadFile[]>([])
const uploadForm = reactive({ albumId: null as number | null })

const previewDialogVisible = ref(false)
const currentPhoto = ref<Photo | null>(null)

const batchEditDialogVisible = ref(false)
const batchEditLoading = ref(false)
const batchEditForm = reactive({
  title: '',
  description: '',
  albumId: null as number | null,
})

const batchMoveDialogVisible = ref(false)
const batchMoveLoading = ref(false)
const batchMoveAlbumId = ref<number | null>(null)

const API_BASE = 'http://localhost:8080/api'

const getPhotoUrl = (path: string | undefined) => {
  if (!path) return ''
  if (path.startsWith('http')) return path
  return `${API_BASE}${path}`
}

const formatDate = (date: string) => new Date(date).toLocaleDateString()
const formatDateTime = (date: string) => new Date(date).toLocaleString()

const formatFileSize = (bytes: number) => {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(2) + ' MB'
}

const formatHistoryTime = (timestamp: number) => {
  const now = Date.now()
  const diff = now - timestamp
  if (diff < 60000) return currentLang.value === 'zh-CN' ? '刚刚' : 'Just now'
  if (diff < 3600000) return Math.floor(diff / 60000) + (currentLang.value === 'zh-CN' ? '分钟前' : ' min ago')
  if (diff < 86400000) return Math.floor(diff / 3600000) + (currentLang.value === 'zh-CN' ? '小时前' : ' hours ago')
  return new Date(timestamp).toLocaleDateString()
}

const getShootTimeRange = () => {
  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  
  switch (shootTimeType.value) {
    case 'today':
      return { start: today, end: now }
    case 'week': {
      const weekStart = new Date(today)
      weekStart.setDate(weekStart.getDate() - weekStart.getDay())
      return { start: weekStart, end: now }
    }
    case 'month': {
      const monthStart = new Date(now.getFullYear(), now.getMonth(), 1)
      return { start: monthStart, end: now }
    }
    case 'year': {
      const yearStart = new Date(now.getFullYear(), 0, 1)
      return { start: yearStart, end: now }
    }
    case 'custom':
      return shootDateRange.value ? { start: shootDateRange.value[0], end: shootDateRange.value[1] } : null
    default:
      return null
  }
}

const fetchPhotos = async () => {
  loading.value = true
  try {
    const timeRange = getShootTimeRange()
    const params: any = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: filters.keyword || undefined,
      albumId: filters.albumId || undefined,
      cameraModel: filters.cameraModel || undefined,
      location: filters.location || undefined,
      sortBy: filters.sortBy,
      sortOrder: filters.sortOrder,
      tagIds: selectedTags.value.length > 0 ? selectedTags.value.join(',') : undefined,
    }
    if (timeRange) {
      params.shootStartTime = timeRange.start
      params.shootEndTime = timeRange.end
    }
    const result = await photoStore.fetchPhotos(params)
    photos.value = result.records
    total.value = result.total
  } finally {
    loading.value = false
  }
}

const fetchAlbums = async () => {
  const result = await albumStore.fetchAlbums()
  albums.value = result
}

const fetchCameraModels = async () => {
  try {
    const result = await photoApi.getCameraModels()
    cameraModels.value = result.data || []
  } catch (e) {
    cameraModels.value = []
  }
}

const fetchTags = async () => {
  try {
    const result = await photoApi.getTags()
    availableTags.value = result.data || []
  } catch (e) {
    availableTags.value = []
  }
}

const toggleTag = (tagId: number) => {
  const index = selectedTags.value.indexOf(tagId)
  if (index > -1) {
    selectedTags.value.splice(index, 1)
  } else {
    selectedTags.value.push(tagId)
  }
}

const loadSearchHistory = () => {
  const saved = localStorage.getItem('photoSearchHistory')
  if (saved) {
    searchHistory.value = JSON.parse(saved)
  }
}

const saveSearchHistory = () => {
  localStorage.setItem('photoSearchHistory', JSON.stringify(searchHistory.value))
}

const addToHistory = () => {
  if (!filters.keyword && !filters.albumId && !filters.cameraModel && !filters.location && shootTimeType.value === 'all' && selectedTags.value.length === 0) {
    return
  }
  
  const item: SearchHistoryItem = {
    keyword: filters.keyword,
    albumId: filters.albumId,
    cameraModel: filters.cameraModel,
    location: filters.location,
    shootTimeType: shootTimeType.value,
    timestamp: Date.now(),
  }
  
  searchHistory.value = searchHistory.value.filter(h => 
    h.keyword !== item.keyword || 
    h.albumId !== item.albumId || 
    h.cameraModel !== item.cameraModel
  )
  searchHistory.value.unshift(item)
  if (searchHistory.value.length > 20) {
    searchHistory.value = searchHistory.value.slice(0, 20)
  }
  saveSearchHistory()
}

const applyHistorySearch = (item: SearchHistoryItem) => {
  filters.keyword = item.keyword
  filters.albumId = item.albumId
  filters.cameraModel = item.cameraModel
  filters.location = item.location
  shootTimeType.value = item.shootTimeType
  showSearchHistory.value = false
  handleFilter()
}

const removeHistoryItem = (index: number) => {
  searchHistory.value.splice(index, 1)
  saveSearchHistory()
}

const clearSearchHistory = () => {
  searchHistory.value = []
  saveSearchHistory()
}

const handleFileChange = (file: UploadFile) => {
  const isImage = file.raw?.type.startsWith('image/')
  const isLt50M = (file.raw?.size || 0) / 1024 / 1024 < 50
  if (!isImage) {
    ElMessage.error(currentLang.value === 'zh-CN' ? '只能上传图片文件' : 'Only image files are allowed')
    fileList.value = fileList.value.filter(f => f.uid !== file.uid)
    return
  }
  if (!isLt50M) {
    ElMessage.error(currentLang.value === 'zh-CN' ? '图片大小不能超过50MB' : 'Image size cannot exceed 50MB')
    fileList.value = fileList.value.filter(f => f.uid !== file.uid)
  }
}

const handleFileRemove = (file: UploadFile) => {
  fileList.value = fileList.value.filter(f => f.uid !== file.uid)
}

const handleUpload = async () => {
  if (fileList.value.length === 0) {
    ElMessage.warning(currentLang.value === 'zh-CN' ? '请选择要上传的照片' : 'Please select photos to upload')
    return
  }
  uploadLoading.value = true
  try {
    const files = fileList.value.map(f => f.raw).filter(Boolean) as File[]
    if (files.length === 1) {
      await photoApi.uploadPhoto(files[0], uploadForm.albumId || undefined)
    } else {
      await photoApi.uploadPhotos(files)
    }
    ElMessage.success(currentLang.value === 'zh-CN' ? '上传成功' : 'Upload successful')
    uploadDialogVisible.value = false
    fileList.value = []
    uploadForm.albumId = null
    await fetchPhotos()
  } catch (error: any) {
    ElMessage.error(currentLang.value === 'zh-CN' ? '上传失败' : 'Upload failed')
  } finally {
    uploadLoading.value = false
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
    ElMessage.success(photo.isFavorite === 1 ? (currentLang.value === 'zh-CN' ? '已收藏' : 'Added to favorites') : (currentLang.value === 'zh-CN' ? '已取消收藏' : 'Removed from favorites'))
  } catch (error: any) {
    ElMessage.error(currentLang.value === 'zh-CN' ? '操作失败' : 'Operation failed')
  }
}

const handleDelete = async (photo: Photo) => {
  try {
    await ElMessageBox.confirm(currentLang.value === 'zh-CN' ? '确定要删除这张照片吗？' : 'Are you sure you want to delete this photo?', currentLang.value === 'zh-CN' ? '提示' : 'Confirm', { confirmButtonText: currentLang.value === 'zh-CN' ? '确定' : 'Confirm', cancelButtonText: currentLang.value === 'zh-CN' ? '取消' : 'Cancel', type: 'warning' })
    await photoStore.deletePhoto(photo.id)
    ElMessage.success(currentLang.value === 'zh-CN' ? '删除成功，已移入回收站' : 'Deleted successfully')
    await safeRefresh()
  } catch (error: any) {
    if (error === 'cancel') return
    handleOperationError(error, currentLang.value === 'zh-CN' ? '删除失败' : 'Delete failed')
  }
}

const toggleSelect = (photoId: number) => {
  const index = selectedPhotos.value.indexOf(photoId)
  if (index > -1) {
    selectedPhotos.value.splice(index, 1)
  } else {
    selectedPhotos.value.push(photoId)
  }
  selectAll.value = selectedPhotos.value.length === photos.value.length
}

const handleSelectAll = (val: boolean) => {
  if (val) {
    selectedPhotos.value = photos.value.map(p => p.id)
  } else {
    selectedPhotos.value = []
  }
}

const showBatchEditDialog = () => {
  batchEditForm.title = ''
  batchEditForm.description = ''
  batchEditForm.albumId = null
  batchEditDialogVisible.value = true
}

const handleBatchEdit = async () => {
  batchEditLoading.value = true
  try {
    await photoApi.batchUpdate({
      photoIds: selectedPhotos.value,
      title: batchEditForm.title || undefined,
      description: batchEditForm.description || undefined,
      albumId: batchEditForm.albumId || undefined,
    })
    ElMessage.success(currentLang.value === 'zh-CN' ? '批量更新成功' : 'Batch update successful')
    batchEditDialogVisible.value = false
    selectedPhotos.value = []
    await safeRefresh()
  } catch (error: any) {
    handleOperationError(error, currentLang.value === 'zh-CN' ? '批量更新失败' : 'Batch update failed')
  } finally {
    batchEditLoading.value = false
  }
}

const showBatchMoveDialog = () => {
  batchMoveAlbumId.value = null
  batchMoveDialogVisible.value = true
}

const handleBatchMove = async () => {
  if (!batchMoveAlbumId.value) {
    ElMessage.warning(currentLang.value === 'zh-CN' ? '请选择相册' : 'Please select an album')
    return
  }
  batchMoveLoading.value = true
  try {
    await photoApi.batchMoveToAlbum(selectedPhotos.value, batchMoveAlbumId.value)
    ElMessage.success(currentLang.value === 'zh-CN' ? '移动成功' : 'Move successful')
    batchMoveDialogVisible.value = false
    selectedPhotos.value = []
    await safeRefresh()
  } catch (error: any) {
    handleOperationError(error, currentLang.value === 'zh-CN' ? '移动失败' : 'Move failed')
  } finally {
    batchMoveLoading.value = false
  }
}

const handleBatchFavorite = async () => {
  try {
    await photoApi.batchOperation({ ids: selectedPhotos.value, operation: 'favorite' })
    ElMessage.success(currentLang.value === 'zh-CN' ? '批量收藏成功' : 'Batch favorite successful')
    selectedPhotos.value = []
    await safeRefresh()
  } catch (error: any) {
    handleOperationError(error, currentLang.value === 'zh-CN' ? '操作失败' : 'Operation failed')
  }
}

const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(currentLang.value === 'zh-CN' ? `确定要删除选中的 ${selectedPhotos.value.length} 张照片吗？` : `Are you sure you want to delete ${selectedPhotos.value.length} photos?`, currentLang.value === 'zh-CN' ? '提示' : 'Confirm', { confirmButtonText: currentLang.value === 'zh-CN' ? '确定' : 'Confirm', cancelButtonText: currentLang.value === 'zh-CN' ? '取消' : 'Cancel', type: 'warning' })
    await photoApi.batchOperation({ ids: selectedPhotos.value, operation: 'delete' })
    ElMessage.success(currentLang.value === 'zh-CN' ? '批量删除成功，已移入回收站' : 'Batch delete successful')
    selectedPhotos.value = []
    await safeRefresh()
  } catch (error: any) {
    if (error === 'cancel') return
    handleOperationError(error, currentLang.value === 'zh-CN' ? '删除失败' : 'Delete failed')
  }
}

const safeRefresh = async () => {
  try {
    await fetchPhotos()
  } catch (refreshError) {
    console.warn('操作成功但刷新列表失败:', refreshError)
  }
}

const handleOperationError = (error: any, defaultMessage: string) => {
  if (error?.message?.includes('403') || error?.message?.includes('401') || error?.response?.status === 403 || error?.response?.status === 401) {
    ElMessage.warning(currentLang.value === 'zh-CN' ? '登录已过期，请重新登录后刷新页面' : 'Session expired, please re-login and refresh')
  } else {
    const errorMsg = error?.response?.data?.message || error?.message || defaultMessage
    ElMessage.error(errorMsg)
  }
}

const handleFilter = () => {
  addToHistory()
  pageNum.value = 1
  fetchPhotos()
}

const handleReset = () => {
  filters.keyword = ''
  filters.albumId = null
  filters.cameraModel = ''
  filters.location = ''
  filters.locationFilter = 'contains'
  filters.sortBy = 'created_at'
  filters.sortOrder = 'DESC'
  shootTimeType.value = 'all'
  shootDateRange.value = null
  selectedTags.value = []
  pageNum.value = 1
  fetchPhotos()
}

const handleSizeChange = () => { pageNum.value = 1; fetchPhotos() }
const handlePageChange = () => fetchPhotos()

watch(() => shootTimeType.value, (val) => {
  if (val !== 'custom') {
    shootDateRange.value = null
  }
})

onMounted(() => {
  fetchPhotos()
  fetchAlbums()
  fetchCameraModels()
  fetchTags()
  loadSearchHistory()
})
</script>

<style scoped lang="scss">
.photos-page {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  h2 { margin: 0; color: #333; }
}

.header-actions {
  display: flex;
  gap: 10px;
}

.search-container {
  position: relative;
  margin-bottom: 15px;
}

.search-main {
  display: flex;
  gap: 10px;
  align-items: center;
}

.search-history-btn {
  cursor: pointer;
  color: #909399;
  &:hover { color: #409eff; }
}

.advanced-search-panel {
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
  border-radius: 12px;
  padding: 20px;
  margin-top: 15px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.filter-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.filter-group {
  display: flex;
  align-items: flex-start;
  gap: 15px;
}

.filter-label {
  display: flex;
  align-items: center;
  gap: 6px;
  min-width: 80px;
  font-weight: 500;
  color: #606266;
  padding-top: 6px;
  
  .el-icon { font-size: 16px; color: #409eff; }
}

.filter-content {
  flex: 1;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
}

.custom-date-range {
  margin-top: 10px;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-item {
  cursor: pointer;
  transition: all 0.2s;
  &:hover { transform: scale(1.05); }
}

.no-tags {
  color: #909399;
  font-size: 13px;
}

.filter-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
  padding-top: 15px;
  border-top: 1px solid #dcdfe6;
}

.search-history-panel {
  position: absolute;
  top: 100%;
  left: 0;
  width: 350px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  z-index: 100;
  margin-top: 5px;
  overflow: hidden;
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 15px;
  background: #f5f7fa;
  border-bottom: 1px solid #ebeef5;
  font-weight: 500;
}

.history-list {
  max-height: 300px;
  overflow-y: auto;
}

.history-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 15px;
  cursor: pointer;
  transition: background 0.2s;
  
  &:hover {
    background: #f5f7fa;
    .history-delete { opacity: 1; }
  }
  
  .el-icon:first-child { color: #909399; }
}

.history-keyword {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.history-time {
  font-size: 12px;
  color: #909399;
}

.history-delete {
  opacity: 0;
  color: #f56c6c;
  transition: opacity 0.2s;
  &:hover { color: #f56c6c; }
}

.selection-bar {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 10px 15px;
  background: #ecf5ff;
  border-radius: 8px;
  margin-bottom: 15px;
  .selected-count { color: #409eff; font-weight: 500; }
}

.photo-timeline-container {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
  border-radius: 16px;
}

.timeline-group {
  margin-bottom: 50px;
  position: relative;
  
  &:last-child {
    margin-bottom: 0;
  }
  
  &::before {
    content: '';
    position: absolute;
    left: 20px;
    top: 60px;
    bottom: 0;
    width: 2px;
    background: linear-gradient(to bottom, #409eff, #67c23a);
    opacity: 0.3;
  }
}

.timeline-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  margin-bottom: 24px;
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  border-left: 4px solid #409eff;
  position: relative;
  
  &::before {
    content: '';
    position: absolute;
    left: -12px;
    top: 50%;
    transform: translateY(-50%);
    width: 20px;
    height: 20px;
    background: #409eff;
    border-radius: 50%;
    border: 4px solid #fff;
    box-shadow: 0 2px 8px rgba(64, 158, 255, 0.4);
  }
  
  .timeline-date {
    display: flex;
    align-items: center;
    gap: 12px;
    font-size: 20px;
    font-weight: 700;
    color: #303133;
    
    .el-icon {
      font-size: 24px;
      color: #409eff;
    }
  }
  
  .timeline-count {
    font-size: 14px;
    color: #909399;
    background: #f0f2f5;
    padding: 6px 16px;
    border-radius: 20px;
    font-weight: 500;
  }
}

.photo-masonry {
  column-count: 4;
  column-gap: 20px;
  padding-left: 40px;
  
  @media (max-width: 1400px) {
    column-count: 3;
  }
  
  @media (max-width: 1000px) {
    column-count: 2;
    column-gap: 16px;
    padding-left: 20px;
  }
  
  @media (max-width: 600px) {
    column-count: 2;
    column-gap: 12px;
    padding-left: 10px;
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

.photo-wrapper {
  position: relative;
  width: 100%;
  overflow: hidden;
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
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  font-size: 13px;
  margin-bottom: 14px;
  opacity: 0.9;
  
  span {
    display: flex;
    align-items: center;
    gap: 6px;
    text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
    
    .el-icon {
      font-size: 14px;
    }
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
    
    &.el-button--danger {
      background: rgba(245, 108, 108, 0.85);
      border-color: rgba(245, 108, 108, 0.95);
    }
  }
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

.image-loading {
  width: 100%;
  height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
  font-size: 32px;
  color: #409eff;
  animation: pulse 1.5s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 0.6;
    transform: scale(1);
  }
  50% {
    opacity: 1;
    transform: scale(1.1);
  }
}

.image-loading {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
  font-size: 30px;
  color: #409eff;
}

.el-pagination {
  margin-top: 20px;
  justify-content: center;
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
</style>
