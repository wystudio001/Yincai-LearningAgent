import { request } from '@/service/request';

export function getSystemSettings() {
  return request.get('/admin/system/settings');
}
