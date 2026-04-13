<template>
  <div class="admin-layout">
    <el-container>
      <el-aside width="200px">
        <div class="logo">
          <h2>管理后台</h2>
        </div>
        <el-menu
          :default-active="activeMenu"
          router
          class="side-menu"
        >
          <el-menu-item index="/admin/users">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/config">
            <el-icon><Setting /></el-icon>
            <span>系统配置</span>
          </el-menu-item>
          <el-menu-item index="/admin/storage">
            <el-icon><Coin /></el-icon>
            <span>存储监控</span>
          </el-menu-item>
          <el-menu-item index="/admin/announcements">
            <el-icon><Bell /></el-icon>
            <span>公告管理</span>
          </el-menu-item>
          <el-menu-item index="/photos">
            <el-icon><Back /></el-icon>
            <span>返回前台</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <el-container>
        <el-header>
          <div class="header-content">
            <div class="header-left">
              <h3>{{ pageTitle }}</h3>
            </div>
            <div class="header-right">
              <span class="admin-badge">管理员</span>
              <el-dropdown @command="handleCommand">
                <div class="user-info">
                  <el-avatar :size="32">
                    {{ userStore.user?.nickname?.charAt(0) }}
                  </el-avatar>
                  <span class="username">{{ userStore.user?.nickname }}</span>
                </div>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="logout">退出登录</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </el-header>

        <el-main>
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => {
  return route.path
})

const pageTitle = computed(() => {
  const titles: Record<string, string> = {
    '/admin/users': '用户管理',
    '/admin/config': '系统配置',
    '/admin/storage': '存储监控',
    '/admin/announcements': '公告管理',
  }
  return titles[route.path] || '管理后台'
})

const handleCommand = (command: string) => {
  if (command === 'logout') {
    userStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped lang="scss">
.admin-layout {
  height: 100%;
}

.el-container {
  height: 100%;
}

.el-aside {
  background-color: #1d1e1f;
  color: #fff;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #141414;

  h2 {
    color: #fff;
    font-size: 18px;
  }
}

.side-menu {
  border: none;
  background-color: #1d1e1f;

  :deep(.el-menu-item) {
    color: #a6adb4;

    &:hover {
      background-color: #141414;
    }

    &.is-active {
      color: #409eff;
      background-color: #141414;
    }
  }
}

.el-header {
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  padding: 0 20px;
}

.header-content {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-left {
  h3 {
    margin: 0;
    color: #333;
  }
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.admin-badge {
  background: #409eff;
  color: #fff;
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 12px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;

  .username {
    font-size: 14px;
    color: #333;
  }
}

.el-main {
  background-color: #f0f2f5;
  padding: 20px;
}
</style>
