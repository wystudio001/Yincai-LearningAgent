import type { RouteRecordRaw } from 'vue-router';
import BasicLayout from '@/layouts/BasicLayout.vue';
import BlankLayout from '@/layouts/BlankLayout.vue';

export const managementChildren: RouteRecordRaw[] = [
  { path: 'dashboard', name: 'Dashboard', component: () => import('@/views/dashboard/Index.vue'), meta: { title: '工作台', icon: 'Odometer' } },
  { path: 'course', name: 'Course', component: () => import('@/views/course/Index.vue'), meta: { title: '课程管理', icon: 'Reading' } },
  { path: 'profile', name: 'Profile', component: () => import('@/views/profile/Index.vue'), meta: { title: '学生画像', icon: 'UserFilled' } },
  { path: 'resource', name: 'Resource', component: () => import('@/views/resource/Index.vue'), meta: { title: '资源管理', icon: 'Files' } },
  { path: 'learning-path', name: 'LearningPath', component: () => import('@/views/learning-path/Index.vue'), meta: { title: '学习路径', icon: 'Connection' } },
  { path: 'exam', name: 'Exam', component: () => import('@/views/exam/Index.vue'), meta: { title: '测评考试', icon: 'EditPen' } },
  { path: 'tutor', name: 'Tutor', component: () => import('@/views/tutor/Index.vue'), meta: { title: '智能辅导', icon: 'ChatDotRound' } },
  { path: 'analytics', name: 'Analytics', component: () => import('@/views/analytics/Index.vue'), meta: { title: '数据分析', icon: 'DataAnalysis' } },
  { path: 'review', name: 'Review', component: () => import('@/views/review/Index.vue'), meta: { title: '内容审核', icon: 'Finished' } },
  { path: 'user', name: 'User', component: () => import('@/views/user/Index.vue'), meta: { title: '用户权限', icon: 'User' } },
  { path: 'ai', name: 'Ai', component: () => import('@/views/ai/Index.vue'), meta: { title: 'AI 配置', icon: 'Cpu' } },
  { path: 'audit', name: 'Audit', component: () => import('@/views/audit/Index.vue'), meta: { title: '审计日志', icon: 'Operation' } },
  { path: 'system', name: 'System', component: () => import('@/views/system/Index.vue'), meta: { title: '系统设置', icon: 'Setting' } },
  { path: 'notification', name: 'Notification', component: () => import('@/views/notification/Index.vue'), meta: { title: '消息通知', icon: 'Bell' } }
];

export const constantRoutes: RouteRecordRaw[] = [
  {
    path: '/login',
    component: BlankLayout,
    children: [{ path: '', name: 'Login', component: () => import('@/views/login/Index.vue'), meta: { title: '登录', hidden: true } }]
  },
  {
    path: '/',
    component: BasicLayout,
    redirect: '/dashboard',
    children: managementChildren
  },
  { path: '/:pathMatch(.*)*', name: 'NotFound', component: () => import('@/views/error/NotFound.vue'), meta: { title: '页面不存在', hidden: true } }
];
