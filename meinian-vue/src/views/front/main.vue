<template>
    <div class="container">
        <header>
            <div class="logo-container" @click="router.push({name: 'FrontIndex'})">
                <img src="../../assets/front/index/logo.png" class="logo">
                <div>
                    <h3>美年大健康</h3>
                    <p>www.mnhealth.com</p>
                </div>
            </div>
            
            <div class="search-container">
                <div>
                    <el-input v-model="header.keyword" size="large" maxlength="50" placeholder="请输入套餐名或编号" class="keyword-input" clearable>
                    <template #append>
                        <el-button @click="searchGoods">搜索</el-button>
                    </template>
                    </el-input>
                </div>
                <div>
                    <!-- 
                        type属性用来指定tag的类型，可填写：primary、success、info等。
                        key属性是标签的唯一标识
                        effect设置主题是高亮
                        round是圆角
                        class="tag" 将来可以设置样式。比如外补丁。
                    -->
                    <el-tag v-for="item in header.tags"
                        :type="item.type" :key="item.label"
                        effect="light" class="tag" round
                        @click="tagHandle(item.label)">
                        {{ item.label }}
                    </el-tag>
                </div>
            </div>
            <div class="oper-container">
                <el-button type="primary" size="large" v-if="dialog.status == 'logout'" @click="showDialog">
                    登录/注册
                </el-button>
                <div class="btn" v-if="dialog.status == 'login'" @click="router.push({name:'FrontMine'})">
                    <el-icon><User /></el-icon>
                    <span>个人中心</span>
                </div>
                <div class="btn" v-if="dialog.status == 'login'" @click="logout">
                    <el-icon><SwitchButton /></el-icon>
                    <span>退出系统</span>
                </div>
            </div>
        </header>

        <!-- 二级路由的出口 -->
        <!-- router是路由器 -->
        <!-- router.currentRoute 获取的是当前路由器对象的响应式对象Ref -->
        <!-- router.currentRoute.value 获取的是当前真正的路由对象 -->
        <!-- router.currentRoute.value.fullPath 获取当前路由对应的全路径。 -->
        <!-- :key 的值决定了当前路由出口对应的组件是否重新创建，如果key不同，则一定会新建该路由组件。这样路由组件的生命周期钩子都会执行。 -->
        <!-- 当用户的请求路径是：/user/100，那么router.currentRoute.value.fullPath结果就是 /user/100 -->
        <!-- 当用户的请求路径是：/user/110，那么router.currentRoute.value.fullPath结果就是 /user/110 -->
        <RouterView :key="router.currentRoute.value.fullPath"/>  

    </div>
          
    <!-- 页脚 -->
    <footer>
        <!-- 灰色区域 -->
        <div class="aboutus-container">
            <ul class="list">
                <li class="item">
                    <img src="../../assets/front/index/d1.svg" alt="">
                    <div>
                        <h3>全国连锁</h3>
                        <p>覆盖全国主要城市的专业体检网络，随时随地享受便捷服务</p>
                    </div>
                </li>
                <li class="item">
                    <img src="../../assets/front/index/d2.svg" alt="">
                    <div>
                        <h3>智能管理</h3>
                        <p>数字化健康档案系统，为您提供个性化的健康管理方案</p>
                    </div>
                </li>
                <li class="item">
                    <img src="../../assets/front/index/d3.svg" alt="">
                    <div>
                        <h3>权威专家</h3>
                        <p>由三甲医院资深医师组成的专业团队，确保检查结果准确可靠</p>
                    </div>
                </li>
                <li class="item">
                    <img src="../../assets/front/index/d4.svg" alt="">
                    <div>
                        <h3>贴心服务</h3>
                        <p>一对一健康顾问，报告专业解读，后续健康指导全程陪伴</p>
                    </div>
                </li>
            </ul>
        </div>

        <!-- 蓝色区域：联系我们 -->
        <div class="contactus-container">
            <ul class="list">
                <!-- 美年大健康和电话 -->
                <li class="left">
                    <!-- 美年大健康图片 -->
                    <img src="../../assets/front/index/service.png">
                    <!-- 电话 -->
                    <p class="tel">400-6666-8888</p>
                </li>
                <!-- 了解美年 -->
                <li>
                    <ul class="link-list">
                        <li><a href="">了解美年</a></li>
                        <li><a href="">公司概况</a></li>
                        <li><a href="">成长轨迹</a></li>
                        <li><a href="">健康生态体系</a></li>
                        <li><a href="">价值理念</a></li>
                    </ul>
                </li>
                <!-- 健康资讯 -->
                <li>
                    <ul class="link-list">
                        <li><a href="">健康资讯</a></li>
                        <li><a href="">最新动态</a></li>
                        <li><a href="">媒体聚焦</a></li>
                        <li><a href="">党建风采</a></li>
                    </ul>
                </li>
                <!-- 服务支持 -->
                <li>
                    <ul class="link-list">
                        <li><a href="">服务支持</a></li>
                        <li><a href="">常见问题</a></li>
                        <li><a href="">在线预约</a></li>
                        <li><a href="">报告查询</a></li>
                        <li><a href="">服务条款</a></li>
                    </ul>
                </li>
                <!-- 合作共赢 -->
                <li>
                    <ul class="link-list">
                        <li><a href="">合作共赢</a></li>
                        <li><a href="">合作伙伴</a></li>
                        <li><a href="">加入我们</a></li>
                    </ul>
                </li>
                <!-- 公众号二维码和下载体检app的二维码 -->
                <li class="right">
                    <div class="ercode">
                        <p>微信公众号</p>
                        <img src="../../assets/front/index/ercode-1.png">
                    </div>
                    <div class="ercode">
                        <p>下载体检APP</p>
                        <img src="../../assets/front/index/ercode-2.png">
                    </div>
                </li>
            </ul>
        </div>
    </footer>


    <el-dialog v-model="dialog.visible" title="手机快速登录" width="400" class="dialog">
        <el-row>
            <el-col :span="24">
                <el-input v-model="dialog.phone" placeholder="输入手机号，快捷登录"
                    size="large" maxlength="11" clearable>
                    <template #prepend>
                        <el-icon><Iphone /></el-icon>
                    </template>
                </el-input>
            </el-col>
        </el-row>
        <el-row :gutter="10">
            <el-col :span="16">
                <el-input v-model="dialog.code" placeholder="输入短信验证码"
                    size="large" maxlength="6" clearable>
                    <template #prepend>
                        <el-icon><Message /></el-icon>
                    </template>
                </el-input>
            </el-col>
            <el-col :span="8">
                <el-button size="large" class="receive-btn" type="primary" plain
                    @click="sendSmsCode" :disabled="dialog.disabled">
                    {{ dialog.btnContent }}
                </el-button>
            </el-col>
        </el-row>
        <el-button type="primary" class="login-btn" size="large" @click="login">
            登录系统
        </el-button>
    </el-dialog>

    
</template>

<script lang="ts" setup>
    import router from '../../router';
    import { reactive, getCurrentInstance } from 'vue';

    // 获取当前组件实例，组件实例中有proxy属性，解构拿到proxy
    // 以后通过proxy访问全局属性/全局变量
    const { proxy } = getCurrentInstance();

    // 定义响应式对象
    const header = reactive({
        // 对应头部区域的文本框
        keyword: '',
        // 文本框下面的tag标签，以下tag标签的内容
        tags: [
            {
                label: '青少年体检',
                type: 'info'
            },
            {
                label: '婚前体检',
                type: 'info'
            },
            {
                label: '精英体检',
                type: 'info'
            }
        ]
    });

    import { isPhone, stringIsEmpty, isSmsCode } from '../../utils/validate';
    import axios from 'axios';
    import { ElMessage } from 'element-plus';
    
    const dialog = reactive({
        visible: false,
        phone: null,
        code: null,
        disabled: false,
        btnContent: '获取短信验证码',
        num: 0,
        status: 'logout'
    });

    const dataRule = reactive({
        phone: [{ required: true, pattern: '^1[1-9]\d{9}$', message: '手机号码错误' }]
    });

    function showDialog() {
        dialog.visible = true;
    }

    // 发送短信验证码
    async function sendSmsCode(){
        // 拿到用户填写的手机号
        const phone = dialog.phone;
        // 判断手机号是否为空
        if(stringIsEmpty(phone)){
            proxy.$message({
                message: '请填写手机号',
                type: 'error',
                duration: 1200
            });
        }else if(!isPhone(phone)){
            proxy.$message({
                message: '手机号格式错误',
                type: 'error',
                duration: 1200
            });
        }else{
            // 发送ajax post请求，去发送短信验证码
            // 准备数据
            const json = {
                phone: phone
            };
            // 不需要提交token，发送ajax post请求
            const response = await axios.post('/meinian-api/front/customer/sendSmsCode', json);
            const result = response.data.result;
            if(result){
                // 提示用户用户短信已发送
                proxy.$message({
                    message: '短信已发送',
                    type: 'success',
                    duration: 1200
                });
                // 获取短信验证码的按钮进入倒计时
                dialog.disabled = true;
                const interval = setInterval(()=>{
                    dialog.num++;
                    dialog.btnContent = `${60 - dialog.num}秒后重新获取`;
                    if(dialog.num == 60){
                        dialog.num = 0;
                        dialog.disabled = false;
                        dialog.btnContent = "获取短信验证码";
                        clearInterval(interval);
                    }
                }, 1000);
            }
        }
    }

    async function login(){
        const phone = dialog.phone;
        const code = dialog.code;
        
        if(stringIsEmpty(phone)){ // 验证手机号是否合法
            proxy.$message({
                message: "填写手机号",
                type: "error",
                duration: 1200
            });
        }else if(!isPhone(phone)){
            proxy.$message({
                message: "手机号格式错误",
                type: "error",
                duration: 1200
            });
        }else if(stringIsEmpty(code)){ // 检查验证码是否合法
            proxy.$message({
                message: "填写验证码",
                type: "error",
                duration: 1200
            });
        }else if(!isSmsCode(code)){
            proxy.$message({
                message: "验证码格式错误",
                type: "error",
                duration: 1200
            });
        }else{
            // 数据都合法，则提交ajax post请求，提交手机号和验证码。
            // 准备数据
            const json = {
                phone: phone,
                code: code
            };

            const response = await axios.post('/meinian-api/front/customer/login', json);
            const resp = response.data;
            const result = resp.result;
            if(result){
                dialog.phone = null;
                dialog.code = null;
                dialog.visible = false;
                dialog.status = 'login';
                localStorage.setItem('token', resp.token);
            }else{
                proxy.$message({
                    message: resp.msg,
                    type: "error",
                    duration: 1200
                });
            }
        }
        
    }

    async function checkLogin(){
        const token = localStorage.getItem('token');
        if(token != null){
            const config = {
                headers : {
                    satoken: token
                }
            }

            const response = await axios.get('/meinian-api/front/customer/isLogin', config);
            if(response.data.result){
                dialog.status = 'login';
            }else{
                dialog.status = 'logout';
            }
        }
    }

    checkLogin();

    async function logout(){
        try{
            const config = {
                headers: {
                    satoken: localStorage.getItem('token'),
                }
            };

            const response = await axios.get('/meinian-api/front/customer/logout', config);
            if(response.data.result){
                localStorage.removeItem('token');
                dialog.status = 'logout';
                router.push({name: 'FrontIndex'});
                ElMessage({
                    message: '已退出系统',
                    type: 'success',
                    duration: 1200
                });
            }
            
        }catch(e){
            console.log(e);
            ElMessage({
                type: 'error',
                message: '退出失败',
                duration: 1200
            });
        }
    }

    //防止刷新页面后搜索框里的内容消失
    //使用页面地址栏上的查询参数
    header.keyword = router.currentRoute.value.query.keyword;

    function searchGoods(){
        router.push({
            name: 'FrontGoodsList',
            //查询参数传给子页面
            query: {
                //搜索框里的数据
                keyword: header.keyword,
                //提交时间戳，避免走缓存
                random: new Date().getTime()
            }
        });
    }

    function tagHandle(tagLabel: string){
        header.keyword = tagLabel;
    }

</script>

<style lang="less" scoped>
    @import url('main.less');
</style>