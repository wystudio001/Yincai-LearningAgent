import { request } from './request';

export function uploadFile(file: File, businessType = 'common') {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('businessType', businessType);
  return request.post('/files/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  });
}
