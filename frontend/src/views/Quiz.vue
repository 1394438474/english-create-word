<template>
  <div class="page">
    <div class="page-head flex-between">
      <div>
        <h1 class="page-title">智能测试</h1>
        <p class="page-subtitle">系统根据你的薄弱点自动组卷，检验学习成果</p>
      </div>
      <div class="head-actions">
        <el-select v-model="quizType" style="width: 140px">
          <el-option label="英译汉" value="EN_ZH" />
          <el-option label="汉译英" value="ZH_EN" />
          <el-option label="选词填空" value="CLOZE" />
        </el-select>
        <el-select v-model="bookId" placeholder="选择词书" style="width: 180px">
          <el-option v-for="b in books" :key="b.id" :label="b.name" :value="b.id" />
        </el-select>
        <el-button type="primary" :disabled="!bookId" @click="onGenerate">开始测试</el-button>
      </div>
    </div>

    <div v-if="!quiz" class="hero">
      <div class="hero-card glass">
        <h2 class="serif">为什么需要测试？</h2>
        <p>研究表明，主动回忆（Active Recall）能让记忆留存率提升 60%。系统会根据你的错题率智能组卷，帮你精准定位薄弱点。</p>
        <div class="tip-grid">
          <div class="tip-item"><b>英译汉</b><span>看英文选中文，夯实基础</span></div>
          <div class="tip-item"><b>汉译英</b><span>看中文选英文，强化输出</span></div>
          <div class="tip-item"><b>选词填空</b><span>语境运用，提升实战</span></div>
        </div>
        <el-button type="primary" size="large" :disabled="!bookId" @click="onGenerate">立即开始</el-button>
      </div>
    </div>

    <div v-else-if="!result" class="quiz-stage">
      <div class="quiz-progress glass">
        <div class="quiz-info">
          <span>第 <b class="mono">{{ idx + 1 }}</b> / {{ quiz.questions.length }} 题</span>
          <span class="text-mute">{{ typeMap[quiz.type] }} · 用时 {{ elapsed }}s</span>
        </div>
        <el-progress :percentage="((idx + 1) / quiz.questions.length) * 100" :show-text="false" :stroke-width="6" color="#2D6BFF" />
      </div>

      <div class="quiz-card" :key="idx">
        <div class="quiz-prompt">
          <div class="prompt-image" v-if="quiz.questions[idx].imageUrl" :style="{ backgroundImage: `url(${quiz.questions[idx].imageUrl})` }"></div>
          <div class="prompt-text serif">{{ quiz.questions[idx].prompt }}</div>
        </div>
        <div class="quiz-options">
          <button v-for="(opt, i) in quiz.questions[idx].options" :key="i" class="opt" :class="{ active: chosen === i }" @click="chosen = i">
            <span class="opt-label">{{ opt.label }}</span>
            <span class="opt-text">{{ opt.text }}</span>
          </button>
        </div>
        <div class="quiz-foot">
          <el-button :disabled="idx === 0" @click="prevQ">上一题</el-button>
          <el-button v-if="idx < quiz.questions.length - 1" type="primary" :disabled="chosen === null" @click="nextQ">下一题</el-button>
          <el-button v-else type="primary" :disabled="chosen === null" @click="onSubmit">交卷</el-button>
        </div>
      </div>
    </div>

    <div v-else class="result-stage">
      <div class="result-card glass">
        <div class="score-ring" :style="{ '--p': result.score + '%' }">
          <div class="score-num mono">{{ result.score }}</div>
          <div class="score-l">分</div>
        </div>
        <h2 class="serif">测试完成！</h2>
        <p class="text-mute">{{ typeMap[quiz.type] }} · 共 {{ result.total }} 题，答对 {{ result.correct }} 题</p>
        <div class="result-stats">
          <div><b class="mono text-accent">{{ result.correct }}</b><span>答对</span></div>
          <div><b class="mono text-danger">{{ result.total - result.correct }}</b><span>答错</span></div>
          <div><b class="mono">{{ result.score }}%</b><span>正确率</span></div>
        </div>
        <div class="result-actions">
          <el-button @click="quiz = null; result = null; idx = 0; chosen = null">再测一次</el-button>
          <el-button type="primary" @click="$router.push('/errorbook')">查看错题</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { quizApi, type QuizDTO } from '@/api/study'
import { bookApi, type Book } from '@/api/book'

const books = ref<Book[]>([])
const bookId = ref<number | null>(null)
const quizType = ref('EN_ZH')
const quiz = ref<QuizDTO | null>(null)
const idx = ref(0)
const chosen = ref<number | null>(null)
const chosenLabels = ref<Record<number, string>>({})
const result = ref<QuizDTO | null>(null)
const startTime = ref(0)
const elapsed = ref(0)
const typeMap: Record<string, string> = { EN_ZH: '英译汉', ZH_EN: '汉译英', CLOZE: '选词填空' }
let timer: any

async function onGenerate() {
  if (!bookId.value) {
    ElMessage.warning('请先选择词书')
    return
  }
  result.value = null
  quiz.value = await quizApi.generate(quizType.value, bookId.value, 10)
  idx.value = 0
  chosen.value = null
  chosenLabels.value = {}
  startTime.value = Date.now()
  if (timer) clearInterval(timer)
  timer = setInterval(() => { elapsed.value = Math.floor((Date.now() - startTime.value) / 1000) }, 1000)
}

function nextQ() {
  if (chosen.value !== null) {
    const opt = quiz.value!.questions[idx.value].options[chosen.value]
    chosenLabels.value[quiz.value!.questions[idx.value].wordId] = opt.label
  }
  idx.value++
  chosen.value = null
}
function prevQ() {
  if (chosen.value !== null) {
    const opt = quiz.value!.questions[idx.value].options[chosen.value]
    chosenLabels.value[quiz.value!.questions[idx.value].wordId] = opt.label
  }
  idx.value--
  // 回显
  const prev = quiz.value!.questions[idx.value]
  const lbl = chosenLabels.value[prev.wordId]
  if (lbl) {
    chosen.value = prev.options.findIndex(o => o.label === lbl)
  } else {
    chosen.value = null
  }
}

async function onSubmit() {
  if (chosen.value !== null) {
    const opt = quiz.value!.questions[idx.value].options[chosen.value]
    chosenLabels.value[quiz.value!.questions[idx.value].wordId] = opt.label
  }
  const payload: QuizDTO = {
    ...quiz.value!,
    durationMs: Date.now() - startTime.value,
    chosenLabels: chosenLabels.value
  }
  result.value = await quizApi.submit(payload)
  if (timer) clearInterval(timer)
}

onMounted(async () => {
  books.value = await bookApi.list()
  if (books.value[0]) bookId.value = books.value[0].id
})
</script>

<style lang="scss" scoped>
.page-head { margin-bottom: 18px; align-items: flex-end; }
.head-actions { display: flex; gap: 10px; }

.hero { display: flex; justify-content: center; }
.hero-card {
  width: 100%; max-width: 720px; padding: 36px 40px; border-radius: 20px;
  text-align: center; border: 1px solid rgba(255,255,255,0.5);
}
.hero-card h2 { font-size: 24px; font-weight: 800; margin-bottom: 12px; }
.hero-card p { color: var(--text-secondary); line-height: 1.7; margin-bottom: 24px; }
.tip-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 14px; margin: 24px 0; }
.tip-item { padding: 16px; background: var(--bg-page); border-radius: 12px; text-align: left; }
.tip-item b { display: block; color: var(--primary); font-size: 15px; margin-bottom: 4px; }
.tip-item span { font-size: 12px; color: var(--text-mute); }

.quiz-stage { max-width: 720px; margin: 0 auto; }
.quiz-progress { padding: 14px 18px; border-radius: 14px; margin-bottom: 16px; }
.quiz-info { display: flex; justify-content: space-between; font-size: 13px; margin-bottom: 6px; }

.quiz-card {
  background: #fff; padding: 32px; border-radius: 20px;
  box-shadow: 0 12px 32px rgba(31,41,78,0.08);
  animation: fade-up .35s ease both;
}
.quiz-prompt { margin-bottom: 24px; }
.prompt-image {
  width: 100%; height: 220px; border-radius: 14px;
  background-size: cover; background-position: center; background-color: #EEF0F5;
  margin-bottom: 16px;
}
.prompt-text { font-size: 32px; font-weight: 800; text-align: center; }

.quiz-options { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
.opt {
  display: flex; align-items: center; gap: 12px;
  padding: 14px 18px; border-radius: 12px;
  background: var(--bg-page); border: 2px solid transparent;
  cursor: pointer; text-align: left;
  transition: all .15s;
  &:hover { border-color: var(--primary-light); }
  &.active {
    background: rgba(45,107,255,0.08); border-color: var(--primary);
    .opt-label { background: var(--primary); color: #fff; }
  }
}
.opt-label {
  width: 28px; height: 28px; border-radius: 50%;
  background: #fff; color: var(--text-secondary);
  display: flex; align-items: center; justify-content: center;
  font-weight: 800; font-size: 13px;
  transition: all .15s;
}
.opt-text { font-size: 15px; font-weight: 600; }

.quiz-foot { display: flex; justify-content: space-between; margin-top: 28px; }

.result-stage { display: flex; justify-content: center; }
.result-card {
  width: 100%; max-width: 540px; padding: 40px;
  text-align: center; border-radius: 24px;
  border: 1px solid rgba(255,255,255,0.5);
}
.score-ring {
  width: 160px; height: 160px; margin: 0 auto 24px;
  border-radius: 50%;
  background: conic-gradient(#2D6BFF 0% var(--p, 0%), #EEF0F5 var(--p, 0%) 100%);
  display: flex; align-items: center; justify-content: center; flex-direction: column;
  position: relative;
  &::before { content: ''; position: absolute; inset: 12px; background: #fff; border-radius: 50%; }
  .score-num, .score-l { position: relative; z-index: 1; }
}
.score-num { font-size: 48px; font-weight: 800; color: var(--primary); line-height: 1; }
.score-l { color: var(--text-mute); font-size: 12px; }
.result-card h2 { font-size: 24px; font-weight: 800; margin-bottom: 8px; }
.result-stats { display: flex; justify-content: space-around; margin: 24px 0; }
.result-stats > div { text-align: center; }
.result-stats b { font-size: 24px; font-weight: 800; display: block; }
.result-stats span { color: var(--text-mute); font-size: 12px; }
.result-actions { display: flex; justify-content: center; gap: 10px; }

@media (max-width: 768px) {
  .head-actions { flex-direction: column; }
  .quiz-options { grid-template-columns: 1fr; }
  .tip-grid { grid-template-columns: 1fr; }
  .prompt-text { font-size: 24px; }
  .prompt-image { height: 160px; }
}
</style>
