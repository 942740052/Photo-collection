import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import Login from './components/Login.vue'

const login = createApp(Login)
login.use(ElementPlus)
login.mount('#login')
