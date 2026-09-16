<template>
  <aside class="app-sidebar" :class="{ collapsed: appStore.sidebarCollapsed }">
    <div class="brand">
      <div class="brand-mark">因</div>
      <span v-if="!appStore.sidebarCollapsed">因材智学</span>
    </div>
    <el-menu :collapse="appStore.sidebarCollapsed" :default-active="route.path" router class="sidebar-menu">
      <el-menu-item v-for="item in managementChildren" :key="item.path" :index="`/${item.path}`">
        <el-icon><component :is="iconMap[item.meta?.icon || 'Menu']" /></el-icon>
        <template #title>{{ item.meta?.title }}</template>
      </el-menu-item>
    </el-menu>
  </aside>
</template>

<script setup lang="ts">
import type { Component } from 'vue';
import { useRoute } from 'vue-router';
import * as Icons from '@element-plus/icons-vue';
import { managementChildren } from '@/router/constantRoutes';
import { useAppStore } from '@/stores';

const route = useRoute();
const appStore = useAppStore();
const iconMap = Icons as Record<string, Component>;
</script>
