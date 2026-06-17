<template>
  <div class="page">
    <div class="page-head flex-between">
      <div>
        <h1 class="page-title">错题本</h1>
        <p class="page-subtitle">AI 自动归类的薄弱词，建议优先攻克</p>
      </div>
      <el-segmented v-model="quizType" :options="quizTypes" @change="load" />
    </div>

    <div class="error-grid" v-if="list.length">
      <div v-for="(w, i) in list" :key="w.id" class="error-card fade-up" :style="{ animationDelay: (i*0.05) + 's' }">
        <div class="err-img" :style="{ backgroundImage: `url(${w.imageUrl})` }">
          <div class="err-rank">#{{ i + 1 }}</div>
        </div>
        <div class="err-info">
          <div class="err-head">
            <span class="err-spelling serif">{{ w.spelling }}</span>
            <span class="chip chip-danger">{{ w.status || 'STRANGE' }}</span>
          </div>
          <div class="err-phonetic mono text-mute">/{{ w.phoneticUs?.replace(/[\/\[\]]/g, '') }}/</div>
          <div class="err-meaning">{{ w.meanings?.[0]?.meaningZh }}</div>
          <div class="err-actions">
            <el-button size="small" type="primary" plain @click="goStudy(w)">再学一次</el-button>
            <el-button size="small" @click="remove(w)">移出错题本</el-button>
          </div>
        </div>
      </div>
    </div>
    <div v-else class="empty">
      <div class="emoji">🌟</div>
      <h2 class="serif">暂无错题</h2>
      <p class="text-mute">继续保持，错题本空空如也</p>
      <button class="btn-primary" @click="$router.push('/books')">去词书</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { errorBookApi } from '@/api/study'
import { type WordCard } from '@/api/book'

const router = useRouter()
const list = ref<WordCard[]>([])
const quizType = ref<string>('ALL')
const quizTypes = [
  { label: '全部', value: 'ALL' },
  { label: '英译汉', value: 'EN_ZH' },
  { label: '汉译英', value: 'ZH_EN' },
  { label: '选词填空', value: 'CLOZE' },
  { label: '学习中', value: 'STUDY' }
]

async function load() {
  const t = quizType.value === 'ALL' ? undefined : quizType.value
  list.value = await errorBookApi.list(t)
}

async function remove(w: WordCard) {
  await ElMessageBox.confirm(`将「${w.spelling}」移出错题本？`, '提示', { type: 'warning' })
  await errorBookApi.remove(w.id)
  list.value = list.value.filter(x => x.id !== w.id)
  ElMessage.success('已移除')
}

function goStudy(w: WordCard) {
  router.push(`/study/${w.bookId}`)
}

onMounted(load)
</script>

<style lang="scss" scoped>
.page-head { margin-bottom: 18px; align-items: flex-end; }
.error-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 16px; }
.error-card {
  display: flex; gap: 14px; background: #fff;
  border-radius: 16px; overflow: hidden; padding: 14px;
  box-shadow: 0 4px 14px rgba(31,41,78,0.06);
  transition: transform .2s;
  &:hover { transform: translateY(-2px); box-shadow: 0 10px 24px rgba(31,41,78,0.10); }
}
.err-img {
  width: 100px; height: 130px; flex-shrink: 0;
  border-radius: 12px; background-size: cover; background-position: center; background-color: #EEF0F5;
  position: relative;
}
.err-rank {
  position: absolute; top: 6px; left: 6px;
  background: rgba(255,90,95,0.92); color: #fff;
  font-size: 11px; font-weight: 700;
  padding: 2px 8px; border-radius: 999px;
}
.err-info { flex: 1; display: flex; flex-direction: column; gap: 4px; min-width: 0; }
.err-head { display: flex; align-items: center; gap: 6px; }
.err-spelling { font-size: 18px; font-weight: 800; }
.err-phonetic { font-size: 12px; }
.err-meaning {
  font-size: 13px; color: var(--text-secondary);
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;
  height: 38px;
}
.err-actions { display: flex; gap: 6px; margin-top: auto; }

.empty { text-align: center; padding: 80px 20px; }
.empty .emoji { font-size: 80px; margin-bottom: 12px; }
.empty h2 { font-size: 24px; font-weight: 800; margin-bottom: 8px; }
.empty .btn-primary { margin-top: 24px; }
</style>
