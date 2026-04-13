// index.js
import axios from 'axios';

const api_system = axios.create({
    baseURL: 'http://localhost:9001/login/system', // 你的API基础URL
    timeout: 5000, // 请求超时时间
    headers: {'X-Custom-Header': 'foobar'} // 自定义请求头
});

// 添加请求拦截器
api_system.interceptors.request.use(config => {
    // 在发送请求之前做些什么，比如添加认证token
    const token = localStorage.getItem('token');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
}, error => {
    // 对请求错误做些什么
    return Promise.reject(error);
});

// 添加响应拦截器
api_system.interceptors.response.use(response => {
    // 对响应数据做点什么
    return response.data;
}, error => {
    // 对响应错误做点什么，比如错误处理
    return Promise.reject(error);
});

export default api_system;

