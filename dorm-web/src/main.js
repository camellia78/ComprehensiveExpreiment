import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.use(ElementPlus)
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.directive('permission', {
  mounted(el, binding) {
    const role = parseInt(localStorage.getItem('role') || '-1')
    const allowed = Array.isArray(binding.value) ? binding.value : [binding.value]
    if (!allowed.includes(role)) {
      el.style.display = 'none'
    }
  }
})

app.mount('#app')
