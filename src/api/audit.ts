import { request } from '@/service/request';

export function listAuditLogs(params?: Record<string, unknown>) {
  return request.get('/audit/logs', { params });
}
