<template>
  <div class="shares-page">
    <div class="page-header">
      <h2>{{ t('menu.shares') }}</h2>
    </div>

    <el-empty v-if="!loading && shares.length === 0" :description="currentLang === 'zh-CN' ? '暂无分享记录' : 'No shares yet'" />

    <div v-else v-loading="loading" class="share-list">
      <div
        v-for="share in shares"
        :key="share.id"
        class="share-card"
      >
        <div class="share-info">
          <div class="share-title">
            {{ share.photoTitle || share.albumName || (currentLang === 'zh-CN' ? '未命名分享' : 'Untitled Share') }}
          </div>
          <div class="share-meta">
            <span class="share-code">{{ currentLang === 'zh-CN' ? '分享码' : 'Code' }}: {{ share.shareCode }}</span>
            <span class="share-views">{{ currentLang === 'zh-CN' ? '浏览' : 'Views' }}: {{ share.viewCount }}</span>
          </div>
        </div>
        <div class="share-actions">
          <el-button size="small" @click="handleCopyLink(share)">
            {{ currentLang === 'zh-CN' ? '复制链接' : 'Copy Link' }}
          </el-button>
          <el-button size="small" type="danger" @click="handleCancel(share)">
            {{ t('share.cancel') }}
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { useShareStore } from '@/stores/share'
import type { Share } from '@/types/share'

const { t, locale } = useI18n()
const shareStore = useShareStore()

const currentLang = computed(() => locale.value)

const loading = ref(false)
const shares = ref<Share[]>([])

const fetchShares = async () => {
  loading.value = true
  try {
    const result = await shareStore.fetchShares()
    shares.value = result
  } finally {
    loading.value = false
  }
}

const handleCopyLink = (share: Share) => {
  const link = `${window.location.origin}/s/${share.shareCode}`
  navigator.clipboard.writeText(link)
  ElMessage.success(currentLang.value === 'zh-CN' ? '链接已复制' : 'Link copied')
}

const handleCancel = async (share: Share) => {
  try {
    await shareStore.cancelShare(share.id)
    ElMessage.success(currentLang.value === 'zh-CN' ? '已取消分享' : 'Share cancelled')
    await fetchShares()
  } catch (error) {
    ElMessage.error(currentLang.value === 'zh-CN' ? '操作失败' : 'Operation failed')
  }
}

onMounted(() => {
  fetchShares()
})
</script>

<style scoped lang="scss">
.shares-page {
  height: 100%;
}

.page-header {
  margin-bottom: 20px;

  h2 {
    margin: 0;
    color: #333;
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;

  .empty-image {
    width: 300px;
    max-width: 100%;
    margin-bottom: 20px;
  }

  .empty-text {
    color: #999;
    font-size: 16px;
  }
}

.share-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.share-card {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.share-info {
  .share-title {
    font-size: 16px;
    font-weight: 500;
    color: #333;
    margin-bottom: 8px;
  }

  .share-meta {
    font-size: 13px;
    color: #999;

    span {
      margin-right: 16px;
    }
  }
}

.share-actions {
  display: flex;
  gap: 10px;
}
</style>
