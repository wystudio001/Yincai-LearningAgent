import { request } from '@/service/request';

export function getAnalyticsOverview(params?: Record<string, unknown>) {
  return request.get('/analytics/overview', { params });
}
