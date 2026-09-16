import { defineStore } from 'pinia';

export const useDictStore = defineStore('dict', {
  state: () => ({ cache: {} as Record<string, unknown[]> }),
  actions: {
    setDict(key: string, values: unknown[]) {
      this.cache[key] = values;
    }
  }
});
