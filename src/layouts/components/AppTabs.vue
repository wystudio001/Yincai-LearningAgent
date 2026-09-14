<template>
  <div class="app-tabs">
    <el-tag type="info" effect="plain" round>{{ route.meta.title }}</el-tag>
  </div>
</template>

<script setup lang="ts">
import { useRoute } from 'vue-router';

const route = useRoute();
</script>
