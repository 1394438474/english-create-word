<template>
  <div class="auth-page">
    <div class="bg-deco">
      <div class="blob blob-1"></div>
      <div class="blob blob-2"></div>
    </div>
    <div class="auth-card glass fade-up">
      <div class="logo">
        <div class="logo-mark">S</div>
        <div>
          <h1 class="title serif">加入智绘记</h1>
          <p class="subtitle">创建账号，开启 AI 背单词之旅</p>
        </div>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" size="large">
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="给自己起个名字" :prefix-icon="User" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="example@mail.com" :prefix-icon="Message" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="至少 6 位" :prefix-icon="Lock" />
        </el-form-item>
        <el-button class="submit-btn" :loading="loading" @click="onSubmit">立即注册</el-button>
      </el-form>
      <div class="actions">
        <span class="text-mute">已有账号？</span>
        <router-link to="/login" class="link">前往登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { User, Message, Lock } from '@element-plus/icons-vue'
import { authApi } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const loading = ref(false)
const form = reactive({ email: '', password: '', nickname: '' })
const rules: FormRules = {
  email: [{ required: true, type: 'email', message: '请输入正确邮箱' }],
  password: [{ required: true, min: 6, message: '密码至少 6 位' }],
  nickname: [{ required: true, min: 1, message: '请输入昵称' }]
}
async function onSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const res = await authApi.register(form)
      userStore.setLogin(res.token, res.user)
      ElMessage.success('注册成功，欢迎加入！')
      router.push('/dashboard')
    } finally { loading.value = false }
  })
}
</script>

<style lang="scss" scoped>
.auth-page {
  min-height: 100vh;
  display: flex; align-items: center; justify-content: center;
  padding: 40px;
  position: relative; overflow: hidden;
  background: linear-gradient(135deg, #EEF2FF 0%, #F7F8FC 50%, #E0F7F1 100%);
}
.bg-deco { position: absolute; inset: 0; pointer-events: none; }
.blob { position: absolute; border-radius: 50%; filter: blur(80px); opacity: 0.5; animation: float 8s ease-in-out infinite; }
.blob-1 { width: 360px; height: 360px; background: rgba(0,200,150,0.32); top: -10%; right: -8%; }
.blob-2 { width: 280px; height: 280px; background: rgba(45,107,255,0.40); bottom: -8%; left: -6%; animation-delay: 1.5s; }

.auth-card {
  position: relative; z-index: 1;
  width: 100%; max-width: 460px;
  padding: 40px 36px; border-radius: 24px;
  border: 1px solid rgba(255,255,255,0.5);
}
.logo { display: flex; align-items: center; gap: 14px; margin-bottom: 28px; }
.logo-mark {
  width: 56px; height: 56px; border-radius: 16px;
  background: var(--grad-accent);
  display: flex; align-items: center; justify-content: center;
  color: #fff; font-family: 'Sora', serif; font-weight: 800; font-size: 30px;
  box-shadow: 0 12px 24px rgba(0,200,150,0.32);
}
.title { font-size: 22px; font-weight: 800; }
.subtitle { color: var(--text-mute); font-size: 13px; margin-top: 4px; }
.submit-btn {
  width: 100%; height: 46px; font-size: 15px; font-weight: 600;
  background: var(--grad-accent); color: #fff; border: none;
  border-radius: 12px; margin-top: 6px;
  box-shadow: 0 12px 24px rgba(0,200,150,0.32);
  transition: transform .15s, box-shadow .15s;
  &:hover { transform: translateY(-1px); box-shadow: 0 16px 32px rgba(0,200,150,0.40); }
}
:deep(.el-form-item__label) { color: var(--text-secondary); font-weight: 600; }
:deep(.el-input__wrapper) { border-radius: 12px; padding: 4px 12px; }
.actions { text-align: center; margin-top: 20px; font-size: 14px; }
.link { color: var(--primary); font-weight: 600; margin-left: 4px; }
</style>
