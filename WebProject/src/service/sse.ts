export function createTaskEventSource(taskId: string) {
  const baseUrl = import.meta.env.VITE_SSE_BASE_URL;
  return new EventSource(`${baseUrl}/api/v1/tasks/${taskId}/events`);
}
