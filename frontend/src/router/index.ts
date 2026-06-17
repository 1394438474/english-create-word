import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'

const Layout = () => import('@/components/Layout.vue')

const routes: RouteRecordRaw[] = [
  { path: '/login', component: () => import('@/views/Login.vue'), meta: { public: true } },
  { path: '/register', component: () => import('@/views/Register.vue'), meta: { public: true } },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', component: () => import('@/views/Dashboard.vue'), meta: { title: '学习数据大屏', icon: 'DataAnalysis' } },
      { path: 'books', component: () => import('@/views/Books.vue'), meta: { title: '词书广场', icon: 'Reading' } },
      { path: 'books/:id', component: () => import('@/views/BookDetail.vue'), meta: { title: '词书详情' } },
      { path: 'study/:bookId', component: () => import('@/views/Study.vue'), meta: { title: '学习中心', icon: 'Notebook' } },
      { path: 'review', component: () => import('@/views/Review.vue'), meta: { title: '智能复习', icon: 'Refresh' } },
      { path: 'errorbook', component: () => import('@/views/ErrorBook.vue'), meta: { title: '错题本', icon: 'CollectionTag' } },
      { path: 'quiz', component: () => import('@/views/Quiz.vue'), meta: { title: '智能测试', icon: 'Cpu' } },
      { path: 'vocabulary', component: () => import('@/views/Vocabulary.vue'), meta: { title: '生词本', icon: 'Collection' } },
      { path: 'checkin', component: () => import('@/views/CheckIn.vue'), meta: { title: '打卡日历', icon: 'Calendar' } },
      { path: 'profile', component: () => import('@/views/Profile.vue'), meta: { title: '个人中心', icon: 'User' } }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/dashboard' }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 })
})

router.beforeEach((to, _from, next) => {
  const user = useUserStore()
  user.restore()
  if (to.meta.public) return next()
  if (!user.token) return next('/login')
  next()
})

export default router
