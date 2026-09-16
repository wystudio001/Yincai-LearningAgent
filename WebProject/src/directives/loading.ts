import type { App } from 'vue';

export function installLoadingDirective(app: App) {
  app.directive('loading-text', {
    mounted(el: HTMLElement, binding) {
      el.setAttribute('aria-busy', String(Boolean(binding.value)));
    },
    updated(el: HTMLElement, binding) {
      el.setAttribute('aria-busy', String(Boolean(binding.value)));
    }
  });
}
