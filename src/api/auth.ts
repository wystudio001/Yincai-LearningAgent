import { request } from '@/service/request';

export interface LoginPayload {
  username: string;
  password: string;
  clientType?: string;
  deviceId?: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  userId: string | number;
  username: string;
  roleCode: string;
  permissions?: string[];
  expiresIn?: number;
}

export function loginApi(payload: LoginPayload): Promise<LoginResponse> {
  return request.post('/auth/login', payload) as unknown as Promise<LoginResponse>;
}

export function getCurrentUserApi(): Promise<Record<string, unknown>> {
  return request.get('/auth/me') as unknown as Promise<Record<string, unknown>>;
}

export function getPermissionsApi(): Promise<string[]> {
  return request.get('/auth/permissions') as unknown as Promise<string[]>;
}

export function logoutApi(): Promise<void> {
  return request.post('/auth/logout') as unknown as Promise<void>;
}
