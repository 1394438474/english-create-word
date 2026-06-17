<template>
  <div class="study-page">
    <header class="topbar glass">
      <button class="back-btn" @click="$router.back()">
        <el-icon><ArrowLeft /></el-icon>
        <span>返回</span>
      </button>
      <div class="progress-wrap">
        <div class="progress-info">
          <span>第 <b class="mono">{{ idx + 1 }}</b> / {{ words.length }} 词</span>
          <span class="text-mute">已学 {{ learned }} · 陌生 {{ strange }}</span>
        </div>
        <div class="progress"><div class="progress-fill" :style="{ width: progress + '%' }"></div></div>
      </div>
      <div class="top-actions">
        <el-button text @click="onSkip">跳过 →</el-button>
      </div>
    </header>

    <div class="stage" v-if="current">
      <div class="flip-card" :class="{ flipped }" @click="onFlip">
        <div class="card-face card-front">
          <img class="scene" :src="current.imageUrl" :alt="current.spelling" />
          <div class="image-overlay">
            <div class="overlay-meta">
              <span class="chip chip-accent">{{ bookName }}</span>
            </div>
          </div>
          <div class="bottom-hint">点击翻面查看释义</div>
        </div>
        <div class="card-face card-back">
          <div class="back-head">
            <h1 class="spelling serif">{{ current.spelling }}</h1>
            <span class="phonetic mono">/{{ current.phoneticUs?.replace(/[\/\[\]]/g, '') }}/</span>
            <button class="audio-btn" @click.stop="playAudio" :title="'播放发音'">
              <el-icon :size="20"><Microphone /></el-icon>
            </button>
          </div>
          <div class="meanings">
            <div v-for="(m, i) in current.meanings" :key="i" class="meaning-item">
              <span class="pos">{{ m.pos }}</span>
              <span class="m-zh">{{ m.meaningZh }}</span>
              <span class="m-en text-mute">{{ m.meaningEn }}</span>
            </div>
          </div>
          <div class="examples" v-if="current.sentences?.length">
            <div class="ex-title">真题例句</div>
            <div v-for="(s, i) in current.sentences.slice(0, 2)" :key="i" class="ex-item">
              <div class="ex-en">{{ s.en }}</div>
              <div class="ex-zh text-mute">{{ s.zh }} <span class="ex-src">{{ s.source }}</span></div>
            </div>
          </div>
          <div class="back-foot">
            <button class="action-btn vocab-btn" @click.stop="toggleVocab">
              <el-icon><Collection /></el-icon>
              {{ current.inVocab ? '已在生词本' : '加入生词本' }}
            </button>
          </div>
        </div>
      </div>

      <div class="action-bar">
        <button class="big-btn strange" @click="onMark('STRANGE')">
          <el-icon :size="20"><Close /></el-icon>
          <span>陌生</span>
        </button>
        <button class="big-btn hazy" @click="onMark('HAZY')">
          <el-icon :size="20"><Question /></el-icon>
          <span>模糊</span>
        </button>
        <button class="big-btn familiar" @click="onMark('FAMILIAR')">
          <el-icon :size="20"><Check /></el-icon>
          <span>熟悉</span>
        </button>
      </div>
    </div>

    <div class="empty" v-else>
      <div class="emoji">🎉</div>
      <h2 class="serif">本轮学习完成！</h2>
      <p class="text-mute">已掌握 {{ mastered }} 词，陌生 {{ strange }} 词</p>
      <button class="btn-primary" @click="$router.push('/dashboard')">返回大屏</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { learnApi } from '@/api/learn'
import { bookApi, type WordCard } from '@/api/book'
import { vocabApi, errorBookApi } from '@/api/study'

const route = useRoute()
const router = useRouter()
const words = ref<WordCard[]>([])
const idx = ref(0)
const flipped = ref(false)
const bookName = ref('')
const learned = ref(0)
const mastered = ref(0)
const strange = ref(0)
const startTime = ref(0)

const current = computed(() => words.value[idx.value])
const progress = computed(() => words.value.length ? ((idx.value + 1) / words.value.length) * 100 : 0)

async function load() {
  const bookId = Number(route.params.bookId)
  const [b, list] = await Promise.all([bookApi.detail(bookId), learnApi.today(bookId, 20)])
  bookName.value = b.name
  words.value = list
  if (list.length === 0) {
    return
  }
  startTime.value = Date.now()
}

function onFlip() { flipped.value = !flipped.value }

function onSkip() {
  flipped.value = false
  idx.value++
  if (idx.value >= words.value.length) onFinish()
  else startTime.value = Date.now()
}

async function onMark(status: 'FAMILIAR' | 'HAZY' | 'STRANGE') {
  const w = current.value
  if (!w) return
  const duration = Date.now() - startTime.value
  await learnApi.record({
    bookId: w.bookId,
    wordId: w.id,
    status,
    durationMs: duration
  })
  if (status === 'FAMILIAR') {
    learned.value++; mastered.value++
  } else if (status === 'STRANGE') {
    strange.value++
    // 陌生自动加入错题
    errorBookApi.add(w.id, 'STUDY').catch(() => {})
  } else {
    learned.value++
  }
  ElMessage.success(status === 'FAMILIAR' ? '已记录为熟悉' : status === 'HAZY' ? '已记录为模糊' : '已记录为陌生')
  flipped.value = false
  setTimeout(() => {
    idx.value++
    startTime.value = Date.now()
    if (idx.value >= words.value.length) onFinish()
  }, 300)
}

async function toggleVocab() {
  const w = current.value
  if (!w) return
  await vocabApi.add({ wordId: w.id, isMarked: 1 })
  w.inVocab = !w.inVocab
  ElMessage.success(w.inVocab ? '已加入生词本' : '已移出')
}

function playAudio() {
  if (!current.value) return
  // 使用 Web Speech API 兜底
  try {
    const u = new SpeechSynthesisUtterance(current.value.spelling)
    u.lang = 'en-US'; u.rate = 0.9
    speechSynthesis.speak(u)
  } catch (e) { /* noop */ }
}

function onFinish() {
  ElMessage.success('本轮学习完成')
}

onMounted(load)
</script>

<style lang="scss" scoped>
.study-page {
  min-height: calc(100vh - 48px);
  margin: -24px;
  padding: 16px 24px 24px;
  background: linear-gradient(180deg, #EEF2FF 0%, #F7F8FC 60%);
  display: flex; flex-direction: column;
}
.topbar {
  display: flex; align-items: center; gap: 16px; padding: 12px 18px;
  border-radius: 16px; margin-bottom: 16px;
}
.back-btn {
  display: flex; align-items: center; gap: 4px;
  padding: 8px 14px; border-radius: 10px;
  background: rgba(45,107,255,0.06); color: var(--primary);
  border: none; cursor: pointer; font-weight: 600;
  &:hover { background: rgba(45,107,255,0.12); }
}
.progress-wrap { flex: 1; }
.progress-info { display: flex; justify-content: space-between; font-size: 13px; margin-bottom: 6px; }
.progress { height: 8px; background: #EEF0F5; border-radius: 4px; overflow: hidden; }
.progress-fill { height: 100%; background: var(--grad-primary); border-radius: 4px; transition: width .4s; }

.stage { flex: 1; display: flex; flex-direction: column; align-items: center; padding: 20px 0; }

.flip-card {
  width: 100%; max-width: 720px; height: 460px;
  perspective: 2000px;
  cursor: pointer;
  position: relative;
}
.card-face {
  position: absolute; inset: 0;
  border-radius: 24px;
  overflow: hidden;
  backface-visibility: hidden;
  -webkit-backface-visibility: hidden;
  transition: transform .7s cubic-bezier(.4, .2, .2, 1);
  background: #fff;
  box-shadow: 0 20px 60px rgba(31,41,78,0.18);
}
.card-front { transform: rotateY(0); }
.card-back { transform: rotateY(180deg); padding: 28px 32px; display: flex; flex-direction: column; }
.flip-card.flipped .card-front { transform: rotateY(-180deg); }
.flip-card.flipped .card-back { transform: rotateY(0); }

.scene { width: 100%; height: 100%; object-fit: cover; transition: transform .8s; }
.flip-card:hover .scene { transform: scale(1.04); }
.image-overlay {
  position: absolute; inset: 0;
  background: linear-gradient(180deg, rgba(0,0,0,0.30) 0%, rgba(0,0,0,0) 30%, rgba(0,0,0,0) 60%, rgba(0,0,0,0.5) 100%);
  display: flex; flex-direction: column; justify-content: space-between; padding: 18px;
}
.overlay-meta { display: flex; justify-content: space-between; }
.bottom-hint {
  align-self: center;
  padding: 6px 14px; border-radius: 999px;
  background: rgba(0,0,0,0.50); color: #fff; font-size: 12px; backdrop-filter: blur(6px);
}

.back-head { display: flex; align-items: center; gap: 14px; }
.spelling { font-size: 40px; font-weight: 800; line-height: 1; }
.phonetic { color: var(--text-mute); font-size: 18px; }
.audio-btn {
  width: 36px; height: 36px; border-radius: 50%;
  background: var(--grad-primary); color: #fff;
  border: none; cursor: pointer; display: flex; align-items: center; justify-content: center;
  box-shadow: 0 6px 14px rgba(45,107,255,0.32);
  &:hover { transform: scale(1.06); }
}

.meanings { margin-top: 18px; display: flex; flex-direction: column; gap: 8px; }
.meaning-item { display: flex; align-items: baseline; gap: 10px; }
.pos { font-style: italic; color: var(--primary); font-weight: 600; min-width: 36px; }
.m-zh { font-weight: 600; font-size: 16px; }
.m-en { font-size: 13px; }

.examples { margin-top: 16px; }
.ex-title { font-size: 12px; color: var(--text-mute); margin-bottom: 6px; letter-spacing: 1px; }
.ex-item { padding: 8px 12px; background: var(--bg-page); border-radius: 10px; margin-bottom: 6px; }
.ex-en { font-size: 14px; }
.ex-zh { font-size: 12px; margin-top: 2px; }
.ex-src { color: var(--accent); margin-left: 6px; font-size: 11px; }

.back-foot { margin-top: auto; }
.vocab-btn {
  display: flex; align-items: center; gap: 6px;
  padding: 8px 14px; border-radius: 10px;
  background: rgba(255,180,0,0.10); color: #C68A00;
  border: 1px dashed rgba(255,180,0,0.40);
  cursor: pointer; font-weight: 600;
  &:hover { background: rgba(255,180,0,0.20); }
}

.action-bar {
  display: flex; gap: 14px; margin-top: 20px;
  width: 100%; max-width: 720px;
}
.big-btn {
  flex: 1; height: 56px; border-radius: 16px;
  display: flex; align-items: center; justify-content: center; gap: 6px;
  font-weight: 700; font-size: 15px;
  border: none; cursor: pointer;
  color: #fff;
  transition: transform .15s, box-shadow .15s;
  &:hover { transform: translateY(-2px); }
}
.strange { background: linear-gradient(135deg, #FF5A5F, #FF8A65); box-shadow: 0 10px 20px rgba(255,90,95,0.30); }
.hazy { background: linear-gradient(135deg, #FFB400, #FFC845); box-shadow: 0 10px 20px rgba(255,180,0,0.30); color: #4A3000; }
.familiar { background: linear-gradient(135deg, #00C896, #2D6BFF); box-shadow: 0 10px 20px rgba(0,200,150,0.30); }

.empty {
  margin: auto; text-align: center;
  padding: 60px 20px;
  .emoji { font-size: 80px; margin-bottom: 16px; }
  h2 { font-size: 28px; font-weight: 800; margin-bottom: 8px; }
  .btn-primary { margin-top: 24px; }
}

@media (max-width: 768px) {
  .study-page { padding: 12px; }
  .flip-card { height: 420px; }
  .spelling { font-size: 30px; }
  .topbar { padding: 10px 12px; gap: 10px; }
  .progress-info { font-size: 12px; }
}
</style>
