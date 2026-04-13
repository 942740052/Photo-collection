<template>
  <el-dialog v-model="visible" title="分享设置" width="500px">
    <el-form :model="form" label-width="100px">
      <el-form-item label="分享链接">
        <el-input v-model="shareUrl" readonly>
          <template #append>
            <el-button @click="copyShareUrl">复制</el-button>
          </template>
        </el-input>
      </el-form-item>
      <el-form-item label="访问密码">
        <el-input
          v-model="form.password"
          placeholder="留空则无需密码访问"
          show-password
        />
      </el-form-item>
      <el-form-item label="有效期">
        <el-select v-model="form.expireHours" placeholder="选择有效期">
          <el-option label="永久有效" :value="0" />
          <el-option label="1小时" :value="1" />
          <el-option label="24小时" :value="24" />
          <el-option label="7天" :value="168" />
          <el-option label="30天" :value="720" />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="loading" @click="handleCreate">
        创建分享
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useShareStore } from '@/stores/share'

const props = defineProps<{
  modelValue: boolean
  type: 'photo' | 'album'
  id: number
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  success: [shareCode: string]
}>()

const shareStore = useShareStore()
const loading = ref(false)
const shareCode = ref('')

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const form = ref({
  password: '',
  expireHours: 0
})

const shareUrl = computed(() => {
  if (shareCode.value) {
    return `${window.location.origin}/share/${shareCode.value}`
  }
  return ''
})

watch(() => props.modelValue, (val) => {
  if (val) {
    form.value = {
      password: '',
      expireHours: 0
    }
    shareCode.value = ''
  }
})

const handleCreate = async () => {
  loading.value = true
  try {
    const data: any = {}
    if (form.value.password) {
      data.password = form.value.password
    }
    if (form.value.expireHours > 0) {
      data.expireHours = form.value.expireHours
    }

    let share
    if (props.type === 'photo') {
      share = await shareStore.sharePhoto(props.id, data)
    } else {
      share = await shareStore.shareAlbum(props.id, data)
    }

    shareCode.value = share.shareCode
    ElMessage.success('分享创建成功')
    emit('success', share.shareCode)
  } catch (error: any) {
    ElMessage.error(error.message || '创建分享失败')
  } finally {
    loading.value = false
  }
}

const copyShareUrl = () => {
  if (shareUrl.value) {
    navigator.clipboard.writeText(shareUrl.value)
    ElMessage.success('链接已复制到剪贴板')
  }
}
</script>
