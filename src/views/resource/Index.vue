<template>
  <DomainPage
    title="资源管理"
    description="管理课件、习题、案例、视频和大模型生成资源。"
    primary-action="新增资源"
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
    @edit="openEdit"
  />
  <DomainCrudDialog
    v-model="dialogVisible"
    title="资源"
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
import { createResource, listResources, updateResource, type ResourceItem } from '@/api/resource';
import { displayDate, displayStatus, getPageRecords, getPageTotal, valueText } from '@/utils/page';

const fields: DomainField[] = [
  { label: '资源标题', prop: 'title' },
  { label: '资源摘要', prop: 'summary', type: 'textarea' },
  { label: '资源类型', prop: 'resourceType', type: 'select', options: [{ label: '课件', value: 'COURSEWARE' }, { label: '习题', value: 'EXERCISE' }, { label: '案例', value: 'CASE' }, { label: '视频', value: 'VIDEO' }] },
  { label: '课程ID', prop: 'courseId', type: 'number' },
  { label: '难度', prop: 'difficulty', type: 'select', options: [{ label: '基础', value: 'BASIC' }, { label: '中等', value: 'MEDIUM' }, { label: '进阶', value: 'ADVANCED' }] },
  { label: '状态', prop: 'status', type: 'select', options: [{ label: '草稿', value: 'DRAFT' }, { label: '启用', value: 'PUBLISHED' }, { label: '归档', value: 'ARCHIVED' }] }
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
const status = ref<string>();

function mapRecord(item: ResourceItem): DomainRecord {
  return {
    id: item.id,
    name: item.title,
    owner: valueText(item.resourceType || item.difficulty, '资源中心'),
    status: displayStatus(item.status),
    updatedAt: displayDate(item.updatedAt || item.createdAt),
    raw: item
  };
}

async function fetchData() {
  loading.value = true;
  try {
    const data = await listResources({ page: page.value, size: pageSize.value, keyword: keyword.value || undefined, status: status.value });
    records.value = getPageRecords(data).map(mapRecord);
    total.value = getPageTotal(data);
  } finally {
    loading.value = false;
  }
}

function handleSearch(payload: { keyword: string; status?: string }) {
  keyword.value = payload.keyword;
  status.value = payload.status;
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
  currentRecord.value = { resourceType: 'COURSEWARE', difficulty: 'BASIC', status: 'DRAFT' };
  dialogVisible.value = true;
}

function openDetail(record: DomainRecord) {
  dialogMode.value = 'detail';
  currentRecord.value = record.raw as Record<string, unknown>;
  dialogVisible.value = true;
}

function openEdit(record: DomainRecord) {
  dialogMode.value = 'edit';
  currentRecord.value = record.raw as Record<string, unknown>;
  dialogVisible.value = true;
}

async function handleSubmit(value: Record<string, unknown>) {
  saving.value = true;
  try {
    if (dialogMode.value === 'create') {
      await createResource(value as Partial<ResourceItem>);
      ElMessage.success('资源已新增');
    } else if (currentRecord.value?.id) {
      await updateResource(currentRecord.value.id as string | number, value as Partial<ResourceItem>);
      ElMessage.success('资源已更新');
    }
    dialogVisible.value = false;
    await fetchData();
  } finally {
    saving.value = false;
  }
}

onMounted(fetchData);
</script>
