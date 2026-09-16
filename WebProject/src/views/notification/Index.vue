<template>
  <DomainPage
    title="消息通知"
    description="配置站内信、邮件、短信、任务提醒和异常告警模板。"
    primary-action="新增模板"
    :records="records"
    :metrics="metrics"
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
    title="通知模板"
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
import DomainPage, { type DomainMetric, type DomainRecord } from '@/components/business/DomainPage.vue';
import { createNotificationTemplate, getUnreadCount, listNotifications, updateNotificationTemplate, type NotificationItem, type NotificationTemplateItem } from '@/api/notification';
import { displayDate, displayStatus, getPageRecords, getPageTotal, valueText } from '@/utils/page';

const fields: DomainField[] = [
  { label: '模板编码', prop: 'code' },
  { label: '模板名称', prop: 'name' },
  { label: '标题', prop: 'title' },
  { label: '内容', prop: 'content', type: 'textarea' },
  { label: '类型', prop: 'type', type: 'select', options: [{ label: '系统', value: 'SYSTEM' }, { label: '学习', value: 'LEARNING' }, { label: '告警', value: 'ALERT' }] },
  { label: '启用', prop: 'enabled', type: 'select', options: [{ label: '启用', value: 1 }, { label: '停用', value: 0 }] }
];

const loading = ref(false);
const saving = ref(false);
const dialogVisible = ref(false);
const dialogMode = ref<'create' | 'edit' | 'detail'>('detail');
const currentRecord = ref<Record<string, unknown> | null>(null);
const records = ref<DomainRecord[]>([]);
const metrics = ref<DomainMetric[]>([]);
const total = ref(0);
const page = ref(1);
const pageSize = ref(10);
const keyword = ref('');

function mapRecord(item: NotificationItem): DomainRecord {
  return {
    id: item.id,
    name: valueText(item.title, '未命名通知'),
    owner: valueText(item.type, 'SYSTEM'),
    status: displayStatus(item.status),
    updatedAt: displayDate(item.updatedAt || item.createdAt),
    raw: item
  };
}

async function fetchData() {
  loading.value = true;
  try {
    const [data, unread] = await Promise.all([listNotifications({ pageNo: page.value, pageSize: pageSize.value, keyword: keyword.value || undefined }), getUnreadCount()]);
    records.value = getPageRecords(data).map(mapRecord);
    total.value = getPageTotal(data);
    metrics.value = [
      { label: '通知总数', value: total.value, trend: '实时同步' },
      { label: '未读通知', value: unread.unreadCount || 0, trend: '当前账号' },
      { label: '当前页', value: records.value.length, trend: `第 ${page.value} 页` },
      { label: '模板维护', value: '可用', trend: '新增/编辑' }
    ];
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
  currentRecord.value = { type: 'SYSTEM', enabled: 1 };
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
      await createNotificationTemplate(value as NotificationTemplateItem);
      ElMessage.success('通知模板已新增');
    } else if (currentRecord.value?.id) {
      await updateNotificationTemplate(currentRecord.value.id as string | number, value as NotificationTemplateItem);
      ElMessage.success('通知模板已更新');
    }
    dialogVisible.value = false;
    await fetchData();
  } finally {
    saving.value = false;
  }
}

onMounted(fetchData);
</script>
