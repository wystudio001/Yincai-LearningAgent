export interface RouteMetaInfo {
  title: string;
  icon?: string;
  hidden?: boolean;
  permission?: string;
}

export interface ApiResult<T = unknown> {
  code: string | number;
  message: string;
  data: T;
  timestamp?: string;
}

export interface PageResult<T> {
  records?: T[];
  list?: T[];
  content?: T[];
  total?: number;
  pageNo?: number;
  pageNum?: number;
  page?: number;
  current?: number;
  pageSize?: number;
  size?: number;
}

export interface QueryParams {
  pageNo?: number;
  pageNum?: number;
  page?: number;
  pageSize?: number;
  size?: number;
  keyword?: string;
  status?: string;
  [key: string]: unknown;
}
