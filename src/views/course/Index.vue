<template>
  <DomainPage
    title="课程管理"
    description="维护课程目录、章节结构、管理端归属和知识点覆盖。"
    primary-action="新增课程"
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
    title="课程"
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
import { createCourse, listCourses, updateCourse, type CourseItem } from '@/api/course';
import { displayDate, displayStatus, getPageRecords, getPageTotal, valueText } from '@/utils/page';

const fields: DomainField[] = [
  { label: '课程名称', prop: 'name' },
  { label: '课程描述', prop: 'description', type: 'textarea' },
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

function mapRecord(item: CourseItem): DomainRecord {
  return {
    id: item.id,
    name: item.name,
    owner: valueText(item.description, '课程中心'),
    status: displayStatus(item.status),
    updatedAt: displayDate(item.updatedAt || item.createdAt),
    raw: item
  };
}

async function fetchData() {
  loading.value = true;
  try {
    const data = await listCourses({ page: page.value, size: pageSize.value, keyword: keyword.value || undefined, status: status.value });
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
  currentRecord.value = null;
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
      await createCourse(value as Partial<CourseItem>);
      ElMessage.success('课程已新增');
    } else if (currentRecord.value?.id) {
      await updateCourse(currentRecord.value.id as string | number, value as Partial<CourseItem>);
      ElMessage.success('课程已更新');
    }
    dialogVisible.value = false;
    await fetchData();
  } finally {
    saving.value = false;
  }
}

onMounted(fetchData);
</script>
