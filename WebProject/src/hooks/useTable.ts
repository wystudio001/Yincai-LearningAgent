import { ref } from 'vue';

export function useTable<T>(initialRows: T[] = []) {
  const loading = ref(false);
  const rows = ref<T[]>(initialRows);
  const total = ref(initialRows.length);
  return { loading, rows, total };
}
