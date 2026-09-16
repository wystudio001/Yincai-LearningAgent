<template>
  <DomainPage
    title="智能辅导"
    description="查看智能体会话、追问质量、管理端接管和安全拦截。"
    primary-action="新建任务"
    :records="records"
    :metrics="metrics"
    @refresh="refreshTasks"
    @primary="openCreate"
    @detail="openDetail"
    @edit="openDetail"
  />
  <DomainCrudDialog
    v-model="dialogVisible"
    title="智能体任务"
    :mode="dialogMode"
    :fields="fields"
    :record="currentRecord"
    :saving="saving"
    @submit="handleSubmit"
  />
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus';
import { ref } from 'vue';
import DomainCrudDialog, { type DomainField } from '@/components/business/DomainCrudDialog.vue';
import DomainPage, { type DomainMetric, type DomainRecord } from '@/components/business/DomainPage.vue';
import { createAgentTask, getAgentTaskStatus } from '@/api/ai';

const fields: DomainField[] = [
  { label: '任务类型', prop: 'type', type: 'select', options: [{ label: '课程问答', value: 'tutor-answer' }, { label: '资源生成', value: 'resource-generate' }, { label: '学习路径规划', value: 'learning-path-plan' }] },
  { label: '课程ID', prop: 'courseId', type: 'number' },
  { label: '学生ID', prop: 'studentId', type: 'number' },
  { label: '输入摘要', prop: 'prompt', type: 'textarea' }
];

const saving = ref(false);
const dialogVisible = ref(false);
const dialogMode = ref<'create' | 'edit' | 'detail'>('detail');
const currentRecord = ref<Record<string, unknown> | null>(null);
const records = ref<DomainRecord[]>([
  { id: 'policy-qa', name: '课程问答智能体', owner: 'AI 平台组', status: '启用', updatedAt: '2026-06-14 10:50' },
  { id: 'policy-code', name: '代码调试智能体', owner: 'AI 平台组', status: '启用', updatedAt: '2026-06-13 20:30' },
  { id: 'policy-safety', name: '安全拦截策略', owner: '平台安全', status: '启用', updatedAt: '2026-06-13 17:40' }
]);
const metrics = ref<DomainMetric[]>([
  { label: '智能体策略', value: records.value.length, trend: '已配置' },
  { label: '运行任务', value: 0, trend: '当前会话' },
  { label: '安全策略', value: '启用', trend: '内容审核' },
  { label: '后端接口', value: '已接入', trend: 'internal/v1/ai' }
]);

function openCreate() {
  dialogMode.value = 'create';
  currentRecord.value = { type: 'tutor-answer' };
  dialogVisible.value = true;
}

function openDetail(record: DomainRecord) {
  dialogMode.value = 'detail';
  currentRecord.value = record.raw as Record<string, unknown> || { ...record };
  dialogVisible.value = true;
}

async function handleSubmit(value: Record<string, unknown>) {
  saving.value = true;
  try {
    const result = await createAgentTask(value as { type: string; [key: string]: unknown });
    const taskId = String(result.taskId || `task-${Date.now()}`);
    records.value = [
      { id: taskId, name: `智能体任务 ${taskId}`, owner: String(value.type || 'agent'), status: String(result.status || 'pending'), updatedAt: new Date().toLocaleString(), raw: result },
      ...records.value
    ];
    metrics.value[1] = { label: '运行任务', value: Number(metrics.value[1].value) + 1, trend: '当前会话' };
    ElMessage.success('智能体任务已创建');
    dialogVisible.value = false;
  } finally {
    saving.value = false;
  }
}

async function refreshTasks() {
  const taskRecords = records.value.filter((item) => String(item.id).startsWith('task-'));
  const updated = await Promise.all(taskRecords.map(async (item) => ({ item, status: await getAgentTaskStatus(String(item.id)) })));
  records.value = records.value.map((record) => {
    const match = updated.find((item) => item.item.id === record.id);
    return match ? { ...record, status: String(match.status.status || record.status), updatedAt: new Date().toLocaleString(), raw: match.status } : record;
  });
  ElMessage.success('智能体任务状态已刷新');
}
</script>
