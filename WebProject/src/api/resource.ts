import { request } from '@/service/request';
import type { PageResult, QueryParams } from '@/types/api';

export interface ResourceItem {
  id: string | number;
  title: string;
  summary?: string;
  resourceType?: string;
  courseId?: string | number;
  difficulty?: string;
  status?: string;
  qualityScore?: number;
  createdAt?: string;
  updatedAt?: string;
}

export function listResources(params?: QueryParams): Promise<PageResult<ResourceItem>> {
  return request.get('/resources', { params }) as unknown as Promise<PageResult<ResourceItem>>;
}

export function createResource(data: Partial<ResourceItem>): Promise<ResourceItem> {
  return request.post('/resources', data) as unknown as Promise<ResourceItem>;
}

export function updateResource(id: string | number, data: Partial<ResourceItem>): Promise<void> {
  return request.put(`/resources/${id}`, data) as unknown as Promise<void>;
}

export function getResourceDownloadUrl(id: string | number): Promise<{ downloadUrl: string }> {
  return request.get(`/resources/${id}/download-url`) as unknown as Promise<{ downloadUrl: string }>;
}
