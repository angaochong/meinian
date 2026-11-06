<template>
    <div class="summary-container">
        <el-card class="box-card" shadow="never">
            <div class="info">
                <div class="left">
                    <el-avatar :size="45" shape="square" :src="data.photoUrl">
                        <el-icon size="25">
                            <UserFilled />
                        </el-icon>
                    </el-avatar>
                </div>
                <div class="right">
                    <div class="base">
                        <span>姓名：{{ data.customerName }}</span>
                        <span>性别：{{ data.gender }}</span>
                        <span>电话号码：{{ data.phone }}</span>
                        <div class="operate" @click="updateHandle">
                            <el-icon :size="18">
                                <Edit />
                            </el-icon>
                            <div>修改资料</div>
                        </div>
                    </div>
                    <p>注册时间：{{ data.registerTime }}</p>
                </div>
            </div>
            <el-divider />
            <el-row :gutter="16">
                <el-col :span="6">
                    <div class="statistic-card">
                        <el-statistic :value="data.amount" suffix="元">
                            <template #title>
                                <div class="title">累计消费金额</div>
                            </template>
                        </el-statistic>
                    </div>
                </el-col>
                <el-col :span="5">
                    <div class="statistic-card">
                        <el-statistic :value="data.count" suffix="笔">
                            <template #title>
                                <div class="title">有效订单数量</div>
                            </template>
                        </el-statistic>
                    </div>
                </el-col>
                <el-col :span="5">
                    <div class="statistic-card">
                        <el-statistic :value="data.number" suffix="个">
                            <template #title>
                                <div class="title">体检套餐数量</div>
                            </template>
                        </el-statistic>
                    </div>
                </el-col>
            </el-row>
        </el-card>
    </div>

    <el-dialog title="修改资料" :close-on-click-modal="false"
        v-model="dialog.visible" width="420px">
        <el-form :model="dialog.dataForm" ref="dialogForm"
            :rules="dialog.dataRule" label-width="60px">
            <el-form-item label="姓名" prop="customerName">
                <el-input v-model="dialog.dataForm.customerName" placeholder="输入姓名"
                    maxlength="10" size="medium" class="input" clearable />
            </el-form-item>
            <el-form-item label="性别" prop="gender">
                <el-select v-model="dialog.dataForm.gender" placeholder="选择性别"
                    size="medium" clearable="clearable">
                    <el-option label="男" value="男" />
                    <el-option label="女" value="女" />
                </el-select>
            </el-form-item>
            <el-form-item label="电话" prop="phone">
                <el-input v-model="dialog.dataForm.phone" placeholder="输入电话"
                    maxlength="11" size="medium" class="input" clearable />
            </el-form-item>
        </el-form>
        <template #footer>
            <span class="dialog-footer">
                <el-button size="medium" @click="dialog.visible = false">
                    取消
                </el-button>
                <el-button type="primary" size="medium" @click="dataFormSubmit">
                    确定
                </el-button>
            </span>
        </template>
    </el-dialog>
</template>

<script lang="ts" setup>
    import { reactive, ref, type Ref, getCurrentInstance } from 'vue';
    import router from '../../router/index';
    import axios from 'axios';
import { ElMain, ElMessage } from 'element-plus';
    const { proxy } = getCurrentInstance();

    const data = reactive({
        customerName: null,
        gender: null,
        phone: null,
        photoUrl: null,
        registerTime: null,
        count: 0,
        number: 0,
        amount: 0
    });

    async function loadCustomerInfo(){
        const config = {
            headers: {
                satoken: localStorage.getItem('token')
            }
        };

        const response = await axios.get('/meinian-api/front/customer/getSummary', config);
        const result = response.data.result;
        data.customerName = result.customerName ? result.customerName : "未填写";
        data.gender = result.gender ? result.gender : "未填写";
        data.phone = result.phone;
        data.photoUrl = result.photoUrl; // 头像为null不用处理，因为前端有默认头像。
        data.registerTime = result.registerTime;
        data.count = result.totalCount;
        // 当 result.totalQuantity 没有从后端返回的时候，这个值是 undefined，vue做了特殊处理，如果是undefined则采用之前的默认值0
        data.number = result.totalQuantity; 
        data.amount = result.totalAmount;
    }

    loadCustomerInfo();

    const dialog = reactive({
        // 控制可见性
        visible: false,
        // 表单数据
        dataForm: {
            customerName: null,
            gender: null,
            phone: null
        },
        // 校验规则
        dataRule: {
            customerName: [{pattern: '^[\u4e00-\u9fa5]{2,10}$', message: '姓名格式错误'}],
            phone: [{required: true, message: '电话不能为空'},{pattern:'^1[1-9]\\d{9}$', message:'电话格式错误'}]
        }
    });

    function updateHandle(){
        // 显示弹窗
        dialog.visible = true;
        // 等弹窗完全显示完毕之后
        proxy.$nextTick(() => {
            // 清空弹窗上残留的错误提示信息
            proxy.$refs['dialogForm'].resetFields();
            // 给表单上每一项填充值
            dialog.dataForm.customerName = (data.customerName == '未填写' ? null : data.customerName);
            dialog.dataForm.gender = (data.gender == '未填写' ? null : data.gender);
            dialog.dataForm.phone = data.phone;
        });
    }

    async function dataFormSubmit(){
        proxy.$refs['dialogForm'].validate(async valid => {
            if(valid){
                // 如果校验通过
                const config = {
                    headers: {
                        satoken: localStorage.getItem('token')
                    }
                };
                const response = await axios.post('/meinian-api/front/customer/modify', dialog.dataForm, config);
                const result = response.data.result;
                if(result){
                    proxy.$message({
                        message: '个人资料更新成功',
                        type: 'success',
                        duration: 1200,
                        onClose: ()=>{
                            dialog.visible = false;
                            loadCustomerInfo();
                        }
                    });
                }else{
                    ElMessage({
                    message: '手机号已存在',
                    type: 'error',
                    duration: 1200
                });
                }
            }
        });
    }
</script>

<style lang="less" scoped>
    @import url('mine.less');
</style>