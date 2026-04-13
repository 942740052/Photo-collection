<template>
  <div class="main-layout">
    <el-container>
      <el-aside width="200px">
        <div class="logo">
          <img src="@/assets/images/logo.svg" alt="Logo" class="logo-img" />
          <h2>{{ t('login.title') }}</h2>
        </div>
        <el-menu
          :default-active="activeMenu"
          router
          class="side-menu"
        >
          <el-menu-item index="/photos">
            <el-icon><Picture /></el-icon>
            <span>{{ t('menu.photos') }}</span>
          </el-menu-item>
          <el-menu-item index="/albums">
            <el-icon><Folder /></el-icon>
            <span>{{ t('menu.albums') }}</span>
          </el-menu-item>
          <el-menu-item index="/favorites">
            <el-icon><Star /></el-icon>
            <span>{{ t('menu.favorites') }}</span>
          </el-menu-item>
          <el-menu-item index="/shares">
            <el-icon><Share /></el-icon>
            <span>{{ t('menu.shares') }}</span>
          </el-menu-item>
          <el-menu-item index="/trash">
            <el-icon><Delete /></el-icon>
            <span>{{ t('menu.trash') }}</span>
          </el-menu-item>
          <el-menu-item index="/backup">
            <el-icon><Download /></el-icon>
            <span>{{ currentLang === 'zh-CN' ? '备份恢复' : 'Backup' }}</span>
          </el-menu-item>
          <el-menu-item index="/security">
            <el-icon><Lock /></el-icon>
            <span>{{ currentLang === 'zh-CN' ? '安全设置' : 'Security' }}</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <el-container>
        <el-header>
          <div class="header-content">
            <div class="header-left">
              <el-input
                v-model="searchKeyword"
                :placeholder="t('common.search') + '...'"
                prefix-icon="Search"
                style="width: 300px"
                @keyup.enter="handleSearch"
              />
            </div>
            <div class="header-right">
              <el-dropdown trigger="click" @command="handleCommand">
                <div class="user-info">
                  <el-avatar :size="32" :src="userStore.user?.avatar">
                    {{ userStore.user?.nickname?.charAt(0) }}
                  </el-avatar>
                  <span class="username">{{ userStore.user?.nickname }}</span>
                </div>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="profile">{{ t('common.profile') }}</el-dropdown-item>
                    <el-dropdown-item command="logout" divided>{{ t('common.logout') }}</el-dropdown-item>
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
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useUserStore } from '@/stores/user'

const { t, locale } = useI18n()
const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const currentLang = computed(() => locale.value)

const searchKeyword = ref('')

const activeMenu = computed(() => {
  return route.path
})

const handleSearch = () => {
  if (searchKeyword.value) {
    router.push({ path: '/photos', query: { keyword: searchKeyword.value } })
  }
}

const handleCommand = (command: string) => {
  if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'logout') {
    userStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped lang="scss">
.main-layout {
  height: 100%;
}

.el-container {
  height: 100%;
}

.el-aside {
  background-color: #304156;
  color: #fff;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  background-color: #263445;

  .logo-img {
    width: 32px;
    height: 32px;
  }

  h2 {
    color: #fff;
    font-size: 16px;
    margin: 0;
  }
}

.side-menu {
  border: none;
  background-color: #304156;

  :deep(.el-menu-item) {
    color: #bfcbd9;

    &:hover {
      background-color: #263445;
    }

    &.is-active {
      color: #409eff;
      background-color: #263445;
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
  display: flex;
  align-items: center;
}

.header-right {
  display: flex;
  align-items: center;
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
