<template>
  <div class="page">
    <div class="page-head flex-between">
      <div>
        <h1 class="page-title">智能复习</h1>
        <p class="page-subtitle">AI 按艾宾浩斯曲线自动筛选今日必须复习的 {{ list.length }} 个单词</p>
      </div>
      <div class="head-stat glass">
        <span class="text-mute">待复习</span>
        <span class="mono big-num">{{ list.length }}</span>
      </div>
    </div>

    <div v-if="current" class="review-stage">
      <div class="review-card fade-up" :key="current.id">
        <div class="review-image" :style="{ backgroundImage: `url(${current.imageUrl})` }">
          <div class="img-shade"></div>
          <div class="word-overlay">
            <h1 class="spelling serif">{{ current.spelling }}</h1>
            <div class="phonetic mono">/{{ current.phoneticUs?.replace(/[\/\[\]]/g, '') }}/</div>
            <div class="meaning" v-if="revealed">{{ current.meanings?.[0]?.meaningZh }}</div>
            <button v-else class="reveal-btn" @click="revealed = true">
              <el-icon><View /></el-icon> 显示释义
            </button>
          </div>
        </div>
        <div class="review-info" v-if="revealed">
          <div class="info-row">
            <span class="info-label">稳定性</span>
            <el-progress :percentage="Math.min(100, (current.stability || 0) * 20)" :show-text="false" :stroke-width="6" color="#2D6BFF" />
            <span class="mono text-mute">{{ (current.stability || 0).toFixed(2) }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">置信度</span>
            <el-progress :percentage="Math.min(100, (current.confidence || 0) * 100)" :show-text="false" :stroke-width="6" color="#00C896" />
            <span class="mono text-mute">{{ ((current.confidence || 0) * 100).toFixed(0) }}%</span>
          </div>
          <div class="examples" v-if="current.sentences?.length">
            <div class="ex-title">例句</div>
            <div v-for="(s, i) in current.sentences.slice(0, 1)" :key="i" class="ex-item">
              <div class="ex-en">{{ s.en }}</div>
              <div class="ex-zh text-mute">{{ s.zh }}</div>
            </div>
          </div>
        </div>
        <div class="action-bar" v-if="revealed">
          <button class="action-btn strange" @click="onMark('STRANGE')">陌生</button>
          <button class="action-btn hazy" @click="onMark('HAZY')">模糊</button>
          <button class="action-btn familiar" @click="onMark('FAMILIAR')">记得</button>
        </div>
      </div>
    </div>

    <div v-else class="empty">
      <div class="emoji">✨</div>
      <h2 class="serif">今日复习已完成！</h2>
      <p class="text-mute">系统将在合适的时间再次提醒你</p>
      <button class="btn-primary" @click="$router.push('/dashboard')">返回大屏</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { reviewApi } from '@/api/learn'
import { type WordCard } from '@/api/book'

const list = ref<WordCard[]>([])
const idx = ref(0)
const revealed = ref(false)
const startTime = ref(0)
const current = computed(() => list.value[idx.value])

async function load() {
  list.value = await reviewApi.today(30)
  if (list.value.length > 0) startTime.value = Date.now()
}

async function onMark(status: 'FAMILIAR' | 'HAZY' | 'STRANGE') {
  const w = current.value
  if (!w) return
  const duration = Date.now() - startTime.value
  await reviewApi.record({ bookId: w.bookId, wordId: w.id, status, durationMs: duration })
  ElMessage.success('已记录')
  revealed.value = false
  idx.value++
  if (idx.value < list.value.length) startTime.value = Date.now()
}

onMounted(load)
</script>

<style lang="scss" scoped>
.page-head { margin-bottom: 20px; align-items: flex-end; }
.head-stat { display: flex; flex-direction: column; align-items: center; padding: 10px 24px; border-radius: 14px; }
.big-num { font-size: 32px; font-weight: 800; color: var(--primary); line-height: 1.1; }

.review-stage { display: flex; justify-content: center; }
.review-card {
  width: 100%; max-width: 720px; background: #fff;
  border-radius: 24px; overflow: hidden;
  box-shadow: 0 20px 60px rgba(31,41,78,0.10);
}
.review-image {
  position: relative; height: 360px; background-size: cover; background-position: center;
  background-color: #EEF0F5;
}
.img-shade {
  position: absolute; inset: 0;
  background: linear-gradient(180deg, rgba(0,0,0,0.10) 0%, rgba(0,0,0,0.40) 100%);
}
.word-overlay {
  position: absolute; inset: 0; padding: 28px 32px;
  display: flex; flex-direction: column; justify-content: flex-end; color: #fff;
}
.spelling { font-size: 48px; font-weight: 800; color: #fff; line-height: 1; text-shadow: 0 4px 16px rgba(0,0,0,0.30); }
.phonetic { font-size: 18px; opacity: 0.9; margin: 4px 0 12px; }
.meaning { font-size: 22px; font-weight: 700; }
.reveal-btn {
  display: inline-flex; align-items: center; gap: 4px;
  background: rgba(255,255,255,0.20); color: #fff; border: 1px solid rgba(255,255,255,0.40);
  padding: 8px 16px; border-radius: 10px; cursor: pointer; font-weight: 600;
  align-self: flex-start;
  backdrop-filter: blur(6px);
  &:hover { background: rgba(255,255,255,0.32); }
}

.review-info { padding: 20px 28px; display: flex; flex-direction: column; gap: 10px; }
.info-row { display: flex; align-items: center; gap: 12px; }
.info-label { color: var(--text-mute); font-size: 13px; min-width: 56px; }
.info-row :deep(.el-progress) { flex: 1; }

.examples { margin-top: 10px; }
.ex-title { font-size: 12px; color: var(--text-mute); margin-bottom: 6px; }
.ex-item { padding: 10px 14px; background: var(--bg-page); border-radius: 10px; }
.ex-en { font-size: 14px; }
.ex-zh { font-size: 12px; margin-top: 2px; }

.action-bar { display: flex; gap: 12px; padding: 0 28px 24px; }
.action-btn {
  flex: 1; height: 48px; border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
  font-weight: 700; color: #fff; border: none; cursor: pointer;
  transition: transform .15s;
  &:hover { transform: translateY(-2px); }
}
.strange { background: linear-gradient(135deg, #FF5A5F, #FF8A65); }
.hazy { background: linear-gradient(135deg, #FFB400, #FFC845); color: #4A3000; }
.familiar { background: linear-gradient(135deg, #00C896, #2D6BFF); }

.empty { text-align: center; padding: 80px 20px; }
.empty .emoji { font-size: 80px; margin-bottom: 12px; }
.empty h2 { font-size: 24px; font-weight: 800; margin-bottom: 8px; }
.empty .btn-primary { margin-top: 24px; }

@media (max-width: 768px) {
  .spelling { font-size: 36px; }
  .review-image { height: 280px; }
}
</style>
