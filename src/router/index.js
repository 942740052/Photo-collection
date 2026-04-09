import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { requiresAuth: false, title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue'),
    meta: { requiresAuth: false, title: '注册' }
  },
  {
    path: '/',
    component: () => import('../layouts/MainLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'Dashboard',
        component: () => import('../views/Dashboard.vue'),
        meta: { title: '首页', icon: 'HomeFilled' }
      },
      {
        path: 'photos',
        name: 'Photos',
        component: () => import('../views/Photos.vue'),
        meta: { title: '照片', icon: 'Picture' }
      },
      {
        path: 'photos/:id',
        name: 'PhotoDetail',
        component: () => import('../views/PhotoDetail.vue'),
        meta: { title: '照片详情', hidden: true }
      },
      {
        path: 'albums',
        name: 'Albums',
        component: () => import('../views/Albums.vue'),
        meta: { title: '相册', icon: 'Folder' }
      },
      {
        path: 'albums/:id',
        name: 'AlbumDetail',
        component: () => import('../views/AlbumDetail.vue'),
        meta: { title: '相册详情', hidden: true }
      },
      {
        path: 'favorites',
        name: 'Favorites',
        component: () => import('../views/Favorites.vue'),
        meta: { title: '收藏', icon: 'Star' }
      },
      {
        path: 'people',
        name: 'People',
        component: () => import('../views/People.vue'),
        meta: { title: '人物', icon: 'User' }
      },
      {
        path: 'search',
        name: 'Search',
        component: () => import('../views/Search.vue'),
        meta: { title: '搜索', icon: 'Search', hidden: true }
      },
      {
        path: 'upload',
        name: 'Upload',
        component: () => import('../views/Upload.vue'),
        meta: { title: '上传', icon: 'Upload' }
      },
      {
        path: 'trash',
        name: 'Trash',
        component: () => import('../views/Trash.vue'),
        meta: { title: '回收站', icon: 'Delete' }
      },
      {
        path: 'shares',
        name: 'Shares',
        component: () => import('../views/Shares.vue'),
        meta: { title: '分享', icon: 'Share' }
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('../views/Settings.vue'),
        meta: { title: '设置', icon: 'Setting' }
      },
      {
        path: 'admin',
        name: 'Admin',
        component: () => import('../views/Admin.vue'),
        meta: { title: '管理后台', icon: 'Tools', roles: ['admin'] }
      }
    ]
  },
  {
    path: '/share/:token',
    name: 'SharedContent',
    component: () => import('../views/SharedContent.vue'),
    meta: { requiresAuth: false, title: '分享内容' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.meta.requiresAuth !== false && !token) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
  } else if ((to.name === 'Login' || to.name === 'Register') && token) {
    next({ name: 'Dashboard' })
  } else {
    next()
  }
})

export default router
