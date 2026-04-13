<template>
  <div class="security-page">
    <div class="page-header">
      <h2>{{ currentLang === 'zh-CN' ? '安全与隐私' : 'Security & Privacy' }}</h2>
    </div>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="操作日志" name="logs">
        <div class="logs-section">
          <div class="filter-bar">
            <el-select v-model="logFilter.module" :placeholder="currentLang === 'zh-CN' ? '模块' : 'Module'" clearable style="width: 120px">
              <el-option label="照片" value="photo" />
              <el-option label="相册" value="album" />
              <el-option label="备份" value="backup" />
              <el-option label="用户" value="user" />
            </el-select>
            <el-date-picker
              v-model="logDateRange"
              type="daterange"
              :range-separator="currentLang === 'zh-CN' ? '至' : 'To'"
              style="width: 260px"
            />
            <el-button type="primary" @click="fetchLogs">{{ currentLang === 'zh-CN' ? '查询' : 'Search' }}</el-button>
            <el-button type="danger" @click="clearLogs">{{ currentLang === 'zh-CN' ? '清空日志' : 'Clear Logs' }}</el-button>
          </div>

          <el-table :data="logs" v-loading="logsLoading" stripe>
            <el-table-column prop="operation" :label="currentLang === 'zh-CN' ? '操作' : 'Operation'" width="150" />
            <el-table-column prop="module" :label="currentLang === 'zh-CN' ? '模块' : 'Module'" width="100">
              <template #default="{ row }">
                <el-tag size="small">{{ row.module }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="description" :label="currentLang === 'zh-CN' ? '描述' : 'Description'" />
            <el-table-column prop="ipAddress" :label="currentLang === 'zh-CN' ? 'IP地址' : 'IP'" width="130" />
            <el-table-column :label="currentLang === 'zh-CN' ? '状态' : 'Status'" width="80">
              <template #default="{ row }">
                <el-tag :type="row.responseStatus === 200 ? 'success' : 'danger'" size="small">
                  {{ row.responseStatus }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" :label="currentLang === 'zh-CN' ? '时间' : 'Time'" width="180">
              <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>

      <el-tab-pane label="敏感照片保护" name="protect">
        <div class="protect-section">
          <el-alert
            :title="currentLang === 'zh-CN' ? '敏感照片保护功能可以为您的私密照片设置访问密码' : 'Protect your private photos with password'"
            type="info"
            :closable="false"
            show-icon
            style="margin-bottom: 20px"
          />

          <div class="protected-photos" v-if="protectedPhotos.length > 0">
            <h4>{{ currentLang === 'zh-CN' ? '已保护的照片' : 'Protected Photos' }}</h4>
            <el-table :data="protectedPhotos" stripe>
              <el-table-column :label="currentLang === 'zh-CN' ? '照片' : 'Photo'" width="100">
                <template #default="{ row }">
                  <el-image :src="getPhotoUrl(row.thumbnailPath)" style="width: 60px; height: 60px" fit="cover" />
                </template>
              </el-table-column>
              <el-table-column prop="title" :label="currentLang === 'zh-CN' ? '标题' : 'Title'" />
              <el-table-column :label="currentLang === 'zh-CN' ? '操作' : 'Actions'" width="150">
                <template #default="{ row }">
                  <el-button type="danger" link @click="unprotectPhoto(row)">
                    {{ currentLang === 'zh-CN' ? '取消保护' : 'Unprotect' }}
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <el-empty v-else :description="currentLang === 'zh-CN' ? '暂无保护的照片' : 'No protected photos'" />
        </div>
      </el-tab-pane>

      <el-tab-pane label="数据导出" name="export">
        <div class="export-section">
          <el-row :gutter="20">
            <el-col :span="6">
              <div class="export-card" @click="exportAll">
                <el-icon :size="40"><Download /></el-icon>
                <h4>{{ currentLang === 'zh-CN' ? '导出全部照片' : 'Export All' }}</h4>
                <p>{{ currentLang === 'zh-CN' ? '导出所有照片' : 'Export all photos' }}</p>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="export-card" @click="showExportAlbumDialog = true">
                <el-icon :size="40"><FolderOpened /></el-icon>
                <h4>{{ currentLang === 'zh-CN' ? '按相册导出' : 'By Album' }}</h4>
                <p>{{ currentLang === 'zh-CN' ? '选择相册导出' : 'Export by album' }}</p>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="export-card" @click="showExportDateDialog = true">
                <el-icon :size="40"><Calendar /></el-icon>
                <h4>{{ currentLang === 'zh-CN' ? '按时间导出' : 'By Date' }}</h4>
                <p>{{ currentLang === 'zh-CN' ? '选择时间范围导出' : 'Export by date range' }}</p>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="export-card" @click="exportFavorites">
                <el-icon :size="40"><Star /></el-icon>
                <h4>{{ currentLang === 'zh-CN' ? '导出收藏' : 'Export Favorites' }}</h4>
                <p>{{ currentLang === 'zh-CN' ? '导出收藏的照片' : 'Export favorite photos' }}</p>
              </div>
            </el-col>
          </el-row>
        </div>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="showExportAlbumDialog" :title="currentLang === 'zh-CN' ? '选择相册' : 'Select Album'" width="400px">
      <el-select v-model="selectedAlbumId" :placeholder="currentLang === 'zh-CN' ? '选择相册' : 'Select album'" style="width: 100%">
        <el-option v-for="album in albums" :key="album.id" :label="album.name" :value="album.id" />
      </el-select>
      <template #footer>
        <el-button @click="showExportAlbumDialog = false">{{ currentLang === 'zh-CN' ? '取消' : 'Cancel' }}</el-button>
        <el-button type="primary" @click="exportByAlbum">{{ currentLang === 'zh-CN' ? '导出' : 'Export' }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showExportDateDialog" :title="currentLang === 'zh-CN' ? '选择时间范围' : 'Select Date Range'" width="400px">
      <el-date-picker
        v-model="exportDateRange"
        type="daterange"
        :range-separator="currentLang === 'zh-CN' ? '至' : 'To'"
        style="width: 100%"
      />
      <template #footer>
        <el-button @click="showExportDateDialog = false">{{ currentLang === 'zh-CN' ? '取消' : 'Cancel' }}</el-button>
        <el-button type="primary" @click="exportByDate">{{ currentLang === 'zh-CN' ? '导出' : 'Export' }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="protectDialogVisible" :title="currentLang === 'zh-CN' ? '设置保护密码' : 'Set Protection Password'" width="400px">
      <el-form label-width="80px">
        <el-form-item :label="currentLang === 'zh-CN' ? '密码' : 'Password'">
          <el-input v-model="protectPassword" type="password" show-password />
        </el-form-item>
        <el-form-item :label="currentLang === 'zh-CN' ? '确认密码' : 'Confirm'">
          <el-input v-model="protectPasswordConfirm" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="protectDialogVisible = false">{{ currentLang === 'zh-CN' ? '取消' : 'Cancel' }}</el-button>
        <el-button type="primary" @click="confirmProtect">{{ currentLang === 'zh-CN' ? '确定' : 'Confirm' }}</el-button>
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

const activeTab = ref('logs')
const logs = ref<any[]>([])
const logsLoading = ref(false)
const logFilter = reactive({ module: '' })
const logDateRange = ref<[Date, Date] | null>(null)

const protectedPhotos = ref<any[]>([])
const albums = ref<any[]>([])
const selectedAlbumId = ref<number | null>(null)
const exportDateRange = ref<[Date, Date] | null>(null)
const showExportAlbumDialog = ref(false)
const showExportDateDialog = ref(false)
const protectDialogVisible = ref(false)
const protectPassword = ref('')
const protectPasswordConfirm = ref('')
const selectedPhotoIds = ref<number[]>([])

const API_BASE = 'http://localhost:8080/api'

const getPhotoUrl = (path: string | undefined) => {
  if (!path) return ''
  if (path.startsWith('http')) return path
  return `${API_BASE}${path}`
}

const fetchLogs = async () => {
  logsLoading.value = true
  try {
    const params: any = {}
    if (logFilter.module) params.module = logFilter.module
    if (logDateRange.value) {
      params.startTime = logDateRange.value[0]
      params.endTime = logDateRange.value[1]
    }
    const res = await request.get('/v1/logs', { params })
    logs.value = res.data || []
  } finally {
    logsLoading.value = false
  }
}

const clearLogs = async () => {
  try {
    await ElMessageBox.confirm(
      currentLang.value === 'zh-CN' ? '确定要清空所有日志吗？' : 'Clear all logs?',
      currentLang.value === 'zh-CN' ? '提示' : 'Confirm',
      { type: 'warning' }
    )
    await request.delete('/v1/logs')
    ElMessage.success(currentLang.value === 'zh-CN' ? '清空成功' : 'Cleared')
    fetchLogs()
  } catch (e) {}
}

const fetchProtectedPhotos = async () => {
  try {
    const res = await request.get('/v1/photos', { params: { isPrivate: 1 } })
    protectedPhotos.value = res.data?.records?.filter((p: any) => p.isPrivate === 1) || []
  } catch (e) {}
}

const fetchAlbums = async () => {
  try {
    const res = await request.get('/v1/albums')
    albums.value = res.data || []
  } catch (e) {}
}

const unprotectPhoto = async (photo: any) => {
  try {
    await request.post(`/v1/security/photos/${photo.id}/unprotect`)
    ElMessage.success(currentLang.value === 'zh-CN' ? '已取消保护' : 'Protection removed')
    fetchProtectedPhotos()
  } catch (e) {
    ElMessage.error(currentLang.value === 'zh-CN' ? '操作失败' : 'Failed')
  }
}

const exportAll = () => {
  window.open('/api/v1/export/all', '_blank')
}

const exportByAlbum = () => {
  if (!selectedAlbumId.value) {
    ElMessage.warning(currentLang.value === 'zh-CN' ? '请选择相册' : 'Please select album')
    return
  }
  window.open(`/api/v1/export/album/${selectedAlbumId.value}`, '_blank')
  showExportAlbumDialog.value = false
}

const exportByDate = () => {
  if (!exportDateRange.value) {
    ElMessage.warning(currentLang.value === 'zh-CN' ? '请选择时间范围' : 'Please select date range')
    return
  }
  const start = exportDateRange.value[0].toISOString()
  const end = exportDateRange.value[1].toISOString()
  window.open(`/api/v1/export/date-range?startTime=${start}&endTime=${end}`, '_blank')
  showExportDateDialog.value = false
}

const exportFavorites = () => {
  window.open('/api/v1/export/favorites', '_blank')
}

const confirmProtect = async () => {
  if (!protectPassword.value) {
    ElMessage.warning(currentLang.value === 'zh-CN' ? '请输入密码' : 'Please enter password')
    return
  }
  if (protectPassword.value !== protectPasswordConfirm.value) {
    ElMessage.warning(currentLang.value === 'zh-CN' ? '密码不一致' : 'Passwords do not match')
    return
  }
  try {
    await request.post('/v1/security/photos/batch-protect', {
      photoIds: selectedPhotoIds.value,
      password: protectPassword.value
    })
    ElMessage.success(currentLang.value === 'zh-CN' ? '保护成功' : 'Protected')
    protectDialogVisible.value = false
    fetchProtectedPhotos()
  } catch (e) {
    ElMessage.error(currentLang.value === 'zh-CN' ? '操作失败' : 'Failed')
  }
}

const formatDateTime = (date: string) => {
  if (!date) return '-'
  return new Date(date).toLocaleString()
}

onMounted(() => {
  fetchLogs()
  fetchProtectedPhotos()
  fetchAlbums()
})
</script>

<style scoped lang="scss">
.security-page {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
  h2 { margin: 0; }
}

.logs-section, .protect-section, .export-section {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
}

.filter-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
}

.export-card {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 30px 20px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  
  &:hover {
    background: #e6f0ff;
    transform: translateY(-4px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }
  
  .el-icon { color: #409eff; margin-bottom: 10px; }
  h4 { margin: 10px 0 5px; color: #303133; }
  p { margin: 0; color: #909399; font-size: 13px; }
}
</style>
