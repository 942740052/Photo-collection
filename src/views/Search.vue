<template>
  <div class="page-container search-page">
    <div class="search-bar">
      <el-input
        v-model="photoStore.searchQuery"
        placeholder="搜索照片、标签、地点、相机..."
        size="large"
        :prefix-icon="Search"
        clearable
        class="search-input"
        @keyup.enter="doSearch"
      />
      <el-button size="large" type="primary" @click="doSearch">搜索</el-button>
      <el-button size="large" @click="showFilter = !showFilter">
        <el-icon><Filter /></el-icon> 筛选
      </el-button>
    </div>

    <div v-if="showFilter" class="filter-panel">
      <div class="filter-row">
        <span class="filter-label">标签：</span>
        <el-select v-model="photoStore.filterTags" multiple placeholder="选择标签" size="small" style="flex: 1">
          <el-option v-for="tag in photoStore.tags" :key="tag.id" :label="tag.name" :value="tag.name" />
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
        <el-select v-model="photoStore.filterCamera" placeholder="选择相机" size="small" clearable style="flex: 1">
          <el-option v-for="camera in photoStore.cameraList" :key="camera" :label="camera" :value="camera" />
        </el-select>
      </div>
      <div class="filter-row" style="justify-content: flex-end">
        <el-button size="small" @click="photoStore.clearFilters()">清除筛选</el-button>
      </div>
    </div>

    <div v-if="hasSearched" class="search-results">
      <div class="results-header">
        <span>找到 {{ photoStore.filteredPhotos.length }} 张照片</span>
        <el-radio-group v-model="photoStore.viewMode" size="small">
          <el-radio-button value="grid">网格</el-radio-button>
          <el-radio-button value="timeline">时间轴</el-radio-button>
        </el-radio-group>
      </div>

      <div v-if="photoStore.viewMode === 'grid'" class="photo-grid medium">
        <div
          v-for="photo in photoStore.filteredPhotos"
          :key="photo.id"
          class="photo-item"
          @click="$router.push(`/photos/${photo.id}`)"
        >
          <img :src="photo.thumbnail_medium" :alt="photo.file_name" loading="lazy" />
          <div class="photo-overlay">
            <span class="photo-name">{{ photo.file_name }}</span>
            <span class="photo-date">{{ photo.location_name || '' }}</span>
          </div>
        </div>
      </div>

      <div v-else>
        <div v-for="group in photoStore.timelinePhotos" :key="group.date" class="timeline-group">
          <div class="timeline-group-header">
            <span class="date-label">{{ group.displayDate }}</span>
            <span class="photo-count">{{ group.photos.length }} 张</span>
          </div>
          <div class="photo-grid medium">
            <div
              v-for="photo in group.photos"
              :key="photo.id"
              class="photo-item"
              @click="$router.push(`/photos/${photo.id}`)"
            >
              <img :src="photo.thumbnail_medium" :alt="photo.file_name" loading="lazy" />
            </div>
          </div>
        </div>
      </div>

      <div v-if="photoStore.filteredPhotos.length === 0" class="empty-state">
        <el-icon :size="64"><Search /></el-icon>
        <p>没有找到匹配的照片</p>
        <el-button @click="photoStore.clearFilters()">清除筛选</el-button>
      </div>
    </div>

    <div v-else class="search-suggestions">
      <h3>热门标签</h3>
      <div class="tag-cloud">
        <el-tag
          v-for="tag in photoStore.tags"
          :key="tag.id"
          :type="tagTypes[tag.id % tagTypes.length]"
          class="tag-item"
          @click="searchByTag(tag.name)"
          style="cursor: pointer"
        >
          {{ tag.name }} ({{ tag.usage_count }})
        </el-tag>
      </div>

      <h3 style="margin-top: 24px">最近搜索</h3>
      <div class="recent-searches">
        <el-tag
          v-for="(q, i) in recentSearches"
          :key="i"
          size="large"
          class="tag-item"
          @click="photoStore.searchQuery = q; doSearch()"
          style="cursor: pointer"
        >
          {{ q }}
        </el-tag>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { usePhotoStore } from '../stores/photo'
import { Search, Filter } from '@element-plus/icons-vue'

const route = useRoute()
const photoStore = usePhotoStore()

const showFilter = ref(false)
const hasSearched = ref(false)
const recentSearches = ref(['风景', '海边', '人像'])
const tagTypes = ['', 'success', 'warning', 'danger', 'info']

onMounted(() => {
  if (route.query.q) {
    photoStore.searchQuery = route.query.q
    doSearch()
  }
})

const doSearch = () => {
  if (photoStore.searchQuery || photoStore.filterTags.length || photoStore.filterDateRange || photoStore.filterCamera) {
    hasSearched.value = true
    if (photoStore.searchQuery && !recentSearches.value.includes(photoStore.searchQuery)) {
      recentSearches.value.unshift(photoStore.searchQuery)
      if (recentSearches.value.length > 5) recentSearches.value.pop()
    }
  }
}

const searchByTag = (tagName) => {
  photoStore.searchQuery = tagName
  doSearch()
}
</script>

<style scoped>
.search-page {
  max-width: 1600px;
  margin: 0 auto;
}

.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.search-input {
  flex: 1;
}

.results-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  font-size: 14px;
  color: #606266;
}

.search-suggestions h3 {
  font-size: 16px;
  color: #303133;
  margin-bottom: 12px;
}

.tag-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-item {
  margin: 0;
}

.recent-searches {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
</style>
