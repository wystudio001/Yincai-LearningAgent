<template>
  <div class="login-page">
    <section class="login-visual">
      <div>
        <div class="brand">
          <div class="brand-mark">因</div>
          <span>因材智学系统</span>
        </div>
      </div>
      <div>
        <h1>教学资源、学习画像与智能体运行的一体化管理后台</h1>
        <p>面向学校、教师和平台运营团队，支撑个性化资源生成、学习路径编排、内容审核和大模型供应商配置。</p>
      </div>
      <span>Yincai Zhixue Admin Console</span>
    </section>
    <section class="login-panel-wrap">
      <el-form ref="formRef" :model="form" :rules="rules" class="login-panel" @submit.prevent="handleLogin">
        <h2>登录管理端</h2>
        <p>使用平台管理员账号进入控制台。</p>
        <el-form-item prop="username">
          <el-input v-model="form.username" size="large" placeholder="账号" :prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" size="large" type="password" placeholder="密码" :prefix-icon="Lock" show-password />
        </el-form-item>
        <el-button type="primary" size="large" style="width: 100%" :loading="loading" @click="handleLogin">登录</el-button>
      </el-form>
    </section>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import type { FormInstance, FormRules } from 'element-plus';
import { Lock, User } from '@element-plus/icons-vue';
import { useRoute, useRouter } from 'vue-router';
import { useUserStore } from '@/stores';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();
const formRef = ref<FormInstance>();
const loading = ref(false);

const form = reactive({ username: '', password: '' });
const rules: FormRules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
};

async function handleLogin() {
  await formRef.value?.validate();
  loading.value = true;
  try {
    await userStore.login(form.username, form.password);
    router.push((route.query.redirect as string) || '/dashboard');
  } finally {
    loading.value = false;
  }
}
</script>
