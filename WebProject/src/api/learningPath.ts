import { request } from '@/service/request';
import type { PageResult, QueryParams } from '@/types/api';

export interface LearningPathItem {
  id: string | number;
  studentId?: string | number;
  courseId?: string | number;
  status?: string;
  version?: number;
  generatedBy?: string;
  currentNodeId?: string | number;
  createdAt?: string;
  updatedAt?: string;
}

export function getCurrentLearningPath(params?: QueryParams): Promise<LearningPathItem | null> {
  return request.get('/student/learning-path/current', { params }) as unknown as Promise<LearningPathItem | null>;
}

export function generateLearningPath(data: Record<string, unknown>): Promise<LearningPathItem> {
  return request.post('/student/learning-path/generate', data) as unknown as Promise<LearningPathItem>;
}

export function listLearningPaths(params?: QueryParams): Promise<PageResult<LearningPathItem>> {
  return request.get('/student/learning-path/current', { params }) as unknown as Promise<PageResult<LearningPathItem>>;
}

export function getLearningPathNodes(pathId: string | number): Promise<Record<string, unknown>[]> {
  return request.get(`/learning-paths/${pathId}/nodes`) as unknown as Promise<Record<string, unknown>[]>;
}
