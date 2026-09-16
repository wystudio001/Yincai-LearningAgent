<template>
  <DomainPage title="内容审核" description="审核 AI 生成资源、敏感内容、题目质量和教师发布申请。" primary-action="批量审核" :records="records" />
</template>

<script setup lang="ts">
import DomainPage from '@/components/business/DomainPage.vue';

const records = [
  { name: 'AI 生成课件待复核', owner: '审核组', status: '启用', updatedAt: '2026-06-14 12:45' },
  { name: '题目难度标注待确认', owner: '教研组', status: '草稿', updatedAt: '2026-06-14 09:18' },
  { name: '敏感内容拦截记录', owner: '安全组', status: '启用', updatedAt: '2026-06-13 17:40' }
];
</script>
