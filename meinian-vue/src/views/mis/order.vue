<template>
    <div v-if="proxy.isAuth(['ROOT', 'ORDER:SELECT'])">
        <el-form :inline="true" :model="dataForm" :rules="dataRule" ref="form">
            <el-form-item prop="packageCode">
                <el-input v-model="dataForm.packageCode" placeholder="套餐编号"
                    maxlength="20" class="input" clearable="clearable" />
            </el-form-item>
            <el-form-item prop="keyword">
                <el-input v-model="dataForm.keyword" placeholder="套餐名称"
                    class="keyword" maxlength="50" clearable="clearable" />
            </el-form-item>
            <el-form-item prop="phone">
                <el-input v-model="dataForm.phone" placeholder="电话号码"
                    maxlength="11" class="input" clearable="clearable" />
            </el-form-item>
            <el-form-item class="range">
                <el-date-picker v-model="dataForm.dateRange" type="daterange"
                    range-separator="~" start-placeholder="起始日期"
                    end-placeholder="结束日期" format="YYYY-MM-DD"
                    value-format="YYYY-MM-DD" />
            </el-form-item>
            <el-form-item>
                <el-select v-model="dataForm.orderStatus" class="input"
                    placeholder="订单状态" clearable="clearable">
                    <el-option label="未付款" :value="1" />
                    <el-option label="已关闭" :value="2" />
                    <el-option label="已付款" :value="3" />
                    <el-option label="已退款" :value="4" />
                    <el-option label="已预约" :value="5" />
                    <el-option label="已结束" :value="6" />
                </el-select>
            </el-form-item>
            <el-form-item>
                <el-button type="primary" @click="searchHandle()">查询</el-button>
            </el-form-item>
            <el-form-item>
                <el-button type="primary"
                    v-if="proxy.isAuth(['ROOT', 'ORDER:UPDATE'])" :icon="Refresh"
                    @click="checkPaymentResultHandle()">同步付款结果</el-button>
            </el-form-item>
        </el-form>
    </div>
    <el-table :data="data.dataList"
        :header-cell-style="{'background':'#f5f7fa'}" border
        v-loading="data.loading" @selection-change="selectionChangeHandle"
        @expand-change="expand" :row-key="data.getRowKeys"
        :expand-row-keys="data.expands">
        <el-table-column type="expand">
            <template #default="scope">
                <div class="content-container">
                    <div class="left-panel">
                        <el-card class="box-card" shadow="never">
                            <div class="info">
                                <div class="left">
                                    <el-avatar :size="57" shape="square" :src="scope.row.photoUrl">
                                        <el-icon size="35"><UserFilled /></el-icon>
                                    </el-avatar>
                                </div>
                                <div class="right">
                                    <h4 class="customer-name">{{scope.row.customerName}}</h4>
                                    <p class="customer-desc">
                                        <el-icon class="icon"><User /></el-icon>
                                        <span class="value">{{scope.row.gender}}</span>
                                        <el-icon class="icon"><Phone /></el-icon>
                                        <span class="value">{{scope.row.phone}}</span>
                                        <el-icon class="icon"><Calendar /></el-icon>
                                        <span class="value">{{scope.row.registerTime}}</span>
                                    </p>
                                </div>
                            </div>
                            <el-divider />
                            <el-row :gutter="16">
                                <el-col :span="6">
                                    <div class="statistic-card">
                                        <el-statistic :value="scope.row.quantity-scope.row.num"
                                            suffix="个">
                                            <template #title>
                                                <div class="title">可预约体检</div>
                                            </template>
                                        </el-statistic>
                                    </div>
                                </el-col>
                                <el-col :span="6">
                                    <div class="statistic-card">
                                        <el-statistic :value="scope.row.num"
                                            suffix="个">
                                            <template #title>
                                                <div class="title">已预约体检</div>
                                            </template>
                                        </el-statistic>
                                    </div>
                                </el-col>
                                <el-col :span="6">
                                    <div class="statistic-card">
                                        <el-statistic :value="scope.row.quantity"
                                            suffix="个">
                                            <template #title>
                                                <div class="title">总计数量</div>
                                            </template>
                                        </el-statistic>
                                    </div>
                                </el-col>
                            </el-row>
                        </el-card>
                        <el-descriptions :column="1" class="order-code" border>
                            <el-descriptions-item label="订单编号：" label-align="center">
                            {{scope.row.outTradeNo}}
                            </el-descriptions-item>
                            <el-descriptions-item label="付款编号：" label-align="center">
                            {{scope.row.transactionId==null?"无":scope.row.transactionId}}
                            </el-descriptions-item>
                            <el-descriptions-item label="退款编号：" label-align="center">
                            {{scope.row.outRefundNo==null?"无":scope.row.outRefundNo}}
                            </el-descriptions-item>
                        </el-descriptions>
                    </div>
                    <div class="right-panel">
                        <el-table :data="data.appointment"
                            :header-cell-style="{'background':'#f5f7fa'}"
                            height="350" border>
                            <el-table-column label="序号" type="index" label-align="center" 
                                align="center" min-width="90">
                                <template #default="scope">
                                    <span>{{ scope.$index + 1 }}</span>
                                </template>
                            </el-table-column>
                            <el-table-column prop="patientName" label="体检人" label-align="center" 
                                align="center" min-width="180" />
                            <el-table-column prop="gender" label="性别" label-align="center" 
                                align="center" min-width="120" />
                            <el-table-column prop="age" label="年龄" label-align="center" 
                                align="center" min-width="120" />
                            <el-table-column prop="phone" label="电话" label-align="center" 
                                align="center" min-width="180" />
                            <el-table-column prop="appointmentDate" label="体检日" label-align="center" 
                                align="center" min-width="180" />
                            <el-table-column prop="status" label="状态" label-align="center" 
                                align="center" min-width="130" />
                        </el-table>
                    </div>
                </div>
            </template>
        </el-table-column>
        <el-table-column type="selection" header-align="center"
            align="center" width="50" :selectable="selectable" />
        <el-table-column type="index" header-align="center" align="center"
            width="100" label="序号">
            <template #default="scope">
                <span>{{ (data.pageIndex - 1) * data.pageSize + scope.$index + 1 }}</span>
            </template>
        </el-table-column>
        <el-table-column prop="goodsTitle" header-align="left" align="left"
            min-width="220" label="套餐名称" />
        <el-table-column header-align="center" align="center" min-width="80"
            label="价格">
            <template #default="scope">
                <span>￥{{ scope.row.goodsPrice }}</span>
            </template>
        </el-table-column>
        <el-table-column prop="quantity" header-align="center" align="center"
            min-width="100" label="数量" />
        <el-table-column header-align="center" align="center"
            min-width="100" label="总计">
            <template #default="scope">
                <span>￥{{ scope.row.totalAmount }}</span>
            </template>
        </el-table-column>
        <el-table-column prop="orderStatus" header-align="center" align="center"
            min-width="100" label="状态" />
        <el-table-column prop="createTime" header-align="center"
            align="center" min-width="100" label="下单时间" />
        <el-table-column prop="refundTime" header-align="center"
            align="center" min-width="100" label="退款时间" />
        <el-table-column header-align="center" align="center" width="200"
            label="操作">
            <template #default="scope">
                <el-button type="text"
                    v-if="proxy.isAuth(['ROOT', 'ORDER:SELECT'])"
                    @click="viewHandle(scope.row.snapshotId)">
                    预览
                </el-button>
                <el-button type="text"
                    v-if="proxy.isAuth(['ROOT', 'ORDER:DELETE'])"
                    :disabled="scope.row.orderStatus!='已关闭'"
                    @click="deleteHandle(scope.row.orderId)">
                    删除
                </el-button>
                <el-button type="text"
                    v-if="proxy.isAuth(['ROOT', 'ORDER:UPDATE'])"
                    :disabled="scope.row.orderStatus!='已付款'"
                    @click="updateHandle(scope.row.outTradeNo)">
                    线下退款
                </el-button>
            </template>
        </el-table-column>
    </el-table>
    <el-pagination @size-change="sizeChangeHandle"
        @current-change="currentChangeHandle" :current-page="data.pageIndex"
        :page-sizes="[10, 20, 50]" :page-size="data.pageSize"
        :total="data.totalCount"
        layout="total, sizes, prev, pager, next, jumper">
    </el-pagination>
</template>

<script lang="ts" setup>
    import { reactive, getCurrentInstance, ref, type Ref } from 'vue';
    import router from '../../router/index';
    import { Refresh, Search } from '@element-plus/icons-vue';
import axios from 'axios';
import { ElMessage, ElMessageBox } from 'element-plus';
import { isAsteriskToken } from 'typescript';

    const { proxy } = getCurrentInstance();

    //查询表单数据
    const dataForm = reactive({
        packageCode: null,
        keyword: null,
        phone: null,
        dateRange: [],
        orderStatus: null
    });

    const dataRule = reactive({
        packageCode: [
            { min: 6, message: '编号不能少于6个字符' },
            { pattern: '^[a-zA-Z0-9]{6,20}$', message: '编号格式错误' }
        ],
        keyword: [
            { pattern: '^[a-zA-Z0-9\u4e00-\u9fa5]{1,50}$', message: '名称格式错误' }
        ],
        phone: [
            { pattern: '^1[1-9]\\d{9}$', message: '电话号码格式错误' }
        ]
    });

    //分页数据
    const data = reactive({
        dataList: [],
        pageIndex: 1,
        pageSize: 10,
        totalCount: 0,
        loading: false,
        selections: [],
        expands: [],
        getRowKeys(row) {
            return row.orderId;
        },
        appointment: []
    });

    async function loadPageData(){
        data.loading = true;
        // 准备配置
        const config = {
            headers : {
                satoken: localStorage.getItem('token')
            }
        };
        // 准备数据
        const json = {
            packageCode: dataForm.packageCode,
            keyword: dataForm.keyword,
            phone: dataForm.phone,
            orderStatus: dataForm.orderStatus,
            pageNo: data.pageIndex,
            pageSize: data.pageSize,
            startDate: dataForm.dateRange.length == 2 ? dataForm.dateRange[0] : null,
            endData: dataForm.dateRange.length == 2 ? dataForm.dateRange[1] : null,
        }

        // 发送ajax请求
        const response = await axios.post('/meinian-api/mis/order/pageQuery', json, config);
        const pageResult = response.data.pageResult;

        // 将 pageResult.records 中的订单的orderStatus设置为文字形式，底层数据库返回的是数字。
        const orderStatusEnum = {
            "1": "未付款",
            "2": "已关闭",
            "3": "已付款",
            "4": "已退款",
            "5": "已预约",
            "6": "已结束"
        };

        const list = pageResult.records;
        for(const one of list){
            one.orderStatus = orderStatusEnum[one.orderStatus + ""];
        }

        // 渲染数据
        data.dataList = list;
        data.pageIndex = pageResult.pageNo;
        data.pageSize = pageResult.pageSize;
        data.totalCount = pageResult.total;

        data.loading = false;
    }

    // 页面加载的时候调用一下
    loadPageData();

    const form: Ref = ref(null);
    async function searchHandle(){
        let ok = form.value.validate();
        if(ok){
            form.value.clearValidate();
            data.pageIndex = 1;
            loadPageData();
        }
    }

    function sizeChangeHandle(val) {
        data.pageSize = val;
        data.pageIndex = 1;
        loadPageData();
    }
    function currentChangeHandle(val) {
        data.pageIndex = val;
        loadPageData();
    }

    //折叠面版中的预约信息
    async function expand(row){

        // 这行代码实现只展开一个面板的逻辑
        data.expands = data.expands.includes(row.orderId) ? [] : [row.orderId];

        if(data.expands.length > 0){
            // 走到这里说明用户是一个打开操作
            // 打开的时候发送ajax请求获取预约记录
            const json = {
                orderId: row.orderId
            };
            const config = {
                headers: {
                    satoken: localStorage.getItem('token')
                }
            };
            const response = await axios.post('/meinian-api/mis/appointment/findByOrderId', json, config);
            const list = response.data.result;
            // 将list集合中的每一个预约记录对象的状态修改为中文（数据库中存储的数字）
            const statusEnum = {
                "1": "未签到",
                "2": "已签到",
                "3": "已完成",
                "4": "已取消"
            };
            for(let one of list){
                one.status = statusEnum[one.status + ""];
            }
            // 将list赋值给响应式对象
            data.appointment = list;
        }else{
            // 走到这里这说明用户是一个折叠操作。
            // 折叠的时候把预约记录清空掉
            data.appointment = [];
        }
    }

    //点击预览执行的回调函数
    function viewHandle(snapshotId){
        router.push({
            name: 'FrontGoodsSnapshot',
            params: {
                id: snapshotId,
                mode: 'mis'
            }
        });                                             
    }

    function selectable(row){
        
        return (row.orderStatus =='已关闭' || row.orderStatus == '未付款');
    }

    // EP组件库自动调用这个回调函数，并且将当前所有被选中的项传递给 rows 这个参数。
    function selectionChangeHandle(rows){
        data.selections = rows;
    }

    function checkPaymentResultHandle(){
        if(data.selections.length == 0){
            ElMessage({
                message: '请选择需要同步的数据',
                type: 'warning',
                duration: 1200
            });
            return;
        }

        ElMessageBox.confirm("您确定要同步付款结果吗？", "提示信息", {
            type: "warning",
            confirmButtonText: "确定",
            cancelButtonText: "取消"
        }).then(async () => {
            try {
                // 准备数据
                let outTradeNos = data.selections.map(item => {
                    return item.outTradeNo;
                });
                console.log(outTradeNos);
                let sendData = {
                    outTradeNos: outTradeNos
                };
                // 准备token
                let config = {
                    headers: {
                        satoken: localStorage.getItem("token")
                    }
                };
                // 发送ajax post请求同步付款结果
                let response = await axios.post("/meinian-api/mis/order/syncPaymentResult", sendData, config);
                let rows = response.data.rows;
                // 提示同步了几条记录。
                if(rows == 0){
                    ElMessage({
                        type: "error",
                        message: `同步失败`,
                        duration: 1200
                    });
                }else{
                    ElMessage({
                        type: "success",
                        message: `同步${rows}条记录`,
                        duration: 1200
                    });
                    // 重新加载分页数据
                    loadPageData();
                }
            }catch(e){
                console.log(e);
                ElMessage({
                    type: "error",
                    message: "同步失败",
                    duration: 1200
                });
            }
        });
    }

    function deleteHandle(orderId){
        proxy.$confirm("您确定要删除订单记录吗？", "提示信息", {
            confirmButtonText: "确定",
            cancelButtonText: "取消"
        }).then(async ()=>{
            // 准备数据
            const json = {
                orderId: orderId
            };
            // 准备配置
            const config = {
                headers: {
                    satoken: localStorage.getItem('token')
                }
            };
            // 发送ajax post请求删除数据
            const response = await axios.post('/meinian-api/mis/order/removeById', json, config);
            const rows = response.data.rows;
            if(rows > 0){
                proxy.$message({
                    message: "删除成功",
                    type: "success",
                    duration: 1200,
                    onClose:()=>{
                        loadPageData();
                    }
                });
            }else{
                proxy.$message({
                    message: "删除失败",
                    type: "error",
                    duration: 1200
                });
            }
        });
    }

    function updateHandle(outTradeNo){
        ElMessageBox.confirm("您确定要执行线下退款吗？", "提示信息", {
            confirmButtonText: "确定",
            cancelButtonText: "取消"
        }).then(async () => {
            // 准备数据
            const json = {
                outTradeNo : outTradeNo
            };
            // 准备配置
            const config  = {
                headers: {
                    satoken: localStorage.getItem('token')
                }
            }
            // 发送ajax请求
            const response = await axios.post('/meinian-api/mis/order/offlineRefund', json, config);
            const success = response.data.success;
            if(success){
                ElMessage({
                    message: "退款成功",
                    type: "success",
                    duration: 1200,
                    onClose: ()=>{
                        loadPageData();
                    }
                });
            }else{
                ElMessage({
                    message: "退款失败",
                    type: "error",
                    duration: 1200
                });
            }
        });
    }
</script>

<style lang="less" scoped>
    @import url('order.less');
</style>