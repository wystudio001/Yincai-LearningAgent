<template>
  <div class="metric-card">
    <p class="metric-label">{{ label }}</p>
    <div class="metric-value">{{ value }}</div>
    <span class="metric-trend">{{ trend }}</span>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  label: string;
  value: string | number;
  trend: string;
}>();
</script>
