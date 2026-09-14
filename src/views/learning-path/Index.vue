<template>
  <DomainPage
    title="学习路径"
    description="编排课程目标、知识点依赖、个性化推荐路径和补弱计划。"
    primary-action="生成路径"
    :records="records"
    :loading="loading"
    :total="total"
    :page="page"
    :page-size="pageSize"
    @refresh="fetchData"
    @search="handleSearch"
    @page-change="handlePageChange"
    @size-change="handleSizeChange"
    @primary="openCreate"
    @detail="openDetail"
    @edit="openDetail"
  />
  <DomainCrudDialog
    v-model="dialogVisible"
    title="学习路径"
    :mode="dialogMode"
    :fields="fields"
    :record="currentRecord"
    :saving="saving"
    @submit="handleSubmit"
  />
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus';
import { onMounted, ref } from 'vue';
import DomainCrudDialog, { type DomainField } from '@/components/business/DomainCrudDialog.vue';
import DomainPage, { type DomainRecord } from '@/components/business/DomainPage.vue';
import { generateLearningPath, listLearningPaths, type LearningPathItem } from '@/api/learningPath';
import { displayDate, displayStatus, getPageRecords, getPageTotal, valueText } from '@/utils/page';

const fields: DomainField[] = [
  { label: '学生ID', prop: 'studentId', type: 'number' },
  { label: '课程ID', prop: 'courseId', type: 'number' },
  { label: '状态', prop: 'status', readonly: true },
  { label: '版本', prop: 'version', readonly: true }
];

const loading = ref(false);
const saving = ref(false);
const dialogVisible = ref(false);
const dialogMode = ref<'create' | 'edit' | 'detail'>('detail');
const currentRecord = ref<Record<string, unknown> | null>(null);
const records = ref<DomainRecord[]>([]);
const total = ref(0);
const page = ref(1);
const pageSize = ref(10);
const keyword = ref('');

function mapRecord(item: LearningPathItem): DomainRecord {
  return {
    id: item.id,
    name: `学习路径 ${item.id}`,
    owner: `学生 ${valueText(item.studentId, '-')} / 课程 ${valueText(item.courseId, '-')}`,
    status: displayStatus(item.status),
    updatedAt: displayDate(item.updatedAt || item.createdAt),
    raw: item
  };
}

async function fetchData() {
  loading.value = true;
  try {
    const data = await listLearningPaths({ page: page.value, size: pageSize.value, keyword: keyword.value || undefined, studentId: 3 });
    records.value = getPageRecords(data).filter(Boolean).map(mapRecord);
    total.value = getPageTotal(data);
  } finally {
    loading.value = false;
  }
}

function handleSearch(payload: { keyword: string }) {
  keyword.value = payload.keyword;
  page.value = 1;
  fetchData();
}

function handlePageChange(value: number) {
  page.value = value;
  fetchData();
}

function handleSizeChange(value: number) {
  pageSize.value = value;
  page.value = 1;
  fetchData();
}

function openCreate() {
  dialogMode.value = 'create';
  currentRecord.value = { studentId: 3 };
  dialogVisible.value = true;
}

function openDetail(record: DomainRecord) {
  dialogMode.value = 'detail';
  currentRecord.value = record.raw as Record<string, unknown>;
  dialogVisible.value = true;
}

async function handleSubmit(value: Record<string, unknown>) {
  if (!value.studentId || !value.courseId) {
    ElMessage.warning('请填写学生ID和课程ID');
    return;
  }
  saving.value = true;
  try {
    const result = await generateLearningPath(value);
    records.value = [mapRecord(result), ...records.value];
    total.value += 1;
    ElMessage.success('学习路径已生成');
    dialogVisible.value = false;
  } finally {
    saving.value = false;
  }
}

onMounted(fetchData);
</script>
