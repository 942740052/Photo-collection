<template>
  <body id="paper">
  <el-form :rules="rules" class="login-container" label-position="left"
           label-width="0px" v-loading="loading">
    <h3 class="login_title">图书管理系统登录</h3>
    <el-form-item prop="username">
      <el-input type="text" v-model="loginForm.username"
                auto-complete="off" placeholder="账号"></el-input>
    </el-form-item>
    <el-form-item prop="password">
      <el-input type="password" v-model="loginForm.password"
                auto-complete="off" placeholder="密码"></el-input>
    </el-form-item>
    <el-checkbox class="login_remember" v-model="checked"
                 label-position="left"><span style="color: #505458">记住密码</span></el-checkbox>
    <el-form-item style="width: 100%">
      <el-button type="primary" style="width: 47%;background: #505458;border: none" @click="login()">登录</el-button>
      <el-button type="primary" style="width: 47%;background: #505458;border: none">注册</el-button>
    </el-form-item>
  </el-form>
  </body>
</template>

<script>
import api_system from "../router";
import {ElMessage} from "element-plus";

export default {
  name: 'LoginPage',
  data() {
    return {
      checked: '',
      loginForm: {
        username: '',
        password: '',
      },
      rules: {}, // 定义rules，具体规则根据需求设定
      loading: false, // 定义loading，默认设为false
    };
  }, methods: {
    async login() {
      if (!this.loginForm.username || !this.loginForm.password) {
        ElMessage.warning('请输入账号和密码');
        return;
      }
      this.loading = true;
      
      try {
        const response = await api_system.post('/login', {
          username: this.loginForm.username,
          password: this.loginForm.password
        });
        
        if (response === 'success') {
          ElMessage({
            message: '登录成功',
            type: 'success',
            duration: 2000
          });
        } else {
          ElMessage.error('登录失败：' + response);
        }
      } catch (error) {
        if (error.code === 'ERR_NETWORK' || error.message.includes('Network Error')) {
          if (this.loginForm.username === 'admin' && this.loginForm.password === '123456') {
            ElMessage({
              message: '登录成功（模拟模式）',
              type: 'success',
              duration: 2000
            });
          } else {
            ElMessage.error('登录失败：账号 admin，密码 123456');
          }
        } else {
          ElMessage.error('请求错误：' + error.message);
        }
      } finally {
        this.loading = false;
      }
    }
  }
}
</script>

<style scoped>
#paper {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  background-position: center;
  height: 100%;
  width: 100%;
  background-size: cover;
  position: fixed;
}
body{
  margin: 0;
}
.login-container {
  border-radius: 15px;
  background-clip: padding-box;
  margin: 90px auto;
  width: 350px;
  padding: 35px 35px 15px 35px;
  background: #fff;
  border: 1px solid #eaeaea;
  box-shadow: 0 0 25px #cac6c6;
}
.login_title {
  margin: 0px auto 40px auto;
  text-align: center;
  color: #505458;
}
.login_remember {
  margin: 0px 0px 35px 0px;
  text-align: left;
}
</style>
