<template>
  <div class="auth-container">
    <div class="language-switch">
      <el-dropdown @command="changeLanguage">
        <span class="lang-btn">
          <el-icon><Globe /></el-icon>
          {{ currentLang === 'zh-CN' ? '中文' : 'English' }}
          <el-icon class="arrow"><ArrowDown /></el-icon>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="zh-CN" :class="{ active: currentLang === 'zh-CN' }">
              中文
            </el-dropdown-item>
            <el-dropdown-item command="en-US" :class="{ active: currentLang === 'en-US' }">
              English
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>

    <div class="auth-content">
      <div class="auth-left">
        <div class="auth-box">
          <div class="auth-header">
            <div class="brand-logo">
              <el-icon :size="36" color="#667eea"><Picture /></el-icon>
            </div>
            <h2>{{ isLogin ? t('login.loginBtn') : t('login.registerBtn') }}</h2>
            <p class="auth-subtitle">
              {{ isLogin ? t('login.welcomeDesc') : t('login.welcomeRegisterDesc') }}
            </p>
          </div>

          <el-form
            ref="formRef"
            :model="formData"
            :rules="rules"
            label-width="0"
            class="auth-form"
          >
            <el-form-item prop="username">
              <el-input
                v-model="formData.username"
                :placeholder="t('login.usernamePlaceholder')"
                prefix-icon="User"
                size="large"
              />
            </el-form-item>

            <el-form-item v-if="!isLogin" prop="email">
              <el-input
                v-model="formData.email"
                :placeholder="t('login.emailPlaceholder')"
                prefix-icon="Message"
                size="large"
              />
            </el-form-item>

            <el-form-item prop="password">
              <el-input
                v-model="formData.password"
                type="password"
                :placeholder="t('login.passwordPlaceholder')"
                prefix-icon="Lock"
                size="large"
                show-password
              />
            </el-form-item>

            <el-form-item v-if="!isLogin" prop="confirmPassword">
              <el-input
                v-model="formData.confirmPassword"
                type="password"
                :placeholder="t('login.confirmPasswordPlaceholder')"
                prefix-icon="Lock"
                size="large"
                show-password
              />
            </el-form-item>

            <el-form-item v-if="!isLogin" prop="nickname">
              <el-input
                v-model="formData.nickname"
                :placeholder="t('login.nicknamePlaceholder')"
                prefix-icon="UserFilled"
                size="large"
              />
            </el-form-item>

            <el-form-item v-if="isLogin" class="remember-row">
              <el-checkbox v-model="rememberMe">{{ t('login.rememberMe') }}</el-checkbox>
              <el-link type="primary" :underline="false">{{ t('login.forgotPassword') }}</el-link>
            </el-form-item>

            <el-form-item>
              <el-button
                type="primary"
                size="large"
                :loading="loading"
                class="submit-btn"
                @click="handleSubmit"
              >
                {{ isLogin ? t('login.loginBtn') : t('login.registerBtn') }}
              </el-button>
            </el-form-item>
          </el-form>

          <div class="auth-footer">
            <span>{{ isLogin ? t('login.noAccount') : t('login.hasAccount') }}</span>
            <el-link type="primary" @click="toggleMode">
              {{ isLogin ? t('login.goRegister') : t('login.goLogin') }}
            </el-link>
          </div>

          <div class="auth-divider">
            <span>{{ currentLang === 'zh-CN' ? '或使用以下方式' : 'Or continue with' }}</span>
          </div>

          <div class="social-login">
            <el-button class="social-btn" circle>
              <svg viewBox="0 0 24 24" width="20" height="20">
                <path fill="currentColor" d="M12 2C6.477 2 2 6.477 2 12c0 4.42 2.865 8.166 6.839 9.489.5.092.682-.217.682-.482 0-.237-.008-.866-.013-1.7-2.782.604-3.369-1.341-3.369-1.341-.454-1.155-1.11-1.462-1.11-1.462-.908-.62.069-.608.069-.608 1.003.07 1.531 1.03 1.531 1.03.892 1.529 2.341 1.087 2.91.831.092-.646.35-1.086.636-1.336-2.22-.253-4.555-1.11-4.555-4.943 0-1.091.39-1.984 1.029-2.683-.103-.253-.446-1.27.098-2.647 0 0 .84-.269 2.75 1.025A9.578 9.578 0 0112 6.836c.85.004 1.705.114 2.504.336 1.909-1.294 2.747-1.025 2.747-1.025.546 1.377.203 2.394.1 2.647.64.699 1.028 1.592 1.028 2.683 0 3.842-2.339 4.687-4.566 4.935.359.309.678.919.678 1.852 0 1.336-.012 2.415-.012 2.743 0 .267.18.578.688.48C19.138 20.163 22 16.418 22 12c0-5.523-4.477-10-10-10z"/>
              </svg>
            </el-button>
            <el-button class="social-btn" circle>
              <svg viewBox="0 0 24 24" width="20" height="20">
                <path fill="currentColor" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
                <path fill="currentColor" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
                <path fill="currentColor" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
                <path fill="currentColor" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
              </svg>
            </el-button>
            <el-button class="social-btn" circle>
              <svg viewBox="0 0 24 24" width="20" height="20">
                <path fill="currentColor" d="M18.244 2.25h3.308l-7.227 8.26 8.502 11.24H16.17l-5.214-6.817L4.99 21.75H1.68l7.73-8.835L1.254 2.25H8.08l4.713 6.231zm-1.161 17.52h1.833L7.084 4.126H5.117z"/>
              </svg>
            </el-button>
          </div>
        </div>
      </div>

      <div class="auth-right">
        <div class="brand-content">
          <div class="brand-header">
            <div class="brand-icon">
              <el-icon :size="80"><PictureFilled /></el-icon>
            </div>
            <h1>{{ t('login.title') }}</h1>
            <p class="brand-slogan">{{ currentLang === 'zh-CN' ? '记录美好瞬间，珍藏珍贵回忆' : 'Capture moments, treasure memories' }}</p>
          </div>

          <div class="illustration-area">
            <div class="floating-elements">
              <div class="float-item float-1">
                <el-icon :size="40"><Picture /></el-icon>
              </div>
              <div class="float-item float-2">
                <el-icon :size="35"><FolderOpened /></el-icon>
              </div>
              <div class="float-item float-3">
                <el-icon :size="30"><Star /></el-icon>
              </div>
              <div class="float-item float-4">
                <el-icon :size="25"><Share /></el-icon>
              </div>
              <div class="float-item float-5">
                <el-icon :size="28"><Camera /></el-icon>
              </div>
            </div>
            <div class="central-illustration">
              <div class="photo-stack">
                <div class="photo-card card-1">
                  <img src="https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=120&h=150&fit=crop&q=80" alt="Photo 1" />
                </div>
                <div class="photo-card card-2">
                  <img src="https://images.unsplash.com/photo-1469474968028-56623f02e42e?w=120&h=150&fit=crop&q=80" alt="Photo 2" />
                </div>
                <div class="photo-card card-3">
                  <img src="https://images.unsplash.com/photo-1441974231531-c6227db76b6e?w=120&h=150&fit=crop&q=80" alt="Photo 3" />
                </div>
              </div>
            </div>
          </div>

          <div class="feature-highlights">
            <div class="highlight-item">
              <div class="highlight-icon">
                <el-icon><Upload /></el-icon>
              </div>
              <div class="highlight-text">
                <h4>{{ currentLang === 'zh-CN' ? '轻松上传' : 'Easy Upload' }}</h4>
                <p>{{ currentLang === 'zh-CN' ? '支持批量上传，快速导入照片' : 'Batch upload support for quick imports' }}</p>
              </div>
            </div>
            <div class="highlight-item">
              <div class="highlight-icon">
                <el-icon><Collection /></el-icon>
              </div>
              <div class="highlight-text">
                <h4>{{ currentLang === 'zh-CN' ? '智能分类' : 'Smart Organization' }}</h4>
                <p>{{ currentLang === 'zh-CN' ? '自动提取EXIF信息，智能分类整理' : 'Auto EXIF extraction and smart categorization' }}</p>
              </div>
            </div>
            <div class="highlight-item">
              <div class="highlight-icon">
                <el-icon><Connection /></el-icon>
              </div>
              <div class="highlight-text">
                <h4>{{ currentLang === 'zh-CN' ? '安全分享' : 'Secure Sharing' }}</h4>
                <p>{{ currentLang === 'zh-CN' ? '一键生成分享链接，支持密码保护' : 'One-click sharing with password protection' }}</p>
              </div>
            </div>
          </div>

          <div class="brand-footer">
            <div class="stats-row">
              <div class="stat-item">
                <span class="stat-number">10K+</span>
                <span class="stat-label">{{ currentLang === 'zh-CN' ? '用户' : 'Users' }}</span>
              </div>
              <div class="stat-divider"></div>
              <div class="stat-item">
                <span class="stat-number">1M+</span>
                <span class="stat-label">{{ currentLang === 'zh-CN' ? '照片' : 'Photos' }}</span>
              </div>
              <div class="stat-divider"></div>
              <div class="stat-item">
                <span class="stat-number">99.9%</span>
                <span class="stat-label">{{ currentLang === 'zh-CN' ? '可用性' : 'Uptime' }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="mobile-brand">
      <div class="mobile-brand-content">
        <el-icon :size="24"><PictureFilled /></el-icon>
        <span>{{ t('login.title') }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useUserStore } from '@/stores/user'

const { t, locale } = useI18n()
const router = useRouter()
const userStore = useUserStore()

const formRef = ref<FormInstance>()
const loading = ref(false)
const isLogin = ref(true)
const rememberMe = ref(false)

const formData = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
  nickname: '',
})

const currentLang = computed(() => locale.value)

const validateConfirmPassword = (rule: any, value: any, callback: any) => {
  if (!isLogin.value) {
    if (value !== formData.password) {
      callback(new Error(t('profile.passwordMismatch')))
    } else {
      callback()
    }
  } else {
    callback()
  }
}

const rules = computed<FormRules>(() => ({
  username: [
    { required: true, message: t('login.usernamePlaceholder'), trigger: 'blur' },
    { min: 3, max: 20, message: '3-20 characters', trigger: 'blur' },
  ],
  email: [
    { required: !isLogin.value, message: t('login.emailPlaceholder'), trigger: 'blur' },
    { type: 'email', message: 'Invalid email format', trigger: 'blur' },
  ],
  password: [
    { required: true, message: t('login.passwordPlaceholder'), trigger: 'blur' },
    { min: 6, max: 20, message: '6-20 characters', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: !isLogin.value, message: t('login.confirmPasswordPlaceholder'), trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' },
  ],
}))

const changeLanguage = (lang: string) => {
  locale.value = lang
  localStorage.setItem('locale', lang)
}

const toggleMode = () => {
  isLogin.value = !isLogin.value
  formRef.value?.resetFields()
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        if (isLogin.value) {
          await userStore.login(formData.username, formData.password)
          ElMessage.success(t('login.loginSuccess'))
        } else {
          await userStore.register(
            formData.username,
            formData.password,
            formData.email,
            formData.nickname || undefined
          )
          ElMessage.success(t('login.registerSuccess'))
        }
        router.push('/photos')
      } catch (error: any) {
        const errorMsg = error?.response?.data?.message || error?.message || (isLogin.value ? t('login.loginFailed') : t('login.registerFailed'))
        ElMessage.error(errorMsg)
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped lang="scss">
.auth-container {
  width: 100%;
  height: 100%;
  display: flex;
  position: relative;
  overflow: hidden;
}

.language-switch {
  position: absolute;
  top: 20px;
  left: 20px;
  z-index: 100;

  .lang-btn {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 8px 16px;
    background: rgba(255, 255, 255, 0.9);
    border-radius: 20px;
    color: #333;
    cursor: pointer;
    transition: all 0.3s;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

    &:hover {
      background: #fff;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    }

    .arrow {
      font-size: 12px;
    }
  }
}

.auth-content {
  display: flex;
  width: 100%;
  height: 100%;
}

.auth-left {
  width: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fafbfc;
  padding: 40px;
  position: relative;

  @media (max-width: 900px) {
    width: 100%;
  }
}

.auth-box {
  width: 100%;
  max-width: 380px;
}

.auth-header {
  text-align: center;
  margin-bottom: 30px;

  .brand-logo {
    width: 60px;
    height: 60px;
    margin: 0 auto 16px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 16px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
  }

  h2 {
    font-size: 28px;
    font-weight: 600;
    color: #1a1a2e;
    margin: 0 0 8px;
  }

  .auth-subtitle {
    font-size: 14px;
    color: #666;
    margin: 0;
  }
}

.auth-form {
  .el-form-item {
    margin-bottom: 20px;
  }

  :deep(.el-input__wrapper) {
    border-radius: 10px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
    border: 1px solid #e8e8e8;
    transition: all 0.3s;

    &:hover, &.is-focus {
      border-color: #667eea;
      box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
    }
  }

  .remember-row {
    margin-bottom: 10px;

    :deep(.el-form-item__content) {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
  }

  .submit-btn {
    width: 100%;
    height: 48px;
    font-size: 16px;
    border-radius: 10px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border: none;
    font-weight: 500;
    letter-spacing: 0.5px;
    transition: all 0.3s;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 8px 20px rgba(102, 126, 234, 0.4);
    }
  }
}

.auth-footer {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
  color: #666;

  .el-link {
    margin-left: 5px;
    font-size: 14px;
    font-weight: 500;
  }
}

.auth-divider {
  display: flex;
  align-items: center;
  margin: 24px 0;

  &::before,
  &::after {
    content: '';
    flex: 1;
    height: 1px;
    background: #e8e8e8;
  }

  span {
    padding: 0 16px;
    font-size: 13px;
    color: #999;
  }
}

.social-login {
  display: flex;
  justify-content: center;
  gap: 16px;

  .social-btn {
    width: 44px;
    height: 44px;
    border: 1px solid #e8e8e8;
    background: #fff;
    color: #666;
    transition: all 0.3s;

    &:hover {
      border-color: #667eea;
      color: #667eea;
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    }
  }
}

.auth-right {
  width: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40px;
  position: relative;
  overflow: hidden;

  @media (max-width: 900px) {
    display: none;
  }

  &::before {
    content: '';
    position: absolute;
    top: -50%;
    right: -50%;
    width: 100%;
    height: 100%;
    background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 70%);
  }

  &::after {
    content: '';
    position: absolute;
    bottom: -50%;
    left: -50%;
    width: 100%;
    height: 100%;
    background: radial-gradient(circle, rgba(255,255,255,0.08) 0%, transparent 70%);
  }
}

.brand-content {
  text-align: center;
  color: #fff;
  z-index: 1;
  max-width: 500px;
  position: relative;
}

.brand-header {
  margin-bottom: 40px;

  .brand-icon {
    width: 100px;
    height: 100px;
    margin: 0 auto 24px;
    background: rgba(255, 255, 255, 0.2);
    border-radius: 24px;
    display: flex;
    align-items: center;
    justify-content: center;
    backdrop-filter: blur(10px);
    animation: pulse 3s ease-in-out infinite;
  }

  h1 {
    font-size: 36px;
    font-weight: 700;
    margin: 0 0 12px;
    text-shadow: 0 2px 20px rgba(0, 0, 0, 0.2);
  }

  .brand-slogan {
    font-size: 18px;
    opacity: 0.9;
    margin: 0;
  }
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
    box-shadow: 0 0 0 0 rgba(255, 255, 255, 0.4);
  }
  50% {
    transform: scale(1.05);
    box-shadow: 0 0 30px 10px rgba(255, 255, 255, 0.2);
  }
}

.illustration-area {
  position: relative;
  height: 200px;
  margin-bottom: 40px;
}

.floating-elements {
  position: absolute;
  width: 100%;
  height: 100%;

  .float-item {
    position: absolute;
    background: rgba(255, 255, 255, 0.15);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    backdrop-filter: blur(5px);
    animation: floatAround 8s ease-in-out infinite;
  }

  .float-1 {
    top: 10%;
    left: 10%;
    width: 70px;
    height: 70px;
    animation-delay: 0s;
  }

  .float-2 {
    top: 5%;
    right: 15%;
    width: 60px;
    height: 60px;
    animation-delay: 1.5s;
  }

  .float-3 {
    bottom: 20%;
    left: 5%;
    width: 50px;
    height: 50px;
    animation-delay: 3s;
  }

  .float-4 {
    bottom: 10%;
    right: 10%;
    width: 45px;
    height: 45px;
    animation-delay: 4.5s;
  }

  .float-5 {
    top: 50%;
    right: 5%;
    width: 50px;
    height: 50px;
    animation-delay: 6s;
  }
}

@keyframes floatAround {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
  }
  25% {
    transform: translateY(-15px) rotate(5deg);
  }
  50% {
    transform: translateY(0) rotate(0deg);
  }
  75% {
    transform: translateY(15px) rotate(-5deg);
  }
}

.central-illustration {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);

  .photo-stack {
    position: relative;
    width: 140px;
    height: 170px;

    .photo-card {
      position: absolute;
      width: 100px;
      height: 130px;
      background: #fff;
      border-radius: 8px;
      box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
      transition: transform 0.5s ease;
      overflow: hidden;
      border: 3px solid #fff;

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }
    }

    .card-1 {
      transform: rotate(-8deg) translateX(-20px);
      animation: cardFloat1 4s ease-in-out infinite;
    }

    .card-2 {
      transform: rotate(0deg);
      animation: cardFloat2 4s ease-in-out infinite 0.5s;
    }

    .card-3 {
      transform: rotate(8deg) translateX(20px);
      animation: cardFloat3 4s ease-in-out infinite 1s;
    }
  }
}

@keyframes cardFloat1 {
  0%, 100% {
    transform: rotate(-8deg) translateX(-20px) translateY(0);
  }
  50% {
    transform: rotate(-8deg) translateX(-20px) translateY(-10px);
  }
}

@keyframes cardFloat2 {
  0%, 100% {
    transform: rotate(0deg) translateY(0);
  }
  50% {
    transform: rotate(0deg) translateY(-10px);
  }
}

@keyframes cardFloat3 {
  0%, 100% {
    transform: rotate(8deg) translateX(20px) translateY(0);
  }
  50% {
    transform: rotate(8deg) translateX(20px) translateY(-10px);
  }
}

.feature-highlights {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 40px;

  .highlight-item {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 16px 20px;
    background: rgba(255, 255, 255, 0.1);
    border-radius: 12px;
    backdrop-filter: blur(10px);
    text-align: left;
    transition: all 0.3s;

    &:hover {
      background: rgba(255, 255, 255, 0.2);
      transform: translateX(10px);
    }

    .highlight-icon {
      width: 44px;
      height: 44px;
      background: rgba(255, 255, 255, 0.2);
      border-radius: 10px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 20px;
      flex-shrink: 0;
    }

    .highlight-text {
      h4 {
        margin: 0 0 4px;
        font-size: 15px;
        font-weight: 600;
      }

      p {
        margin: 0;
        font-size: 13px;
        opacity: 0.85;
      }
    }
  }
}

.brand-footer {
  .stats-row {
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 24px;

    .stat-item {
      text-align: center;

      .stat-number {
        display: block;
        font-size: 24px;
        font-weight: 700;
      }

      .stat-label {
        display: block;
        font-size: 12px;
        opacity: 0.8;
        margin-top: 4px;
      }
    }

    .stat-divider {
      width: 1px;
      height: 30px;
      background: rgba(255, 255, 255, 0.3);
    }
  }
}

.mobile-brand {
  display: none;
  position: absolute;
  top: 20px;
  right: 20px;
  z-index: 100;

  @media (max-width: 900px) {
    display: block;
  }

  .mobile-brand-content {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 16px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 20px;
    color: #fff;
    font-weight: 500;
  }
}
</style>
