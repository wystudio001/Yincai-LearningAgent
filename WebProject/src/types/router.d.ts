import type { RouteMeta } from 'vue-router';

declare module 'vue-router' {
  interface RouteMeta {
    title?: string;
    icon?: string;
    hidden?: boolean;
    permission?: string;
  }
}

export {};
