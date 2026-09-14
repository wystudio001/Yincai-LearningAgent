import router from './index';
import { getToken } from '@/utils/auth';

router.beforeEach((to) => {
  document.title = `${to.meta.title ? `${to.meta.title} - ` : ''}${import.meta.env.VITE_APP_TITLE}`;
  const token = getToken();
  if (!token && to.path !== '/login') {
    return { path: '/login', query: { redirect: to.fullPath } };
  }
  if (token && to.path === '/login') {
    return { path: '/dashboard' };
  }
  return true;
});
