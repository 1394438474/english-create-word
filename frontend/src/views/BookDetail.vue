<template>
  <div class="page" v-if="book">
    <div class="hero" :style="{ background: `linear-gradient(135deg, ${book.color}, ${shade(book.color, 30)})` }">
      <div class="hero-text">
        <h1 class="hero-title serif">{{ book.name }}</h1>
        <p class="hero-desc">{{ book.description }}</p>
        <div class="hero-stats">
          <div><b class="mono">{{ book.wordCount }}</b><span>词汇</span></div>
          <div><b class="mono">{{ stats.learned }}</b><span>已学</span></div>
          <div><b class="mono">{{ stats.mastered }}</b><span>已掌握</span></div>
          <div><b class="mono">{{ stats.progress }}%</b><span>完成度</span></div>
        </div>
        <div class="hero-actions">
          <button class="btn-primary" @click="$router.push(`/study/${book.id}`)">
            <el-icon><Notebook /></el-icon> 开始学习
          </button>
          <button class="btn-ghost" @click="onChoose" v-if="!stats.chosen">
            <el-icon><Plus /></el-icon> 加入学习计划
          </button>
        </div>
      </div>
      <div class="hero-illus">
        <div class="big-letter">{{ book.name.slice(0, 1) }}</div>
      </div>
    </div>

    <div class="bar-card">
      <div class="bar-head">
        <h3>学习进度</h3>
        <span class="text-mute">系统按艾宾浩斯曲线智能排程</span>
      </div>
      <div class="bar"><div class="bar-fill" :style="{ width: stats.progress + '%' }"></div></div>
      <div class="bar-foot">
        <span>已学 <b class="mono">{{ stats.learned }}</b> / {{ total }}</span>
        <span>已掌握 <b class="mono text-accent">{{ stats.mastered }}</b></span>
      </div>
    </div>

    <div class="word-list">
      <h3 class="sec-title serif">词条预览</h3>
      <div class="word-grid">
        <div v-for="w in words" :key="w.id" class="word-item">
          <div class="word-img" :style="{ backgroundImage: `url(${w.imageUrl})` }"></div>
          <div class="word-info">
            <div class="word-spelling serif">{{ w.spelling }}</div>
            <div class="word-phonetic text-mute mono">{{ w.phoneticUs }}</div>
            <div class="word-meaning">{{ w.meanings?.[0]?.meaningZh }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { bookApi, wordApi, type Book, type WordCard } from '@/api/book'
import { userApi } from '@/api/user'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const book = ref<Book | null>(null)
const words = ref<WordCard[]>([])
const stats = reactive({ learned: 0, mastered: 0, progress: 0, chosen: false })
const total = ref(0)

function shade(hex: string, percent: number) {
  const f = parseInt(hex.slice(1), 16),
    t = percent < 0 ? 0 : 255,
    p = percent / 100,
    R = f >> 16, G = (f >> 8) & 0x00FF, B = f & 0x0000FF
  return '#' + (0x1000000 + (Math.round((t - R) * p) + R) * 0x10000 + (Math.round((t - G) * p) + G) * 0x100 + (Math.round((t - B) * p) + B)).toString(16).slice(1)
}

async function onChoose() {
  await bookApi.choose(Number(route.params.id))
  stats.chosen = true
  ElMessage.success('已加入学习计划')
}

onMounted(async () => {
  const id = Number(route.params.id)
  book.value = await bookApi.detail(id)
  const page = await wordApi.pageByBook(id, 1, 12)
  words.value = page.records
  total.value = page.total
  // 统计
  const dash: any = await (await import('@/api/study')).statApi.dashboard()
  const cur = dash.bookProgress?.find((b: any) => b.bookId === id)
  if (cur) {
    stats.learned = cur.learned
    stats.mastered = cur.mastered
    stats.progress = cur.progress
  }
})
</script>

<style lang="scss" scoped>
.hero {
  position: relative;
  display: flex; align-items: center; justify-content: space-between;
  padding: 40px 48px; border-radius: 24px; color: #fff; margin-bottom: 24px;
  overflow: hidden;
}
.hero-text { flex: 1; z-index: 1; }
.hero-title { font-size: 38px; font-weight: 800; color: #fff; }
.hero-desc { font-size: 14px; line-height: 1.7; opacity: 0.9; margin: 10px 0 24px; max-width: 540px; }
.hero-stats { display: flex; gap: 28px; margin-bottom: 24px; }
.hero-stats > div b { font-size: 28px; font-weight: 800; display: block; }
.hero-stats > div span { font-size: 12px; opacity: 0.8; }
.hero-actions { display: flex; gap: 12px; }
.btn-primary, .btn-ghost { display: inline-flex; align-items: center; gap: 6px; }
.btn-ghost { background: rgba(255,255,255,0.20); color: #fff; border: 1px solid rgba(255,255,255,0.40); &:hover { background: rgba(255,255,255,0.30); } }

.hero-illus { width: 180px; height: 180px; }
.big-letter {
  width: 100%; height: 100%; display: flex; align-items: center; justify-content: center;
  font-family: 'Sora', serif; font-size: 140px; font-weight: 800;
  text-shadow: 0 16px 40px rgba(0,0,0,0.30);
  background: rgba(255,255,255,0.12);
  border-radius: 50%;
  border: 1px solid rgba(255,255,255,0.20);
}

.bar-card { background: #fff; border-radius: 18px; padding: 22px; box-shadow: 0 4px 14px rgba(31,41,78,0.06); margin-bottom: 24px; }
.bar-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.bar-head h3 { font-size: 16px; font-weight: 700; }
.bar { height: 12px; background: #EEF0F5; border-radius: 6px; overflow: hidden; }
.bar-fill { height: 100%; background: var(--grad-accent); border-radius: 6px; transition: width 1.2s ease; }
.bar-foot { display: flex; justify-content: space-between; margin-top: 10px; color: var(--text-mute); font-size: 13px; }

.sec-title { font-size: 18px; font-weight: 800; margin: 12px 0 16px; }
.word-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 14px; }
.word-item { display: flex; align-items: center; gap: 12px; padding: 12px; background: #fff; border-radius: 14px; box-shadow: 0 4px 12px rgba(31,41,78,0.05); transition: transform .15s; &:hover { transform: translateY(-2px); } }
.word-img { width: 56px; height: 56px; border-radius: 12px; background-size: cover; background-position: center; background-color: #EEF0F5; flex-shrink: 0; }
.word-spelling { font-size: 16px; font-weight: 800; }
.word-phonetic { font-size: 11px; }
.word-meaning { font-size: 12px; color: var(--text-secondary); }

@media (max-width: 768px) {
  .hero { flex-direction: column; padding: 28px 22px; }
  .hero-title { font-size: 26px; }
  .hero-illus { display: none; }
  .hero-stats { flex-wrap: wrap; gap: 16px; }
}
</style>
