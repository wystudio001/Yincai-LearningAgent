import { request } from '@/service/request';

export interface AgentTaskPayload {
  type: string;
  [key: string]: unknown;
}

export interface ModelProviderItem {
  id: string;
  name: string;
  providerCode?: string;
  baseUrl: string;
  defaultModel: string;
  model?: string;
  apiKeyMasked?: string;
  keyMasked?: string;
  enabled: boolean;
  timeoutMs?: number;
  temperature?: number;
  maxTokens?: number;
  remark?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface ModelProviderPayload {
  name: string;
  providerCode?: string;
  baseUrl: string;
  apiKey?: string;
  defaultModel: string;
  enabled: boolean;
  timeoutMs?: number;
  temperature?: number;
  maxTokens?: number;
  remark?: string;
}

export interface ModelCatalogPayload {
  providerId?: string;
  baseUrl?: string;
  apiKey?: string;
  timeoutMs?: number;
}

export interface ModelCatalogResult {
  endpoint: string;
  models: string[];
  total: number;
}

export function createAgentTask(data: AgentTaskPayload): Promise<Record<string, unknown>> {
  return request.post('/internal/v1/ai/agent-tasks', data) as unknown as Promise<Record<string, unknown>>;
}

export function getAgentTaskStatus(taskId: string): Promise<Record<string, unknown>> {
  return request.get(`/internal/v1/ai/${taskId}`) as unknown as Promise<Record<string, unknown>>;
}

export function testLlmChat(data: Record<string, unknown>): Promise<Record<string, unknown>> {
  return request.post('/internal/v1/ai/llm/chat', data) as unknown as Promise<Record<string, unknown>>;
}

export function safetyReview(data: Record<string, unknown>): Promise<Record<string, unknown>> {
  return request.post('/internal/v1/ai/safety/review', data) as unknown as Promise<Record<string, unknown>>;
}

export function listModelProviders(): Promise<ModelProviderItem[]> {
  return request.get('/admin/model-providers') as unknown as Promise<ModelProviderItem[]>;
}

export function createModelProvider(data: ModelProviderPayload): Promise<ModelProviderItem> {
  return request.post('/admin/model-providers', data) as unknown as Promise<ModelProviderItem>;
}

export function fetchModelCatalog(data: ModelCatalogPayload): Promise<ModelCatalogResult> {
  return request.post('/admin/model-providers/models', data) as unknown as Promise<ModelCatalogResult>;
}

export function updateModelProvider(id: string, data: Partial<ModelProviderPayload>): Promise<ModelProviderItem> {
  return request.put(`/admin/model-providers/${id}`, data) as unknown as Promise<ModelProviderItem>;
}

export function deleteModelProvider(id: string): Promise<void> {
  return request.delete(`/admin/model-providers/${id}`) as unknown as Promise<void>;
}

export function testModelProvider(id: string, prompt?: string): Promise<Record<string, unknown>> {
  return request.post(`/admin/model-providers/${id}/test`, { prompt }) as unknown as Promise<Record<string, unknown>>;
}
