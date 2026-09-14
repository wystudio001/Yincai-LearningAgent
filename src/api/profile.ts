import { request } from '@/service/request';
import type { PageResult, QueryParams } from '@/types/api';

export interface StudentProfileItem {
  id?: string | number;
  studentId?: string | number;
  courseId?: string | number;
  summary?: string;
  abilityLevel?: string;
  riskLevel?: string;
  tags?: string;
  createdAt?: string;
  updatedAt?: string;
}

export function listStudentProfiles(params?: QueryParams): Promise<PageResult<StudentProfileItem>> {
  return request.get('/profiles', { params }) as unknown as Promise<PageResult<StudentProfileItem>>;
}

export function getProfileDimensions(profileId: string | number): Promise<Record<string, unknown>[]> {
  return request.get(`/profiles/${profileId}/dimensions`) as unknown as Promise<Record<string, unknown>[]>;
}
