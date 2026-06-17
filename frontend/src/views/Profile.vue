<template>
  <div class="page" v-if="user">
    <div class="profile-head" :style="{ background: 'linear-gradient(135deg, #2D6BFF 0%, #00C896 100%)' }">
      <div class="avatar-wrap">
        <img class="avatar" :src="user.avatar || defaultAvatar" :alt="user.nickname" />
        <label class="upload-btn" :title="'更换头像'">
          <el-icon><Camera /></el-icon>
          <input type="file" accept="image/*" @change="onUpload" hidden />
        </label>
      </div>
      <div class="info">
        <h1 class="serif name">{{ user.nickname }}</h1>
        <div class="email mono">{{ user.email }}</div>
        <div class="bio">{{ user.bio || '这个人很懒，还没有签名～' }}</div>
      </div>
    </div>

    <section class="stat-grid">
      <div class="stat-card">
        <div class="stat-num mono">{{ user.streakDays || 0 }}</div>
        <div class="stat-l">连续打卡</div>
      </div>
      <div class="stat-card">
        <div class="stat-num mono">{{ user.totalDays || 0 }}</div>
        <div class="stat-l">累计天数</div>
      </div>
      <div class="stat-card">
        <div class="stat-num mono">∞</div>
        <div class="stat-l">学习不设限</div>
      </div>
    </section>

    <section class="forms">
      <div class="form-card">
        <h3 class="serif">基本资料</h3>
        <el-form :model="form" label-position="top">
          <el-form-item label="昵称">
            <el-input v-model="form.nickname" />
          </el-form-item>
          <el-form-item label="个性签名">
            <el-input v-model="form.bio" type="textarea" :rows="3" />
          </el-form-item>
          <el-button type="primary" @click="onSave">保存</el-button>
        </el-form>
      </div>
      <div class="form-card">
        <h3 class="serif">修改密码</h3>
        <el-form :model="pwd" label-position="top">
          <el-form-item label="原密码">
            <el-input v-model="pwd.oldPwd" type="password" show-password />
          </el-form-item>
          <el-form-item label="新密码">
            <el-input v-model="pwd.newPwd" type="password" show-password />
          </el-form-item>
          <el-button type="primary" @click="onChangePwd">修改密码</el-button>
        </el-form>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Camera } from '@element-plus/icons-vue'
import { userApi } from '@/api/user'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const user = ref<any>(userStore.user)
const defaultAvatar = 'https://api.dicebear.com/7.x/avataaars/svg?seed=guest'
const form = reactive({ nickname: '', bio: '' })
const pwd = reactive({ oldPwd: '', newPwd: '' })

async function load() {
  const me = await userApi.me()
  user.value = me
  userStore.setUser(me)
  form.nickname = me.nickname
  form.bio = me.bio || ''
}

async function onSave() {
  await userApi.update(form)
  ElMessage.success('已保存')
  load()
}

async function onChangePwd() {
  if (!pwd.oldPwd || !pwd.newPwd) { ElMessage.warning('请填写完整'); return }
  if (pwd.newPwd.length < 6) { ElMessage.warning('新密码至少 6 位'); return }
  await userApi.changePassword(pwd)
  ElMessage.success('密码已修改')
  pwd.oldPwd = pwd.newPwd = ''
}

async function onUpload(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file) return
  const res = await userApi.uploadAvatar(file)
  user.value.avatar = res.url
  userStore.setUser({ ...user.value, avatar: res.url })
  ElMessage.success('头像已更新')
}

onMounted(load)
</script>

<style lang="scss" scoped>
.profile-head {
  display: flex; align-items: center; gap: 24px;
  padding: 32px 40px; border-radius: 24px; color: #fff;
  margin-bottom: 24px;
  position: relative; overflow: hidden;
}
.profile-head::before {
  content: ''; position: absolute; inset: 0;
  background: radial-gradient(circle at 80% 20%, rgba(255,255,255,0.18), transparent 50%);
}
.avatar-wrap { position: relative; z-index: 1; }
.avatar {
  width: 110px; height: 110px; border-radius: 50%; object-fit: cover;
  border: 4px solid rgba(255,255,255,0.5);
  background: #fff;
}
.upload-btn {
  position: absolute; right: 0; bottom: 0;
  width: 32px; height: 32px; border-radius: 50%;
  background: rgba(0,0,0,0.4); color: #fff;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer; border: 2px solid #fff;
  &:hover { background: rgba(0,0,0,0.6); }
}
.info { flex: 1; z-index: 1; }
.name { font-size: 28px; color: #fff; font-weight: 800; }
.email { font-size: 13px; opacity: 0.85; margin-top: 4px; }
.bio { font-size: 14px; opacity: 0.95; margin-top: 8px; max-width: 480px; }

.stat-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; margin-bottom: 24px; }
.stat-card { background: #fff; padding: 24px; border-radius: 18px; text-align: center; box-shadow: 0 4px 14px rgba(31,41,78,0.05); }
.stat-num { font-size: 36px; font-weight: 800; background: var(--grad-primary); -webkit-background-clip: text; background-clip: text; color: transparent; }
.stat-l { color: var(--text-mute); font-size: 13px; margin-top: 4px; }

.forms { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.form-card { background: #fff; padding: 24px; border-radius: 18px; box-shadow: 0 4px 14px rgba(31,41,78,0.05); }
.form-card h3 { font-size: 18px; font-weight: 800; margin-bottom: 16px; }

@media (max-width: 900px) {
  .profile-head { flex-direction: column; text-align: center; padding: 24px; }
  .forms { grid-template-columns: 1fr; }
  .stat-grid { grid-template-columns: 1fr; }
}
</style>
