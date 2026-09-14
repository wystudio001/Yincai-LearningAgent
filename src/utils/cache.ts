export function getCache<T>(key: string): T | null {
  const value = localStorage.getItem(key);
  return value ? (JSON.parse(value) as T) : null;
}

export function setCache(key: string, value: unknown) {
  localStorage.setItem(key, JSON.stringify(value));
}
