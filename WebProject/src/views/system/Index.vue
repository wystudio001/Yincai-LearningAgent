<template>
  <PageShell title="系统设置" description="维护平台基础参数、安全策略、文件存储和任务调度配置。">
    <template #actions>
      <el-button :icon="Refresh" @click="loadSettings">重载配置</el-button>
      <el-button type="primary" :icon="Check" @click="saveSettings">保存设置</el-button>
    </template>

    <div class="content-split">
      <div class="panel">
        <h3>基础配置</h3>
        <el-form label-width="120px" :model="form">
          <el-form-item label="系统名称"><el-input v-model="form.appName" /></el-form-item>
          <el-form-item label="默认学校"><el-input v-model="form.defaultTenant" /></el-form-item>
          <el-form-item label="上传限制"><el-input v-model="form.uploadLimit" /></el-form-item>
          <el-form-item label="审计保留"><el-input v-model="form.auditRetention" /></el-form-item>
          <el-form-item label="后端地址"><el-input :model-value="apiBaseUrl" disabled /></el-form-item>
        </el-form>
      </div>
      <div class="panel">
        <h3>安全策略</h3>
        <el-form label-width="120px" :model="security">
          <el-form-item label="MFA 登录"><el-switch v-model="security.mfa" /></el-form-item>
          <el-form-item label="IP 白名单"><el-switch v-model="security.ipWhitelist" /></el-form-item>
          <el-form-item label="密钥轮换提醒"><el-switch v-model="security.keyRotation" /></el-form-item>
          <el-form-item label="内容安全复核"><el-switch v-model="security.contentReview" /></el-form-item>
        </el-form>
      </div>
    </div>
  </PageShell>
</template>

<script setup lang="ts">
import { onMounted, reactive } from 'vue';
import { ElMessage } from 'element-plus';
import { Check, Refresh } from '@element-plus/icons-vue';
import PageShell from '@/components/common/PageShell.vue';

const STORAGE_KEY = 'yincai_system_settings';
const apiBaseUrl = import.meta.env.VITE_API_BASE_URL;

const defaults = {
  form: {
    appName: '因材智学系统',
    defaultTenant: '因材智学演示校区',
    uploadLimit: '500MB',
    auditRetention: '180 天'
  },
  security: {
    mfa: true,
    ipWhitelist: false,
    keyRotation: true,
    contentReview: true
  }
};

const form = reactive({ ...defaults.form });
const security = reactive({ ...defaults.security });

function loadSettings() {
  const raw = localStorage.getItem(STORAGE_KEY);
  if (!raw) {
    Object.assign(form, defaults.form);
    Object.assign(security, defaults.security);
    ElMessage.success('已加载默认配置');
    return;
  }
  const parsed = JSON.parse(raw) as typeof defaults;
  Object.assign(form, parsed.form || defaults.form);
  Object.assign(security, parsed.security || defaults.security);
  ElMessage.success('配置已重载');
}

function saveSettings() {
  localStorage.setItem(STORAGE_KEY, JSON.stringify({ form, security }));
  ElMessage.success('系统设置已保存到本地配置');
}

onMounted(loadSettings);
</script>
