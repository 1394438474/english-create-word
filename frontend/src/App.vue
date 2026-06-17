<template>
  <router-view v-slot="{ Component }">
    <transition name="page" mode="out-in">
      <component :is="Component" />
    </transition>
  </router-view>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
const userStore = useUserStore()
onMounted(() => {
  userStore.restore()
})
</script>

<style>
#app {
  width: 100%;
  min-height: 100vh;
  background: var(--bg-page);
  color: var(--text-primary);
  font-family: 'Plus Jakarta Sans', 'PingFang SC', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
  -webkit-font-smoothing: antialiased;
}
.page-enter-active, .page-leave-active { transition: opacity .25s ease, transform .25s ease; }
.page-enter-from { opacity: 0; transform: translateY(8px); }
.page-leave-to { opacity: 0; transform: translateY(-8px); }
</style>
