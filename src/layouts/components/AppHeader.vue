<template>
  <header class="app-header">
    <div class="header-left">
      <el-button text class="icon-button" @click="appStore.toggleSidebar">
        <el-icon><component :is="appStore.sidebarCollapsed ? Expand : Fold" /></el-icon>
      </el-button>
      <AppBreadcrumb />
    </div>
    <div class="header-right">
      <el-input class="header-search" placeholder="搜索课程、学生、资源" :prefix-icon="Search" clearable />
      <el-button text class="icon-button"><el-icon><Bell /></el-icon></el-button>
      <AppUserMenu />
    </div>
  </header>
</template>

<script setup lang="ts">
import { Bell, Expand, Fold, Search } from '@element-plus/icons-vue';
import { useAppStore } from '@/stores';
import AppBreadcrumb from './AppBreadcrumb.vue';
import AppUserMenu from './AppUserMenu.vue';

const appStore = useAppStore();
</script>
