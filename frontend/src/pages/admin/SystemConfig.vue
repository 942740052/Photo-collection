<template>
  <div class="system-config-page">
    <el-card>
      <template #header>
        <span>系统配置</span>
      </template>
      <el-form v-loading="loading" label-width="150px">
        <el-form-item label="网站名称">
          <el-input v-model="config.site_name" />
        </el-form-item>
        <el-form-item label="网站描述">
          <el-input v-model="config.site_description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="最大上传大小">
          <el-input-number v-model="maxUploadSizeMB" :min="1" :max="500" />
          <span style="margin-left: 10px">MB</span>
        </el-form-item>
        <el-form-item label="允许的文件类型">
          <el-input v-model="config.allowed_file_types" placeholder="多个类型用逗号分隔" />
        </el-form-item>
        <el-form-item label="启用分享功能">
          <el-switch v-model="enableShare" />
        </el-form-item>
        <el-form-item label="启用AI功能">
          <el-switch v-model="enableAi" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="handleSave">
            保存配置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { adminApi } from '@/api/admin'
import type { SystemConfig } from '@/types/admin'

const loading = ref(false)
const saving = ref(false)
const config = ref<SystemConfig>({
  site_name: '',
  site_description: '',
  max_upload_size: '52428800',
  allowed_file_types: 'jpg,jpeg,png,gif,webp,bmp',
  enable_share: 'true',
  enable_ai_features: 'false',
})

const maxUploadSizeMB = computed({
  get: () => Math.round(parseInt(config.value.max_upload_size || '52428800') / 1024 / 1024),
  set: (val) => {
    config.value.max_upload_size = String(val * 1024 * 1024)
  },
})

const enableShare = computed({
  get: () => config.value.enable_share === 'true',
  set: (val) => {
    config.value.enable_share = val ? 'true' : 'false'
  },
})

const enableAi = computed({
  get: () => config.value.enable_ai_features === 'true',
  set: (val) => {
    config.value.enable_ai_features = val ? 'true' : 'false'
  },
})

const fetchConfig = async () => {
  loading.value = true
  try {
    const res = await adminApi.getSystemConfig()
    config.value = res.data
  } finally {
    loading.value = false
  }
}

const handleSave = async () => {
  saving.value = true
  try {
    await adminApi.updateSystemConfig(config.value)
    ElMessage.success('保存成功')
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  fetchConfig()
})
</script>

<style scoped lang="scss">
.system-config-page {
  .el-card {
    max-width: 800px;
  }
}
</style>
