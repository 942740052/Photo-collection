<template>
  <div class="page-container admin-page">
    <el-tabs v-model="activeTab" type="border-card">
      <el-tab-pane label="用户管理" name="users">
        <div style="margin-bottom: 16px; display: flex; justify-content: space-between">
          <el-input v-model="userSearch" placeholder="搜索用户" style="width: 300px" clearable />
          <el-button type="primary" :icon="Plus" @click="showCreateUser = true">创建用户</el-button>
        </div>

        <el-table :data="filteredUsers" stripe>
          <el-table-column prop="username" label="用户名" width="120" />
          <el-table-column prop="display_name" label="显示名称" width="120" />
          <el-table-column prop="email" label="邮箱" min-width="180" />
          <el-table-column prop="role" label="角色" width="100">
            <template #default="{ row }">
              <el-tag :type="row.role === 'admin' ? 'danger' : ''" size="small">
                {{ row.role === 'admin' ? '管理员' : '用户' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="row.status === 'active' ? 'success' : 'danger'" size="small">
                {{ row.status === 'active' ? '正常' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="存储" width="120">
            <template #default="{ row }">
              {{ formatSize(row.storage_used) }} / {{ formatSize(row.storage_quota) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button size="small" @click="editUser(row)">编辑</el-button>
              <el-button
                size="small"
                :type="row.status === 'active' ? 'warning' : 'success'"
                @click="toggleUserStatus(row)"
              >
                {{ row.status === 'active' ? '禁用' : '启用' }}
              </el-button>
              <el-button size="small" type="danger" @click="deleteUser(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="系统监控" name="monitor">
        <el-row :gutter="16">
          <el-col :span="6">
            <el-card shadow="hover" class="stat-card">
              <el-icon :size="28" color="#409eff"><Monitor /></el-icon>
              <div class="stat-value">运行中</div>
              <div class="stat-label">系统状态</div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover" class="stat-card">
              <el-icon :size="28" color="#67c23a"><User /></el-icon>
              <div class="stat-value">{{ users.length }}</div>
              <div class="stat-label">注册用户</div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover" class="stat-card">
              <el-icon :size="28" color="#e6a23c"><Picture /></el-icon>
              <div class="stat-value">{{ photoStore.photos.length }}</div>
              <div class="stat-label">照片总数</div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover" class="stat-card">
              <el-icon :size="28" color="#f56c6c"><Coin /></el-icon>
              <div class="stat-value">{{ formatSize(totalStorage) }}</div>
              <div class="stat-label">存储使用</div>
            </el-card>
          </el-col>
        </el-row>

        <el-card style="margin-top: 16px">
          <template #header><span>服务状态</span></template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="Web 服务">
              <el-tag type="success" size="small">运行中</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="数据库">
              <el-tag type="success" size="small">运行中</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="AI 服务">
              <el-tag type="warning" size="small">CPU 模式</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="存储服务">
              <el-tag type="success" size="small">运行中</el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="审计日志" name="logs">
        <el-table :data="auditLogs" stripe>
          <el-table-column prop="time" label="时间" width="180" />
          <el-table-column prop="user" label="用户" width="120" />
          <el-table-column prop="action" label="操作" width="120" />
          <el-table-column prop="resource" label="资源" min-width="200" />
          <el-table-column prop="ip" label="IP" width="140" />
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="showCreateUser" :title="editingUser ? '编辑用户' : '创建用户'" width="480px">
      <el-form :model="userForm" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="userForm.username" :disabled="!!editingUser" />
        </el-form-item>
        <el-form-item label="显示名称">
          <el-input v-model="userForm.displayName" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="userForm.email" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="userForm.role">
            <el-option label="管理员" value="admin" />
            <el-option label="用户" value="user" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="!editingUser" label="密码">
          <el-input v-model="userForm.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="存储配额">
          <el-input-number v-model="userForm.storageQuotaGB" :min="1" :max="10000" />
          <span style="margin-left: 8px">GB</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateUser = false">取消</el-button>
        <el-button type="primary" @click="saveUser">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useUserStore } from '../stores/user'
import { usePhotoStore } from '../stores/photo'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import dayjs from 'dayjs'

const userStore = useUserStore()
const photoStore = usePhotoStore()

const activeTab = ref('users')
const userSearch = ref('')
const showCreateUser = ref(false)
const editingUser = ref(null)
const users = ref([])

const userForm = reactive({
  username: '',
  displayName: '',
  email: '',
  role: 'user',
  password: '',
  storageQuotaGB: 50
})

const filteredUsers = computed(() => {
  if (!userSearch.value) return users.value
  const q = userSearch.value.toLowerCase()
  return users.value.filter(u =>
    u.username.toLowerCase().includes(q) ||
    u.display_name.toLowerCase().includes(q)
  )
})

const totalStorage = computed(() =>
  users.value.reduce((sum, u) => sum + u.storage_used, 0)
)

const auditLogs = computed(() => [
  { time: dayjs().subtract(1, 'hour').format('YYYY-MM-DD HH:mm:ss'), user: 'admin', action: '登录', resource: '系统', ip: '192.168.1.100' },
  { time: dayjs().subtract(2, 'hour').format('YYYY-MM-DD HH:mm:ss'), user: 'admin', action: '上传', resource: 'IMG_001.jpg', ip: '192.168.1.100' },
  { time: dayjs().subtract(3, 'hour').format('YYYY-MM-DD HH:mm:ss'), user: 'zhangsan', action: '登录', resource: '系统', ip: '192.168.1.101' },
  { time: dayjs().subtract(5, 'hour').format('YYYY-MM-DD HH:mm:ss'), user: 'admin', action: '删除', resource: 'IMG_old.jpg', ip: '192.168.1.100' },
  { time: dayjs().subtract(1, 'day').format('YYYY-MM-DD HH:mm:ss'), user: 'lisi', action: '分享', resource: '旅行相册', ip: '192.168.1.102' }
])

onMounted(async () => {
  users.value = await userStore.fetchUsers()
})

const editUser = (user) => {
  editingUser.value = user
  userForm.username = user.username
  userForm.displayName = user.display_name
  userForm.email = user.email
  userForm.role = user.role
  userForm.storageQuotaGB = Math.round(user.storage_quota / (1024 * 1024 * 1024))
  showCreateUser.value = true
}

const toggleUserStatus = async (user) => {
  const newStatus = user.status === 'active' ? 'disabled' : 'active'
  await userStore.updateUser(user.id, { status: newStatus })
  user.status = newStatus
  ElMessage.success(newStatus === 'active' ? '用户已启用' : '用户已禁用')
}

const deleteUser = async (user) => {
  try {
    await ElMessageBox.confirm(`确定要删除用户「${user.display_name}」吗？`, '确认删除', { type: 'warning' })
    await userStore.deleteUser(user.id)
    users.value = users.value.filter(u => u.id !== user.id)
    ElMessage.success('用户已删除')
  } catch {}
}

const saveUser = () => {
  if (!userForm.username) {
    ElMessage.warning('请输入用户名')
    return
  }
  if (editingUser.value) {
    userStore.updateUser(editingUser.value.id, {
      display_name: userForm.displayName,
      email: userForm.email,
      role: userForm.role,
      storage_quota: userForm.storageQuotaGB * 1024 * 1024 * 1024
    })
    const idx = users.value.findIndex(u => u.id === editingUser.value.id)
    if (idx !== -1) {
      users.value[idx].display_name = userForm.displayName
      users.value[idx].email = userForm.email
      users.value[idx].role = userForm.role
    }
    ElMessage.success('用户已更新')
  } else {
    ElMessage.success('用户已创建')
  }
  showCreateUser.value = false
  editingUser.value = null
}

function formatSize(bytes) {
  if (!bytes) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(1024))
  return (bytes / Math.pow(1024, i)).toFixed(1) + ' ' + units[i]
}
</script>

<style scoped>
.admin-page {
  max-width: 1200px;
  margin: 0 auto;
}

.stat-card {
  text-align: center;
  padding: 12px;
}

.stat-card .stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin: 8px 0 4px;
}

.stat-card .stat-label {
  font-size: 13px;
  color: #909399;
}
</style>
