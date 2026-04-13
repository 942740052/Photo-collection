<template>
  <div class="backup-page">
    <div class="page-header">
      <h2>{{ currentLang === 'zh-CN' ? '备份与恢复' : 'Backup & Restore' }}</h2>
      <el-button type="primary" @click="showCreateBackupDialog">
        <el-icon><Plus /></el-icon>
        {{ currentLang === 'zh-CN' ? '创建备份' : 'Create Backup' }}
      </el-button>
    </div>

    <div class="backup-stats">
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon"><el-icon><FolderOpened /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ backups.length }}</div>
              <div class="stat-label">{{ currentLang === 'zh-CN' ? '备份数量' : 'Total Backups' }}</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon success"><el-icon><Check /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ completedCount }}</div>
              <div class="stat-label">{{ currentLang === 'zh-CN' ? '已完成' : 'Completed' }}</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon warning"><el-icon><Loading /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ processingCount }}</div>
              <div class="stat-label">{{ currentLang === 'zh-CN' ? '处理中' : 'Processing' }}</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon danger"><el-icon><Close /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ failedCount }}</div>
              <div class="stat-label">{{ currentLang === 'zh-CN' ? '失败' : 'Failed' }}</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <div class="backup-list">
      <el-table :data="backups" v-loading="loading" stripe>
        <el-table-column prop="name" :label="currentLang === 'zh-CN' ? '备份名称' : 'Name'" />
        <el-table-column prop="type" :label="currentLang === 'zh-CN' ? '类型' : 'Type'" width="120">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.type)">{{ getTypeName(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" :label="currentLang === 'zh-CN' ? '状态' : 'Status'" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">{{ getStatusName(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="currentLang === 'zh-CN' ? '大小' : 'Size'" width="120">
          <template #default="{ row }">{{ formatFileSize(row.fileSize) }}</template>
        </el-table-column>
        <el-table-column :label="currentLang === 'zh-CN' ? '内容' : 'Content'" width="200">
          <template #default="{ row }">
            <span v-if="row.photoCount">{{ row.photoCount }} {{ currentLang === 'zh-CN' ? '照片' : 'photos' }}</span>
            <span v-if="row.albumCount">, {{ row.albumCount }} {{ currentLang === 'zh-CN' ? '相册' : 'albums' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" :label="currentLang === 'zh-CN' ? '创建时间' : 'Created'" width="180">
          <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column :label="currentLang === 'zh-CN' ? '操作' : 'Actions'" width="180" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'completed'" type="primary" link @click="downloadBackup(row)">
              <el-icon><Download /></el-icon>
              {{ currentLang === 'zh-CN' ? '下载' : 'Download' }}
            </el-button>
            <el-button type="danger" link @click="deleteBackup(row)">
              <el-icon><Delete /></el-icon>
              {{ currentLang === 'zh-CN' ? '删除' : 'Delete' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="createDialogVisible" :title="currentLang === 'zh-CN' ? '创建备份' : 'Create Backup'" width="500px">
      <el-form :model="backupForm" label-width="100px">
        <el-form-item :label="currentLang === 'zh-CN' ? '备份名称' : 'Name'">
          <el-input v-model="backupForm.name" :placeholder="currentLang === 'zh-CN' ? '留空则自动生成' : 'Auto generate if empty'" />
        </el-form-item>
        <el-form-item :label="currentLang === 'zh-CN' ? '备份类型' : 'Type'">
          <el-radio-group v-model="backupForm.type">
            <el-radio label="full">{{ currentLang === 'zh-CN' ? '完整备份' : 'Full' }}</el-radio>
            <el-radio label="database">{{ currentLang === 'zh-CN' ? '仅数据库' : 'Database Only' }}</el-radio>
            <el-radio label="photos">{{ currentLang === 'zh-CN' ? '仅照片' : 'Photos Only' }}</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">{{ currentLang === 'zh-CN' ? '取消' : 'Cancel' }}</el-button>
        <el-button type="primary" :loading="creating" @click="createBackup">{{ currentLang === 'zh-CN' ? '创建' : 'Create' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { request } from '@/utils/request'

const { locale } = useI18n()
const currentLang = computed(() => locale.value)

interface Backup {
  id: number
  name: string
  type: string
  status: string
  fileSize: number
  photoCount: number
  albumCount: number
  createdAt: string
}

const loading = ref(false)
const backups = ref<Backup[]>([])
const createDialogVisible = ref(false)
const creating = ref(false)
const backupForm = reactive({
  name: '',
  type: 'full'
})

const completedCount = computed(() => backups.value.filter(b => b.status === 'completed').length)
const processingCount = computed(() => backups.value.filter(b => b.status === 'processing').length)
const failedCount = computed(() => backups.value.filter(b => b.status === 'failed').length)

const fetchBackups = async () => {
  loading.value = true
  try {
    const res = await request.get('/v1/backup')
    backups.value = res.data || []
  } finally {
    loading.value = false
  }
}

const showCreateBackupDialog = () => {
  backupForm.name = ''
  backupForm.type = 'full'
  createDialogVisible.value = true
}

const createBackup = async () => {
  creating.value = true
  try {
    await request.post('/v1/backup', backupForm)
    ElMessage.success(currentLang.value === 'zh-CN' ? '备份创建成功' : 'Backup created successfully')
    createDialogVisible.value = false
    await fetchBackups()
  } catch (e) {
    ElMessage.error(currentLang.value === 'zh-CN' ? '创建失败' : 'Failed to create backup')
  } finally {
    creating.value = false
  }
}

const downloadBackup = (backup: Backup) => {
  window.open(`/api/v1/backup/${backup.id}/download`, '_blank')
}

const deleteBackup = async (backup: Backup) => {
  try {
    await ElMessageBox.confirm(
      currentLang.value === 'zh-CN' ? '确定要删除此备份吗？' : 'Are you sure to delete this backup?',
      currentLang.value === 'zh-CN' ? '提示' : 'Confirm',
      { type: 'warning' }
    )
    await request.delete(`/v1/backup/${backup.id}`)
    ElMessage.success(currentLang.value === 'zh-CN' ? '删除成功' : 'Deleted successfully')
    await fetchBackups()
  } catch (e) {}
}

const formatFileSize = (bytes: number) => {
  if (!bytes) return '-'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(2) + ' MB'
}

const formatDateTime = (date: string) => {
  if (!date) return '-'
  return new Date(date).toLocaleString()
}

const getTypeName = (type: string) => {
  const map: Record<string, string> = {
    full: currentLang.value === 'zh-CN' ? '完整备份' : 'Full',
    database: currentLang.value === 'zh-CN' ? '数据库' : 'Database',
    photos: currentLang.value === 'zh-CN' ? '照片' : 'Photos'
  }
  return map[type] || type
}

const getTypeTagType = (type: string) => {
  const map: Record<string, string> = { full: 'primary', database: 'success', photos: 'warning' }
  return map[type] || 'info'
}

const getStatusName = (status: string) => {
  const map: Record<string, string> = {
    completed: currentLang.value === 'zh-CN' ? '已完成' : 'Completed',
    processing: currentLang.value === 'zh-CN' ? '处理中' : 'Processing',
    failed: currentLang.value === 'zh-CN' ? '失败' : 'Failed'
  }
  return map[status] || status
}

const getStatusTagType = (status: string) => {
  const map: Record<string, string> = { completed: 'success', processing: 'warning', failed: 'danger' }
  return map[status] || 'info'
}

onMounted(() => {
  fetchBackups()
})
</script>

<style scoped lang="scss">
.backup-page {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  h2 { margin: 0; }
}

.backup-stats {
  margin-bottom: 20px;
}

.stat-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 15px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 10px;
  background: #409eff;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  
  &.success { background: #67c23a; }
  &.warning { background: #e6a23c; }
  &.danger { background: #f56c6c; }
}

.stat-info {
  .stat-value {
    font-size: 24px;
    font-weight: 600;
    color: #303133;
  }
  .stat-label {
    font-size: 13px;
    color: #909399;
  }
}

.backup-list {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
}
</style>
