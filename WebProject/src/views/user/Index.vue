<template>
  <DomainPage
    title="用户权限"
    description="管理管理员、教师、学生账号、角色授权和组织归属。"
    primary-action="新增用户"
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
    title="用户"
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
import { createUser, listUsers, updateUser, type UserItem } from '@/api/user';
import { displayDate, displayStatus, getPageRecords, getPageTotal, valueText } from '@/utils/page';

const fields: DomainField[] = [
  { label: '登录账号', prop: 'username' },
  { label: '真实姓名', prop: 'realName' },
  { label: '角色', prop: 'roleCode', type: 'select', options: [{ label: '管理员', value: 'ADMIN' }, { label: '平台管理员', value: 'PLATFORM_ADMIN' }, { label: '教师', value: 'TEACHER' }, { label: '学生', value: 'STUDENT' }] },
  { label: '邮箱', prop: 'email' },
  { label: '手机号', prop: 'phone' },
  { label: '状态', prop: 'status', type: 'select', options: [{ label: '启用', value: 1 }, { label: '停用', value: 0 }] }
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

function mapRecord(item: UserItem): DomainRecord {
  return {
    id: item.id,
    name: valueText(item.realName || item.username, '未命名用户'),
    owner: valueText(item.roleCode, '未分配角色'),
    status: displayStatus(item.status),
    updatedAt: displayDate(item.updatedAt || item.createdAt),
    raw: item
  };
}

async function fetchData() {
  loading.value = true;
  try {
    const data = await listUsers({ page: page.value, size: pageSize.value, keyword: keyword.value || undefined });
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
  currentRecord.value = { status: 1, roleCode: 'TEACHER' };
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
      if (!value.username) {
        ElMessage.warning('请填写登录账号');
        return;
      }
      await createUser(value as Partial<UserItem>);
      ElMessage.success('用户已新增');
    } else if (currentRecord.value?.id) {
      await updateUser(currentRecord.value.id as string | number, value as Partial<UserItem>);
      ElMessage.success('用户已更新');
    }
    dialogVisible.value = false;
    await fetchData();
  } finally {
    saving.value = false;
  }
}

onMounted(fetchData);
</script>
