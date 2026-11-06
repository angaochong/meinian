<template>
    <!-- 默认使用的样式是site-wrapper,当sidebar.sidebarFold值是true时，再叠加一个site-sidebar--fold样式 -->
    <!-- site-sidebar--fold这个样式专门负责折叠的左边栏的。 -->
    <!-- v-loading 是 Element Plus 提供的自定义指令，用于在元素上显示加载状态。 -->
    <!-- .fullscreen：修饰符，表示全屏加载。 -->
    <!-- .lock：修饰符，表示锁定屏幕（防止用户在加载时进行其他操作）。 -->
    <!-- "loading"：对应模型层的loading变量，当 loading 为 true 时显示加载动画，为 false 时隐藏。 -->
    <!-- element-loading-text：是 v-loading 指令的配套属性，用于设置加载时显示的文字。 -->
    <div class="site-wrapper" :class="{'site-sidebar--fold': sidebar.sidebarFold}"
        v-loading.fullscreen.lock="loading" element-loading-text="加载中">
        <!-- 导航栏：site-navbar样式决定了导航栏的背景色是蓝色，可以看源码确认下。 -->
        <nav class="site-navbar" :class="{'site-navbar--fold': sidebar.sidebarFold}">
            <!-- 这部分是显示标题的 -->
            <div class="site-navbar__header">
                <h1 class="site-navbar__brand">
                    <!-- 到底显示哪个是需要编写TS代码来实现的。 -->
                    <!-- 展开时显示它 -->
                    <a class="site-navbar__brand-lg">美年大健康体检系统</a>
                    <!-- 折叠时显示它 -->
                    <a class="site-navbar__brand-mini">体检</a>
                </h1>
            </div>

            <!-- 这一部分是折叠图标和用户信息，这个样式navbar-container决定了背景色是白色，可以通过源码看到背景色是白色，它把导航栏的蓝色遮蔽了。 -->
            <div class="navbar-container" :class="{'navbar-container--fold': sidebar.sidebarFold}">
                <!-- 折叠图标 -->
                <div class="switch" @click="handleSwitch">
                    <SvgIcon name="zhedie" class="icon-svg"/>
                </div>
                <!-- 用户信息是一个下拉菜单 -->
                <div class="right-container">
                    <el-dropdown>
                        <span class="el-dropdown-link">
                            <!-- 头像外边套一个span的目的是为了挪动头像的位置。为啥不能是div呢？因为它的父控件是span。父控件是span的，子控件不能使用div。 -->
                            <span class="avatar-container">
                                <!-- 头像：circle将头像修饰为圆形，user.photo不为空时显示它，如果为空时使用EP库中的UserFilled图标代替 -->
                                <el-avatar shape="circle" :size="25" :src="user.photo" :icon="UserFilled"></el-avatar>
                            </span>
                            <!-- 名字 -->
                            {{ user.name }}
                        </span>
                        <!-- 下拉菜单 -->
                        <template #dropdown>
                            <el-dropdown-menu>
                                <el-dropdown-item @click="updatePassword">修改密码</el-dropdown-item>
                                <el-dropdown-item @click="logout">退出</el-dropdown-item>
                            </el-dropdown-menu>
                        </template>
                    </el-dropdown>
                </div>
            </div>
        </nav>

        <!-- 左边栏：菜单栏 -->
        <!--样式是公共样式，主题色使用深色，这是边栏-->
        <aside class="site-sidebar site-sidebar--dark">
            <!--边栏中放一个div-->
            <div class="site-sidebar__inner">
                <!--div中放的是菜单组件-->
                <!--菜单项文字颜色：text-color="#8a979e" 灰黑色-->
                <!--菜单项被选中的话，文字颜色变为白色：active-text-color="#fff"-->
                <!--它来控制展开或折叠   :collapse="sidebar.sidebarFold" -->
                <!--展开或折叠时是否需要动画，它来决定：:collapseTransition="false" false表示不用动画-->
                <!--它决定了默认选择的菜单项  :default-active="siteContent.menuActiveName || 'Home'" -->
                <!-- background-color="#263238" 来规定一级菜单鼠标悬停时的背景色。-->
                <el-menu :default-active="siteContent.menuActiveName || 'Home'"
                    :collapse="sidebar.sidebarFold" :collapseTransition="false"
                    class="site-sidebar__menu" background-color="#263238"
                    active-text-color="#fff" text-color="#8a979e">
                    <!--没有二级菜单项就不需要定义template-->
                    <!-- $router.push({ name: 'MisHome' })的作用：当点击这个菜单项时，会跳转到名为 'MisHome' 的路由页面 -->
                    <el-menu-item index="Home" @click="$router.push({ name: 'MisHome' })">
                        <el-icon>
                            <SvgIcon name="home" class="icon-svg" />
                        </el-icon>
                        <span slot="title">首页</span>
                    </el-menu-item>
                    <el-sub-menu index="组织管理"
                        :popper-class="'site-sidebar--' + sidebar.sidebarLayoutSkin + '-popper'">
                        <template #title>
                            <el-icon>
                                <SvgIcon name="users_fill" class="icon-svg" />
                            </el-icon>
                            <span slot="title">组织管理</span>
                        </template>
                        <el-menu-item index="MisDept"
                            v-if="proxy.isAuth(['ROOT', 'DEPT:SELECT'])"
                            @click="$router.push({ name: 'MisDept' })"> 
                            <el-icon>
                                <SvgIcon name="company_fill" class="icon-svg" />
                            </el-icon>
                            <span slot="title">部门管理</span>
                        </el-menu-item>
                        <el-menu-item index="MisRole"
                            v-if="proxy.isAuth(['ROOT', 'ROLE:SELECT'])"
                            @click="$router.push({ name: 'MisRole' })">
                            <el-icon>
                                <SvgIcon name="role_fill" class="icon-svg" />
                            </el-icon>
                            <span slot="title">角色管理</span>
                        </el-menu-item>
                        <el-menu-item index="MisUser"
                            v-if="proxy.isAuth(['ROOT', 'USER:SELECT'])"
                            @click="$router.push({ name: 'MisUser' })">
                            <el-icon>
                                <SvgIcon name="user_fill" class="icon-svg" />
                            </el-icon>
                            <span slot="title">用户管理</span>
                        </el-menu-item>
                    </el-sub-menu>
                    <el-sub-menu index="业务管理"
                        :popper-class="'site-sidebar--' + sidebar.sidebarLayoutSkin + '-popper'">
                        <template #title>
                            <el-icon>
                                <SvgIcon name="trust_fill" class="icon-svg" />
                            </el-icon>
                            <span slot="title">业务管理</span>
                        </template>
                        <el-menu-item index="MisGoods"
                            v-if="proxy.isAuth(['ROOT', 'GOODS:SELECT'])"
                            @click="$router.push({ name: 'MisGoods' })">
                            <el-icon>
                                <SvgIcon name="goods_fill" class="icon-svg" />
                            </el-icon>
                            <span slot="title">体检套餐</span>
                        </el-menu-item>
                        <el-menu-item index="MisRule"
                            v-if="proxy.isAuth(['ROOT', 'RULE:SELECT'])"
                            @click="$router.push({ name: 'MisRule' })">
                            <el-icon>
                                <SvgIcon name="rule_fill" class="icon-svg" />
                            </el-icon>
                            <span slot="title">促销规则</span>
                        </el-menu-item>
                        <el-menu-item index="MisCustomer"
                            v-if="proxy.isAuth(['ROOT', 'CUSTOMER:SELECT'])"
                            @click="$router.push({ name: 'MisCustomer' })">
                            <el-icon>
                                <SvgIcon name="customer_fill"
                                    class="icon-svg" />
                            </el-icon>
                            <span slot="title">客户档案</span>
                        </el-menu-item>
                        <el-menu-item index="MisOrder"
                            v-if="proxy.isAuth(['ROOT', 'ORDER:SELECT'])"
                            @click="$router.push({ name: 'MisOrder' })">
                            <el-icon>
                                <SvgIcon name="order_fill" class="icon-svg" />
                            </el-icon>
                            <span slot="title">订单管理</span>
                        </el-menu-item>
                        <el-menu-item index="MisCustomerIm"
                            v-if="proxy.isAuth(['ROOT', 'CUSTOMER_IM:SELECT'])"
                            @click="$router.push({ name: 'MisCustomerIm' })">
                            <el-icon>
                                <SvgIcon name="im_fill" class="icon-svg" />
                            </el-icon>
                            <span slot="title">客服IM</span>
                        </el-menu-item>
                    </el-sub-menu>
                    <el-sub-menu index="体检管理"
                        :popper-class="'site-sidebar--' + sidebar.sidebarLayoutSkin + '-popper'">
                        <template #title>
                            <el-icon>
                                <SvgIcon name="night_fill" class="icon-svg" />
                            </el-icon>
                            <span slot="title">体检管理</span>
                        </template>
                        <el-menu-item index="MisAppointment"
                            v-if="proxy.isAuth(['ROOT', 'APPOINTMENT:SELECT'])"
                            @click="$router.push({ name: 'MisAppointment' })">
                            <el-icon>
                                <SvgIcon name="appointment_fill"
                                    class="icon-svg" />
                            </el-icon>
                            <span slot="title">体检预约</span>
                        </el-menu-item>
                        <el-menu-item index="MisCustomerCheckin"
                            v-if="proxy.isAuth(['ROOT', 'CUSTOMER_CHICKIN:SELECT'])"
                            @click="$router.push({ name: 'MisCustomerCheckin' })">
                            <el-icon>
                                <SvgIcon name="checkin_fill" class="icon-svg" />
                            </el-icon>
                            <span slot="title">体检签到</span>
                        </el-menu-item>
                        <el-menu-item index="MisAppointmentRestriction"
                            v-if="proxy.isAuth(['ROOT', 'APPOINTMENT_RESTRICTION:SELECT'])"
                            @click="$router.push({ name: 'MisAppointmentRestriction' })">
                            <el-icon>
                                <SvgIcon name="setting_fill" class="icon-svg" />
                            </el-icon>
                            <span slot="title">预约设置</span>
                        </el-menu-item>
                        <el-menu-item index="MisCheckup"
                            v-if="proxy.isAuth(['ROOT', 'CHECKUP:SELECT'])"
                            @click="$router.push({ name: 'MisCheckup' })">
                            <el-icon>
                                <SvgIcon name="doctor_fill" class="icon-svg" />
                            </el-icon>
                            <span slot="title">医生检查</span>
                        </el-menu-item>
                        <el-menu-item index="MisCheckupReport"
                            v-if="proxy.isAuth(['ROOT', 'CHECKUP_REPORT:SELECT'])"
                            @click="$router.push({ name: 'MisCheckupReport' })">
                            <el-icon>
                                <SvgIcon name="file_fill" class="icon-svg" />
                            </el-icon>
                            <span slot="title">体检报告</span>
                        </el-menu-item>
                    </el-sub-menu>
                    <el-sub-menu index="系统设置"
                        :popper-class="'site-sidebar--' + sidebar.sidebarLayoutSkin + '-popper'"
                        v-if="proxy.isAuth(['ROOT', 'SYSTEM:SELECT'])"
                        >
                        <template #title>
                            <el-icon>
                                <SvgIcon name="system_fill" class="icon-svg" />
                            </el-icon>
                            <span slot="title">系统设置</span>
                        </template>
                        <el-menu-item index="MisFlowRegulation"
                            v-if="proxy.isAuth(['ROOT', 'FLOW_REGULATION:SELECT'])"
                            @click="$router.push({ name: 'MisFlowRegulation' })">
                            <el-icon>
                                <SvgIcon name="people_fill" class="icon-svg" />
                            </el-icon>
                            <span slot="title">人员限流</span>
                        </el-menu-item>
                    </el-sub-menu>
                </el-menu>
            </div>
        </aside>
        
        <!-- 内容区 -->
        <div class="site-content__wrapper">
            <!-- 修改位置1：以前写死的true，修改为动态的，是tab页面就用site-content--tabs 样式，不是tab页面就用site-content样式 -->
            <main class="site-content" :class="{ 'site-content--tabs': $route.meta.isTab }">
                <!-- 修改位置2：添加了v-if，当是tab页就显示tab面板 -->
                <el-tabs v-if="$route.meta.isTab"
                    v-model="siteContent.mainTabsActiveName" :closable="true"
                    @tab-click="selectedTabHandle"
                    @tab-remove="removeTabHandle">
                    <el-tab-pane v-for="item in siteContent.mainTabs" :label="item.title" :name="item.name">
                        <el-card :body-style="siteContent.siteContentViewHeight">
                            <router-view :key="router.currentRoute.value.fullPath"></router-view>
                        </el-card>
                    </el-tab-pane>
                </el-tabs>
                <!-- 修改位置3：如果不是tab页，则直接显示卡片。 -->
                <el-card v-else :body-style="siteContent.siteContentViewHeight">
                    <router-view :key="router.currentRoute.value.fullPath"></router-view>
                </el-card>
            </main>
        </div>                
    </div>
    <el-dialog title="提示" v-model="dialog.visible" width="25%">
        <el-form :model="dialog.dataForm" :rules="dialog.dataRule"
            ref="dialogForm" label-width="80px">
            <el-form-item label="原密码" prop="password">
                <el-input type="password" v-model="dialog.dataForm.password"
                    size="medium" maxlength="20" clearable />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
                <el-input type="password" v-model="dialog.dataForm.newPassword"
                    size="medium" maxlength="20" clearable />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
                <el-input type="password"
                    v-model="dialog.dataForm.confirmPassword" size="medium"
                    maxlength="20" clearable />
            </el-form-item>
        </el-form>
        <template #footer>
            <span class="dialog-footer">
                <el-button size="medium"
                    @click="dialog.visible = false">取消</el-button>
                <el-button type="primary" size="medium"
                    @click="dataFormSubmit">确定</el-button>
            </span>
        </template>
    </el-dialog>

</template>

<script lang="ts" setup>
    import SvgIcon from '../../components/SvgIcon.vue';
    import { UserFilled } from '@element-plus/icons-vue';
    import { isURL } from '../../utils/validate';
    import { ref, reactive, provide, getCurrentInstance, onMounted, watch, nextTick, type Ref } from 'vue';
    import { useRoute, useRouter } from 'vue-router';
import axios from 'axios';
import { ElMessage } from 'element-plus';
    //import { Component, Vue, Watch } from 'vue-property-decorator';

    // 用于获取路由参数
    const route = useRoute();
    // 用于切换页面
    const router = useRouter();
    // 获取当前上文对象中的proxy属性，用它来访问全局变量和全局函数
    const { proxy } = getCurrentInstance();

    // 左边栏常量
    const sidebar = reactive({
        // 左边栏状态：true折叠，false展开
        sidebarFold: false,
        // 左边栏主题色
        sidebarLayoutSkin: 'dark'
    });

    //内容区相关常量（后续用到）
    const siteContent = reactive({
        documentClientHeight: 0,
        siteContentViewHeight: {},
        height: null,
        mainTabs: [],
        mainTabsActiveName: '',
        menuActiveName: ''
    });

    // 用户信息常量
    const user = reactive({
        // 将来ajax请求动态获取姓名和头像
        // 姓名
        name: '',
        // 头像
        photo: '',
        // 是否显示修改密码的弹窗,false隐藏，true显示
        updatePasswordVisible: false
    });

    // 方法名随意
    // 调用该函数需要传递过来一个route对象，通过该对象可以获取当前路由参数。
    function routeHandle(route) {
        //每次切换页面，重新计算页面高度和内容区高度
        resetDocumentClientHeight();
        loadSiteContentViewHeight();
        // 判断：如果当前路由参数中meta.isTab是true时，才会显示tab页。
        if (route.meta.isTab) {
            // 从mainTabs数组中进行筛选，拿着当前路由参数的name(route.name)和数组中已存在的tab对象的name(item.name)有没有一致的
            // 如果有一致的，表示这个tab页之前已经创建了。
            let tab = siteContent.mainTabs.filter(item => item.name === route.name)[0];
            // 如果tab对象为空，说明数值中还没有这个tab页。
            if (tab == null) {
                // 创建新的tab对象
                tab = {
                    title: route.meta.title,
                    name: route.name
                };
                // 把tab对象存储到数组中，这样v-for立即遍历，会生成新的tab页。
                siteContent.mainTabs.push(tab);
            }
            // 不管是新的tab页，还是已存在的tab页，最终都要选中对应的菜单项和选中对应的tab页。
            // 选中对应的菜单项
            siteContent.menuActiveName = tab.name;
            // 选中对应的tab页
            siteContent.mainTabsActiveName = tab.name;
        }else{
            // 关闭所有打开的tab页
            siteContent.mainTabs = []
            // tab页选中状态置空
            siteContent.mainTabsActiveName = "";
            // 菜单项选中Home
            siteContent.menuActiveName = "Home";                        
        }
    }

    // 用户有可能是在浏览器直接输入URL。或者用户有可能是刷新页面。而不是点击的菜单项。
    // 在这里编写的代码是在初始化阶段执行的，也就是说在setup函数第一次执行时执行的。
    // 专门用来处理用户直接输入URL，或者刷新浏览器。
    routeHandle(route)

    // 这个是用来监听路由变化的，也就是说初始化已经在第一次访问时已经完成了。
    // 后续用户点击某个菜单项的时候，会再次执行 routeHandle(route); 
    watch(
        () => route.path,  
        (newPath, oldPath) => {
            routeHandle(route);
        },
        { immediate: true }
    );

    //计算网页可见区域的高度
    function resetDocumentClientHeight() {
        //获取网页可见区域的高度
        siteContent.documentClientHeight = document.documentElement.clientHeight;
    }

    //计算内容区卡片控件高度
    function loadSiteContentViewHeight() {
        //卡片控件高度 = 网页可见区域高度 - 导航区高度 - 卡片控件上下外填充 - 上下边框
        let height = siteContent.documentClientHeight - 50 - 30 - 2;
        if (route.meta.isTab) {
            //如果引用的Vue页面需要Tab控件，卡片控件高度还要减去40
            height -= 40;
        }
        //保存卡片控件高度
        siteContent.height = height
        //声明CSS样式
        siteContent.siteContentViewHeight = { minHeight: height + 'px' };
    }

    //浏览器尺寸发生变化的回调函数
    window.onresize = () => {
        //更新保存的网页可见区域高度
        siteContent.documentClientHeight = document.documentElement.clientHeight;
        //重新计算内容区的高度
        loadSiteContentViewHeight();
    };

    // 当用户选中某个tab页的时候加载该tab页
    function selectedTabHandle(tab){
        // router.push() 会：
        // 更新浏览器地址栏的 URL
        // 触发 Vue Router 的路由变化检测
        // 执行相关的导航守卫（如果有）
        // 当路由发生变化时，watch 监听器会检测到并调用 routeHandle(route)
        router.push({
            name: tab.paneName
        });
    }   
    
    // 当用户点击关闭tab时执行回调函数
    function removeTabHandle(tabName) {
        //让mainTabs数组剔除要关闭的Tab
        siteContent.mainTabs = siteContent.mainTabs.filter(item => item.name !== tabName);
        //如果还存在剩余的Tab，就切换到最后的Tab上面
        if (siteContent.mainTabs.length >= 1) {
            //获取mainTabs数组最后一个元素
            let tab = siteContent.mainTabs[siteContent.mainTabs.length - 1];
            //选中这个Tab控件
            siteContent.mainTabsActiveName = tab.name;
            //内容区切换引用的页面
            router.push({ name: tab.name });
        } else {
            siteContent.mainTabsActiveName = '';
            router.push({ name: 'MisHome' });
        }
    }    

    function handleSwitch() {
        sidebar.sidebarFold = !sidebar.sidebarFold;
    }    

    async function logout(){
        try{
            const token = localStorage.getItem('token');
            const config = {
                headers: {
                    'satoken': token
                }
            };

            const response = await axios.get('/meinian-api/mis/user/logout', config);
            const data = response.data;
            if(data.code == 200){
                localStorage.removeItem('token');                
                localStorage.removeItem('permissions');                
                router.push({name: 'MisLogin'});
            }
        }catch(error){
            console.log('错误对象:', error);
            console.log('响应数据:', error.response);
            console.log('错误信息:', error.message);
            console.log('错误代码:', error.code);
        }
    }

    // 修改密码控件对应的模型层代码
    const validateConfirmPassword = (rule, value, callback) => {
        // value是确认密码
        if (value != dialog.dataForm.newPassword) {
            // 通过调用回调函数来通知表单验证系统，表单验证系统根据结果动态更新UI。
            callback(new Error('两次输入的密码不一致'));
        } else {
            // 通过调用回调函数来通知表单验证系统，表单验证系统根据结果动态更新UI。
            // 函数不传参数表示验证通过。
            callback();
        }
    };
    const dialog = reactive({
        visible: false,
        dataForm: {
            password: '',
            newPassword: '',
            confirmPassword: ''
        },
        dataRule: {
            password: [{ required: true, pattern: '^[a-zA-Z0-9]{6,20}$', message: '密码格式错误' }],
            newPassword: [{ required: true, pattern: '^[a-zA-Z0-9]{6,20}$', message: '密码格式错误' }],
            confirmPassword: [
                { required: true, pattern: '^[a-zA-Z0-9]{6,20}$', message: '密码格式错误' },
                // blur失去焦点则调用validateConfirmPassword进行验证
                { validator: validateConfirmPassword, trigger: 'blur' }
            ]
        }
    });

    // 拿到表单实例
    const dialogForm: Ref = ref(null);
    async function updatePassword(){
        dialog.visible = true;
        //等弹窗完全显示出来防止下面if执行失误
        //等表单完全显示出来再重置
        await nextTick();
        if(dialogForm.value != null){
            //重置表单
            dialogForm.value.resetFields();
        }
    }

    async function dataFormSubmit(){
        try{
            if(dialogForm.value){
                let valid = await dialogForm.value.validate();
                if(valid){
                    let sendData = {
                        oldPassword: dialog.dataForm.password,
                        newPassword: dialog.dataForm.newPassword
                    };
                    let config = {
                        headers: {
                            satoken: localStorage.getItem("token")
                        }
                    }
                    let response = await axios.post("/meinian-api/mis/user/modifyPassword", sendData, config)
                    if(response.data.result){
                        ElMessage({
                            type: "success",
                            message: "更新成功",
                            duration: 1200
                        });
                        dialog.visible = false;
                    }else{
                        ElMessage({
                            type: "error",
                            message: "更新失败",
                            duration: 1200
                            
                        });
                    }
                }
            }
        }catch{
            ElMessage({
                type: "error",
                message: "更新失败",
                duration: 1200
            });           
        }
    }
</script>
<style lang="scss">
    @import '../../assets/scss/index.scss';

    // 当鼠标悬停到头像上的时候，el自带的黑边框就会显示出来，以下样式可以消除头像四周的黑边框
    .el-dropdown-link {
    outline: none !important;
    }
    
    // 由于大部分样式使用了开源的scss样式，因此样式就不再定义到单独的文件中了。
    // 以下样式是定位头像的样式
    .avatar-container {
        vertical-align: -8px;
        margin-right: 5px;
    }

    
</style>
