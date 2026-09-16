import { defineStore } from 'pinia';

export const useSettingsStore = defineStore('settings', {
  state: () => ({ compactMode: false, showTabs: true }),
  actions: {
    setCompactMode(value: boolean) {
      this.compactMode = value;
    }
  }
});
