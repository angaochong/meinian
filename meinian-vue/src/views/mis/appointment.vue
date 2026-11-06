<template>
    <div v-if="proxy.isAuth(['ROOT', 'APPOINTMENT:SELECT'])">
        <el-form :inline="true" :model="dataForm" :rules="dataRule" ref="form">
            <el-form-item>
                <el-date-picker v-model="dataForm.appointmentDate" type="appointmentDate"
                    placeholder="选择日期" :editable="false" format="YYYY-MM-DD"
                    value-format="YYYY-MM-DD" clearable />
            </el-form-item>
            <el-form-item prop="patientName">
                <el-input v-model="dataForm.patientName" placeholder="姓名"
                    maxlength="10" class="input" clearable />
            </el-form-item>
            <el-form-item prop="phone">
                <el-input v-model="dataForm.phone" placeholder="电话号码"
                    maxlength="11" class="input" clearable />
            </el-form-item>
            <el-form-item>
                <el-button type="primary" @click="searchHandle()">查询</el-button>
                <el-button type="danger"
                    :disabled="!proxy.isAuth(['ROOT', 'APPOINTMENT:DELETE'])"
                    @click="deleteHandle()">
                    批量删除
                </el-button>
            </el-form-item>
            <el-form-item class="mold">
                <el-radio-group v-model="dataForm.statusLabel"
                    @change="searchHandle()">
                    <el-radio-button label="全部"></el-radio-button>
                    <el-radio-button label="未签到"></el-radio-button>
                    <el-radio-button label="已签到"></el-radio-button>
                    <el-radio-button label="已完成"></el-radio-button>
                    <el-radio-button label="已取消"></el-radio-button>
                </el-radio-group>
            </el-form-item>
        </el-form>
        <el-table :data="data.dataList"
            :header-cell-style="{'background':'#f5f7fa'}" border
            v-loading="data.loading" @selection-change="selectionChangeHandle">
            <el-table-column type="selection" :selectable="selectable"
                header-align="center" align="center" width="50" fixed />
            <el-table-column type="index" header-align="center" align="center"
                width="120" label="序号" fixed>
                <template #default="scope">
                    <span>{{ (data.pageIndex - 1) * data.pageSize + scope.$index + 1 }}</span>
                </template>
            </el-table-column>
            <el-table-column prop="patientName" header-align="center" align="center"
                label="姓名" width="200" fixed />
            <el-table-column prop="gender" header-align="center" align="center"
                label="性别" width="100" />
            <el-table-column prop="age" header-align="center" align="center"
                label="年龄" width="100" />
            <el-table-column prop="phone" header-align="center" align="center"
                label="联系电话" width="150" />
            <el-table-column prop="idCardNo" header-align="center" align="center"
                label="身份证号" width="190" />
            <el-table-column prop="company" header-align="center" align="center"
                label="公司名称" width="200" />
            <el-table-column prop="goodsTitle" header-align="center" align="center"
                label="体检套餐" min-width="200" />
            <el-table-column prop="status" header-align="center" align="center"
                label="状态" width="120" />
            <el-table-column fixed="right" header-align="center" align="center"
                width="150" label="操作">
                <template #default="scope">
                    <el-button type="text"
                        :disabled="!proxy.isAuth(['ROOT', 'APPOINTMENT:DELETE'])||scope.row.status!='未签到'"
                        @click="deleteHandle(scope.row.id)">
                        删除
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
    </div>
</template>

<script lang="ts" setup>
    import { reactive, getCurrentInstance, ref, Ref } from 'vue';
    import { dayjs, ElMessage, ElMessageBox } from 'element-plus';
    import isBetween from 'dayjs/plugin/isBetween';
import axios from 'axios';
    dayjs.extend(isBetween);

    const { proxy } = getCurrentInstance();
    const dataForm = reactive({
        patientName: null,
        phone: null,
        appointmentDate: null,
        statusLabel: '全部',
        status: null
    });

    const dataRule = reactive({
        patientName: [
            { pattern: '^[\u4e00-\u9fa5]{1,10}$', message: '姓名格式错误' }
        ],
        phone: [
            { pattern: '^1[1-9]\\d{9}$', message: '电话号码格式错误' }
        ]
    });

    const data = reactive({
        dataList: [],
        pageIndex: 1,
        pageSize: 10,
        totalCount: 0,
        loading: false,
        selections: []
    });

    async function loadPageData(){
        data.loading = true;

        if(dataForm.statusLabel == '全部'){
            dataForm.status = null;
        }else if(dataForm.statusLabel == '未签到'){
            dataForm.status = 1;
        }else if(dataForm.statusLabel == '已签到'){
            dataForm.status = 2;
        }else if(dataForm.statusLabel == '已完成'){
            dataForm.status = 3;
        }else if(dataForm.statusLabel == '已取消'){
            dataForm.status = 4;
        }

        let sendData = {
            patientName: dataForm.patientName,
            phone: dataForm.phone,
            appointmentDate: dataForm.appointmentDate,
            status: dataForm.status,
            pageNo: data.pageIndex,
            pageSize: data.pageSize
        };

        let config = {
            headers: {
                satoken: localStorage.getItem('token')
            }
        };

        let response = await axios.post("/meinian-api/mis/appointment/pageQuery", sendData, config);
        let pageResult = response.data.pageResult;

        let statusEnum = {
            '1': '未签到',
            '2': '已签到',
            '3': '已完成',
            '4': '已取消'
        };

        pageResult.records.forEach(item => {
            item.status = statusEnum[item.status + ""];
        });

        data.dataList = pageResult.records;
        data.totalCount = pageResult.total;

        data.loading = false;
    }

    loadPageData();

    const form: Ref = ref(null);

    async function searchHandle(){
        let ok = await form.value.validate;
        if(ok){
            form.value.clearValidate;
            data.pageIndex = 1;
            loadPageData();
        }
    }

    function sizeChangeHandle(pageSize: number){
        data.pageSize = pageSize;
        data.pageIndex = 1;
        loadPageData();
    }

    function currentChangeHandle(pageIndex: number){
        data.pageIndex = pageIndex;
        loadPageData();
    }

    function selectionChangeHandle(values){
        data.selections = values;
    }

    function selectable(row, index){
        if(row.status == '未签到'){
            return true;
        }
        return false;
    }

    function deleteHandle(id){
        let ids = (id != null && id != undefined) ? [id] : data.selections.map(item => {
            return item.id;
        });
        console.log(ids)

        if(ids != null && ids.length > 0){
            ElMessageBox.confirm("您确定删除记录吗？", "提示信息", {
                confirmButtonText: "确定",
                cancelButtonText: "取消"
            }).then(async () => {
                let response = await axios.post("/meinian-api/mis/appointment/deleteByIds", {ids: ids}, {
                    headers: {
                        satoken: localStorage.getItem('token')
                    }
                });

                let rows = response.data.rows;
                if(rows > 0){
                    ElMessage({
                        type: "success",
                        message: "操作成功",
                        duration: 1200
                    });
                    loadPageData();
                }else{
                    ElMessage({
                        type: "error",
                        message: "操作失败",
                        duration: 1200
                    });
                }
            });
        }
    }
</script>

<style lang="less" scoped>
@import url('appointment.less');
</style>