<template>
  <!-- 容器div -->
  <div class="panel-container">
    <!-- IM聊天窗口div -->
    <div class="home-TUIKit-main2">
      <TUIKit
        v-if="sdkAppId && userId && userSig" 
        :SDKAppID="Number(sdkAppId)" 
        :userID="userId" 
        :userSig="userSig" 
      />
    </div>
    <!-- 客户信息div -->
    <div class="customer-info">
        <el-card class="box-card" shadow="never">
            <div class="info">
                <div class="left">
                    <el-avatar :size="57" shape="square" :src="data.customer.photoUrl">
                        <el-icon size="35"><UserFilled /></el-icon>
                    </el-avatar>
                </div>
                <div class="right">
                    <h4 class="customer-name">{{data.customer.customerName}}</h4>
                    <p class="customer-desc">
                        <el-icon class="icon"><User /></el-icon>
                        <span class="value">{{data.customer.gender}}</span>
                        <el-icon class="icon"><Phone /></el-icon>
                        <span class="value">{{data.customer.phone}}</span>
                        <el-icon class="icon"><Calendar /></el-icon>
                        <span class="value">{{data.customer.registerTime}}</span>
                    </p>
                </div>
            </div>
            <el-divider />
            <el-row :gutter="16">
                <el-col :span="10">
                    <div class="statistic-card">
                        <el-statistic :value="data.statistic.totalAmount" suffix="元">
                            <template #title>
                                <div class="title">累计消费金额</div>
                            </template>
                        </el-statistic>
                    </div>
                </el-col>
                <el-col :span="7">
                    <div class="statistic-card">
                        <el-statistic :value="data.statistic.count" suffix="笔">
                            <template #title>
                                <div class="title">有效订单数量</div>
                            </template>
                        </el-statistic>
                    </div>
                </el-col>
                <el-col :span="7">
                    <div class="statistic-card">
                        <el-statistic :value="data.statistic.quantity" suffix="个">
                            <template #title>
                                <div class="title">体检套餐数量</div>
                            </template>
                        </el-statistic>
                    </div>
                </el-col>
            </el-row>
        </el-card>
        <el-table  v-loading="data.order.loading" :data="data.order.dataList" border style="width: 100%"
            class="orders-table" :header-cell-style="{'background':'#f5f7fa'}">
            <el-table-column type="index" header-align="center"
                align="center" width="80" label="序号">
                <template #default="scope">
                    <span>{{ (data.order.pageIndex - 1) * data.order.pageSize + scope.$index + 1 }}</span>
                </template>
            </el-table-column>
            <el-table-column prop="goodsTitle" label="套餐名称"
                header-align="left" align="left" min-width="200" />
            <el-table-column prop="createDate" label="购买日期"
                header-align="center" align="center" min-width="120" />
            <el-table-column prop="orderStatus" label="状态" header-align="center"
                align="center" min-width="100" />
        </el-table>
        <el-pagination @size-change="sizeChangeHandle"
            @current-change="currentChangeHandle"
            :current-page="data.order.pageIndex"
            :page-sizes="[5,10, 20, 50]" :page-size="data.order.pageSize"
            :total="data.order.totalCount"
            layout="total, sizes, prev, pager, next, jumper">
        </el-pagination>
    </div>
  </div>
</template>

<script lang="ts" setup>
    // 导入Vue相关依赖
    import { reactive, ref, getCurrentInstance, onMounted, onUnmounted } from 'vue';
    // 导入腾讯云IM TUIKit组件
    import { TUIKit } from '../../TUIKit';
    // 导入HTTP请求库
    import axios from 'axios';
import { et } from 'element-plus/es/locales.mjs';

    // 获取当前组件实例，可用于访问全局属性或方法
    const { proxy } = getCurrentInstance();

    // 定义响应式数据对象，用于存储客户相关信息
    const data = reactive({
        // 客户基本信息
        customer: {
            id: null,           // 客户ID
            customerName: "--",         // 客户姓名
            gender: "--",          // 客户性别
            phone: "--",          // 客户电话
            photoUrl: "--",        // 客户头像
            registerTime: "--"    // 创建时间
        },
        // 客户统计信息
        statistic: {
            totalAmount: 0,  // 总金额
            count: 0,   // 总次数
            quantity: 0   // 数量
        },
        // 订单相关信息
        order: {
            dataList: [],       // 订单数据列表
            pageIndex: 1,       // 当前页码
            pageSize: 5,        // 每页显示条数
            totalCount: 0,      // 总记录数
            loading: false,     // 加载状态
        }
    });

    // 定义腾讯云IM所需的参数
    let sdkAppId = ref('');  // 应用SDK AppID
    let userId = ref('');    // 用户ID
    let userSig = ref('');   // 用户签名

    /**
     * 获取IM账号信息并登录腾讯云IM
     * 通过API请求获取SDK AppID、用户ID和用户签名
     */
    async function login(){
        // 配置请求头，携带认证token
        let config = {
            headers : {
                satoken: localStorage.getItem('token')  // 从本地存储获取token
            }
        };
        
        // 发送GET请求获取IM账号信息
        let response = await axios.get('/meinian-api/mis/customer/im/getServiceAccount', config);
        let result = response.data.result;
        
        // 将获取到的IM参数赋值给响应式变量
        sdkAppId.value = result.sdkAppId;
        userSig.value = result.userSig;
        userId.value = result.account;
    }
    
    // 页面加载时自动调用登录函数
    login();

    async function loadPageData(){
        data.order.loading = true;

        let sendData = {
            customerId: data.customer.id,
            pageNo: data.order.pageIndex,
            pageSize: data.order.pageSize
        };

        let config = {
            headers: {
                satoken: localStorage.getItem('token')
            }
        };

        const response = await axios.post('/meinian-api/mis/order/pageQuery', sendData, config);
        const pageResult = response.data.pageResult;    

        let statusEnum = {
            "1": "未付款",
            "2": "已关闭",
            "3": "已付款",
            "4": "已退款",
            "5": "已预约",
            "6": "已结束",
        }
        for(let one of pageResult.records){
            one.orderStatus = statusEnum[one.orderStatus + ""];
        }

        data.order.dataList = pageResult.records;
        data.order.totalCount = pageResult.total;
        data.order.loading = false;
    }

    import { TUIStore, StoreName } from '@tencentcloud/chat-uikit-engine-lite';

    //当客服点击某一个会话时，捕捉会话切换事件，拿到当前会话对象后接可以获取customer_id
    onMounted(() => {
        // 监听当前会话变化
        TUIStore.watch(StoreName.CONV, {
            currentConversation: (conversation: any) => {
                // 会话发生变化时执行这个回调函数，在前面代码中我们已经把它定义出来了。
                handleCurrentConversation(conversation);
            }
        });
    });

    onUnmounted(() => {
        // 清理监听
        TUIStore.unwatch(StoreName.CONV, {
            currentConversation: () => {}
        });
    });

    /**
     * 处理当前会话切换事件
     * @param conversation - 当前选中的会话对象
     * 当用户在聊天界面切换不同会话时触发
     */
    let lastConversationId = '';
    async function handleCurrentConversation(conversation: any) {
        if(conversation){
            // 1. 获取当前会话的唯一标识
            const currentId = conversation?.conversationID;
            
            // 2. 检查是否与上次相同
            if (currentId === lastConversationId) {
                return; // 跳过重复处理
            }
            
            // 3. 更新记录
            lastConversationId = currentId;
            
            // 4. 执行实际业务逻辑
            console.log('处理新会话:', conversation);
            // ...你的业务代码

            let customerId = conversation.conversationID.split("_")[1];
            data.customer.id = customerId;

            let sendData = {
                customerId: customerId
            }

            let config = {
                headers: {
                    satoken: localStorage.getItem('token')
                }
            };
            
            let response = await axios.post('/meinian-api/mis/customer/findSummary', sendData, config);
            let result = response.data.result ;

            data.customer.customerName = result.customerName
            data.customer.gender = result.gender
            data.customer.phone = result.phone
            data.customer.registerTime = result.registerTime
            //客户的头像是没有的所以返回来的json对象中没有photoUrl这个键
            //但是一旦有头像，就可以查出来，所以这样写是没有问题的
            data.customer.photoUrl = result.photoUrl
            data.statistic.totalAmount = result.totalAmount
            data.statistic.count = result.totalCount
            data.statistic.quantity = result.totalQuantity

            //用户点击会话的时候才获取客户摘要信息和分页查询订单数据
            loadPageData();
        }
    }

    function sizeChangeHandle(val){
        data.order.pageSize = val;
        data.order.pageIndex = 1;
        loadPageData();
    }

    function currentChangeHandle(val){
        data.order.pageIndex = val;
        loadPageData();
    }
</script>

<!-- 不能添加scoped，因为样式中需要设置TUIKit内部组件的样式。需要编写全局样式。-->
<style lang="less">
    @import url('customer_im.less');
</style>