<template>
  <div class="layout">
    <aside class="sidebar" :class="{ collapsed: collapsed }">
      <div class="brand" @click="$router.push('/dashboard')">
        <div class="brand-mark">S</div>
        <div v-if="!collapsed" class="brand-text">
          <div class="brand-name serif">智绘记</div>
          <div class="brand-en">SmartVocab</div>
        </div>
      </div>
      <nav class="nav">
        <router-link v-for="m in menus" :key="m.path" :to="m.path" class="nav-item" :class="{ active: isActive(m.path) }">
          <el-icon :size="20"><component :is="m.icon" /></el-icon>
          <span v-if="!collapsed" class="nav-label">{{ m.title }}</span>
        </router-link>
      </nav>
      <div class="sidebar-foot">
        <div class="collapse-btn" @click="collapsed = !collapsed">
          <el-icon><component :is="collapsed ? 'Expand' : 'Fold'" /></el-icon>
          <span v-if="!collapsed">收起</span>
        </div>
      </div>
    </aside>

    <div class="main">
      <header class="topbar glass">
        <div class="crumb">
          <el-icon><LocationFilled /></el-icon>
          <span class="crumb-text">{{ currentTitle }}</span>
        </div>
        <div class="topbar-right">
          <div class="streak" @click="$router.push('/checkin')">
            <el-icon class="text-gold"><Sunny /></el-icon>
            <span>连续 <b class="mono">{{ user.user?.streakDays || 0 }}</b> 天</span>
          </div>
          <el-dropdown trigger="click" @command="onCommand">
            <div class="user-chip">
              <img class="avatar" :src="user.user?.avatar || defaultAvatar" alt="" />
              <span class="nickname">{{ user.user?.nickname || '游客' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile"><el-icon><User /></el-icon> 个人中心</el-dropdown-item>
                <el-dropdown-item command="checkin"><el-icon><Calendar /></el-icon> 打卡</el-dropdown-item>
                <el-dropdown-item divided command="logout"><el-icon><SwitchButton /></el-icon> 退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <main class="content">
        <router-view v-slot="{ Component }">
          <transition name="page" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>

      <nav class="mobile-tabbar">
        <router-link v-for="m in mobileMenus" :key="m.path" :to="m.path" class="tab-item" :class="{ active: isActive(m.path) }">
          <el-icon :size="22"><component :is="m.icon" /></el-icon>
          <span>{{ m.title }}</span>
        </router-link>
      </nav>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const user = useUserStore()
const collapsed = ref(false)
const defaultAvatar = 'https://api.dicebear.com/7.x/avataaars/svg?seed=guest'

const menus = [
  { path: '/dashboard', title: '数据大屏', icon: 'DataAnalysis' },
  { path: '/books', title: '词书广场', icon: 'Reading' },
  { path: '/review', title: '智能复习', icon: 'Refresh' },
  { path: '/errorbook', title: '错题本', icon: 'CollectionTag' },
  { path: '/quiz', title: '智能测试', icon: 'Cpu' },
  { path: '/vocabulary', title: '生词本', icon: 'Collection' },
  { path: '/checkin', title: '打卡日历', icon: 'Calendar' },
  { path: '/profile', title: '个人中心', icon: 'User' }
]
const mobileMenus = [
  { path: '/dashboard', title: '大屏', icon: 'DataAnalysis' },
  { path: '/books', title: '词书', icon: 'Reading' },
  { path: '/review', title: '复习', icon: 'Refresh' },
  { path: '/checkin', title: '打卡', icon: 'Calendar' },
  { path: '/profile', title: '我的', icon: 'User' }
]

const currentTitle = computed(() => {
  const m = menus.find(m => m.path === route.path)
  if (m) return m.title
  if (route.path.startsWith('/study')) return '学习中心'
  if (route.path.startsWith('/books/')) return '词书详情'
  return '智绘记'
})

function isActive(path: string) {
  if (path === '/dashboard') return route.path === '/dashboard'
  return route.path.startsWith(path)
}

function onCommand(cmd: string) {
  if (cmd === 'profile') router.push('/profile')
  if (cmd === 'checkin') router.push('/checkin')
  if (cmd === 'logout') {
    user.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  }
}
</script>

<style lang="scss" scoped>
.layout { display: flex; min-height: 100vh; }

.sidebar {
  width: 220px;
  background: linear-gradient(180deg, #FFFFFF 0%, #F7F8FC 100%);
  border-right: 1px solid var(--border);
  display: flex;
  flex-direction: column;
  position: sticky;
  top: 0;
  height: 100vh;
  transition: width .25s ease;
  z-index: 50;
  &.collapsed { width: 76px; }
}

.brand {
  display: flex; align-items: center; gap: 12px;
  padding: 22px 18px;
  cursor: pointer;
  border-bottom: 1px solid var(--border);
}
.brand-mark {
  width: 40px; height: 40px; border-radius: 12px;
  background: var(--grad-primary);
  color: #fff; display: flex; align-items: center; justify-content: center;
  font-family: 'Sora', serif; font-weight: 800; font-size: 22px;
  box-shadow: 0 8px 16px rgba(45,107,255,0.36);
}
.brand-name { font-family: 'Noto Serif SC', serif; font-weight: 800; font-size: 18px; line-height: 1; }
.brand-en { font-family: 'JetBrains Mono', monospace; font-size: 11px; color: var(--text-mute); margin-top: 4px; }

.nav { flex: 1; padding: 16px 12px; display: flex; flex-direction: column; gap: 4px; }
.nav-item {
  display: flex; align-items: center; gap: 12px;
  padding: 11px 14px; border-radius: 12px;
  color: var(--text-secondary); font-weight: 500; font-size: 14px;
  transition: all .2s ease;
  cursor: pointer;
  &:hover { background: rgba(45,107,255,0.06); color: var(--primary); }
  &.active {
    background: var(--grad-primary);
    color: #fff;
    box-shadow: 0 8px 20px rgba(45,107,255,0.32);
  }
}
.nav-label { white-space: nowrap; }

.sidebar-foot { padding: 12px; border-top: 1px solid var(--border); }
.collapse-btn {
  display: flex; align-items: center; gap: 8px;
  padding: 8px 12px; border-radius: 10px;
  color: var(--text-mute); cursor: pointer; font-size: 13px;
  &:hover { background: rgba(45,107,255,0.06); color: var(--primary); }
}

.main { flex: 1; min-width: 0; display: flex; flex-direction: column; }

.topbar {
  position: sticky; top: 0; z-index: 30;
  display: flex; align-items: center; justify-content: space-between;
  padding: 14px 24px;
  border-bottom: 1px solid var(--border);
}
.crumb { display: flex; align-items: center; gap: 6px; color: var(--text-secondary); font-weight: 600; }
.crumb-text { font-family: 'Sora', serif; font-size: 16px; }

.topbar-right { display: flex; align-items: center; gap: 14px; }
.streak {
  display: flex; align-items: center; gap: 6px;
  padding: 6px 12px; border-radius: 999px;
  background: rgba(255,180,0,0.10); color: #C68A00; font-size: 13px;
  cursor: pointer;
  &:hover { background: rgba(255,180,0,0.18); }
}

.user-chip {
  display: flex; align-items: center; gap: 8px;
  padding: 4px 12px 4px 4px; border-radius: 999px;
  background: rgba(45,107,255,0.06);
  cursor: pointer;
  transition: background .2s;
  &:hover { background: rgba(45,107,255,0.12); }
}
.avatar { width: 32px; height: 32px; border-radius: 50%; object-fit: cover; background: #fff; }
.nickname { font-weight: 600; font-size: 14px; }

.content { flex: 1; padding: 24px; min-width: 0; }

.mobile-tabbar {
  display: none;
  position: fixed; bottom: 0; left: 0; right: 0;
  background: rgba(255,255,255,0.92);
  backdrop-filter: blur(14px);
  border-top: 1px solid var(--border);
  z-index: 60;
  padding: 6px 0 8px;
}
.tab-item {
  flex: 1;
  display: flex; flex-direction: column; align-items: center; gap: 2px;
  color: var(--text-mute);
  font-size: 11px;
  padding: 6px 0;
  &.active { color: var(--primary); }
}

@media (max-width: 900px) {
  .sidebar { display: none; }
  .topbar { padding: 12px 14px; }
  .content { padding: 14px; padding-bottom: 80px; }
  .mobile-tabbar { display: flex; }
  .streak { display: none; }
  .nickname { display: none; }
}
</style>
