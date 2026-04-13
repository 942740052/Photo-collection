<template>
  <div class="storage-monitor-page">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-title">总存储空间</div>
          <div class="stat-value">{{ formatSize(statistics.totalStorage) }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-title">已使用空间</div>
          <div class="stat-value used">{{ formatSize(statistics.usedStorage) }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-title">可用空间</div>
          <div class="stat-value available">{{ formatSize(statistics.availableStorage) }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-title">照片总数</div>
          <div class="stat-value">{{ statistics.photoCount }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-title">相册总数</div>
          <div class="stat-value">{{ statistics.albumCount }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-title">用户总数</div>
          <div class="stat-value">{{ statistics.userCount }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card style="margin-top: 20px">
      <template #header>
        <span>存储使用情况</span>
      </template>
      <el-progress
        :percentage="usagePercentage"
        :format="() => `${usagePercentage.toFixed(1)}%`"
        :stroke-width="20"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { adminApi } from '@/api/admin'
import type { StorageStatistics } from '@/types/admin'

const loading = ref(false)
const statistics = ref<StorageStatistics>({
  totalStorage: 0,
  usedStorage: 0,
  availableStorage: 0,
  photoCount: 0,
  albumCount: 0,
  userCount: 0,
})

const usagePercentage = computed(() => {
  if (statistics.value.totalStorage === 0) return 0
  return (statistics.value.usedStorage / statistics.value.totalStorage) * 100
})

const formatSize = (bytes: number) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const fetchStatistics = async () => {
  loading.value = true
  try {
    const res = await adminApi.getStorageStatistics()
    statistics.value = res.data
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchStatistics()
})
</script>

<style scoped lang="scss">
.storage-monitor-page {
  .stat-card {
    text-align: center;
    padding: 20px 0;
  }

  .stat-title {
    font-size: 14px;
    color: #666;
    margin-bottom: 10px;
  }

  .stat-value {
    font-size: 28px;
    font-weight: bold;
    color: #333;

    &.used {
      color: #e6a23c;
    }

    &.available {
      color: #67c23a;
    }
  }
}
</style>
