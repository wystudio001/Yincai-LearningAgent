<template>
  <el-dialog v-model="visible" :title="dialogTitle" width="620px" destroy-on-close>
    <el-descriptions v-if="mode === 'detail'" :column="1" border>
      <el-descriptions-item v-for="field in fields" :key="field.prop" :label="field.label">
        {{ displayValue(field) }}
      </el-descriptions-item>
    </el-descriptions>

    <el-form v-else ref="formRef" :model="form" label-width="110px">
      <el-form-item v-for="field in editableFields" :key="field.prop" :label="field.label" :prop="field.prop">
        <el-select v-if="field.type === 'select'" v-model="form[field.prop]" clearable filterable style="width: 100%">
          <el-option v-for="option in field.options || []" :key="option.value" :label="option.label" :value="option.value" />
        </el-select>
        <el-input-number v-else-if="field.type === 'number'" v-model="form[field.prop]" style="width: 100%" />
        <el-input v-else-if="field.type === 'textarea'" v-model="form[field.prop]" type="textarea" :rows="4" />
        <el-input v-else v-model="form[field.prop]" clearable />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">关闭</el-button>
      <el-button v-if="mode !== 'detail'" type="primary" :loading="saving" @click="handleSubmit">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue';
import type { FormInstance } from 'element-plus';

export interface DomainFieldOption {
  label: string;
  value: string | number;
}

export interface DomainField {
  label: string;
  prop: string;
  type?: 'input' | 'textarea' | 'select' | 'number';
  readonly?: boolean;
  options?: DomainFieldOption[];
}

const props = withDefaults(
  defineProps<{
    modelValue: boolean;
    mode: 'create' | 'edit' | 'detail';
    title: string;
    fields: DomainField[];
    record?: Record<string, unknown> | null;
    saving?: boolean;
  }>(),
  {
    record: null,
    saving: false
  }
);

const emit = defineEmits<{
  'update:modelValue': [value: boolean];
  submit: [value: Record<string, unknown>];
}>();

const formRef = ref<FormInstance>();
const form = reactive<Record<string, unknown>>({});

const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
});

const dialogTitle = computed(() => {
  const action = props.mode === 'create' ? '新增' : props.mode === 'edit' ? '编辑' : '详情';
  return `${action}${props.title}`;
});

const editableFields = computed(() => props.fields.filter((field) => !field.readonly));

watch(
  () => [props.modelValue, props.record, props.mode] as const,
  () => {
    if (!props.modelValue) {
      return;
    }
    for (const key of Object.keys(form)) {
      delete form[key];
    }
    for (const field of props.fields) {
      form[field.prop] = props.mode === 'create' ? '' : props.record?.[field.prop] ?? '';
    }
  },
  { immediate: true }
);

function displayValue(field: DomainField) {
  const value = props.record?.[field.prop];
  if (field.type === 'select') {
    return field.options?.find((option) => option.value === value)?.label || value || '-';
  }
  return value === undefined || value === null || value === '' ? '-' : String(value);
}

async function handleSubmit() {
  await formRef.value?.validate();
  emit('submit', { ...form });
}
</script>
