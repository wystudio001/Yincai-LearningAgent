import { request } from '@/service/request';
import type { PageResult, QueryParams } from '@/types/api';

export interface NotificationItem {
  id: string | number;
  title?: string;
  content?: string;
  type?: string;
  status?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface NotificationTemplateItem {
  id?: string | number;
  code?: string;
  name?: string;
  title?: string;
  content?: string;
  type?: string;
  enabled?: number | boolean;
  createdAt?: string;
  updatedAt?: string;
}

export function listNotifications(params?: QueryParams): Promise<PageResult<NotificationItem>> {
  return request.get('/notifications', { params }) as unknown as Promise<PageResult<NotificationItem>>;
}

export function getUnreadCount(): Promise<{ unreadCount: number }> {
  return request.get('/notifications/unread-count') as unknown as Promise<{ unreadCount: number }>;
}

export function markNotificationRead(id: string | number): Promise<void> {
  return request.patch(`/notifications/${id}/read`) as unknown as Promise<void>;
}

export function markAllNotificationsRead(): Promise<void> {
  return request.patch('/notifications/read-all') as unknown as Promise<void>;
}

export function createNotificationTemplate(data: NotificationTemplateItem): Promise<NotificationTemplateItem> {
  return request.post('/admin/notification-templates', data) as unknown as Promise<NotificationTemplateItem>;
}

export function updateNotificationTemplate(id: string | number, data: NotificationTemplateItem): Promise<NotificationTemplateItem> {
  return request.put(`/admin/notification-templates/${id}`, data) as unknown as Promise<NotificationTemplateItem>;
}
