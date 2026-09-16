import { createApp } from 'vue';
import { createPinia } from 'pinia';
import ElementPlus from 'element-plus';
import 'element-plus/dist/index.css';
import App from './App.vue';
import router from './router';
import './router/guards';
import './assets/styles/index.scss';
import { installDirectives } from './directives';

const app = createApp(App);
app.use(createPinia()).use(router).use(ElementPlus);
installDirectives(app);
app.mount('#app');
