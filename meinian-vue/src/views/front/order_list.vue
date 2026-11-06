<template>
    <el-form :inline="true" :model="dataForm" :rules="dataRule" ref="form">
        <el-form-item prop="keyword">
            <el-input v-model="dataForm.keyword" placeholder="套餐标题 / 订单编号"
                size="medium" class="keyword" maxlength="32" clearable="clearable" />
        </el-form-item>
        <el-form-item>
            <el-button size="medium" type="primary" :icon="Search"
                @click="searchHandle()">查询</el-button>
        </el-form-item>
        <el-form-item class="mold">
            <el-radio-group v-model="dataForm.statusLabel" size="medium"
                @change="searchHandle()">
                <el-radio-button label="全部"></el-radio-button>
                <el-radio-button label="未付款"></el-radio-button>
                <el-radio-button label="已付款"></el-radio-button>
            </el-radio-group>
        </el-form-item>
    </el-form>
    <div class="order-list" v-show="!empty">
        <div class="order" v-for="one in data.dataList">
            <div class="header">
                <div class="datetime">{{ one.createTime }}</div>
                <div class="uuid">
                    订单号：
                    <span>{{ one.outTradeNo }}</span>
                </div>
                <div class="detail" @click="searchDetailHandle(one.snapshotId)">
                    查看商品详情
                </div>
            </div>
            <div class="content">
                <img :src="one.goodsImage" class="image" />
                <div class="info">
                    <h4>{{ one.goodsTitle }}</h4>
                    <p>{{ one.goodsDescription }}</p>
                </div>
                <div class="price">
                    <span class="label">套餐单价</span>
                    <span class="value">￥{{ one.goodsPrice }}</span>
                </div>
                <div class="number">
                    <span class="label">购买数量</span>
                    <span class="value">×{{ one.quantity }}</span>
                </div>
                <div class="amount">
                    <span class="label">合计</span>
                    <span class="value">￥{{ one.totalAmount }}</span>
                </div>
                <div class="status">
                    <span class="label">状态</span>
                    <span class="value">{{ one.orderStatus }}</span>
                </div>
                <div class="operate">
                    <el-button v-if="one.orderStatus == '未付款'" type="primary"
                        :disabled="one.disabled"
                        @click="paymentHandle(one.outTradeNo)">
                        付款
                    </el-button>
                    <el-button v-if="one.orderStatus == '未付款'" type="danger"
                        @click="closeOrderHandle(one.id)">
                        取消订单
                    </el-button>
                    <el-button v-if="['已付款','已预约'].includes(one.orderStatus)" type="primary"
                        :disabled="one.appointCount == one.quantity"
                        @click="appointHandle(one.orderId, one.quantity, one.appointCount)">
                        预约体检
                    </el-button>
                    <el-button v-if="one.orderStatus == '已结束'">获取发票</el-button>
                    <el-button v-if="one.orderStatus == '已付款'" type="danger"
                        :disabled="one.appointCount > 0"
                        @click="refundHandle(one.orderId)">
                        退款
                    </el-button>
                </div>
            </div>
        </div>
        <el-pagination @size-change="sizeChangeHandle"
            @current-change="currentChangeHandle" :current-page="data.pageIndex"
            :page-sizes="[10, 20, 50]" :page-size="data.pageSize"
            :total="data.totalCount"
            layout="total, sizes, prev, pager, next, jumper"></el-pagination>
    </div>
    <div class="empty" v-show="empty">
        <el-empty :image-size="200" />
    </div>

    <el-dialog title="体检预约" :close-on-click-modal="false"
        v-model="appointDialog.visible" width="550px">
        <el-form :model="appointDialog.dataForm" ref="dialogForm"
            :rules="appointDialog.dataRule" label-width="80px">
            <fieldset class="appointment">
                <legend>
                    <h4>我的预约</h4>
                </legend>
                <el-form-item label="预约日期" prop="appointmentDate">
                    <el-date-picker v-model="appointDialog.dataForm.appointmentDate"
                        type="date" placeholder="选择日期" size="medium"
                        :editable="false" format="YYYY-MM-DD"
                        value-format="YYYY-MM-DD"
                        :disabled-date="disabledDate" />
                    <span class="desc">提示：不可预约今日</span>
                </el-form-item>
                <el-form-item label="体检人" prop="patientName">
                    <el-input v-model="appointDialog.dataForm.patientName"
                        size="medium" placeholder="输入姓名" maxlength="10"
                        clearable />
                </el-form-item>
                <el-form-item label="身份证号" prop="idCardNo">
                    <el-input v-model="appointDialog.dataForm.idCardNo" size="medium"
                        placeholder="输入身份证号" maxlength="18" clearable />
                </el-form-item>
                <el-form-item label="电话号码" prop="phone">
                    <el-input v-model="appointDialog.dataForm.phone" size="medium"
                        placeholder="输入电话号码" maxlength="11" clearable />
                </el-form-item>
                <el-form-item label="邮寄地址" prop="address">
                    <el-input v-model="appointDialog.dataForm.address"
                        size="medium" placeholder="输入接收体检报告的邮寄地址"
                        maxlength="100" clearable />
                </el-form-item>
                <el-form-item label="公司名称" prop="company">
                    <el-input v-model="appointDialog.dataForm.company"
                        size="medium" placeholder="输入公司名称" maxlength="100"
                        clearable />
                </el-form-item>
            </fieldset>
        </el-form>
        <template #footer>
            <span class="dialog-footer">
                <el-button size="medium"
                    @click="appointDialog.visible = false">取消</el-button>
                <el-button type="primary" size="medium"
                    @click="dataFormSubmit">确定</el-button>
            </span>
        </template>
    </el-dialog>

</template>

<script lang="ts" setup>
    import { reactive, ref, type Ref, getCurrentInstance } from 'vue';
    import { Search } from '@element-plus/icons-vue';
    import router from '../../router/index';
  
    import { dayjs, ElMessage, ElMessageBox } from 'element-plus';
    import isBetween from 'dayjs/plugin/isBetween';
import axios from 'axios';
    dayjs.extend(isBetween);

    const { proxy } = getCurrentInstance();
  
    let empty = ref(false);
  
    const dataForm = reactive({
        keyword: null,
        statusLabel: '全部',
        status: null
    });
  
    const dataRule = reactive({
        keyword: [
          { required: false, pattern: '^[a-zA-Z0-9\u4e00-\u9fa5]{1,32}$', message: '关键字内容不正确' }
        ]
    });
  
    const data = reactive({
        dataList: [],
        pageIndex: 1,
        pageSize: 10,
        totalCount: 0,
        loading: false
    });

    async function loadPageData(){
        // 显示加载进度条
        data.loading = true;
        // 根据用户的选择，设置订单状态值
        if(dataForm.statusLabel == '全部'){
            dataForm.status = null;
        }else if(dataForm.statusLabel == '未付款'){
            dataForm.status = 1;
        }else {
            dataForm.status = 3;
        }
        // 准备token
        const config = {
            headers : {
                satoken: localStorage.getItem('token')
            }
        };
        // 准备数据
        const json = {
            keyword: dataForm.keyword,
            orderStatus: dataForm.status,
            pageNo: data.pageIndex,
            pageSize: data.pageSize
        };
        
        // 发送ajax请求获取数据
        const response = await axios.post('/meinian-api/front/order/pageQuery', json, config);
        // 获取ajax请求响应的数据，将数据设置到响应式对象中
        const pageResult = response.data.result;

        const statusEnum = {
            '1': '未付款',
            '2': '已关闭',
            '3': '已付款',
            '4': '已退款',
            '5': '已预约',
            '6': '已结束',
        };

        const list = pageResult.records;
        for(let one of list){
            one.goodsImage = `${proxy.$minioUrl}${one.goodsImage}`;
            one.orderStatus = statusEnum[one.orderStatus + ""];
        }

        data.dataList = list;
        data.totalCount = pageResult.total;
        data.loading = false;
        empty = (list.length == 0);
    }

    loadPageData();

    function searchHandle(){
        proxy.$refs['form'].validate((valid)=>{
            if(valid){
                proxy.$refs['form'].clearValidate();
                loadPageData();
            }
        });
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

    function refundHandle(orderId){
        proxy.$confirm('您确定要退款吗？','提示信息', {
            confirmButtonText : '确定',
            cancelButtonText: '取消',
            type: 'warning'
        }).then(async () => {
            // 发送ajax请求
            // 准备数据
            const json = {
                orderId: orderId
            };
            console.log(json)
            // 准备token
            const config = {
                headers: {
                    satoken: localStorage.getItem('token')
                }
            };
            // 发送ajax post请求
            const response = await axios.post('/meinian-api/front/order/refund', json, config);
            const result = response.data.result;
            if(result){
                proxy.$message({
                    message: '已申请原路返回退款资金，请稍后查看到账情况',
                    type: 'success',
                    duration: 1200
                });
            }else{
                proxy.$message({
                    message: '退款失败，请联系客服',
                    type: 'error',
                    duration: 1200
                });
            }
        });
    }    

    function searchDetailHandle(snapshotId){

        router.push({
            name: "FrontGoodsSnapshot",
            params: {
                id: snapshotId,
                mode: "front"
            }
        });
    }

    const appointDialog = reactive({
        visible: false,
        dataForm: {
            orderId: null,
            appointmentDate: null,
            patientName: null,
            idCardNo: null,
            phone: null,
            address: null,
            company: null
        },
        dataRule: {
            appointmentDate: [{ required: true, message: '日期不能为空' }],
            patientName: [
                { required: true, message: '姓名不能为空' },
                { pattern: '^[\u4e00-\u9fa5]{2,10}$', message: '姓名格式错误' }
            ],
            idCardNo: [
                { required: true, message: '身份证号不能为空' },
                { pattern: '^[0-9Xx]{18}$', message: '身份证号格式错误' }
            ],
            phone: [
                { required: true, message: '电话号码不能为空' },
                { pattern: '^1[1-9]\\d{9}$', message: '电话号码格式错误' }
            ],
            address: [
                { required: true, message: '邮寄地址不能为空' },
                { pattern: '^[0-9A-Za-z\u4e00-\u9fa5\\-_#]{10,100}$', message: '邮寄地址格式错误' }
            ],
            company: [
                { required: false, pattern: '^[0-9A-Za-z\u4e00-\u9fa5\\-_#]{2,100}$', message: '公司名称不正确' }
            ]
        }
    });

    // 只能预约从次日起，60天之内的日期。
    function disabledDate(date) {
        //只能预约未来60天的体检
        let bool = dayjs(date).isBetween(dayjs(), dayjs().add(61, 'day'));
        return !bool;
    }

    // 目前这个函数的第二个参数没有用。
    function appointHandle(orderId, quantity, appointCount){
        if(appointCount == 0){
            // 说明客户是第一次预约，提示客户是否确定预约，因为预约后不可退款。
            ElMessageBox.confirm("预约成功后不支持退款，确定要预约吗？", "确认信息", {
                confirmButtonText: "确认",
                cancelButtonText: "取消"
            }).then(() => {
                // 显示弹窗
                appointDialog.visible = true;
                // 设置orderId
                appointDialog.dataForm.orderId = orderId;
                proxy.$nextTick(()=>{
                    // 等预约弹窗显示出来之后，重置表单(重置表单到最初始的状态，清除表单上的错误提示信息)
                    // 不会将响应式数据恢复到初始状态。
                    proxy.$refs["dialogForm"].resetFields();
                });
            });
        }else{
            // 显示弹窗
            appointDialog.visible = true;
            // 设置orderId
            appointDialog.dataForm.orderId = orderId;
            proxy.$nextTick(()=>{
                // 等预约弹窗显示出来之后，重置表单
                proxy.$refs["dialogForm"].resetFields();
            });
        }
    }

    function dataFormSubmit(){
    proxy.$refs["dialogForm"].validate(async (valid)=>{
        if(valid){
            // 准备数据
            let json = {
                orderId: appointDialog.dataForm.orderId,
                appointmentDate: appointDialog.dataForm.appointmentDate,
                patientName: appointDialog.dataForm.patientName,
                idCardNo: appointDialog.dataForm.idCardNo,
                phone: appointDialog.dataForm.phone,
                address: appointDialog.dataForm.address,
                company: appointDialog.dataForm.company
            };
            // 准备配置
            let config = {
                headers: {
                    satoken: localStorage.getItem('token')
                }
            };
            // 发送ajax请求保存预约数据
            const response = await axios.post("/meinian-api/front/appointment/save", json, config);
            const result = response.data.result;
            if(result === "预约成功"){
                // 成功（提示信息，隐藏弹窗，重新加载分页数据）
                appointDialog.visible = false;
                ElMessage({
                    message: "预约成功",
                    type: "success",
                    duration: 1200
                });
                loadPageData();
            }else {
                // 失败（提示失败信息）
                ElMessage({
                    message: "预约失败",
                    type: "error",
                    duration: 1200
                });
            }
        }
    });
}


</script>

<style lang="less" scoped>
    @import url(order_list.less);
</style>