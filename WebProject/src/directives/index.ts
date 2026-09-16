import type { App } from 'vue';
import { installDebounceDirective } from './debounce';
import { installLoadingDirective } from './loading';
import { installPermissionDirective } from './permission';

export function installDirectives(app: App) {
  installDebounceDirective(app);
  installLoadingDirective(app);
  installPermissionDirective(app);
}
