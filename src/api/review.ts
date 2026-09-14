import { request } from '@/service/request';

export function listReviewTasks(params?: Record<string, unknown>) {
  return request.get('/reviews', { params });
}
