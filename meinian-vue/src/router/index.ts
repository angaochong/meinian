import { createRouter, createWebHistory } from "vue-router";

const router = createRouter({
    history: createWebHistory(),
    routes: [
        //项目启动时输入http://localhost:5173/
        {
            path: '/',
            redirect: { name: 'MisHome' }
            },
        // 业务端
        {
            path: '/front',
            name: 'Front',
            component: () => import('../views/front/main.vue'),  // 一级路由
            children: [
                {
                    // 注意：对于子页面的path的开头是不能添加 / 的。
                    path: 'index',
                    name: 'FrontIndex',
                    component: () => import('../views/front/index.vue')  // 二级路由
                },
                {
                    path: 'goods/:id',
                    name: 'FrontGoods',
                    component: () => import('../views/front/goods.vue')
                },
                {
                    path: 'goods_list',
                    name: 'FrontGoodsList',
                    component: () => import('../views/front/goods_list.vue')
                },
                {
                    path: 'customer',
                    name: 'FrontCustomer',
                    component: () => import('../views/front/customer.vue'),
                    children: [
                        {
                            path: 'mine',
                            name: 'FrontMine',
                            component: () => import('../views/front/mine.vue')
                        },
                        {
                            path: 'order_list',
                            name: 'FrontOrderList',
                            component: () => import('../views/front/order_list.vue')
                        },
                        {
                            path: 'customer_im',
                            name: 'FrontCustomerIm',
                            component: () => import('../views/front/customer_im.vue')
                        },
                        {
                            path: 'appointment_list',
                            name: 'FrontAppointmentList',
                            component: () => import('../views/front/appointment_list.vue')
                        }
                    ]
                },
                {
                    path: 'goods_snapshot/:id/:mode',
                    name: 'FrontGoodsSnapshot',
                    component: () => import('../views/front/goods_snapshot.vue')
                }
            ]
        },
        // mis端
        {
            path: '/mis',
            name: 'Main',
            redirect: { name: 'MisHome' },
            component: () => import('../views/mis/main.vue'),
            children: [
                {
                    path: 'home',
                    name: 'MisHome',
                    component: ()=>import('../views/mis/home.vue'),
                    meta: {
                        title : "首页"
                    }
                },
                {
                    path: 'dept',
                    name: 'MisDept',
                    component: ()=>import('../views/mis/dept.vue'),
                    meta: {
                        title : "部门管理",
                        isTab: true
                    }
                },
                {
                    path: 'role',
                    name: 'MisRole',
                    component: ()=>import('../views/mis/role.vue'),
                    meta: {
                        title : "角色管理",
                        isTab: true
                    }
                },
                {
                    path: 'user',
                    name: 'MisUser',
                    component: ()=>import('../views/mis/user.vue'),
                    meta: {
                        title: "用户管理",
                        isTab: true
                    }
                },
                {
                    path: 'goods',
                    name: 'MisGoods',
                    component: ()=>import('../views/mis/goods.vue'),
                    meta: {
                        title: "体检套餐",
                        isTab: true
                    }
                },
                {
                    path: 'order',
                    name: 'MisOrder',
                    component: ()=>import('../views/mis/order.vue'),
                    meta: {
                        title: "订单管理",
                        isTab: true
                    }
                },
                {
                    path: 'rule',
                    name: 'MisRule',
                    component: ()=>import('../views/mis/rule.vue'),
                    meta: {
                        title: "促销规则",
                        isTab: true
                    }
                },
                {
                    path: 'customer_im',
                    name: 'MisCustomerIm',
                    component: ()=>import('../views/mis/customer_im.vue'),
                    meta: {
                        title: "客服IM",
                        isTab: true
                    }
                },
                {
                    path: 'appointment',
                    name: 'MisAppointment',
                    component: ()=>import('../views/mis/appointment.vue'),
                    meta: {
                        title: "体检预约",
                        isTab: true
                    }
                },
                {
                    path: 'customer_checkin',
                    name: 'MisCustomerCheckin',
                    component: ()=>import('../views/mis/customer_checkin.vue'),
                    meta: {
                        title: "体检签到",
                        isTab: true
                    }
                },
                {
                    path: 'doctor_checkup',
                    name: 'MisCheckup',
                    component: ()=>import('../views/mis/doctor_checkup.vue'),
                    meta: {
                        title: "医生检查",
                        isTab: true
                    }
                },
                {
                    path: 'flow_regulation',
                    name: 'MisFlowRegulation',
                    component: ()=>import('../views/mis/flow_regulation.vue'),
                    meta: {
                        title: "人员限流",
                        isTab: true
                    }
                },
                {
                    path: 'checkup_report',
                    name: 'MisCheckupReport',
                    component: ()=>import('../views/mis/checkup_report.vue'),
                    meta: {
                        title: "体检报告",
                        isTab: true
                    }
                }
            ]
        },
        {
            path: '/mis/login',
            name: 'MisLogin',
            component: () => import('../views/mis/login.vue')
        },
        {
            path: '/404',
            name: '404',
            component: () => import('../views/404.vue')
        },
        {
            path: '/:pathMatch(.*)*',
            redirect: '/404'
        },
    ]
});

// router.beforeEach 在每次路由跳转前执行，用于权限控制、登录验证等全局路由守卫。
// to：要跳转到的目标路由对象
// from：当前离开的路由对象
// next：控制跳转的函数，必须调用才能继续
router.beforeEach((to, from, next) => {
    // 从本地存储中获取权限
    let permissions = localStorage.getItem("permissions");
    // 从本地存储中获取令牌
    let token = localStorage.getItem("token");
    // 获取要跳转的路径
    let fullPath = to.fullPath;
  
    // mis端
    if(fullPath.startsWith('/mis') && fullPath != '/mis/login'){ // 是mis端，不是访问登录页，那要鉴权。
        if(permissions == null || permissions == '' || token == null || token == ''){
            // 权限和令牌只要有一个是空，跳转到登录页。
            next({name: 'MisLogin'});
        }else{
            // 放行
            // 没有bug吗？自己手动向浏览器的本地存储中写入权限和令牌不就行了。
            // 确实会放行，不用担心，后端还会再校验。
            return next();
        }
    // 业务端：业务端是客户详情页或者是查看客户商品快照，那就需要登录了。
    } else if(fullPath.startsWith('/front/customer') || fullPath.startsWith('/front/goods_snapshot')){ 
        if(token == null || token == ''){
            next({name: 'FrontIndex'});
        }else{
            // 放行
            // 没有bug吗？自己手动向浏览器的本地存储中写入令牌不就行了。
            // 确实会放行，不用担心，即使放行，页面上也看不到数据，因为令牌在后端还会继续验证。
            return next();
        }
    } else {
        return next();
    }

});

// 导出路由器
export default router;