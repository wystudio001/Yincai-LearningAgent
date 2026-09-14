import type { App, DirectiveBinding } from 'vue';
import { usePermissionStore } from '@/stores/modules/permission';

export function installPermissionDirective(app: App) {
  app.directive('permission', {
    mounted(el: HTMLElement, binding: DirectiveBinding<string>) {
      const permissionStore = usePermissionStore();
      if (binding.value && !permissionStore.hasPermission(binding.value)) {
        el.remove();
      }
    }
  });
}
