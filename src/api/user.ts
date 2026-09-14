import { request } from '@/service/request';
import type { PageResult, QueryParams } from '@/types/api';

export interface UserItem {
  id: string | number;
  username: string;
  realName?: string;
  roleCode?: string;
  email?: string;
  phone?: string;
  status?: number | string;
  createdAt?: string;
  updatedAt?: string;
}

export function listUsers(params?: QueryParams): Promise<PageResult<UserItem>> {
  return request.get('/users', { params }) as unknown as Promise<PageResult<UserItem>>;
}

export function createUser(data: Partial<UserItem>): Promise<UserItem> {
  return request.post('/users', data) as unknown as Promise<UserItem>;
}

export function updateUser(id: string | number, data: Partial<UserItem>): Promise<void> {
  return request.put(`/users/${id}`, data) as unknown as Promise<void>;
}

export function updateUserStatus(id: string | number, status: number): Promise<void> {
  return request.patch(`/users/${id}/status`, { status }) as unknown as Promise<void>;
}
