<template>
    <!-- 整个页面，设置背景图片 -->
    <div class="page">
        <!-- 登录面板 -->
        <div class="panel">
            <!-- 左边一个小div -->
            <div class="left">
                <!-- 上面一个图片 -->
                <img src="../../assets/login/logo.png" class="logo">
                <!-- 下面一个图片 -->
                <img src="../../assets/login/big.png" class="big">
            </div>
            <!-- 右边一个小div -->
            <div class="right">
                <!-- 显示标题字和版本号的div -->
                <div class="title-container">
                    <h2>美年大健康体检系统</h2>
                    <span>V1.0</span>
                </div>
                <!-- 登录表单控件div -->
                <div>
                    <div class="row">
                        <el-input v-model="loginInfo.username" placeholder="用户名" prefix-icon="user" size="large" clearable></el-input>
                    </div>
                    <div class="row">
                        <el-input type="password" v-model="loginInfo.password" placeholder="密码" prefix-icon="Lock" size="large" clearable></el-input>
                    </div>
                    <div class="row">
                        <el-button type="primary" class="btn" size="large" @click="login">
                            登录系统
                        </el-button>
                    </div>
                </div>
                
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
    import { getCurrentInstance, reactive } from 'vue';
    import { isUsername, isPassword } from '../../utils/validate';
    import axios from 'axios';

    const { proxy } = getCurrentInstance();
    import router from '../../router';

    // 登录数据
    const loginInfo = reactive({
        username: "",
        password: ""
    });

    async function login(){
        if(!isUsername(loginInfo.username)){
            proxy.$message({
                message: "用户名格式错误",
                 type: 'error',
                 duration: 1200
            });
        } else if(!isPassword(loginInfo.password)){// 验证密码是否合法
            proxy.$message({
                // 提示信息
                message: "密码格式错误",
                // 错误信息，以红色显示
                type: 'error',
                // 持续1200毫秒
                duration: 1200
            });
        }else{
            const loginData = {
                username: loginInfo.username,
                password: loginInfo.password
            };
            const config = {
                headers: {
                    'Content-Type': 'application/json'
                }
            }

            const response = await axios.post('/meinian-api/mis/user/login', loginData, config);

            const data = response.data;
            if(data.result){
                localStorage.setItem('token', data.token);
                localStorage.setItem('permissions', JSON.stringify(data.permissions));
                router.push({name: 'MisHome'});
            }else{
                proxy.$message({
                    message: "登录失败",
                    type: 'error',
                    duration: 1200
                });
            }
        }
    }

</script>

<style lang="less" scoped>
@import url('login.less');
</style>