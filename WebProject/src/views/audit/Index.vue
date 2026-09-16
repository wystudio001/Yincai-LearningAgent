<template>
  <PageShell title="审计日志" description="追踪登录、权限、资源、模型配置和关键业务操作记录。">
    <template #actions>
      <el-button :icon="Download">导出日志</el-button>
      <el-button type="primary" :icon="Search">查询</el-button>
    </template>

    <div class="panel filter-panel">
      <el-input v-model="keyword" placeholder="操作人 / 模块 / IP" clearable style="width: 260px" />
      <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" />
      <el-select v-model="level" style="width: 150px">
        <el-option label="全部等级" value="all" />
        <el-option label="普通" value="info" />
        <el-option label="高风险" value="risk" />
      </el-select>
    </div>

    <div class="panel">
      <el-table :data="rows" row-key="id">
        <el-table-column prop="time" label="时间" width="180" />
        <el-table-column prop="operator" label="操作人" width="140" />
        <el-table-column prop="module" label="模块" width="140" />
        <el-table-column prop="action" label="操作内容" min-width="220" />
        <el-table-column prop="ip" label="IP" width="150" />
        <el-table-column label="等级" width="110">
          <template #default="scope"><el-tag :type="scope.row.level === '高风险' ? 'danger' : 'info'" effect="plain">{{ scope.row.level }}</el-tag></template>
        </el-table-column>
      </el-table>
    </div>
  </PageShell>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { Download, Search } from '@element-plus/icons-vue';
import PageShell from '@/components/common/PageShell.vue';

const keyword = ref('');
const dateRange = ref('');
const level = ref('all');
const rows = [
  { id: 1, time: '2026-06-14 10:21', operator: '系统管理员', module: 'AI 配置', action: '测试 OpenAI 供应商连通性', ip: '10.0.1.24', level: '普通' },
  { id: 2, time: '2026-06-14 09:42', operator: '审核专员', module: '内容审核', action: '驳回 AI 生成题目', ip: '10.0.1.38', level: '普通' },
  { id: 3, time: '2026-06-13 18:05', operator: '平台管理员', module: '权限', action: '修改角色权限范围', ip: '10.0.1.10', level: '高风险' }
];
</script>
