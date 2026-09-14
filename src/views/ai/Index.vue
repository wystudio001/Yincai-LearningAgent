<template>
  <PageShell title="AI 配置" description="统一管理 OpenAI-compatible 大模型供应商、baseURL、API Key、默认模型和调用参数。">
    <template #actions>
      <el-button :icon="Refresh" :loading="loading" @click="fetchProviders">刷新</el-button>
      <el-button type="primary" :icon="Plus" @click="openCreate">新增供应商</el-button>
    </template>

    <div class="panel filter-panel">
      <el-input v-model="keyword" placeholder="供应商、模型或 baseURL" clearable style="width: 280px" @keyup.enter="fetchProviders" />
      <el-select v-model="enabledFilter" style="width: 150px">
        <el-option label="全部状态" value="all" />
        <el-option label="仅启用" value="enabled" />
        <el-option label="仅停用" value="disabled" />
      </el-select>
      <el-button type="primary" :icon="Search" @click="fetchProviders">查询</el-button>
    </div>

    <div class="panel summary-grid">
      <div class="summary-item">
        <span>供应商总数</span>
        <strong>{{ providers.length }}</strong>
      </div>
      <div class="summary-item">
        <span>已启用</span>
        <strong>{{ enabledCount }}</strong>
      </div>
      <div class="summary-item">
        <span>当前调用协议</span>
        <strong>OpenAI Compatible</strong>
      </div>
    </div>

    <div class="panel">
      <el-table v-loading="loading" :data="filteredProviders" row-key="id" empty-text="暂无大模型供应商">
        <el-table-column prop="name" label="供应商" min-width="150" />
        <el-table-column prop="baseUrl" label="baseURL" min-width="260" show-overflow-tooltip />
        <el-table-column prop="defaultModel" label="默认模型" width="190" show-overflow-tooltip />
        <el-table-column label="API Key" width="180">
          <template #default="scope">{{ scope.row.apiKeyMasked || scope.row.keyMasked || '未配置' }}</template>
        </el-table-column>
        <el-table-column label="速度" width="120">
          <template #default="scope">{{ speedLabel(scope.row.temperature) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="scope">
            <el-tag :type="scope.row.enabled ? 'success' : 'info'" effect="plain">{{ scope.row.enabled ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="310" fixed="right">
          <template #default="scope">
            <el-button link type="primary" :loading="testingId === scope.row.id" @click="testProvider(scope.row)">连通测试</el-button>
            <el-button link @click="toggleEnabled(scope.row)">{{ scope.row.enabled ? '停用' : '启用' }}</el-button>
            <el-button link @click="rotateKey(scope.row)">轮换密钥</el-button>
            <el-button link @click="openEdit(scope.row)">编辑</el-button>
            <el-button link type="danger" @click="removeProvider(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-drawer v-model="drawerVisible" :title="editingId ? '编辑大模型供应商' : '新增大模型供应商'" size="560px">
      <el-form ref="formRef" label-position="top" :model="form" :rules="rules">
        <el-form-item label="供应商名称" prop="name">
          <el-input v-model="form.name" placeholder="OpenAI / DeepSeek / Qwen / 校内模型网关" />
        </el-form-item>
        <el-form-item label="供应商编码" prop="providerCode">
          <el-input v-model="form.providerCode" placeholder="openai / deepseek / qwen / school-gateway" />
        </el-form-item>
        <el-form-item label="baseURL" prop="baseUrl">
          <el-input v-model="form.baseUrl" placeholder="https://api.example.com/v1" />
        </el-form-item>
        <el-form-item label="API Key" :prop="editingId ? undefined : 'apiKey'">
          <el-input v-model="form.apiKey" type="password" show-password :placeholder="editingId ? '留空则保持原密钥' : '请输入 API Key'" />
        </el-form-item>
        <el-form-item label="默认模型" prop="defaultModel">
          <div class="model-picker">
            <el-select
              v-model="form.defaultModel"
              filterable
              allow-create
              default-first-option
              placeholder="先获取模型列表，或手动输入模型名"
              :loading="catalogLoading"
            >
              <el-option v-for="model in modelOptions" :key="model" :label="model" :value="model" />
            </el-select>
            <el-button :icon="Refresh" :loading="catalogLoading" @click="loadModelCatalog">获取模型</el-button>
          </div>
        </el-form-item>
        <div class="form-row">
          <el-form-item label="速度" prop="temperature">
            <el-segmented v-model="speedLevel" :options="speedOptions" />
          </el-form-item>
          <el-form-item label="最大 tokens" prop="maxTokens">
            <el-input-number v-model="form.maxTokens" :min="128" :max="64000" :step="128" controls-position="right" />
          </el-form-item>
          <el-form-item label="超时毫秒" prop="timeoutMs">
            <el-input-number v-model="form.timeoutMs" :min="5000" :max="300000" :step="5000" controls-position="right" />
          </el-form-item>
        </div>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="用途、费用归属或调用说明" />
        </el-form-item>
        <el-form-item label="启用状态">
          <el-switch v-model="form.enabled" active-text="启用" inactive-text="停用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="drawerVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveProvider">保存</el-button>
      </template>
    </el-drawer>
  </PageShell>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus';
import { Plus, Refresh, Search } from '@element-plus/icons-vue';
import PageShell from '@/components/common/PageShell.vue';
import {
  createModelProvider,
  deleteModelProvider,
  fetchModelCatalog,
  listModelProviders,
  testModelProvider,
  updateModelProvider,
  type ModelProviderItem,
  type ModelProviderPayload
} from '@/api/ai';

type SpeedLevel = 'low' | 'medium' | 'high' | 'ultra';

const speedMap: Record<SpeedLevel, number> = {
  low: 0.2,
  medium: 0.7,
  high: 1.2,
  ultra: 1.8
};
const speedOptions = [
  { label: '低', value: 'low' },
  { label: '中', value: 'medium' },
  { label: '高', value: 'high' },
  { label: '超高', value: 'ultra' }
];

const keyword = ref('');
const enabledFilter = ref('all');
const drawerVisible = ref(false);
const loading = ref(false);
const saving = ref(false);
const catalogLoading = ref(false);
const testingId = ref('');
const editingId = ref('');
const speedLevel = ref<SpeedLevel>('low');
const providers = ref<ModelProviderItem[]>([]);
const modelOptions = ref<string[]>([]);
const formRef = ref<FormInstance>();
const form = reactive<ModelProviderPayload>({
  name: '',
  providerCode: '',
  baseUrl: '',
  apiKey: '',
  defaultModel: '',
  enabled: true,
  timeoutMs: 120000,
  temperature: 0.2,
  maxTokens: 2048,
  remark: ''
});

const rules: FormRules = {
  name: [{ required: true, message: '请输入供应商名称', trigger: 'blur' }],
  baseUrl: [{ required: true, message: '请输入 baseURL', trigger: 'blur' }],
  apiKey: [{ required: true, message: '请输入 API Key', trigger: 'blur' }],
  defaultModel: [{ required: true, message: '请选择或输入默认模型', trigger: 'change' }]
};

watch(speedLevel, (level) => {
  form.temperature = speedMap[level];
});

const enabledCount = computed(() => providers.value.filter((item) => item.enabled).length);
const filteredProviders = computed(() => {
  const normalizedKeyword = keyword.value.trim().toLowerCase();
  return providers.value.filter((item) => {
    const text = `${item.name} ${item.providerCode || ''} ${item.baseUrl} ${item.defaultModel || item.model || ''}`.toLowerCase();
    const matchKeyword = !normalizedKeyword || text.includes(normalizedKeyword);
    const matchEnabled = enabledFilter.value === 'all' || (enabledFilter.value === 'enabled' ? item.enabled : !item.enabled);
    return matchKeyword && matchEnabled;
  });
});

async function fetchProviders() {
  loading.value = true;
  try {
    providers.value = await listModelProviders();
  } finally {
    loading.value = false;
  }
}

function resetForm() {
  Object.assign(form, {
    name: '',
    providerCode: '',
    baseUrl: '',
    apiKey: '',
    defaultModel: '',
    enabled: true,
    timeoutMs: 120000,
    temperature: 0.2,
    maxTokens: 2048,
    remark: ''
  });
  speedLevel.value = 'low';
  modelOptions.value = [];
  formRef.value?.clearValidate();
}

function openCreate() {
  editingId.value = '';
  resetForm();
  drawerVisible.value = true;
}

function openEdit(item: ModelProviderItem) {
  editingId.value = item.id;
  const defaultModel = item.defaultModel || item.model || '';
  Object.assign(form, {
    name: item.name,
    providerCode: item.providerCode || '',
    baseUrl: item.baseUrl,
    apiKey: '',
    defaultModel,
    enabled: item.enabled,
    timeoutMs: item.timeoutMs || 120000,
    temperature: Number(item.temperature ?? 0.2),
    maxTokens: item.maxTokens || 2048,
    remark: item.remark || ''
  });
  speedLevel.value = temperatureToSpeed(form.temperature);
  modelOptions.value = defaultModel ? [defaultModel] : [];
  formRef.value?.clearValidate();
  drawerVisible.value = true;
}

async function loadModelCatalog() {
  if (!form.baseUrl) {
    ElMessage.warning('请先填写 baseURL');
    return;
  }
  if (!editingId.value && !form.apiKey) {
    ElMessage.warning('请先填写 API Key');
    return;
  }
  catalogLoading.value = true;
  try {
    const data = await fetchModelCatalog({
      providerId: editingId.value || undefined,
      baseUrl: form.baseUrl,
      apiKey: form.apiKey || undefined,
      timeoutMs: form.timeoutMs
    });
    modelOptions.value = data.models || [];
    if (!modelOptions.value.length) {
      ElMessage.warning('上游没有返回可用模型，请手动输入模型名');
      return;
    }
    if (!form.defaultModel) {
      form.defaultModel = modelOptions.value[0];
    }
    ElMessage.success(`已获取 ${modelOptions.value.length} 个模型`);
  } finally {
    catalogLoading.value = false;
  }
}

async function saveProvider() {
  await formRef.value?.validate();
  saving.value = true;
  try {
    const payload: ModelProviderPayload = { ...form };
    if (editingId.value && !payload.apiKey) {
      delete payload.apiKey;
    }
    if (editingId.value) {
      await updateModelProvider(editingId.value, payload);
      ElMessage.success('大模型供应商已更新');
    } else {
      await createModelProvider(payload);
      ElMessage.success('大模型供应商已新增');
    }
    drawerVisible.value = false;
    await fetchProviders();
  } finally {
    saving.value = false;
  }
}

async function testProvider(item: ModelProviderItem) {
  testingId.value = item.id;
  try {
    const result = await testModelProvider(item.id, '请回复 pong，用于因材智学系统后台连通测试。');
    ElMessage.success(`连通测试通过：${String(result.content || '模型已响应').slice(0, 60)}`);
  } finally {
    testingId.value = '';
  }
}

async function toggleEnabled(item: ModelProviderItem) {
  await updateModelProvider(item.id, { enabled: !item.enabled });
  ElMessage.success(item.enabled ? '供应商已停用' : '供应商已启用');
  await fetchProviders();
}

async function rotateKey(item: ModelProviderItem) {
  const { value } = await ElMessageBox.prompt('请输入新的 API Key', '轮换密钥', {
    inputType: 'password',
    confirmButtonText: '保存',
    cancelButtonText: '取消',
    inputValidator: (input) => Boolean(input?.trim()) || 'API Key 不能为空'
  });
  await updateModelProvider(item.id, { apiKey: value, defaultModel: item.defaultModel, baseUrl: item.baseUrl, name: item.name, enabled: item.enabled });
  ElMessage.success('API Key 已轮换');
  await fetchProviders();
}

async function removeProvider(item: ModelProviderItem) {
  await ElMessageBox.confirm(`确定删除「${item.name}」吗？`, '删除供应商', { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' });
  await deleteModelProvider(item.id);
  ElMessage.success('供应商已删除');
  await fetchProviders();
}

function temperatureToSpeed(value?: number): SpeedLevel {
  const temperature = Number(value ?? 0.2);
  if (temperature >= 1.6) return 'ultra';
  if (temperature >= 1.0) return 'high';
  if (temperature >= 0.5) return 'medium';
  return 'low';
}

function speedLabel(value?: number) {
  const level = temperatureToSpeed(value);
  return speedOptions.find((item) => item.value === level)?.label || '低';
}

onMounted(fetchProviders);
</script>

<style scoped>
.filter-panel {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.summary-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.summary-item span {
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.summary-item strong {
  color: var(--el-text-color-primary);
  font-size: 22px;
  font-weight: 700;
}

.form-row {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.form-row :deep(.el-input-number) {
  width: 100%;
}

.form-row :deep(.el-segmented) {
  width: 100%;
}

.model-picker {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 10px;
  width: 100%;
}

@media (max-width: 900px) {
  .summary-grid,
  .form-row,
  .model-picker {
    grid-template-columns: 1fr;
  }
}
</style>
