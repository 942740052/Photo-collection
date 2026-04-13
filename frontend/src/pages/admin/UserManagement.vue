<template>
  <div class="user-management-page">
    <div class="page-header">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索用户名、邮箱、昵称"
        style="width: 300px"
        clearable
        @keyup.enter="handleSearch"
      >
        <template #append>
          <el-button icon="Search" @click="handleSearch" />
        </template>
      </el-input>
      <el-select v-model="statusFilter" placeholder="状态筛选" clearable style="width: 120px; margin-left: 10px">
        <el-option label="正常" :value="1" />
        <el-option label="禁用" :value="0" />
      </el-select>
    </div>

    <el-table v-loading="loading" :data="users" style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="nickname" label="昵称" width="120" />
      <el-table-column prop="email" label="邮箱" width="200" />
      <el-table-column prop="role" label="角色" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.role === 'ADMIN'" type="danger">管理员</el-tag>
          <el-tag v-else>普通用户</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag v-if="row.status === 1" type="success">正常</el-tag>
          <el-tag v-else type="danger">禁用</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="photoCount" label="照片数" width="100" />
      <el-table-column prop="createdAt" label="注册时间" width="180">
        <template #default="{ row }">
          {{ formatDate(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button
            v-if="row.role !== 'ADMIN'"
            size="small"
            :type="row.status === 1 ? 'warning' : 'success'"
            @click="handleToggleStatus(row)"
          >
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
          <el-button
            v-if="row.role !== 'ADMIN'"
            size="small"
            type="danger"
            @click="handleDelete(row)"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="pageNum"
      v-model:page-size="pageSize"
      :total="total"
      :page-sizes="[10, 20, 50, 100]"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="handleSizeChange"
      @current-change="handlePageChange"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '@/api/admin'
import type { UserManage } from '@/types/admin'

const loading = ref(false)
const users = ref<UserManage[]>([])
const pageNum = ref(1)
const pageSize = ref(20)
const total = ref(0)
const searchKeyword = ref('')
const statusFilter = ref<number | undefined>()

const fetchUsers = async () => {
  loading.value = true
  try {
    const res = await adminApi.getUserList({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: searchKeyword.value || undefined,
      status: statusFilter.value,
    })
    users.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const formatDate = (date: string) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

const handleSearch = () => {
  pageNum.value = 1
  fetchUsers()
}

const handleSizeChange = () => {
  pageNum.value = 1
  fetchUsers()
}

const handlePageChange = () => {
  fetchUsers()
}

const handleToggleStatus = async (user: UserManage) => {
  const newStatus = user.status === 1 ? 0 : 1
  const action = newStatus === 0 ? '禁用' : '启用'

  try {
    await ElMessageBox.confirm(`确定要${action}用户 "${user.username}" 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await adminApi.updateUserStatus(user.id, newStatus)
    ElMessage.success(`${action}成功`)
    await fetchUsers()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(`${action}失败`)
    }
  }
}

const handleDelete = async (user: UserManage) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户 "${user.username}" 吗？此操作将删除该用户的所有数据，不可恢复！`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    await adminApi.deleteUser(user.id)
    ElMessage.success('删除成功')
    await fetchUsers()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  fetchUsers()
})
</script>

<style scoped lang="scss">
.user-management-page {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
}

.page-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.el-pagination {
  margin-top: 20px;
  justify-content: center;
}
</style>
