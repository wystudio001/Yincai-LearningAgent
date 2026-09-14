export function normalizeRoutePath(path: string) {
  return path.startsWith('/') ? path : `/${path}`;
}
