import { useDictStore } from '@/stores/modules/dict';

export function useDict(key: string) {
  const dictStore = useDictStore();
  return dictStore.cache[key] || [];
}
