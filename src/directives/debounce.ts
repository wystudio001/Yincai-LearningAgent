import type { App } from 'vue';

export function installDebounceDirective(app: App) {
  app.directive('debounce', {
    mounted(el: HTMLElement, binding) {
      let timer: number | undefined;
      const delay = Number(binding.arg) || 300;
      const handler = () => {
        window.clearTimeout(timer);
        timer = window.setTimeout(() => binding.value?.(), delay);
      };
      el.addEventListener('click', handler);
    }
  });
}
