import { request } from '@/service/request';
import type { PageResult, QueryParams } from '@/types/api';

export interface QuestionItem {
  id: string | number;
  courseId?: string | number;
  stem?: string;
  questionStem?: string;
  questionType?: string;
  difficulty?: string;
  score?: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface ExerciseItem {
  id?: string | number;
  title?: string;
  name?: string;
  courseId?: string | number;
  status?: string;
  createdAt?: string;
  updatedAt?: string;
}

export function listQuestions(params?: QueryParams): Promise<PageResult<QuestionItem>> {
  return request.get('/questions', { params }) as unknown as Promise<PageResult<QuestionItem>>;
}

export function listExercises(params?: QueryParams): Promise<PageResult<ExerciseItem>> {
  return request.get('/exercises', { params }) as unknown as Promise<PageResult<ExerciseItem>>;
}

export function createExercise(data: ExerciseItem): Promise<ExerciseItem> {
  return request.post('/exercises', data) as unknown as Promise<ExerciseItem>;
}
