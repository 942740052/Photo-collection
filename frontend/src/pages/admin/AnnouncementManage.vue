<template>
  <div class="announcement-page">
    <div class="page-header">
      <el-button type="primary" @click="handleCreate">
        <el-icon><Plus /></el-icon>
        新建公告
      </el-button>
    </div>

    <el-table v-loading="loading" :data="announcements" style="width: 100%">
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="type" label="类型" width="100">
        <template #default="{ row }">
          <el-tag :type="row.type === 'IMPORTANT' ? 'danger' : 'info'">
            {{ row.type === 'IMPORTANT' ? '重要' : '普通' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag v-if="row.status === 1" type="success">启用</el-tag>
          <el-tag v-else type="info">禁用</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="180">
        <template #default="{ row }">
          {{ formatDate(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑公告' : '新建公告'" width="600px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题" required>
          <el-input v-model="form.title" placeholder="请输入公告标题" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.type" style="width: 100%">
            <el-option label="普通" value="NORMAL" />
            <el-option label="重要" value="IMPORTANT" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="内容" required>
          <el-input v-model="form.content" type="textarea" :rows="5" placeholder="请输入公告内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '@/api/admin'
import type { Announcement } from '@/types/admin'

const loading = ref(false)
const saving = ref(false)
const announcements = ref<Announcement[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)

const form = reactive({
  id: null as number | null,
  title: '',
  content: '',
  type: 'NORMAL',
  status: 1,
})

const formatDate = (date: string) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

const fetchAnnouncements = async () => {
  loading.value = true
  try {
    const res = await adminApi.getAnnouncements()
    announcements.value = res.data
  } finally {
    loading.value = false
  }
}

const handleCreate = () => {
  isEdit.value = false
  form.id = null
  form.title = ''
  form.content = ''
  form.type = 'NORMAL'
  form.status = 1
  dialogVisible.value = true
}

const handleEdit = (announcement: Announcement) => {
  isEdit.value = true
  form.id = announcement.id
  form.title = announcement.title
  form.content = announcement.content
  form.type = announcement.type
  form.status = announcement.status
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!form.title || !form.content) {
    ElMessage.warning('请填写标题和内容')
    return
  }

  saving.value = true
  try {
    if (isEdit.value && form.id) {
      await adminApi.updateAnnouncement(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await adminApi.createAnnouncement(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    await fetchAnnouncements()
  } catch (error) {
    ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
  } finally {
    saving.value = false
  }
}

const handleDelete = async (announcement: Announcement) => {
  try {
    await ElMessageBox.confirm('确定要删除这条公告吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await adminApi.deleteAnnouncement(announcement.id)
    ElMessage.success('删除成功')
    await fetchAnnouncements()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  fetchAnnouncements()
})
</script>

<style scoped lang="scss">
.announcement-page {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
}

.page-header {
  margin-bottom: 20px;
}
</style>
