<template>
  <div class="page-container photo-detail-page">
    <div class="detail-layout">
      <div class="detail-viewer">
        <div class="viewer-toolbar">
          <el-button :icon="Back" @click="$router.back()">返回</el-button>
          <div class="viewer-actions">
            <el-button :icon="Star" :type="photo?.is_favorite ? 'warning' : ''" @click="toggleFavorite">
              {{ photo?.is_favorite ? '已收藏' : '收藏' }}
            </el-button>
            <el-button :icon="Download" @click="handleDownload">下载</el-button>
            <el-button :icon="Share" @click="handleShare">分享</el-button>
            <el-dropdown trigger="click" @command="handleMoreAction">
              <el-button :icon="MoreFilled">更多</el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="rotate-left">向左旋转90°</el-dropdown-item>
                  <el-dropdown-item command="rotate-right">向右旋转90°</el-dropdown-item>
                  <el-dropdown-item command="add-album">加入相册</el-dropdown-item>
                  <el-dropdown-item command="add-tag">添加标签</el-dropdown-item>
                  <el-dropdown-item command="archive" v-if="!photo?.is_archived">归档</el-dropdown-item>
                  <el-dropdown-item command="unarchive" v-if="photo?.is_archived">取消归档</el-dropdown-item>
                  <el-dropdown-item command="delete" divided style="color: #f56c6c">删除</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
        <div class="viewer-content" @click="toggleFullscreen">
          <img
            :src="photo?.thumbnail_large"
            :alt="photo?.file_name"
            :style="{ transform: `rotate(${rotation}deg)` }"
          />
        </div>
        <div class="viewer-nav">
          <el-button :icon="ArrowLeft" circle @click.stop="navigatePrev" :disabled="!hasPrev" />
          <span class="nav-info">{{ currentIndex + 1 }} / {{ totalCount }}</span>
          <el-button :icon="ArrowRight" circle @click.stop="navigateNext" :disabled="!hasNext" />
        </div>
      </div>

      <div class="detail-panel" :class="{ collapsed: panelCollapsed }">
        <div class="panel-toggle" @click="panelCollapsed = !panelCollapsed">
          <el-icon><ArrowRight v-if="panelCollapsed" /><ArrowLeft v-else /></el-icon>
        </div>
        <div v-if="!panelCollapsed" class="panel-content">
          <el-tabs v-model="activeTab">
            <el-tab-pane label="信息" name="info">
              <div class="photo-preview">
                <img :src="photo?.thumbnail_medium" :alt="photo?.file_name" />
              </div>
              <div class="meta-section">
                <h4>基本信息</h4>
                <div class="meta-item">
                  <span class="meta-label">文件名</span>
                  <span class="meta-value">{{ photo?.file_name }}</span>
                </div>
                <div class="meta-item">
                  <span class="meta-label">文件大小</span>
                  <span class="meta-value">{{ formatSize(photo?.file_size || 0) }}</span>
                </div>
                <div class="meta-item">
                  <span class="meta-label">分辨率</span>
                  <span class="meta-value">{{ photo?.width }} × {{ photo?.height }}</span>
                </div>
                <div class="meta-item">
                  <span class="meta-label">格式</span>
                  <span class="meta-value">{{ photo?.mime_type }}</span>
                </div>
                <div class="meta-item">
                  <span class="meta-label">拍摄时间</span>
                  <span class="meta-value">{{ formatDateTime(photo?.date_taken) }}</span>
                </div>
                <div class="meta-item">
                  <span class="meta-label">上传时间</span>
                  <span class="meta-value">{{ formatDateTime(photo?.date_uploaded) }}</span>
                </div>
              </div>

              <div class="meta-section" v-if="photo?.location_name">
                <h4>拍摄位置</h4>
                <div class="meta-item">
                  <span class="meta-label">地点</span>
                  <span class="meta-value">{{ photo.location_name }}</span>
                </div>
                <div class="meta-item" v-if="photo.latitude">
                  <span class="meta-label">坐标</span>
                  <span class="meta-value">{{ photo.latitude }}, {{ photo.longitude }}</span>
                </div>
              </div>

              <div class="meta-section" v-if="photo?.camera_model">
                <h4>相机信息</h4>
                <div class="meta-item">
                  <span class="meta-label">相机</span>
                  <span class="meta-value">{{ photo.camera_model }}</span>
                </div>
                <div class="meta-item" v-if="photo.lens_model">
                  <span class="meta-label">镜头</span>
                  <span class="meta-value">{{ photo.lens_model }}</span>
                </div>
                <div class="meta-item">
                  <span class="meta-label">焦距</span>
                  <span class="meta-value">{{ photo.focal_length }}mm</span>
                </div>
                <div class="meta-item">
                  <span class="meta-label">光圈</span>
                  <span class="meta-value">f/{{ photo.aperture }}</span>
                </div>
                <div class="meta-item">
                  <span class="meta-label">快门</span>
                  <span class="meta-value">{{ photo.shutter_speed }}s</span>
                </div>
                <div class="meta-item">
                  <span class="meta-label">ISO</span>
                  <span class="meta-value">{{ photo.iso }}</span>
                </div>
              </div>
            </el-tab-pane>

            <el-tab-pane label="标签" name="tags">
              <div class="tags-section">
                <div class="current-tags">
                  <el-tag
                    v-for="tag in photo?.tags"
                    :key="tag"
                    closable
                    @close="removeTag(tag)"
                    style="margin: 4px"
                  >
                    {{ tag }}
                  </el-tag>
                  <span v-if="!photo?.tags?.length" style="color: #909399; font-size: 13px">暂无标签</span>
                </div>
                <div class="add-tag">
                  <el-input
                    v-model="newTag"
                    placeholder="添加标签"
                    size="small"
                    @keyup.enter="addTag"
                  >
                    <template #append>
                      <el-button @click="addTag">添加</el-button>
                    </template>
                  </el-input>
                </div>
              </div>
            </el-tab-pane>

            <el-tab-pane label="评分" name="rating">
              <div class="rating-section">
                <el-rate
                  v-model="currentRating"
                  :colors="['#99A9BF', '#F7BA2A', '#FF9900']"
                  show-text
                  :texts="['很差', '一般', '还行', '不错', '优秀']"
                  @change="updateRating"
                />
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
      </div>
    </div>

    <el-dialog v-model="shareDialogVisible" title="分享照片" width="480px">
      <el-form label-width="80px">
        <el-form-item label="密码保护">
          <el-switch v-model="shareForm.hasPassword" />
        </el-form-item>
        <el-form-item v-if="shareForm.hasPassword" label="访问密码">
          <el-input v-model="shareForm.password" placeholder="设置访问密码" />
        </el-form-item>
        <el-form-item label="有效期">
          <el-select v-model="shareForm.expiry" style="width: 100%">
            <el-option label="1天" value="1d" />
            <el-option label="7天" value="7d" />
            <el-option label="30天" value="30d" />
            <el-option label="永久" value="never" />
          </el-select>
        </el-form-item>
        <el-form-item label="允许下载">
          <el-switch v-model="shareForm.allowDownload" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="shareDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="createShare">创建分享链接</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { usePhotoStore } from '../stores/photo'
import { useShareStore } from '../stores/share'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Back, Star, Download, Share } from '@element-plus/icons-vue'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()
const photoStore = usePhotoStore()
const shareStore = useShareStore()

const panelCollapsed = ref(false)
const activeTab = ref('info')
const rotation = ref(0)
const newTag = ref('')
const shareDialogVisible = ref(false)
const currentRating = ref(0)

const shareForm = ref({
  hasPassword: false,
  password: '',
  expiry: '7d',
  allowDownload: true
})

const photo = computed(() => photoStore.getPhoto(route.params.id))
const currentIndex = computed(() => {
  if (!photo.value) return -1
  return photoStore.filteredPhotos.findIndex(p => p.id === photo.value.id)
})
const totalCount = computed(() => photoStore.filteredPhotos.length)
const hasPrev = computed(() => currentIndex.value > 0)
const hasNext = computed(() => currentIndex.value < totalCount.value - 1)

onMounted(() => {
  if (photo.value) {
    currentRating.value = photo.value.rating
  }
  window.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeydown)
})

const handleKeydown = (e) => {
  if (e.key === 'ArrowLeft' && hasPrev.value) navigatePrev()
  if (e.key === 'ArrowRight' && hasNext.value) navigateNext()
  if (e.key === 'Escape') router.back()
  if (e.key === 'f' || e.key === 'F') toggleFullscreen()
}

const navigatePrev = () => {
  if (hasPrev.value) {
    const prevPhoto = photoStore.filteredPhotos[currentIndex.value - 1]
    router.push(`/photos/${prevPhoto.id}`)
  }
}

const navigateNext = () => {
  if (hasNext.value) {
    const nextPhoto = photoStore.filteredPhotos[currentIndex.value + 1]
    router.push(`/photos/${nextPhoto.id}`)
  }
}

const toggleFavorite = () => {
  if (photo.value) {
    photoStore.toggleFavorite(photo.value.id)
    ElMessage.success(photo.value.is_favorite ? '已收藏' : '已取消收藏')
  }
}

const toggleFullscreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
  } else {
    document.exitFullscreen()
  }
}

const handleDownload = () => {
  ElMessage.info('下载功能')
}

const handleShare = () => {
  shareDialogVisible.value = true
}

const createShare = () => {
  const expiryMap = { '1d': 1, '7d': 7, '30d': 30, 'never': null }
  const days = expiryMap[shareForm.value.expiry]
  shareStore.createShare({
    resource_type: 'photo',
    resource_id: photo.value.id,
    resource_name: photo.value.file_name,
    password: shareForm.value.hasPassword ? shareForm.value.password : null,
    expires_at: days ? dayjs().add(days, 'day').format('YYYY-MM-DD HH:mm:ss') : null,
    allow_download: shareForm.value.allowDownload
  })
  shareDialogVisible.value = false
  ElMessage.success('分享链接已创建')
}

const handleMoreAction = async (command) => {
  if (command === 'rotate-left') {
    rotation.value -= 90
  } else if (command === 'rotate-right') {
    rotation.value += 90
  } else if (command === 'delete') {
    try {
      await ElMessageBox.confirm('确定要删除这张照片吗？', '确认删除', { type: 'warning' })
      photoStore.deletePhotos([photo.value.id])
      ElMessage.success('已移入回收站')
      router.back()
    } catch {}
  } else if (command === 'add-album') {
    ElMessage.info('加入相册功能')
  } else if (command === 'add-tag') {
    activeTab.value = 'tags'
  } else if (command === 'archive') {
    photo.value.is_archived = true
    ElMessage.success('已归档')
  } else if (command === 'unarchive') {
    photo.value.is_archived = false
    ElMessage.success('已取消归档')
  }
}

const addTag = () => {
  if (newTag.value.trim() && photo.value) {
    photoStore.addTagToPhoto(photo.value.id, newTag.value.trim())
    photoStore.createTag(newTag.value.trim())
    newTag.value = ''
  }
}

const removeTag = (tag) => {
  if (photo.value) {
    photoStore.removeTagFromPhoto(photo.value.id, tag)
  }
}

const updateRating = (val) => {
  if (photo.value) {
    photoStore.updatePhotoRating(photo.value.id, val)
  }
}

function formatSize(bytes) {
  if (!bytes) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(1024))
  return (bytes / Math.pow(1024, i)).toFixed(1) + ' ' + units[i]
}

function formatDateTime(date) {
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}
</script>

<style scoped>
.photo-detail-page {
  padding: 0 !important;
  height: calc(100vh - 56px);
  overflow: hidden;
}

.detail-layout {
  display: flex;
  height: 100%;
}

.detail-viewer {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #1a1a1a;
  min-width: 0;
}

.viewer-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 16px;
  background: #2c2c2c;
  color: #fff;
}

.viewer-toolbar .el-button {
  color: #fff;
  background: transparent;
  border-color: #555;
}

.viewer-toolbar .el-button:hover {
  background: #444;
  border-color: #777;
}

.viewer-actions {
  display: flex;
  gap: 8px;
}

.viewer-content {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: zoom-in;
  overflow: hidden;
}

.viewer-content img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  transition: transform 0.3s;
}

.viewer-nav {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  padding: 12px;
  background: #2c2c2c;
}

.nav-info {
  color: #ccc;
  font-size: 14px;
  min-width: 60px;
  text-align: center;
}

.detail-panel {
  width: 360px;
  background: #fff;
  border-left: 1px solid #e4e7ed;
  position: relative;
  transition: width 0.3s;
  overflow: hidden;
}

.detail-panel.collapsed {
  width: 40px;
}

.panel-toggle {
  position: absolute;
  top: 50%;
  left: 0;
  transform: translateY(-50%);
  width: 40px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  background: #f5f7fa;
  border-right: 1px solid #e4e7ed;
  z-index: 1;
}

.panel-toggle:hover {
  background: #e4e7ed;
}

.panel-content {
  padding: 16px;
  overflow-y: auto;
  height: 100%;
  margin-left: 40px;
}

.photo-preview {
  margin-bottom: 16px;
}

.photo-preview img {
  width: 100%;
  border-radius: 8px;
}

.meta-section {
  margin-bottom: 20px;
}

.meta-section h4 {
  margin: 0 0 8px;
  font-size: 14px;
  color: #303133;
  padding-bottom: 6px;
  border-bottom: 1px solid #f0f0f0;
}

.meta-item {
  display: flex;
  padding: 6px 0;
  font-size: 13px;
}

.meta-label {
  min-width: 70px;
  color: #909399;
  flex-shrink: 0;
}

.meta-value {
  color: #303133;
  word-break: break-all;
}

.tags-section {
  padding: 8px 0;
}

.current-tags {
  margin-bottom: 12px;
  min-height: 32px;
}

.add-tag {
  margin-top: 8px;
}

.rating-section {
  padding: 20px 0;
  text-align: center;
}

@media (max-width: 768px) {
  .detail-panel {
    display: none;
  }
}
</style>
