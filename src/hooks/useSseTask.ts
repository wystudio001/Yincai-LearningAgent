import { onBeforeUnmount, ref } from 'vue';
import { createTaskEventSource } from '@/service/sse';

export function useSseTask(taskId: string) {
  const messages = ref<MessageEvent[]>([]);
  const source = createTaskEventSource(taskId);
  source.onmessage = (event) => messages.value.push(event);
  onBeforeUnmount(() => source.close());
  return { messages };
}
