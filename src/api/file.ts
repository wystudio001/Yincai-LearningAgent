import { request } from '@/service/request';

export function getFileInfo(fileId: string) {
  return request.get(`/files/${fileId}`);
}
