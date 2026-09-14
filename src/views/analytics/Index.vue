<template>
  <DomainPage title="数据分析" description="汇总教学效果、资源质量、学生活跃和模型调用指标。" primary-action="生成报表" :records="records" />
</template>

<script setup lang="ts">
import DomainPage from '@/components/business/DomainPage.vue';

const records = [
  { name: '课程达成度分析', owner: '数据中心', status: '启用', updatedAt: '2026-06-14 08:15' },
  { name: '资源使用热度分析', owner: '数据中心', status: '启用', updatedAt: '2026-06-13 21:00' },
  { name: '模型成本日报', owner: '平台运营', status: '启用', updatedAt: '2026-06-13 09:00' }
];
</script>
