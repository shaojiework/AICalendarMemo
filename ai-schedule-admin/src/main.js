import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
// Element Plus 官方暗色主题（配合 html.dark 使用，对话框/分页/下拉等组件自动暗色）
import 'element-plus/theme-chalk/dark/css-vars.css'
// Element Plus 中文语言包
import zhCn from 'element-plus/es/locale/lang/zh-cn'
// Element Plus 图标全局注册
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

import App from './App.vue'
import router from './router'
import './styles/index.scss'

const app = createApp(App)

// 全局注册所有 Element Plus 图标组件（菜单 meta.icon 通过组件名动态渲染）
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(createPinia())
app.use(router)
app.use(ElementPlus, { locale: zhCn })

app.mount('#app')
