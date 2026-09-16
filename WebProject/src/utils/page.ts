import type { PageResult } from '@/types/api';

export function getPageRecords<T>(data: PageResult<T> | T[] | T | null | undefined): T[] {
  if (!data) {
    return [];
  }
  if (Array.isArray(data)) {
    return data;
  }
  if (typeof data === 'object') {
    const page = data as PageResult<T>;
    if (Array.isArray(page.records)) {
      return page.records;
    }
    if (Array.isArray(page.list)) {
      return page.list;
    }
    if (Array.isArray(page.content)) {
      return page.content;
    }
  }
  return [data as T];
}

export function getPageTotal<T>(data: PageResult<T> | T[] | T | null | undefined) {
  if (!data) {
    return 0;
  }
  if (Array.isArray(data)) {
    return data.length;
  }
  if (typeof data === 'object') {
    const page = data as PageResult<T>;
    return Number(page.total ?? getPageRecords(data).length);
  }
  return 1;
}

export function displayStatus(value: unknown) {
  if (value === 1 || value === '1') {
    return '启用';
  }
  if (value === 0 || value === '0') {
    return '停用';
  }
  const text = String(value || '未知');
  const dict: Record<string, string> = {
    PUBLISHED: '启用',
    ACTIVE: '启用',
    ENABLED: '启用',
    DRAFT: '草稿',
    DISABLED: '停用',
    ARCHIVED: '归档',
    COMPLETED: '已完成',
    RUNNING: '进行中',
    PENDING: '待处理'
  };
  return dict[text] || text;
}

export function displayDate(value: unknown) {
  return value ? String(value).replace('T', ' ').slice(0, 19) : '-';
}

export function valueText(value: unknown, fallback = '-') {
  return value === undefined || value === null || value === '' ? fallback : String(value);
}
