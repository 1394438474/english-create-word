<template>
  <div class="page">
    <div class="page-head flex-between">
      <div>
        <h1 class="page-title">打卡日历</h1>
        <p class="page-subtitle">每一次坚持，都在塑造更好的自己</p>
      </div>
      <button class="btn-primary" @click="onCheckIn" :disabled="Boolean(todayChecked)">
        <el-icon><Check /></el-icon>
        {{ todayChecked ? '今日已打卡' : '立即打卡' }}
      </button>
    </div>

    <section class="streak-banner glass">
      <div class="streak-card">
        <div class="streak-num mono">{{ data.streakDays || 0 }}</div>
        <div class="streak-l">连续打卡</div>
      </div>
      <div class="streak-card">
        <div class="streak-num mono">{{ data.totalDays || 0 }}</div>
        <div class="streak-l">累计天数</div>
      </div>
      <div class="streak-card">
        <div class="streak-num mono">{{ data.totalLearned || 0 }}</div>
        <div class="streak-l">累计学习</div>
      </div>
      <div class="streak-card heat">
        <div class="heat-title">近 30 天热力</div>
        <div class="heat-grid">
          <div v-for="d in heatMap" :key="d.date" class="heat-cell" :class="'lv' + d.level" :title="`${d.date}: ${d.value} 词`"></div>
        </div>
      </div>
    </section>

    <section class="calendar glass">
      <div class="cal-head">
        <button class="cal-nav" @click="prevMonth"><el-icon><ArrowLeft /></el-icon></button>
        <div class="cal-title serif">{{ year }} 年 {{ month }} 月</div>
        <button class="cal-nav" @click="nextMonth"><el-icon><ArrowRight /></el-icon></button>
      </div>
      <div class="cal-week">
        <div v-for="w in weeks" :key="w">{{ w }}</div>
      </div>
      <div class="cal-grid">
        <div v-for="(d, i) in calendarDays" :key="i" class="cal-day" :class="{ empty: !d.day, today: d.isToday, checked: d.checked }">
          <span v-if="d.day">{{ d.day }}</span>
          <span v-if="d.checked" class="dot"></span>
        </div>
      </div>
    </section>

    <section class="medal-wall">
      <h3 class="sec-title serif">勋章墙</h3>
      <div class="medal-grid">
        <div v-for="(m, i) in medals" :key="m.code" class="medal fade-up" :class="{ owned: m.owned }" :style="{ animationDelay: (i*0.05) + 's' }">
          <div class="medal-icon">{{ m.icon }}</div>
          <div class="medal-name">{{ m.name }}</div>
          <div class="medal-desc text-mute">{{ m.description }}</div>
          <div v-if="!m.owned" class="medal-lock">未解锁</div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { checkInApi, statApi } from '@/api/study'
import dayjs from 'dayjs'

const year = ref(dayjs().year())
const month = ref(dayjs().month() + 1)
const weeks = ['日', '一', '二', '三', '四', '五', '六']
const data = reactive<any>({})
const calendarMap = ref<Record<string, number>>({})
const medals = ref<any[]>([])
const heatMap = ref<{ date: string; value: number; level: number }[]>([])
const todayChecked = computed(() => calendarMap.value[dayjs().format('YYYY-MM-DD')])

const calendarDays = computed(() => {
  const first = dayjs(`${year.value}-${month.value}-1`)
  const days = first.daysInMonth()
  const offset = first.day()
  const arr: any[] = []
  for (let i = 0; i < offset; i++) arr.push({ day: null })
  for (let i = 1; i <= days; i++) {
    const date = `${year.value}-${String(month.value).padStart(2, '0')}-${String(i).padStart(2, '0')}`
    arr.push({
      day: i,
      isToday: date === dayjs().format('YYYY-MM-DD'),
      checked: calendarMap.value[date] !== undefined
    })
  }
  return arr
})

async function loadCal() {
  const res = await checkInApi.calendar(year.value, month.value)
  calendarMap.value = res.days || {}
}

async function loadAll() {
  Object.assign(data, await statApi.dashboard())
  await loadCal()
  medals.value = await checkInApi.medals()
  const trend = await statApi.trend(30)
  heatMap.value = trend.map(t => {
    const v = Number(t.learned || 0)
    return { date: t.date, value: v, level: v === 0 ? 0 : v < 5 ? 1 : v < 15 ? 2 : v < 30 ? 3 : 4 }
  })
}

async function onCheckIn() {
  try {
    const res = await checkInApi.do(data.totalLearned || 10, 0)
    ElMessage.success(`打卡成功！连续 ${res.streakDays} 天`)
    if (res.unlockedMedals?.length) ElMessage.success(`🎉 恭喜解锁 ${res.unlockedMedals.length} 枚勋章！`)
    loadAll()
  } catch (e: any) { /* handled */ }
}

function prevMonth() {
  if (month.value === 1) { year.value--; month.value = 12 } else month.value--
  loadCal()
}
function nextMonth() {
  if (month.value === 12) { year.value++; month.value = 1 } else month.value++
  loadCal()
}

onMounted(loadAll)
</script>

<style lang="scss" scoped>
.page-head { margin-bottom: 20px; align-items: flex-end; }

.streak-banner {
  display: grid; grid-template-columns: repeat(3, 1fr) 2fr; gap: 16px;
  padding: 24px; border-radius: 20px; margin-bottom: 24px;
  border: 1px solid rgba(255,255,255,0.5);
}
.streak-card { text-align: center; padding: 16px; background: var(--bg-page); border-radius: 14px; }
.streak-num { font-size: 36px; font-weight: 800; background: var(--grad-primary); -webkit-background-clip: text; background-clip: text; color: transparent; line-height: 1; }
.streak-l { color: var(--text-mute); font-size: 13px; margin-top: 6px; }
.heat { text-align: left; padding: 14px 16px; }
.heat-title { font-size: 13px; color: var(--text-mute); margin-bottom: 8px; }
.heat-grid { display: grid; grid-template-columns: repeat(15, 1fr); gap: 4px; }
.heat-cell { aspect-ratio: 1; border-radius: 3px; background: #EEF0F5; }
.heat-cell.lv1 { background: rgba(45,107,255,0.20); }
.heat-cell.lv2 { background: rgba(45,107,255,0.45); }
.heat-cell.lv3 { background: rgba(45,107,255,0.70); }
.heat-cell.lv4 { background: var(--primary); }

.calendar { padding: 22px; border-radius: 20px; margin-bottom: 24px; border: 1px solid rgba(255,255,255,0.5); }
.cal-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.cal-title { font-size: 18px; font-weight: 800; }
.cal-nav { width: 32px; height: 32px; border-radius: 8px; background: var(--bg-page); border: none; cursor: pointer; display: flex; align-items: center; justify-content: center; &:hover { background: rgba(45,107,255,0.08); color: var(--primary); } }
.cal-week { display: grid; grid-template-columns: repeat(7, 1fr); text-align: center; color: var(--text-mute); font-size: 12px; padding: 6px 0; }
.cal-grid { display: grid; grid-template-columns: repeat(7, 1fr); gap: 6px; }
.cal-day {
  aspect-ratio: 1; display: flex; align-items: center; justify-content: center;
  border-radius: 8px; font-size: 13px; position: relative;
  color: var(--text-primary);
  &:not(.empty):hover { background: var(--bg-page); }
  &.today { background: var(--grad-primary); color: #fff; font-weight: 800; }
  &.checked { background: rgba(0,200,150,0.10); color: var(--accent); font-weight: 600; }
  &.checked.today { background: var(--grad-primary); color: #fff; }
}
.cal-day .dot { position: absolute; bottom: 4px; width: 4px; height: 4px; border-radius: 50%; background: var(--accent); }
.cal-day.empty { cursor: default; }

.medal-wall { margin-top: 8px; }
.sec-title { font-size: 18px; font-weight: 800; margin-bottom: 14px; }
.medal-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(150px, 1fr)); gap: 14px; }
.medal {
  position: relative;
  padding: 18px 14px; text-align: center; border-radius: 16px;
  background: #fff; box-shadow: 0 4px 14px rgba(31,41,78,0.05);
  opacity: 0.5; filter: grayscale(0.8);
  transition: all .2s;
  &.owned { opacity: 1; filter: none; &:hover { transform: translateY(-3px); box-shadow: 0 10px 24px rgba(31,41,78,0.12); } }
}
.medal-icon { font-size: 42px; margin-bottom: 6px; }
.medal-name { font-weight: 700; }
.medal-desc { font-size: 12px; margin-top: 2px; }
.medal-lock { position: absolute; top: 8px; right: 8px; font-size: 10px; color: var(--text-mute); background: var(--bg-page); padding: 2px 6px; border-radius: 6px; }

@media (max-width: 768px) {
  .streak-banner { grid-template-columns: repeat(2, 1fr); }
  .heat { grid-column: 1 / -1; }
  .heat-grid { grid-template-columns: repeat(15, 1fr); }
}
</style>
