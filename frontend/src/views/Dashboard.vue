<template>
  <div class="page dashboard">
    <header class="hero glass">
      <div class="hero-text">
        <div class="hello">
          <span class="text-mute">{{ greet }}，</span>
          <span class="nickname gradient-text">{{ data.nickname || '同学' }}</span>
        </div>
        <h1 class="hero-title serif">今日，向着你的目标再进一步 🚀</h1>
        <p class="hero-desc">智绘记已为你准备好个性化学习计划，开启高效记忆吧</p>
        <div class="hero-actions">
          <button class="btn-primary" @click="goStudy">
            <el-icon><Notebook /></el-icon>
            继续学习
          </button>
          <button class="btn-ghost" @click="$router.push('/review')">
            <el-icon><Refresh /></el-icon>
            智能复习 ({{ data.todayReview }})
          </button>
        </div>
      </div>
      <div class="hero-illus">
        <div class="orb"></div>
        <div class="orb-mini o1"></div>
        <div class="orb-mini o2"></div>
        <div class="orb-mini o3"></div>
        <div class="big-icon">📚</div>
      </div>
    </header>

    <section class="kpi-grid">
      <div class="kpi-card kpi-1 fade-up" @click="$router.push('/books')">
        <div class="kpi-label">累计学习</div>
        <div class="kpi-value mono">{{ data.totalLearned || 0 }}</div>
        <div class="kpi-unit">词</div>
        <div class="kpi-icon"><el-icon :size="22"><Reading /></el-icon></div>
      </div>
      <div class="kpi-card kpi-2 fade-up delay-1" @click="$router.push('/vocabulary')">
        <div class="kpi-label">已掌握</div>
        <div class="kpi-value mono">{{ data.mastered || 0 }}</div>
        <div class="kpi-unit">词</div>
        <div class="kpi-icon"><el-icon :size="22"><Trophy /></el-icon></div>
      </div>
      <div class="kpi-card kpi-3 fade-up delay-2" @click="$router.push('/checkin')">
        <div class="kpi-label">连续打卡</div>
        <div class="kpi-value mono">{{ data.streakDays || 0 }}</div>
        <div class="kpi-unit">天</div>
        <div class="kpi-icon"><el-icon :size="22"><Sunny /></el-icon></div>
      </div>
      <div class="kpi-card kpi-4 fade-up delay-3" @click="$router.push('/errorbook')">
        <div class="kpi-label">错题数量</div>
        <div class="kpi-value mono">{{ data.errorCount || 0 }}</div>
        <div class="kpi-unit">题</div>
        <div class="kpi-icon"><el-icon :size="22"><Warning /></el-icon></div>
      </div>
    </section>

    <section class="chart-row">
      <div class="chart-card big fade-up">
        <div class="chart-head">
          <h3>近 14 天学习趋势</h3>
          <span class="text-mute">每日学习量 / 打卡</span>
        </div>
        <v-chart class="chart" :option="trendOption" autoresize />
      </div>
      <div class="chart-card fade-up delay-1">
        <div class="chart-head">
          <h3>能力雷达</h3>
          <span class="text-mute">六维度评估</span>
        </div>
        <v-chart class="chart" :option="radarOption" autoresize />
      </div>
    </section>

    <section class="chart-row">
      <div class="chart-card fade-up">
        <div class="chart-head">
          <h3>掌握度分布</h3>
          <span class="text-mute">已学 {{ mastery.total || 0 }} 词</span>
        </div>
        <v-chart class="chart" :option="masteryOption" autoresize />
      </div>
      <div class="chart-card fade-up delay-1">
        <div class="chart-head">
          <h3>词书学习进度</h3>
          <span class="text-mute">{{ data.bookProgress?.length || 0 }} 本进行中</span>
        </div>
        <div class="book-list">
          <div v-for="b in data.bookProgress || []" :key="b.bookId" class="book-item">
            <div class="book-info">
              <div class="dot" :style="{ background: b.color }"></div>
              <div class="book-name">{{ b.bookName }}</div>
              <div class="book-stat mono">{{ b.learned }}/{{ b.total }}</div>
            </div>
            <div class="bar"><div class="bar-fill" :style="{ width: b.progress + '%', background: b.color }"></div></div>
          </div>
          <div v-if="!data.bookProgress?.length" class="empty">还没有进行中的词书，去 <router-link to="/books">词书广场</router-link> 选一本吧</div>
        </div>
      </div>
    </section>

    <section class="chart-row">
      <div class="chart-card fade-up">
        <div class="chart-head">
          <h3>薄弱词 TOP {{ weak.length }}</h3>
          <span class="text-mute">AI 高频识别，建议优先攻克</span>
        </div>
        <div class="weak-list">
          <div v-for="(w, i) in weak" :key="w.wordId" class="weak-item">
            <div class="rank" :class="'r' + (i+1)">{{ i + 1 }}</div>
            <div class="weak-info">
              <div class="weak-meta">
                <span class="weak-type chip chip-danger">{{ w.quizType }}</span>
                <span class="weak-time text-mute">{{ formatTime(w.lastErrorTime) }}</span>
              </div>
              <div class="weak-count">错误 <b class="mono text-danger">{{ w.errorCount }}</b> 次</div>
            </div>
          </div>
          <div v-if="!weak.length" class="empty">还没有错题，继续保持！</div>
        </div>
      </div>
      <div class="chart-card quote fade-up delay-1">
        <div class="quote-mark">"</div>
        <h3 class="quote-title serif">学习，是这个时代唯一稳赚不赔的投入</h3>
        <p class="quote-sub">坚持 21 天，让背单词成为一种生活方式</p>
        <div class="quote-stats">
          <div><div class="qs-num mono">{{ data.totalDays || 0 }}</div><div class="qs-l">累计天数</div></div>
          <div><div class="qs-num mono">{{ data.vocabCount || 0 }}</div><div class="qs-l">生词本</div></div>
          <div><div class="qs-num mono">{{ data.todayReview || 0 }}</div><div class="qs-l">今日待复习</div></div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { BarChart, LineChart, PieChart, RadarChart } from 'echarts/charts'
import {
  GridComponent, TooltipComponent, LegendComponent, TitleComponent, RadarComponent
} from 'echarts/components'
import VChart from 'vue-echarts'
import { statApi } from '@/api/study'
import dayjs from 'dayjs'

use([CanvasRenderer, BarChart, LineChart, PieChart, RadarChart, GridComponent, TooltipComponent, LegendComponent, TitleComponent, RadarComponent])

const router = useRouter()
const data = reactive<any>({})
const trend = ref<any[]>([])
const mastery = ref<any>({ items: [], total: 0 })
const weak = ref<any[]>([])
const radar = ref<any>({ indicator: [], value: [] })

const greet = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '夜深了'
  if (h < 11) return '早上好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
})

const trendOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: 30, right: 20, top: 30, bottom: 30 },
  xAxis: {
    type: 'category',
    data: trend.value.map(t => dayjs(t.date).format('MM-DD')),
    axisLine: { lineStyle: { color: '#E5E8F0' } },
    axisLabel: { color: '#8C95A8', fontSize: 11 }
  },
  yAxis: {
    type: 'value',
    splitLine: { lineStyle: { color: '#EEF0F5' } },
    axisLabel: { color: '#8C95A8', fontSize: 11 }
  },
  series: [
    {
      name: '学习量',
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 8,
      data: trend.value.map(t => t.learned),
      itemStyle: { color: '#2D6BFF' },
      lineStyle: { width: 3 },
      areaStyle: {
        color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [
          { offset: 0, color: 'rgba(45,107,255,0.32)' },
          { offset: 1, color: 'rgba(45,107,255,0)' }
        ]}
      }
    }
  ]
}))

const radarOption = computed(() => ({
  tooltip: {},
  radar: {
    indicator: (radar.value.indicator || []).map((name: string) => ({ name, max: 1 })),
    radius: '64%',
    splitArea: { areaStyle: { color: ['rgba(45,107,255,0.04)', 'rgba(45,107,255,0.08)'] } },
    splitLine: { lineStyle: { color: 'rgba(45,107,255,0.20)' } },
    axisLine: { lineStyle: { color: 'rgba(45,107,255,0.20)' } },
    name: { textStyle: { color: '#4B5566', fontSize: 12 } }
  },
  series: [{
    type: 'radar',
    data: [{
      value: radar.value.value || [],
      name: '能力',
      areaStyle: { color: 'rgba(0,200,150,0.30)' },
      lineStyle: { color: '#00C896', width: 2 },
      itemStyle: { color: '#00C896' }
    }]
  }]
}))

const masteryOption = computed(() => {
  const items = mastery.value.items || []
  return {
    tooltip: { trigger: 'item' },
    legend: { bottom: 0, textStyle: { color: '#4B5566' } },
    series: [{
      type: 'pie',
      radius: ['50%', '76%'],
      center: ['50%', '46%'],
      avoidLabelOverlap: true,
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 3 },
      label: { show: false },
      data: items.map((it: any, i: number) => ({
        ...it,
        itemStyle: { color: ['#2D6BFF', '#00C896', '#FFB400', '#FF5A5F', '#8C95A8'][i % 5] }
      }))
    }]
  }
})

function formatTime(t: string) {
  return t ? dayjs(t).format('MM-DD HH:mm') : '-'
}

function goStudy() {
  const first = data.bookProgress?.[0]
  if (first) router.push(`/study/${first.bookId}`)
  else router.push('/books')
}

onMounted(async () => {
  Object.assign(data, await statApi.dashboard())
  trend.value = await statApi.trend(14)
  mastery.value = await statApi.mastery()
  weak.value = await statApi.weak(8)
  radar.value = await statApi.radar()
})
</script>

<style lang="scss" scoped>
.dashboard { padding: 16px 16px 80px; }
.hero {
  position: relative;
  display: flex; align-items: center; justify-content: space-between;
  padding: 32px 36px; border-radius: 24px; margin-bottom: 24px;
  overflow: hidden;
  background: linear-gradient(135deg, #FFFFFF 0%, #F0F5FF 100%);
  border: 1px solid rgba(45,107,255,0.12);
}
.hello { font-size: 14px; }
.nickname { font-size: 18px; font-weight: 800; margin-left: 4px; }
.hero-title { font-size: 32px; font-weight: 800; margin: 8px 0 10px; line-height: 1.2; }
.hero-desc { color: var(--text-mute); font-size: 14px; margin-bottom: 18px; }
.hero-actions { display: flex; gap: 12px; }
.btn-primary, .btn-ghost { display: inline-flex; align-items: center; gap: 6px; }

.hero-illus { position: relative; width: 220px; height: 160px; }
.orb {
  position: absolute; width: 160px; height: 160px; border-radius: 50%;
  background: radial-gradient(circle, rgba(45,107,255,0.45) 0%, rgba(45,107,255,0) 70%);
  top: 0; right: 30px; animation: float 6s ease-in-out infinite;
}
.orb-mini { position: absolute; border-radius: 50%; animation: float 4s ease-in-out infinite; }
.orb-mini.o1 { width: 24px; height: 24px; background: var(--grad-warm); top: 20px; left: 20px; }
.orb-mini.o2 { width: 16px; height: 16px; background: var(--grad-accent); bottom: 20px; right: 0; animation-delay: 1s; }
.orb-mini.o3 { width: 12px; height: 12px; background: var(--grad-primary); top: 50%; left: 50%; animation-delay: 2s; }
.big-icon { position: absolute; font-size: 80px; right: 60px; top: 30px; filter: drop-shadow(0 8px 16px rgba(31,41,78,0.20)); }

.kpi-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 24px; }
.kpi-card {
  position: relative; padding: 22px 24px; border-radius: 18px;
  background: #fff; box-shadow: 0 4px 14px rgba(31,41,78,0.06);
  cursor: pointer; transition: transform .2s, box-shadow .2s;
  overflow: hidden;
  &:hover { transform: translateY(-3px); box-shadow: 0 12px 28px rgba(31,41,78,0.10); }
  &::before { content: ''; position: absolute; top: 0; left: 0; right: 0; height: 4px; }
  &.kpi-1::before { background: var(--grad-primary); }
  &.kpi-2::before { background: var(--grad-accent); }
  &.kpi-3::before { background: var(--grad-warm); }
  &.kpi-4::before { background: linear-gradient(135deg, #FF5A5F, #FF8A65); }
}
.kpi-label { color: var(--text-mute); font-size: 13px; }
.kpi-value { font-size: 36px; font-weight: 800; line-height: 1.1; margin-top: 6px; color: var(--text-primary); }
.kpi-unit { color: var(--text-mute); font-size: 13px; }
.kpi-icon {
  position: absolute; right: 18px; top: 50%; transform: translateY(-50%);
  width: 44px; height: 44px; border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
  color: #fff;
}
.kpi-1 .kpi-icon { background: var(--grad-primary); }
.kpi-2 .kpi-icon { background: var(--grad-accent); }
.kpi-3 .kpi-icon { background: var(--grad-warm); }
.kpi-4 .kpi-icon { background: linear-gradient(135deg, #FF5A5F, #FF8A65); }

.chart-row { display: grid; grid-template-columns: 1.4fr 1fr; gap: 16px; margin-bottom: 16px; }
.chart-card {
  background: #fff; border-radius: 18px; padding: 20px;
  box-shadow: 0 4px 14px rgba(31,41,78,0.06);
  min-height: 320px;
  display: flex; flex-direction: column;
}
.chart-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.chart-head h3 { font-size: 16px; font-weight: 700; }
.chart { flex: 1; min-height: 260px; }

.book-list { display: flex; flex-direction: column; gap: 14px; padding: 4px 2px; }
.book-item { display: flex; flex-direction: column; gap: 6px; }
.book-info { display: flex; align-items: center; gap: 10px; font-size: 13px; }
.dot { width: 10px; height: 10px; border-radius: 50%; }
.book-name { flex: 1; font-weight: 600; }
.book-stat { color: var(--text-mute); font-size: 12px; }
.bar { height: 8px; background: #EEF0F5; border-radius: 4px; overflow: hidden; }
.bar-fill { height: 100%; border-radius: 4px; transition: width 1s ease; }

.weak-list { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; padding: 4px 2px; }
.weak-item { display: flex; align-items: center; gap: 12px; padding: 12px; border-radius: 12px; background: var(--bg-page); }
.rank {
  width: 28px; height: 28px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  background: #EEF0F5; color: var(--text-mute); font-weight: 700; font-size: 12px;
}
.rank.r1 { background: linear-gradient(135deg, #FF5A5F, #FF8A65); color: #fff; }
.rank.r2 { background: linear-gradient(135deg, #FFB400, #FF8A65); color: #fff; }
.rank.r3 { background: linear-gradient(135deg, #00C896, #2D6BFF); color: #fff; }
.weak-info { flex: 1; }
.weak-meta { display: flex; align-items: center; gap: 6px; font-size: 12px; margin-bottom: 4px; }
.weak-count { font-size: 13px; }
.empty { color: var(--text-mute); font-size: 13px; padding: 20px; text-align: center; }

.quote { position: relative; padding: 24px; background: linear-gradient(135deg, #1A1F36 0%, #2D6BFF 100%); color: #fff; }
.quote-mark { font-family: 'Sora', serif; font-size: 80px; line-height: 0.4; opacity: 0.3; }
.quote-title { color: #fff; font-size: 20px; font-weight: 800; margin: 12px 0 8px; }
.quote-sub { color: rgba(255,255,255,0.7); font-size: 13px; }
.quote-stats { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; margin-top: 24px; }
.quote-stats > div { background: rgba(255,255,255,0.10); border-radius: 12px; padding: 14px 12px; text-align: center; }
.qs-num { font-size: 26px; font-weight: 800; }
.qs-l { font-size: 12px; color: rgba(255,255,255,0.7); margin-top: 4px; }

@media (max-width: 1000px) {
  .kpi-grid { grid-template-columns: repeat(2, 1fr); }
  .chart-row { grid-template-columns: 1fr; }
  .hero { flex-direction: column; align-items: flex-start; padding: 24px; }
  .hero-illus { display: none; }
  .hero-title { font-size: 22px; }
  .weak-list { grid-template-columns: 1fr; }
}
</style>
