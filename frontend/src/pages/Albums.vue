<template>
  <div class="albums-page">
    <div class="page-header">
      <h2>{{ t('menu.albums') }}</h2>
      <el-button type="primary" @click="handleCreate">
        <el-icon><Plus /></el-icon>
        {{ t('album.create') }}
      </el-button>
    </div>

    <el-empty v-if="!loading && albums.length === 0" :description="currentLang === 'zh-CN' ? '暂无相册' : 'No albums yet'">
      <el-button type="primary" @click="handleCreate">
        <el-icon><Plus /></el-icon>
        {{ t('album.create') }}
      </el-button>
    </el-empty>

    <div v-else v-loading="loading" class="album-grid">
      <div v-for="album in albums" :key="album.id" class="album-card" @click="handleViewAlbum(album)">
        <div class="album-cover">
          <el-image :src="getCoverUrl(album.coverPhotoUrl)" fit="cover" class="cover-image">
            <template #error>
              <div class="image-error">
                <el-icon :size="50"><Folder /></el-icon>
              </div>
            </template>
          </el-image>
          <div class="photo-count">{{ album.photoCount }} {{ currentLang === 'zh-CN' ? '张照片' : 'photos' }}</div>
        </div>
        <div class="album-info">
          <div class="album-name">{{ album.name }}</div>
          <div class="album-desc">{{ album.description || (currentLang === 'zh-CN' ? '暂无描述' : 'No description') }}</div>
          <div class="album-actions">
            <el-button text size="small" type="primary" @click.stop="handleShare(album)">
              <el-icon><Share /></el-icon>
              {{ t('photo.share') }}
            </el-button>
            <el-button text size="small" @click.stop="handleEdit(album)">
              {{ t('common.edit') }}
            </el-button>
            <el-button text size="small" type="danger" @click.stop="handleDelete(album)">
              {{ t('common.delete') }}
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? t('album.edit') : t('album.create')" width="500px">
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="80px">
        <el-form-item :label="t('album.name')" prop="name">
          <el-input v-model="formData.name" :placeholder="currentLang === 'zh-CN' ? '请输入相册名称' : 'Enter album name'" />
        </el-form-item>
        <el-form-item :label="t('album.description')" prop="description">
          <el-input v-model="formData.description" type="textarea" :rows="3" :placeholder="currentLang === 'zh-CN' ? '请输入相册描述' : 'Enter album description'" />
        </el-form-item>
        <el-form-item :label="currentLang === 'zh-CN' ? '是否公开' : 'Public'">
          <el-switch v-model="formData.isPublic" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">{{ t('common.confirm') }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="shareDialogVisible" :title="t('share.create')" width="450px">
      <el-form :model="shareForm" label-width="100px">
        <el-form-item :label="currentLang === 'zh-CN' ? '访问密码' : 'Password'">
          <el-input v-model="shareForm.password" :placeholder="currentLang === 'zh-CN' ? '留空则无需密码' : 'Leave empty for no password'" />
        </el-form-item>
        <el-form-item :label="currentLang === 'zh-CN' ? '有效期' : 'Expire Time'">
          <el-select v-model="shareForm.expireHours" style="width: 100%">
            <el-option :label="currentLang === 'zh-CN' ? '永久有效' : 'Never expire'" :value="0" />
            <el-option :label="currentLang === 'zh-CN' ? '1小时' : '1 hour'" :value="1" />
            <el-option :label="currentLang === 'zh-CN' ? '24小时' : '24 hours'" :value="24" />
            <el-option :label="currentLang === 'zh-CN' ? '7天' : '7 days'" :value="168" />
            <el-option :label="currentLang === 'zh-CN' ? '30天' : '30 days'" :value="720" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="shareDialogVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="shareLoading" @click="handleCreateShare">{{ t('common.confirm') }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="shareResultVisible" :title="currentLang === 'zh-CN' ? '分享成功' : 'Share Created'" width="450px">
      <div class="share-result">
        <el-form label-width="100px">
          <el-form-item :label="t('share.shareLink')">
            <el-input :value="shareResult.link" readonly>
              <template #append>
                <el-button @click="copyShareLink">{{ currentLang === 'zh-CN' ? '复制' : 'Copy' }}</el-button>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item v-if="shareResult.password" :label="t('share.password')">
            <el-input :value="shareResult.password" readonly>
              <template #append>
                <el-button @click="copyPassword">{{ currentLang === 'zh-CN' ? '复制' : 'Copy' }}</el-button>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item v-if="shareResult.expireAt" :label="t('share.expireTime')">
            <span>{{ formatDateTime(shareResult.expireAt) }}</span>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button type="primary" @click="shareResultVisible = false">{{ t('common.confirm') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useAlbumStore } from '@/stores/album'
import { shareApi } from '@/api/share'
import type { Album } from '@/types/album'

const { t, locale } = useI18n()
const router = useRouter()
const albumStore = useAlbumStore()

const currentLang = computed(() => locale.value)

const API_BASE = 'http://localhost:8080/api'

const getCoverUrl = (path: string | undefined) => {
  if (!path) return ''
  if (path.startsWith('http')) return path
  return `${API_BASE}${path}`
}

const loading = ref(false)
const albums = ref<Album[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref<FormInstance>()

const formData = reactive({
  id: null as number | null,
  name: '',
  description: '',
  isPublic: false,
})

const rules = computed<FormRules>(() => ({
  name: [
    { required: true, message: currentLang.value === 'zh-CN' ? '请输入相册名称' : 'Please enter album name', trigger: 'blur' },
  ],
}))

const shareDialogVisible = ref(false)
const shareLoading = ref(false)
const shareForm = reactive({
  albumId: null as number | null,
  password: '',
  expireHours: 0,
})

const shareResultVisible = ref(false)
const shareResult = reactive({
  link: '',
  password: '',
  expireAt: '',
})

const fetchAlbums = async () => {
  loading.value = true
  try {
    const result = await albumStore.fetchAlbums()
    albums.value = result
  } finally {
    loading.value = false
  }
}

const handleCreate = () => {
  isEdit.value = false
  formData.id = null
  formData.name = ''
  formData.description = ''
  formData.isPublic = false
  dialogVisible.value = true
}

const handleViewAlbum = (album: Album) => {
  router.push(`/albums/${album.id}`)
}

const handleEdit = (album: Album) => {
  isEdit.value = true
  formData.id = album.id
  formData.name = album.name
  formData.description = album.description || ''
  formData.isPublic = album.isPublic === 1
  dialogVisible.value = true
}

const handleDelete = async (album: Album) => {
  try {
    await ElMessageBox.confirm(
      currentLang.value === 'zh-CN' ? '确定要删除这个相册吗？' : 'Are you sure you want to delete this album?',
      currentLang.value === 'zh-CN' ? '提示' : 'Confirm',
      { confirmButtonText: currentLang.value === 'zh-CN' ? '确定' : 'Confirm', cancelButtonText: currentLang.value === 'zh-CN' ? '取消' : 'Cancel', type: 'warning' }
    )
    await albumStore.deleteAlbum(album.id)
    ElMessage.success(currentLang.value === 'zh-CN' ? '删除成功' : 'Deleted successfully')
    await fetchAlbums()
  } catch (error) {
    if (error !== 'cancel') ElMessage.error(currentLang.value === 'zh-CN' ? '删除失败' : 'Delete failed')
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        const data = { name: formData.name, description: formData.description, isPublic: formData.isPublic ? 1 : 0 }
        if (isEdit.value && formData.id) {
          await albumStore.updateAlbum(formData.id, data)
          ElMessage.success(currentLang.value === 'zh-CN' ? '更新成功' : 'Updated successfully')
        } else {
          await albumStore.createAlbum(data)
          ElMessage.success(currentLang.value === 'zh-CN' ? '创建成功' : 'Created successfully')
        }
        dialogVisible.value = false
        await fetchAlbums()
      } catch (error) {
        ElMessage.error(isEdit.value ? (currentLang.value === 'zh-CN' ? '更新失败' : 'Update failed') : (currentLang.value === 'zh-CN' ? '创建失败' : 'Create failed'))
      } finally {
        submitLoading.value = false
      }
    }
  })
}

const handleShare = (album: Album) => {
  shareForm.albumId = album.id
  shareForm.password = ''
  shareForm.expireHours = 0
  shareDialogVisible.value = true
}

const handleCreateShare = async () => {
  shareLoading.value = true
  try {
    const result = await shareApi.shareAlbum(shareForm.albumId!, {
      password: shareForm.password || undefined,
      expireHours: shareForm.expireHours || undefined,
    })
    shareResult.link = `${window.location.origin}/s/${result.data.shareCode}`
    shareResult.password = shareForm.password
    shareResult.expireAt = result.data.expireAt
    shareDialogVisible.value = false
    shareResultVisible.value = true
  } catch (error) {
    ElMessage.error(currentLang.value === 'zh-CN' ? '分享失败' : 'Share failed')
  } finally {
    shareLoading.value = false
  }
}

const copyShareLink = () => {
  navigator.clipboard.writeText(shareResult.link)
  ElMessage.success(currentLang.value === 'zh-CN' ? '链接已复制' : 'Link copied')
}

const copyPassword = () => {
  navigator.clipboard.writeText(shareResult.password)
  ElMessage.success(currentLang.value === 'zh-CN' ? '密码已复制' : 'Password copied')
}

const formatDateTime = (date: string) => {
  if (!date) return ''
  return new Date(date).toLocaleString()
}

onMounted(() => {
  fetchAlbums()
})
</script>

<style scoped lang="scss">
.albums-page {
  height: 100%;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  h2 { margin: 0; color: #333; }
}

.album-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
}

.album-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  
  &:hover {
    transform: translateY(-6px);
    box-shadow: 0 12px 30px rgba(0, 0, 0, 0.15);
    
    .cover-image {
      transform: scale(1.05);
    }
  }
}

.album-cover {
  position: relative;
  height: 200px;
  overflow: hidden;
}

.cover-image {
  width: 100%;
  height: 100%;
  transition: transform 0.3s ease;
}

.image-error {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
  color: #999;
}

.photo-count {
  position: absolute;
  bottom: 10px;
  right: 10px;
  background: rgba(0, 0, 0, 0.6);
  color: #fff;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.album-info {
  padding: 16px;
}

.album-name {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  margin-bottom: 8px;
}

.album-desc {
  font-size: 14px;
  color: #666;
  margin-bottom: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.album-actions {
  display: flex;
  gap: 5px;
}

.share-result {
  padding: 10px 0;
}
</style>
