import { defineStore } from 'pinia';

export const usePermissionStore = defineStore('permission', {
  state: () => ({ permissions: [] as string[] }),
  actions: {
    setPermissions(permissions: string[]) {
      this.permissions = permissions;
    },
    hasPermission(permission: string) {
      return this.permissions.includes(permission);
    }
  }
});
