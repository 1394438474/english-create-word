<template>
  <div class="auth-page">
    <div class="bg-deco">
      <div class="blob blob-1"></div>
      <div class="blob blob-2"></div>
      <div class="blob blob-3"></div>
    </div>
    <div class="auth-card glass fade-up">
      <div class="logo">
        <div class="logo-mark">S</div>
        <div>
          <h1 class="title serif">智绘记 SmartVocab</h1>
          <p class="subtitle">AI 图文背单词 · 让记忆有迹可循</p>
        </div>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" size="large">
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="demo@smartvocab.com" :prefix-icon="Message" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="至少 6 位" :prefix-icon="Lock" @keyup.enter="onSubmit" />
        </el-form-item>
        <el-button class="submit-btn" :loading="loading" @click="onSubmit">登 录</el-button>
      </el-form>

      <div class="actions">
        <span class="text-mute">没有账号？</span>
        <router-link to="/register" class="link">立即注册</router-link>
      </div>

      <div class="demo-tip">
        <el-icon><InfoFilled /></el-icon>
        <span>演示账号 <b class="mono">demo@smartvocab.com</b> / 密码 <b class="mono">123456</b></span>
      </div>
    </div>

    <div class="feature-grid">
      <div class="feature-item fade-up delay-1">
        <div class="icon-wrap" style="background: var(--grad-primary)"><el-icon><Picture /></el-icon></div>
        <h3>图文场景化</h3>
        <p>每个单词配真实场景高清图，让记忆从图像开始</p>
      </div>
      <div class="feature-item fade-up delay-2">
        <div class="icon-wrap" style="background: var(--grad-accent)"><el-icon><Cpu /></el-icon></div>
        <h3>艾宾浩斯 AI</h3>
        <p>个性化记忆衰减模型，动态生成最优复习节奏</p>
      </div>
      <div class="feature-item fade-up delay-3">
        <div class="icon-wrap" style="background: var(--grad-warm)"><el-icon><TrendCharts /></el-icon></div>
        <h3>数据大屏</h3>
        <p>掌握度 / 趋势 / 薄弱词全维度可视化分析</p>
      </div>
      <div class="feature-item fade-up delay-4">
        <div class="icon-wrap" style="background: var(--grad-cool)"><el-icon><Trophy /></el-icon></div>
        <h3>勋章体系</h3>
        <p>连续打卡解锁成就，让坚持看得见</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { Message, Lock, InfoFilled, Picture, Cpu, TrendCharts, Trophy } from '@element-plus/icons-vue'
import { authApi } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const loading = ref(false)
const form = reactive({ email: 'demo@smartvocab.com', password: '123456' })
const rules: FormRules = {
  email: [{ required: true, type: 'email', message: '请输入正确邮箱' }],
  password: [{ required: true, min: 6, message: '密码至少 6 位' }]
}

async function onSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const res = await authApi.login(form)
      userStore.setLogin(res.token, res.user)
      ElMessage.success('欢迎回来，' + res.user.nickname)
      router.push('/dashboard')
    } finally {
      loading.value = false
    }
  })
}
</script>

<style lang="scss" scoped>
.auth-page {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1fr 1fr;
  align-items: center;
  padding: 40px;
  gap: 60px;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #EEF2FF 0%, #F7F8FC 50%, #E0F7F1 100%);
}

.bg-deco { position: absolute; inset: 0; pointer-events: none; }
.blob {
  position: absolute; border-radius: 50%;
  filter: blur(80px); opacity: 0.5;
  animation: float 8s ease-in-out infinite;
}
.blob-1 { width: 380px; height: 380px; background: rgba(45,107,255,0.40); top: -10%; left: -10%; }
.blob-2 { width: 320px; height: 320px; background: rgba(0,200,150,0.32); bottom: -8%; right: 5%; animation-delay: 1.5s; }
.blob-3 { width: 260px; height: 260px; background: rgba(255,180,0,0.30); top: 30%; right: 35%; animation-delay: 3s; }

.auth-card {
  position: relative; z-index: 1;
  width: 100%;
  max-width: 460px;
  margin: 0 auto;
  padding: 40px 36px;
  border-radius: 24px;
  border: 1px solid rgba(255,255,255,0.5);
}
.logo { display: flex; align-items: center; gap: 14px; margin-bottom: 32px; }
.logo-mark {
  width: 56px; height: 56px; border-radius: 16px;
  background: var(--grad-primary);
  display: flex; align-items: center; justify-content: center;
  color: #fff; font-family: 'Sora', serif; font-weight: 800; font-size: 30px;
  box-shadow: 0 12px 24px rgba(45,107,255,0.36);
}
.title { font-size: 22px; font-weight: 800; }
.subtitle { color: var(--text-mute); font-size: 13px; margin-top: 4px; }

.submit-btn {
  width: 100%; height: 46px; font-size: 15px; font-weight: 600;
  background: var(--grad-primary); color: #fff; border: none;
  border-radius: 12px; margin-top: 6px;
  box-shadow: 0 12px 24px rgba(45,107,255,0.32);
  transition: transform .15s, box-shadow .15s;
  &:hover { transform: translateY(-1px); box-shadow: 0 16px 32px rgba(45,107,255,0.40); }
}
:deep(.el-form-item__label) { color: var(--text-secondary); font-weight: 600; }
:deep(.el-input__wrapper) { border-radius: 12px; padding: 4px 12px; }

.actions { text-align: center; margin-top: 20px; font-size: 14px; }
.link { color: var(--primary); font-weight: 600; margin-left: 4px; }

.demo-tip {
  margin-top: 20px;
  display: flex; align-items: center; gap: 6px;
  padding: 10px 14px; border-radius: 10px;
  background: rgba(45,107,255,0.06); color: var(--text-secondary);
  font-size: 12px;
}

.feature-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 24px; position: relative; z-index: 1; }
.feature-item {
  padding: 26px; border-radius: 18px;
  background: rgba(255,255,255,0.7); backdrop-filter: blur(14px);
  border: 1px solid rgba(255,255,255,0.5);
  transition: transform .2s, box-shadow .2s;
  &:hover { transform: translateY(-3px); box-shadow: 0 20px 40px rgba(31,41,78,0.10); }
}
.icon-wrap {
  width: 48px; height: 48px; border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
  color: #fff; font-size: 22px; margin-bottom: 12px;
  box-shadow: 0 8px 20px rgba(31,41,78,0.16);
}
.feature-item h3 { font-size: 16px; font-weight: 700; margin-bottom: 4px; }
.feature-item p { color: var(--text-mute); font-size: 13px; line-height: 1.6; }

@media (max-width: 900px) {
  .auth-page { grid-template-columns: 1fr; padding: 24px 16px; gap: 30px; }
  .feature-grid { grid-template-columns: 1fr 1fr; }
  .auth-card { padding: 28px 22px; }
}
@media (max-width: 480px) {
  .feature-grid { grid-template-columns: 1fr; }
}
</style>
