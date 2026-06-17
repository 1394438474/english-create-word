<template>
  <div class="page">
    <div class="page-head flex-between">
      <div>
        <h1 class="page-title">生词本</h1>
        <p class="page-subtitle">收藏的高价值词汇、笔记与重点标记</p>
      </div>
      <div class="head-actions">
        <el-button @click="onExport"><el-icon><Download /></el-icon> 导出</el-button>
        <el-button type="danger" plain :disabled="!selection.length" @click="onBatchRemove">批量删除 ({{ selection.length }})</el-button>
      </div>
    </div>

    <div v-if="list.length">
      <div class="vocab-list">
        <div v-for="(item, i) in list" :key="item.word.id" class="vocab-card fade-up" :style="{ animationDelay: (i*0.04) + 's' }">
          <el-checkbox :model-value="isSelected(item.word.id)" @change="(v: any) => onSelChange(item.word.id, v)" class="vocab-check" />
          <div class="vocab-img" :style="{ backgroundImage: `url(${item.word.imageUrl})` }"></div>
          <div class="vocab-body">
            <div class="vocab-head">
              <span class="serif vocab-spelling">{{ item.word.spelling }}</span>
              <span class="vocab-phonetic mono text-mute">/{{ item.word.phoneticUs?.replace(/[\/\[\]]/g, '') }}/</span>
              <div class="star-group">
                <el-icon v-for="n in 5" :key="n" :size="18" :class="['star', n <= item.star ? 'active' : '']" @click="setStar(item, n)">
                  <component :is="n <= item.star ? 'StarFilled' : 'Star'" />
                </el-icon>
              </div>
            </div>
            <div class="vocab-meaning">{{ item.word.meanings?.[0]?.meaningZh }}</div>
            <div class="vocab-note" v-if="item.note">{{ item.note }}</div>
            <el-input v-model="noteDraft[item.word.id]" type="textarea" :rows="2" placeholder="写下你的记忆心得..." class="note-input" />
            <div class="vocab-foot">
              <el-button size="small" plain @click="saveNote(item)">保存笔记</el-button>
              <el-button size="small" @click="goStudy(item)">去学习</el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div v-else class="empty">
      <div class="emoji">📒</div>
      <h2 class="serif">生词本空空如也</h2>
      <p class="text-mute">学习中遇到需要重点记忆的单词，点击"加入生词本"即可收藏</p>
      <button class="btn-primary" @click="$router.push('/books')">去选词书</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { vocabApi } from '@/api/study'

const router = useRouter()
const list = ref<any[]>([])
const selection = ref<number[]>([])
const noteDraft = reactive<Record<number, string>>({})

async function load() {
  list.value = await vocabApi.list()
  list.value.forEach((it: any) => { noteDraft[it.word.id] = it.note || '' })
}

async function setStar(item: any, n: number) {
  await vocabApi.updateStar(item.word.id, n === item.star ? 0 : n)
  item.star = n === item.star ? 0 : n
  ElMessage.success('已更新星级')
}

async function saveNote(item: any) {
  await vocabApi.updateNote(item.word.id, noteDraft[item.word.id] || '')
  item.note = noteDraft[item.word.id]
  ElMessage.success('已保存')
}

async function onBatchRemove() {
  if (!selection.value.length) return
  await ElMessageBox.confirm(`确认删除选中的 ${selection.value.length} 个生词？`, '提示', { type: 'warning' })
  await vocabApi.batchRemove(selection.value)
  ElMessage.success('已删除')
  selection.value = []
  load()
}

async function onExport() {
  const data = await vocabApi.export()
  const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url; a.download = `vocab-${Date.now()}.json`; a.click()
  URL.revokeObjectURL(url)
  ElMessage.success('已导出')
}

function isSelected(id: number) {
  return selection.value.includes(id)
}

function onSelChange(id: number, v: any) {
  const checked = Boolean(v)
  const idx = selection.value.indexOf(id)
  if (checked && idx === -1) selection.value.push(id)
  else if (!checked && idx !== -1) selection.value.splice(idx, 1)
}

function goStudy(item: any) {
  router.push(`/study/${item.word.bookId}`)
}

onMounted(load)
</script>

<style lang="scss" scoped>
.page-head { margin-bottom: 18px; align-items: flex-end; }
.head-actions { display: flex; gap: 10px; }
.vocab-list { display: flex; flex-direction: column; gap: 14px; }
.vocab-card {
  display: flex; gap: 16px; background: #fff;
  padding: 16px; border-radius: 16px;
  box-shadow: 0 4px 14px rgba(31,41,78,0.05);
  transition: transform .15s;
  &:hover { transform: translateY(-1px); }
}
.vocab-check { padding-top: 14px; }
.vocab-img {
  width: 100px; height: 100px; border-radius: 12px;
  background-size: cover; background-position: center; background-color: #EEF0F5;
  flex-shrink: 0;
}
.vocab-body { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 4px; }
.vocab-head { display: flex; align-items: center; gap: 10px; }
.vocab-spelling { font-size: 20px; font-weight: 800; }
.vocab-phonetic { font-size: 13px; }
.star-group { display: flex; gap: 2px; margin-left: auto; }
.star { color: #DDD; cursor: pointer; transition: color .15s; &:hover { color: var(--gold); } }
.star.active { color: var(--gold); }
.vocab-meaning { font-size: 14px; color: var(--text-secondary); }
.vocab-note {
  padding: 8px 12px; background: rgba(255,180,0,0.08); border-left: 3px solid var(--gold);
  border-radius: 0 8px 8px 0; font-size: 13px; color: var(--text-secondary);
}
.note-input :deep(.el-textarea__inner) { border-radius: 10px; }
.vocab-foot { display: flex; gap: 6px; margin-top: 6px; }

.empty { text-align: center; padding: 80px 20px; }
.empty .emoji { font-size: 80px; margin-bottom: 12px; }
.empty h2 { font-size: 24px; font-weight: 800; margin-bottom: 8px; }
.empty .btn-primary { margin-top: 24px; }

@media (max-width: 768px) {
  .vocab-card { flex-direction: column; }
  .vocab-img { width: 100%; height: 160px; }
  .vocab-check { display: none; }
}
</style>
