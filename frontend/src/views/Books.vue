<template>
  <div class="page">
    <div class="page-head">
      <h1 class="page-title">词书广场</h1>
      <p class="page-subtitle">从入门到精通，7 大权威词书任你选</p>
    </div>

    <div class="categories glass">
      <div v-for="c in categories" :key="c.key" class="cat-item" :class="{ active: cat === c.key }" @click="cat = c.key">
        <el-icon :size="18"><component :is="c.icon" /></el-icon>
        <span>{{ c.label }}</span>
      </div>
    </div>

    <div class="book-grid">
      <div v-for="(b, i) in books" :key="b.id" class="book-card fade-up" :style="{ animationDelay: (i*0.05) + 's' }" @click="goDetail(b)">
        <div class="cover" :style="{ background: gradient(b.color) }">
          <div class="cover-mark">{{ b.name.slice(0, 2) }}</div>
          <div class="cover-tag">{{ b.wordCount }} 词</div>
        </div>
        <div class="book-meta">
          <div class="book-name serif">{{ b.name }}</div>
          <div class="book-desc">{{ b.description }}</div>
          <div class="book-foot">
            <span class="chip" :class="levelClass(b.level)">{{ levelLabel(b.level) }}</span>
            <span class="text-mute cat-name">{{ categoryLabel(b.category) }}</span>
          </div>
        </div>
      </div>
      <div v-if="!books.length" class="empty">该分类暂无词书</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { bookApi, type Book } from '@/api/book'

const router = useRouter()
const books = ref<Book[]>([])
const cat = ref('ALL')
const categories = [
  { key: 'ALL', label: '全部', icon: 'Grid' },
  { key: 'CET4', label: '四级', icon: 'Document' },
  { key: 'CET6', label: '六级', icon: 'DocumentChecked' },
  { key: 'KAOYAN', label: '考研', icon: 'EditPen' },
  { key: 'SENIOR', label: '高中', icon: 'School' },
  { key: 'JUNIOR', label: '初中', icon: 'Reading' },
  { key: 'IELTS', label: '雅思', icon: 'Promotion' },
  { key: 'ZHUANSHENG', label: '专升本', icon: 'TrendCharts' }
]

const labelMap: Record<string, string> = { CET4: '四级', CET6: '六级', KAOYAN: '考研', SENIOR: '高中', JUNIOR: '初中', IELTS: '雅思', ZHUANSHENG: '专升本' }
const levelMap: Record<string, { label: string; cls: string }> = {
  EASY: { label: '入门', cls: 'chip-accent' },
  NORMAL: { label: '标准', cls: 'chip' },
  HARD: { label: '进阶', cls: 'chip-gold' }
}

const categoryLabel = (k: string) => labelMap[k] || k
const levelLabel = (l: string) => levelMap[l]?.label || '标准'
const levelClass = (l: string) => levelMap[l]?.cls || 'chip'

function gradient(color: string) {
  return `linear-gradient(135deg, ${color} 0%, ${shade(color, 30)} 100%)`
}
function shade(hex: string, percent: number) {
  const f = parseInt(hex.slice(1), 16),
    t = percent < 0 ? 0 : 255,
    p = percent < 0 ? percent * -1 / 100 : percent / 100,
    R = f >> 16, G = (f >> 8) & 0x00FF, B = f & 0x0000FF
  return '#' + (0x1000000 + (Math.round((t - R) * p) + R) * 0x10000 + (Math.round((t - G) * p) + G) * 0x100 + (Math.round((t - B) * p) + B)).toString(16).slice(1)
}

function goDetail(b: Book) {
  router.push(`/books/${b.id}`)
}

async function load() {
  books.value = await bookApi.list(cat.value === 'ALL' ? undefined : cat.value)
}

watch(cat, load)
onMounted(load)
</script>

<style lang="scss" scoped>
.page-head { margin-bottom: 18px; }
.categories {
  display: flex; gap: 6px; padding: 8px;
  border-radius: 16px; margin-bottom: 24px;
  overflow-x: auto;
  border: 1px solid var(--border);
}
.cat-item {
  display: flex; align-items: center; gap: 6px;
  padding: 8px 14px; border-radius: 12px;
  color: var(--text-secondary); font-weight: 600; font-size: 14px;
  cursor: pointer; white-space: nowrap;
  transition: all .15s;
  &:hover { background: rgba(45,107,255,0.06); color: var(--primary); }
  &.active {
    background: var(--grad-primary); color: #fff;
    box-shadow: 0 6px 14px rgba(45,107,255,0.32);
  }
}

.book-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(260px, 1fr)); gap: 18px; }
.book-card {
  background: #fff; border-radius: 18px; overflow: hidden;
  box-shadow: 0 4px 14px rgba(31,41,78,0.06);
  cursor: pointer; transition: transform .2s, box-shadow .2s;
  &:hover { transform: translateY(-4px); box-shadow: 0 16px 36px rgba(31,41,78,0.10); }
}
.cover { position: relative; height: 140px; display: flex; align-items: center; justify-content: center; color: #fff; }
.cover-mark { font-family: 'Sora', serif; font-size: 56px; font-weight: 800; text-shadow: 0 6px 16px rgba(0,0,0,0.18); }
.cover-tag {
  position: absolute; top: 12px; right: 12px;
  background: rgba(255,255,255,0.30); backdrop-filter: blur(8px);
  padding: 4px 10px; border-radius: 999px; font-size: 12px; font-weight: 600;
}
.book-meta { padding: 16px 18px 18px; }
.book-name { font-size: 17px; font-weight: 800; margin-bottom: 6px; }
.book-desc {
  color: var(--text-mute); font-size: 12px; line-height: 1.5;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;
  height: 36px;
}
.book-foot { display: flex; align-items: center; justify-content: space-between; margin-top: 12px; }
.cat-name { font-size: 12px; }

.empty { text-align: center; color: var(--text-mute); padding: 40px 0; grid-column: 1 / -1; }
</style>
