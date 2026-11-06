<template>
    <el-form :inline="true" :model="dataForm" :rules="dataRule" ref="form">
        <el-form-item prop="keyword">
            <el-input v-model="dataForm.keyword" placeholder="套餐名称 / 姓名 / 电话"
                size="medium" class="keyword" clearable="clearable" />
        </el-form-item>
        <el-form-item class="date">
            <el-date-picker v-model="dataForm.date" type="date"
                placeholder="选择日期" :editable="false" format="YYYY-MM-DD"
                value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item>
            <el-button size="medium" type="primary" :icon="Search"
                @click="searchHandle()">查询</el-button>
        </el-form-item>
        <el-form-item class="mold">
            <el-radio-group v-model="dataForm.statusLabel" size="medium"
                @change="searchHandle()">
                <el-radio-button label="全部"></el-radio-button>
                <el-radio-button label="未签到"></el-radio-button>
                <el-radio-button label="已签到"></el-radio-button>
                <el-radio-button label="已完成"></el-radio-button>
                <el-radio-button label="已取消"></el-radio-button>
            </el-radio-group>
        </el-form-item>
    </el-form>
    <div class="table-conatainer" v-show="!empty">
        <el-table :data="data.dataList" class="appointment-table"
            :header-cell-style="{'background':'#f5f7fa'}" border
            v-loading="data.loading">
            <el-table-column type="index" header-align="center" align="center"
                width="120" label="序号" fixed>
                <template #default="scope">
                    <span>{{ (data.pageIndex - 1) * data.pageSize + scope.$index + 1 }}</span>
                </template>
            </el-table-column>
            <el-table-column prop="goodsTitle" header-align="center"
                align="center" label="套餐名称" min-width="250" fixed />
            <el-table-column prop="patientName" header-align="center" align="center"
                label="体检人" min-width="120" />
            <el-table-column prop="appointmentDate" header-align="center" align="center"
                label="预约日期" min-width="120" />
            <el-table-column prop="status" header-align="center" align="center"
                label="状态" min-width="120" />
            <el-table-column fixed="right" header-align="center" align="center"
                width="150" label="操作">
                <template #default="scope">
                    <el-button type="text" :disabled="scope.row.fileUrl==null"
                        @click="downloadHandle(scope.row.name,scope.row.fileUrl)">
                        体检报告
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

    <div class="empty" v-show="empty">
        <el-empty :image-size="200" />
    </div>
</template>

<script lang="ts" setup>
    import { reactive, ref, Ref, getCurrentInstance } from 'vue';
    import { Search } from '@element-plus/icons-vue';
    import router from '../../router/index';    
    import { dayjs } from 'element-plus';
    import isBetween from 'dayjs/plugin/isBetween';
import axios from 'axios';
    dayjs.extend(isBetween);    
    const { proxy } = getCurrentInstance();
  
    let empty = ref(false);
    const dataForm = reactive({
        keyword: null,
        date: null,
        status: null,
        statusLabel: '全部',
    });
    const dataRule = reactive({
        keyword: [
            { required: false, pattern: '^[a-zA-Z0-9\u4e00-\u9fa5]{1,30}$', message: '关键字内容不正确' }
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
            keyword: dataForm.keyword,
            status: dataForm.status,
            appointmentDate: dataForm.date,
            pageNo: data.pageIndex,
            pageSize: data.pageSize
        };

        let config = {
            headers: {
                satoken: localStorage.getItem('token')
            }
        };

        const response = await axios.post("/meinian-api/front/appointment/pageQuery", sendData, config);
        const pageResult = response.data.pageResult;

        let statusEnum = {
            "1" : "未签到",
            "2" : "已签到",
            "3" : "已完成",
            "4" : "已取消",
        }

        data.dataList = pageResult.records;
        data.totalCount = pageResult.total;

        data.dataList.forEach((item) => {
            item.status = statusEnum[item.status + ""];
        });
        
        data.loading = false;

        //如果查不到数据，就显示一个空的div
        empty = (pageResult.records.length == 0);
    }

    loadPageData();

    const form: Ref = ref(null);

    async function searchHandle(){
        let ok = await form.value.validate;
        if(ok){
            form.value.clearValidate();
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

    function downloadHandle(name, fileUrl){
        document.location.href = `${proxy.$minioUrl}${fileUrl}`;
    }
</script>

<style lang="less" scoped>
    @import url('appointment_list.less');
</style>