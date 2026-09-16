<template>
  <el-breadcrumb separator="/" class="app-breadcrumb">
    <el-breadcrumb-item>管理端</el-breadcrumb-item>
    <el-breadcrumb-item>{{ route.meta.title }}</el-breadcrumb-item>
  </el-breadcrumb>
</template>

<script setup lang="ts">
import { useRoute } from 'vue-router';

const route = useRoute();
</script>
