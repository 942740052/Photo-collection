<template>
  <div class="my-shares-page">
    <div class="page-header">
      <h2>我的分享</h2>
    </div>

    <el-table v-loading="loading" :data="shares" style="width: 100%">
      <el-table-column prop="shareCode" label="分享码" width="120" />
      <el-table-column label="分享类型" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.photoId" type="primary">照片</el-tag>
          <el-tag v-else-if="row.albumId" type="success">相册</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="分享内容">
        <template #default="{ row }">
          {{ row.photoTitle || row.albumName || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="密码保护" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.hasPassword" type="warning">已设置</el-tag>
          <el-tag v-else type="info">无</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="viewCount" label="浏览次数" width="100" />
      <el-table-column label="有效期" width="180">
        <template #default="{ row }">
          {{ row.expireAt ? formatDate(row.expireAt) : '永久有效' }}
        </template>
      </el-table-column>
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag v-if="row.isActive === 1" type="success">有效</el-tag>
          <el-tag v-else type="danger">已失效</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" width="180">
        <template #default="{ row }">
          {{ formatDate(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button size="small" @click="copyShareUrl(row)">复制链接</el-button>
          <el-button size="small" type="danger" @click="handleCancel(row)">取消</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-if="!loading && shares.length === 0" description="暂无分享记录" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useShareStore } from '@/stores/share'
import type { Share } from '@/types/share'

const shareStore = useShareStore()

const loading = ref(false)
const shares = ref<Share[]>([])

const fetchShares = async () => {
  loading.value = true
  try {
    shares.value = await shareStore.fetchShares()
  } finally {
    loading.value = false
  }
}

const formatDate = (date: string) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

const copyShareUrl = (share: Share) => {
  const url = `${window.location.origin}/share/${share.shareCode}`
  navigator.clipboard.writeText(url)
  ElMessage.success('链接已复制到剪贴板')
}

const handleCancel = async (share: Share) => {
  try {
    await ElMessageBox.confirm('确定要取消这个分享吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await shareStore.cancelShare(share.shareCode)
    ElMessage.success('分享已取消')
    await fetchShares()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('取消分享失败')
    }
  }
}

onMounted(() => {
  fetchShares()
})
</script>

<style scoped lang="scss">
.my-shares-page {
  padding: 20px;
  background: #fff;
  border-radius: 8px;
}

.page-header {
  margin-bottom: 20px;

  h2 {
    margin: 0;
    color: #333;
  }
}
</style>
