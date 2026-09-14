<template>
  <div class="basic-layout">
    <AppSidebar />
    <div class="main-shell">
      <AppHeader />
      <AppTabs />
      <AppContent />
    </div>
  </div>
</template>

<script setup lang="ts">
import AppContent from './components/AppContent.vue';
import AppHeader from './components/AppHeader.vue';
import AppSidebar from './components/AppSidebar.vue';
import AppTabs from './components/AppTabs.vue';
</script>
