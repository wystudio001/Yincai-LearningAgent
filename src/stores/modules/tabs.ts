import { defineStore } from 'pinia';

export const useTabsStore = defineStore('tabs', {
  state: () => ({ tabs: [] as string[] }),
  actions: {
    addTab(title: string) {
      if (!this.tabs.includes(title)) this.tabs.push(title);
    }
  }
});
