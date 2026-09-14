import { defineStore } from 'pinia';
import { getCurrentUserApi, loginApi, logoutApi } from '@/api/auth';
import { clearToken, setToken } from '@/utils/auth';

interface UserProfile {
  id: string;
  name: string;
  username: string;
  role: string;
  school: string;
  permissions: string[];
}

const defaultProfile: UserProfile = {
  id: '',
  name: '未登录',
  username: '',
  role: '访客',
  school: '因材智学系统',
  permissions: []
};

function normalizeProfile(data: Record<string, unknown>, fallback?: Partial<UserProfile>): UserProfile {
  const roleCode = String(data.roleCode || fallback?.role || 'ADMIN');
  const username = String(data.username || fallback?.username || '');
  return {
    id: String(data.userId || data.id || fallback?.id || ''),
    username,
    name: String(data.realName || data.displayName || data.name || username || '系统管理员'),
    role: roleCode,
    school: String(data.tenantName || data.school || fallback?.school || '因材智学系统'),
    permissions: Array.isArray(data.permissions) ? (data.permissions as string[]) : fallback?.permissions || []
  };
}

export const useUserStore = defineStore('user', {
  state: () => ({
    profile: { ...defaultProfile } as UserProfile
  }),
  actions: {
    async login(username: string, password: string) {
      if (!username || !password) {
        throw new Error('请输入账号和密码');
      }
      const response = await loginApi({ username, password, clientType: 'WEB_ADMIN', deviceId: 'web-admin' });
      if (!['ADMIN', 'PLATFORM_ADMIN', 'TEACHER'].includes(response.roleCode)) {
        throw new Error('当前账号无管理端登录权限');
      }
      setToken(response.accessToken);
      this.profile = normalizeProfile(response as unknown as Record<string, unknown>, {
        id: String(response.userId),
        username: response.username,
        role: response.roleCode,
        permissions: response.permissions || []
      });
    },
    async fetchProfile() {
      const data = await getCurrentUserApi();
      this.profile = normalizeProfile(data, this.profile);
    },
    async logout() {
      try {
        await logoutApi();
      } finally {
        clearToken();
        this.profile = { ...defaultProfile };
      }
    }
  }
});
