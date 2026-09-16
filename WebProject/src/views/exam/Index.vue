<template>
  <DomainPage
    title="测评考试"
    description="维护题库、试卷、考试安排、自动批改和学情诊断。"
    primary-action="创建测评"
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
    title="测评"
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
import { createExercise, listExercises, type ExerciseItem } from '@/api/exam';
import { displayDate, displayStatus, getPageRecords, getPageTotal, valueText } from '@/utils/page';

const fields: DomainField[] = [
  { label: '测评标题', prop: 'title' },
  { label: '课程ID', prop: 'courseId', type: 'number' },
  { label: '状态', prop: 'status', type: 'select', options: [{ label: '草稿', value: 'DRAFT' }, { label: '启用', value: 'PUBLISHED' }, { label: '结束', value: 'COMPLETED' }] }
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

function mapRecord(item: ExerciseItem): DomainRecord {
  return {
    id: item.id,
    name: valueText(item.title || item.name, `测评 ${item.id}`),
    owner: `课程 ${valueText(item.courseId, '-')}`,
    status: displayStatus(item.status || '启用'),
    updatedAt: displayDate(item.updatedAt || item.createdAt),
    raw: item
  };
}

async function fetchData() {
  loading.value = true;
  try {
    const data = await listExercises({ pageNo: page.value, pageSize: pageSize.value, keyword: keyword.value || undefined });
    records.value = getPageRecords(data).map(mapRecord);
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
  currentRecord.value = { status: 'DRAFT' };
  dialogVisible.value = true;
}

function openDetail(record: DomainRecord) {
  dialogMode.value = 'detail';
  currentRecord.value = record.raw as Record<string, unknown>;
  dialogVisible.value = true;
}

async function handleSubmit(value: Record<string, unknown>) {
  saving.value = true;
  try {
    await createExercise(value as ExerciseItem);
    ElMessage.success('测评已创建');
    dialogVisible.value = false;
    await fetchData();
  } finally {
    saving.value = false;
  }
}

onMounted(fetchData);
</script>
