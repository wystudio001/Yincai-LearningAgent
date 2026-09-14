<template>
  <DomainPage
    title="学生画像"
    description="查看学生能力、兴趣、薄弱点、风险等级和个性化标签。"
    primary-action="导入画像"
    :records="records"
    :loading="loading"
    :total="total"
    :page="page"
    :page-size="pageSize"
    hide-create
    @refresh="fetchData"
    @search="handleSearch"
    @page-change="handlePageChange"
    @size-change="handleSizeChange"
  />
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import DomainPage, { type DomainRecord } from '@/components/business/DomainPage.vue';
import { listStudentProfiles, type StudentProfileItem } from '@/api/profile';
import { displayDate, displayStatus, getPageRecords, getPageTotal, valueText } from '@/utils/page';

const loading = ref(false);
const records = ref<DomainRecord[]>([]);
const total = ref(0);
const page = ref(1);
const pageSize = ref(10);
const keyword = ref('');

function mapRecord(item: StudentProfileItem): DomainRecord {
  return {
    id: item.id || `${item.studentId || 'student'}-${item.courseId || 'course'}`,
    name: `学生 ${valueText(item.studentId, '-')}`,
    owner: valueText(item.abilityLevel || item.summary, '画像中心'),
    status: displayStatus(item.riskLevel || '启用'),
    updatedAt: displayDate(item.updatedAt || item.createdAt),
    raw: item
  };
}

async function fetchData() {
  loading.value = true;
  try {
    const data = await listStudentProfiles({ page: page.value, size: pageSize.value, keyword: keyword.value || undefined });
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

onMounted(fetchData);
</script>
