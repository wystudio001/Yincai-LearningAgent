import { request } from '@/service/request';
import type { PageResult, QueryParams } from '@/types/api';

export interface CourseItem {
  id: string | number;
  name: string;
  description?: string;
  status?: string;
  createdAt?: string;
  updatedAt?: string;
}

export function listCourses(params?: QueryParams): Promise<PageResult<CourseItem>> {
  return request.get('/courses', { params }) as unknown as Promise<PageResult<CourseItem>>;
}

export function createCourse(data: Partial<CourseItem>): Promise<CourseItem> {
  return request.post('/courses', data) as unknown as Promise<CourseItem>;
}

export function updateCourse(id: string | number, data: Partial<CourseItem>): Promise<void> {
  return request.put(`/courses/${id}`, data) as unknown as Promise<void>;
}

export function publishCourse(id: string | number): Promise<void> {
  return request.post(`/courses/${id}/publish`) as unknown as Promise<void>;
}

export function unpublishCourse(id: string | number): Promise<void> {
  return request.post(`/courses/${id}/unpublish`) as unknown as Promise<void>;
}
