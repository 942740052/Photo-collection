<template>
  <div class="page-container people-page">
    <div class="page-header">
      <h2>人物</h2>
    </div>

    <div class="people-grid" v-if="photoStore.people.length > 0">
      <div
        v-for="person in photoStore.people.filter(p => !p.is_hidden)"
        :key="person.id"
        class="person-card"
        @click="viewPerson(person)"
      >
        <el-avatar :size="80" :src="person.cover_url" class="person-avatar">
          {{ person.name[0] }}
        </el-avatar>
        <div class="person-name">{{ person.name }}</div>
        <div class="person-count">{{ person.photo_count }} 张照片</div>
      </div>
    </div>

    <div v-else class="empty-state">
      <el-icon :size="64"><User /></el-icon>
      <p>还没有识别人物</p>
      <p style="font-size: 13px; color: #c0c4cc">AI 人脸识别功能将在照片上传后自动运行</p>
    </div>

    <el-dialog v-model="personDialogVisible" :title="currentPerson?.name" width="800px">
      <div class="photo-grid small" v-if="personPhotos.length > 0">
        <div
          v-for="photo in personPhotos"
          :key="photo.id"
          class="photo-item"
          @click="$router.push(`/photos/${photo.id}`); personDialogVisible = false"
        >
          <img :src="photo.thumbnail_small" :alt="photo.file_name" loading="lazy" />
        </div>
      </div>
      <div v-else class="empty-state" style="padding: 20px">
        <p>暂无照片</p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { usePhotoStore } from '../stores/photo'

const photoStore = usePhotoStore()

const personDialogVisible = ref(false)
const currentPerson = ref(null)

const personPhotos = computed(() => {
  if (!currentPerson.value) return []
  return photoStore.activePhotos.filter(p =>
    p.tags?.includes(currentPerson.value.name)
  ).slice(0, 20)
})

const viewPerson = (person) => {
  currentPerson.value = person
  personDialogVisible.value = true
}
</script>

<style scoped>
.people-page {
  max-width: 1400px;
  margin: 0 auto;
}

.people-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 20px;
}

.person-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
  background: #fff;
  border-radius: 12px;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.person-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.person-avatar {
  margin-bottom: 12px;
  font-size: 28px;
}

.person-name {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.person-count {
  font-size: 12px;
  color: #909399;
}
</style>
