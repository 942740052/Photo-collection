import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '@/utils/storage'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/pages/Login.vue'),
      meta: { requiresAuth: false },
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('@/pages/Register.vue'),
      meta: { requiresAuth: false },
    },
    {
      path: '/share/:code',
      name: 'ShareView',
      component: () => import('@/pages/ShareView.vue'),
      meta: { requiresAuth: false },
    },
    {
      path: '/',
      component: () => import('@/layouts/MainLayout.vue'),
      meta: { requiresAuth: true },
      children: [
        {
          path: '',
          redirect: '/photos',
        },
        {
          path: 'photos',
          name: 'Photos',
          component: () => import('@/pages/Photos.vue'),
        },
        {
          path: 'albums',
          name: 'Albums',
          component: () => import('@/pages/Albums.vue'),
        },
        {
          path: 'albums/:id',
          name: 'AlbumDetail',
          component: () => import('@/pages/AlbumDetail.vue'),
        },
        {
          path: 'favorites',
          name: 'Favorites',
          component: () => import('@/pages/Favorites.vue'),
        },
        {
          path: 'shares',
          name: 'MyShares',
          component: () => import('@/pages/Shares.vue'),
        },
        {
          path: 'trash',
          name: 'Trash',
          component: () => import('@/pages/Trash.vue'),
        },
        {
          path: 'profile',
          name: 'Profile',
          component: () => import('@/pages/Profile.vue'),
        },
        {
          path: 'backup',
          name: 'Backup',
          component: () => import('@/pages/Backup.vue'),
        },
        {
          path: 'security',
          name: 'Security',
          component: () => import('@/pages/Security.vue'),
        },
      ],
    },
    {
      path: '/admin',
      component: () => import('@/layouts/AdminLayout.vue'),
      meta: { requiresAuth: true, requiresAdmin: true },
      children: [
        {
          path: '',
          redirect: '/admin/users',
        },
        {
          path: 'users',
          name: 'UserManagement',
          component: () => import('@/pages/admin/UserManagement.vue'),
        },
        {
          path: 'config',
          name: 'SystemConfig',
          component: () => import('@/pages/admin/SystemConfig.vue'),
        },
        {
          path: 'storage',
          name: 'StorageMonitor',
          component: () => import('@/pages/admin/StorageMonitor.vue'),
        },
        {
          path: 'announcements',
          name: 'AnnouncementManage',
          component: () => import('@/pages/admin/AnnouncementManage.vue'),
        },
      ],
    },
  ],
})

router.beforeEach((to, from, next) => {
  const token = getToken()

  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else if ((to.path === '/login' || to.path === '/register') && token) {
    next('/photos')
  } else {
    next()
  }
})

export default router
