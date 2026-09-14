import axios from 'axios';
import type { AxiosError, InternalAxiosRequestConfig } from 'axios';
import { ElMessage } from 'element-plus';
import router from '@/router';
import { clearToken, getToken } from '@/utils/auth';
import type { ApiResult } from '@/types/api';

function isSuccessCode(code: ApiResult['code']) {
  return code === 'SUCCESS' || code === 0 || code === 200 || code === '0' || code === '200';
}

export const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 20000
});

request.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = getToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

request.interceptors.response.use(
  (response) => {
    const result = response.data as ApiResult;
    if (result && typeof result === 'object' && 'code' in result && 'data' in result) {
      if (isSuccessCode(result.code)) {
        return result.data;
      }
      ElMessage.error(result.message || '请求失败');
      return Promise.reject(new Error(result.message || '请求失败'));
    }
    return response.data;
  },
  async (error: AxiosError<{ message?: string }>) => {
    if (error.response?.status === 401) {
      clearToken();
      if (router.currentRoute.value.path !== '/login') {
        await router.push({ path: '/login', query: { redirect: router.currentRoute.value.fullPath } });
      }
      ElMessage.error('登录已失效，请重新登录');
      return Promise.reject(error);
    }
    const message = error.response?.data?.message || error.message || '请求失败';
    ElMessage.error(message);
    return Promise.reject(error);
  }
);
