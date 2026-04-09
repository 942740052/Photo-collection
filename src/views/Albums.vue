<template>
  <div class="page-container albums-page">
    <div class="page-header">
      <h2>相册</h2>
      <el-button type="primary" :icon="Plus" @click="showCreateDialog = true">创建相册</el-button>
    </div>

    <div class="card-grid" v-if="photoStore.albums.length > 0">
      <el-card
        v-for="album in photoStore.albums"
        :key="album.id"
        class="album-card"
        shadow="hover"
        @click="$router.push(`/albums/${album.id}`)"
      >
        <img
          :src="album.cover_url || 'https://picsum.photos/seed/default/400/300'"
          :alt="album.name"
          class="album-cover"
        />
        <div class="album-info">
          <div class="album-name">
            {{ album.name }}
            <el-tag v-if="album.type === 'smart'" size="small" type="warning" style="margin-left: 6px">智能</el-tag>
          </div>
          <div class="album-meta">
            {{ album.photo_count }} 张照片 · {{ formatDate(album.updated_at) }}
          </div>
        </div>
        <div class="album-actions" @click.stop>
          <el-dropdown trigger="click" @command="handleAlbumAction($event, album)">
            <el-button :icon="MoreFilled" size="small" circle />
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="edit">编辑</el-dropdown-item>
                <el-dropdown-item command="share">分享</el-dropdown-item>
                <el-dropdown-item command="delete" style="color: #f56c6c">删除</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-card>
    </div>

    <div v-else class="empty-state">
      <el-icon :size="64"><Folder /></el-icon>
      <p>还没有相册</p>
      <el-button type="primary" @click="showCreateDialog = true">创建相册</el-button>
    </div>

    <el-dialog v-model="showCreateDialog" :title="editingAlbum ? '编辑相册' : '创建相册'" width="480px">
      <el-form :model="albumForm" label-width="80px">
        <el-form-item label="名称">
          <el-input v-model="albumForm.name" placeholder="相册名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="albumForm.description" type="textarea" :rows="3" placeholder="相册描述（可选）" />
        </el-form-item>
        <el-form-item label="类型">
          <el-radio-group v-model="albumForm.type">
            <el-radio value="normal">普通相册</el-radio>
            <el-radio value="smart">智能相册</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="albumForm.type === 'smart'" label="规则标签">
          <el-select v-model="albumForm.smartTags" multiple placeholder="选择标签">
            <el-option
              v-for="tag in photoStore.tags"
              :key="tag.id"
              :label="tag.name"
              :value="tag.name"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="closeCreateDialog">取消</el-button>
        <el-button type="primary" @click="saveAlbum">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { usePhotoStore } from '../stores/photo'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import dayjs from 'dayjs'

const photoStore = usePhotoStore()

const showCreateDialog = ref(false)
const editingAlbum = ref(null)

const albumForm = reactive({
  name: '',
  description: '',
  type: 'normal',
  smartTags: []
})

const closeCreateDialog = () => {
  showCreateDialog.value = false
  editingAlbum.value = null
  albumForm.name = ''
  albumForm.description = ''
  albumForm.type = 'normal'
  albumForm.smartTags = []
}

const saveAlbum = () => {
  if (!albumForm.name.trim()) {
    ElMessage.warning('请输入相册名称')
    return
  }
  if (editingAlbum.value) {
    photoStore.updateAlbum(editingAlbum.value.id, {
      name: albumForm.name,
      description: albumForm.description,
      smart_rules: albumForm.type === 'smart' ? { tags: albumForm.smartTags } : null
    })
    ElMessage.success('相册已更新')
  } else {
    photoStore.createAlbum({
      name: albumForm.name,
      description: albumForm.description,
      type: albumForm.type,
      smart_rules: albumForm.type === 'smart' ? { tags: albumForm.smartTags } : null
    })
    ElMessage.success('相册已创建')
  }
  closeCreateDialog()
}

const handleAlbumAction = async (command, album) => {
  if (command === 'edit') {
    editingAlbum.value = album
    albumForm.name = album.name
    albumForm.description = album.description
    albumForm.type = album.type
    albumForm.smartTags = album.smart_rules?.tags || []
    showCreateDialog.value = true
  } else if (command === 'share') {
    ElMessage.info('分享功能')
  } else if (command === 'delete') {
    try {
      await ElMessageBox.confirm(`确定要删除相册「${album.name}」吗？`, '确认删除', { type: 'warning' })
      photoStore.deleteAlbum(album.id)
      ElMessage.success('相册已删除')
    } catch {}
  }
}

const formatDate = (date) => {
  return dayjs(date).format('YYYY/MM/DD')
}
</script>

<style scoped>
.albums-page {
  max-width: 1400px;
  margin: 0 auto;
}

.album-info {
  padding: 12px;
}

.album-name {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
}

.album-meta {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.album-actions {
  position: absolute;
  top: 8px;
  right: 8px;
  opacity: 0;
  transition: opacity 0.2s;
}

.album-card:hover .album-actions {
  opacity: 1;
}

.album-card {
  position: relative;
}
</style>
