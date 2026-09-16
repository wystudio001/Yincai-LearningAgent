<template>
  <PageShell :title="title" :description="description">
    <template #actions>
      <el-button :icon="Refresh" :loading="loading" @click="handleRefresh">刷新</el-button>
      <el-button v-if="!hideCreate" type="primary" :icon="Plus" @click="handlePrimary">{{ primaryAction }}</el-button>
    </template>

    <template #filters>
      <div class="panel filter-panel">
        <el-input v-model="keyword" placeholder="输入关键字" clearable style="width: 260px" @keyup.enter="handleSearch" />
        <el-select v-model="status" placeholder="状态" style="width: 160px">
          <el-option label="全部" value="all" />
          <el-option label="启用" value="PUBLISHED" />
          <el-option label="草稿" value="DRAFT" />
          <el-option label="停用" value="DISABLED" />
          <el-option label="归档" value="ARCHIVED" />
        </el-select>
        <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
      </div>
    </template>

    <div class="metrics-grid">
      <MetricCard v-for="item in normalizedMetrics" :key="item.label" v-bind="item" />
    </div>

    <div class="panel">
      <el-table v-loading="loading" :data="displayRecords" row-key="id" empty-text="暂无数据">
        <el-table-column prop="name" label="名称" min-width="220" show-overflow-tooltip />
        <el-table-column prop="owner" label="归属" width="160" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="scope">
            <el-tag :type="statusTagType(scope.row.status)" effect="plain">{{ scope.row.status || '未知' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updatedAt" label="更新时间" width="190" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button link type="primary" @click="handleDetail(scope.row)">详情</el-button>
            <el-button link @click="handleEdit(scope.row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="table-footer">
        <el-pagination
          background
          layout="total, prev, pager, next, sizes"
          :current-page="effectivePage"
          :page-size="effectivePageSize"
          :page-sizes="[10, 20, 50]"
          :total="effectiveTotal"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </div>

    <DomainCrudDialog
      v-model="fallbackVisible"
      title="条目"
      :mode="fallbackMode"
      :fields="fallbackFields"
      :record="fallbackRecord"
      @submit="handleFallbackSubmit"
    />
  </PageShell>
</template>

<script setup lang="ts">
import { computed, getCurrentInstance, ref, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { Plus, Refresh, Search } from '@element-plus/icons-vue';
import PageShell from '@/components/common/PageShell.vue';
import MetricCard from '@/components/common/MetricCard.vue';
import DomainCrudDialog, { type DomainField } from '@/components/business/DomainCrudDialog.vue';

export interface DomainRecord {
  id?: string | number;
  name: string;
  owner: string;
  status: string;
  updatedAt: string;
  raw?: unknown;
}

export interface DomainMetric {
  label: string;
  value: string | number;
  trend: string;
}

const props = withDefaults(
  defineProps<{
    title: string;
    description: string;
    primaryAction: string;
    records?: DomainRecord[];
    metrics?: DomainMetric[];
    loading?: boolean;
    total?: number;
    page?: number;
    pageSize?: number;
    hideCreate?: boolean;
  }>(),
  {
    records: () => [],
    metrics: undefined,
    loading: false,
    total: 0,
    page: 1,
    pageSize: 10,
    hideCreate: false
  }
);

const emit = defineEmits<{
  refresh: [];
  primary: [];
  search: [payload: { keyword: string; status?: string }];
  detail: [record: DomainRecord];
  edit: [record: DomainRecord];
  'page-change': [page: number];
  'size-change': [pageSize: number];
}>();

const keyword = ref('');
const status = ref('all');
const localRecords = ref<DomainRecord[]>([]);
const localPage = ref(1);
const localPageSize = ref(10);
const fallbackVisible = ref(false);
const fallbackMode = ref<'create' | 'edit' | 'detail'>('detail');
const fallbackRecord = ref<Record<string, unknown> | null>(null);
const instance = getCurrentInstance();

const fallbackFields: DomainField[] = [
  { label: '名称', prop: 'name' },
  { label: '归属', prop: 'owner' },
  { label: '状态', prop: 'status', type: 'select', options: [{ label: '启用', value: '启用' }, { label: '草稿', value: '草稿' }, { label: '停用', value: '停用' }, { label: '归档', value: '归档' }] },
  { label: '更新时间', prop: 'updatedAt', readonly: true }
];

const usingExternalData = computed(() => hasListener('onRefresh') || hasListener('onPage-change') || hasListener('onPageChange'));

const sourceRecords = computed(() => (usingExternalData.value ? props.records : localRecords.value));

const filteredLocalRecords = computed(() => {
  if (usingExternalData.value) {
    return props.records;
  }
  return localRecords.value.filter((item) => {
    const matchKeyword = !keyword.value || item.name.includes(keyword.value) || item.owner.includes(keyword.value);
    const matchStatus = status.value === 'all' || item.status === status.value || statusLabel(status.value) === item.status;
    return matchKeyword && matchStatus;
  });
});

const displayRecords = computed(() => {
  if (usingExternalData.value) {
    return props.records;
  }
  const start = (localPage.value - 1) * localPageSize.value;
  return filteredLocalRecords.value.slice(start, start + localPageSize.value);
});

const effectivePage = computed(() => (usingExternalData.value ? props.page : localPage.value));
const effectivePageSize = computed(() => (usingExternalData.value ? props.pageSize : localPageSize.value));
const effectiveTotal = computed(() => (usingExternalData.value ? props.total : filteredLocalRecords.value.length));

const normalizedMetrics = computed(() => {
  if (props.metrics?.length) {
    return props.metrics;
  }
  const records = sourceRecords.value;
  const enabledCount = records.filter((item) => ['启用', 'PUBLISHED', 'ACTIVE'].includes(item.status)).length;
  const draftCount = records.filter((item) => ['草稿', 'DRAFT'].includes(item.status)).length;
  return [
    { label: '当前数据', value: effectiveTotal.value, trend: '实时统计' },
    { label: '启用项目', value: enabledCount, trend: '已上线' },
    { label: '草稿项目', value: draftCount, trend: '待完善' },
    { label: '分页大小', value: effectivePageSize.value, trend: `第 ${effectivePage.value} 页` }
  ];
});

watch(
  () => props.records,
  (records) => {
    if (!usingExternalData.value) {
      localRecords.value = records.map((item, index) => ({ ...item, id: item.id ?? `${titleKey()}-${index + 1}` }));
    }
  },
  { immediate: true, deep: true }
);

watch(status, handleSearch);

function hasListener(name: string) {
  return Boolean(instance?.vnode.props?.[name]);
}

function titleKey() {
  return props.title.replace(/\s+/g, '-');
}

function nowText() {
  const date = new Date();
  const pad = (value: number) => String(value).padStart(2, '0');
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`;
}

function statusLabel(value: string) {
  const dict: Record<string, string> = { PUBLISHED: '启用', ACTIVE: '启用', DRAFT: '草稿', DISABLED: '停用', ARCHIVED: '归档' };
  return dict[value] || value;
}

function handleSearch() {
  if (hasListener('onSearch')) {
    emit('search', { keyword: keyword.value.trim(), status: status.value === 'all' ? undefined : status.value });
    return;
  }
  localPage.value = 1;
}

function handleRefresh() {
  if (hasListener('onRefresh')) {
    emit('refresh');
    return;
  }
  ElMessage.success('当前页面已刷新');
}

function handlePrimary() {
  if (hasListener('onPrimary')) {
    emit('primary');
    return;
  }
  fallbackMode.value = 'create';
  fallbackRecord.value = { status: '启用', updatedAt: nowText() };
  fallbackVisible.value = true;
}

function handleDetail(record: DomainRecord) {
  if (hasListener('onDetail')) {
    emit('detail', record);
    return;
  }
  fallbackMode.value = 'detail';
  fallbackRecord.value = { ...record };
  fallbackVisible.value = true;
}

function handleEdit(record: DomainRecord) {
  if (hasListener('onEdit')) {
    emit('edit', record);
    return;
  }
  fallbackMode.value = 'edit';
  fallbackRecord.value = { ...record };
  fallbackVisible.value = true;
}

function handlePageChange(value: number) {
  if (hasListener('onPage-change') || hasListener('onPageChange')) {
    emit('page-change', value);
    return;
  }
  localPage.value = value;
}

function handleSizeChange(value: number) {
  if (hasListener('onSize-change') || hasListener('onSizeChange')) {
    emit('size-change', value);
    return;
  }
  localPageSize.value = value;
  localPage.value = 1;
}

function handleFallbackSubmit(value: Record<string, unknown>) {
  const fallbackId = fallbackRecord.value?.id;
  const id = typeof fallbackId === 'string' || typeof fallbackId === 'number' ? fallbackId : `${titleKey()}-${Date.now()}`;
  const record: DomainRecord = {
    id,
    name: String(value.name || '未命名条目'),
    owner: String(value.owner || '管理端'),
    status: String(value.status || '启用'),
    updatedAt: nowText(),
    raw: value
  };
  if (fallbackMode.value === 'edit') {
    localRecords.value = localRecords.value.map((item) => (item.id === record.id ? record : item));
    ElMessage.success('条目已更新');
  } else {
    localRecords.value = [record, ...localRecords.value];
    ElMessage.success('条目已新增');
  }
  fallbackVisible.value = false;
}

function statusTagType(value: string) {
  if (['启用', 'PUBLISHED', 'ACTIVE', '1'].includes(value)) {
    return 'success';
  }
  if (['草稿', 'DRAFT', 'PENDING'].includes(value)) {
    return 'warning';
  }
  if (['停用', 'DISABLED', '0'].includes(value)) {
    return 'info';
  }
  return 'info';
}
</script>
