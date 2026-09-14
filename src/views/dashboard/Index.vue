<template>
  <PageShell title="工作台" description="全局教学运行、资源生成和智能体服务状态。">
    <template #actions>
      <el-button :icon="Refresh" :loading="loading" @click="fetchData">刷新数据</el-button>
    </template>

    <div class="metrics-grid">
      <MetricCard label="课程数量" :value="metrics.courseTotal" trend="来自课程服务" />
      <MetricCard label="用户数量" :value="metrics.userTotal" trend="来自用户服务" />
      <MetricCard label="资源数量" :value="metrics.resourceTotal" trend="来自资源服务" />
      <MetricCard label="测评数量" :value="metrics.examTotal" trend="来自测评服务" />
    </div>

    <div class="content-split">
      <div class="panel">
        <h3>最新课程</h3>
        <el-table v-loading="loading" :data="courseRows" row-key="id" empty-text="暂无课程数据">
          <el-table-column prop="name" label="课程" />
          <el-table-column prop="status" label="状态" width="120" />
          <el-table-column prop="updatedAt" label="更新时间" width="190" />
        </el-table>
      </div>
      <div class="panel">
        <h3>服务连通</h3>
        <el-timeline>
          <el-timeline-item type="success">认证服务已接入真实登录接口</el-timeline-item>
          <el-timeline-item type="primary">课程、用户、资源、测评服务使用统一前置地址</el-timeline-item>
          <el-timeline-item type="warning">AI 供应商配置等待后端管理接口补齐</el-timeline-item>
        </el-timeline>
      </div>
    </div>
  </PageShell>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { Refresh } from '@element-plus/icons-vue';
import PageShell from '@/components/common/PageShell.vue';
import MetricCard from '@/components/common/MetricCard.vue';
import { listCourses } from '@/api/course';
import { listUsers } from '@/api/user';
import { listResources } from '@/api/resource';
import { listExercises } from '@/api/exam';
import { displayDate, displayStatus, getPageRecords, getPageTotal } from '@/utils/page';

const loading = ref(false);
const courseRows = ref<{ id: string | number; name: string; status: string; updatedAt: string }[]>([]);
const metrics = reactive({ courseTotal: 0, userTotal: 0, resourceTotal: 0, examTotal: 0 });

async function fetchData() {
  loading.value = true;
  try {
    const [courses, users, resources, exams] = await Promise.all([
      listCourses({ page: 1, size: 5 }),
      listUsers({ page: 1, size: 1 }),
      listResources({ page: 1, size: 1 }),
      listExercises({ pageNo: 1, pageSize: 1 })
    ]);
    metrics.courseTotal = getPageTotal(courses);
    metrics.userTotal = getPageTotal(users);
    metrics.resourceTotal = getPageTotal(resources);
    metrics.examTotal = getPageTotal(exams);
    courseRows.value = getPageRecords(courses).map((item) => ({
      id: item.id,
      name: item.name,
      status: displayStatus(item.status),
      updatedAt: displayDate(item.updatedAt || item.createdAt)
    }));
  } finally {
    loading.value = false;
  }
}

onMounted(fetchData);
</script>
