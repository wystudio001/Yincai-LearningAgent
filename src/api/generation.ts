import { request } from '@/service/request';

export function listGenerationTasks(params?: Record<string, unknown>) {
  return request.get('/generation/tasks', { params });
}
