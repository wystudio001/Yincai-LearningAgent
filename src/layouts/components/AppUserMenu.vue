<template>
  <el-dropdown trigger="click" @command="handleCommand">
    <button class="user-menu">
      <el-avatar :size="30">管</el-avatar>
      <span>{{ userStore.profile.name }}</span>
      <el-icon><ArrowDown /></el-icon>
    </button>
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item disabled>{{ userStore.profile.role }}</el-dropdown-item>
        <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<script setup lang="ts">
import { ArrowDown } from '@element-plus/icons-vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/stores';

const router = useRouter();
const userStore = useUserStore();

function handleCommand(command: string) {
  if (command === 'logout') {
    userStore.logout();
    router.push('/login');
  }
}
</script>
