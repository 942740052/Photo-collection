<template>
  <div class="page-container shares-page">
    <div class="page-header">
      <h2>分享管理</h2>
    </div>

    <el-table :data="shareStore.shareLinks" stripe style="width: 100%">
      <el-table-column prop="resource_name" label="资源" min-width="180" />
      <el-table-column prop="resource_type" label="类型" width="100">
        <template #default="{ row }">
          <el-tag :type="row.resource_type === 'album' ? 'warning' : 'primary'" size="small">
            {{ row.resource_type === 'album' ? '相册' : '照片' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="链接" min-width="200">
        <template #default="{ row }">
          <el-link type="primary" :underline="false" style="font-size: 12px">
            {{ shareBaseUrl }}/{{ row.token }}
          </el-link>
        </template>
      </el-table-column>
      <el-table-column label="密码" width="80">
        <template #default="{ row }">
          <el-tag v-if="row.password" type="warning" size="small">有</el-tag>
          <span v-else style="color: #c0c4cc">无</span>
        </template>
      </el-table-column>
      <el-table-column label="有效期" width="120">
        <template #default="{ row }">
          <span v-if="row.expires_at">{{ formatDate(row.expires_at) }}</span>
          <span v-else style="color: #67c23a">永久</span>
        </template>
      </el-table-column>
      <el-table-column prop="view_count" label="访问次数" width="100" />
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.is_active ? 'success' : 'info'" size="small">
            {{ row.is_active ? '有效' : '已撤销' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="copyLink(row)">复制链接</el-button>
          <el-button
            v-if="row.is_active"
            size="small"
            type="danger"
            @click="revokeShare(row.id)"
          >
            撤销
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div v-if="shareStore.shareLinks.length === 0" class="empty-state">
      <el-icon :size="64"><Share /></el-icon>
      <p>还没有分享链接</p>
    </div>
  </div>
</template>

<script setup>
import { useShareStore } from '../stores/share'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'

const shareStore = useShareStore()
const shareBaseUrl = window.location.origin + '/share'

const copyLink = (share) => {
  const link = `${shareBaseUrl}/${share.token}`
  navigator.clipboard?.writeText(link)
  ElMessage.success('链接已复制到剪贴板')
}

const revokeShare = async (shareId) => {
  try {
    await ElMessageBox.confirm('确定要撤销此分享链接吗？', '确认撤销', { type: 'warning' })
    shareStore.revokeShare(shareId)
    ElMessage.success('分享链接已撤销')
  } catch {}
}

const formatDate = (date) => dayjs(date).format('YYYY/MM/DD')
</script>

<style scoped>
.shares-page {
  max-width: 1200px;
  margin: 0 auto;
}
</style>
