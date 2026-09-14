import { request } from '@/service/request';

export function listTutorSessions(params?: Record<string, unknown>) {
  return request.get('/tutor/sessions', { params });
}
