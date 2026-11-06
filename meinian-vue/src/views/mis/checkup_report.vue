<template>
    <div v-if="proxy.isAuth(['ROOT', 'CHECKUP_REPORT:SELECT'])">
        <el-form :inline="true" :model="dataForm" :rules="dataRule" ref="form">
            <el-form-item prop="patientName">
                <el-input v-model="dataForm.patientName" placeholder="姓名"
                    maxlength="20" class="input" clearable="clearable" />
            </el-form-item>
            <el-form-item prop="phone">
                <el-input v-model="dataForm.phone" placeholder="电话号码"
                    maxlength="11" class="input" clearable="clearable" />
            </el-form-item>
            <el-form-item prop="expressNo">
                <el-input v-model="dataForm.expressNo" placeholder="快递单号"
                    maxlength="24" class="input" clearable="clearable" />
            </el-form-item>
            <el-form-item>
                <el-button type="primary" @click="searchHandle()">查询</el-button>
            </el-form-item>
            <el-form-item>
                <el-upload :action="upload.action" name="file" accept=".xlsx"
                    :headers="upload.headers" :show-file-list="false"
                    :before-upload="excelBeforeUpload"
                    :on-success="excelUploadSuccess"
                    :on-error="excelUploadError">
                    <el-button type="primary">导入运单</el-button>
                </el-upload>
            </el-form-item>
            <el-form-item class="mold">
                <el-radio-group v-model="dataForm.statusLabel" @change="searchHandle()">
                    <el-radio-button label="全部"></el-radio-button>
                    <el-radio-button label="未生成"></el-radio-button>
                    <el-radio-button label="已生成"></el-radio-button>
                    <el-radio-button label="已寄出"></el-radio-button>
                </el-radio-group>
            </el-form-item>
        </el-form>
        <el-table :data="data.dataList"
            :header-cell-style="{'background':'#f5f7fa'}" border
            v-loading="data.loading">
            <el-table-column type="index" header-align="center" align="center"
                width="120" label="序号" fixed>
                <template #default="scope">
                    <span>{{ (data.pageIndex - 1) * data.pageSize + scope.$index + 1 }}</span>
                </template>
            </el-table-column>
            <el-table-column header-align="center" align="center" label="姓名"
                min-width="150" fixed>
                <template #default="scope">
                    <span class="customer-name" @dblclick="copyAddressHandle(scope.row.patientName,scope.row.phone,scope.row.address)">{{scope.row.patientName}}</span>
                </template>
            </el-table-column>
            <el-table-column prop="gender" header-align="center" align="center"
                label="性别" width="130" />
            <el-table-column prop="age" header-align="center" align="center"
                label="年龄" width="130" />
            <el-table-column prop="hidePhone" header-align="center" align="center"
                label="电话" width="180" />
            <el-table-column prop="status" header-align="center" align="center"
                label="报告状态" width="140" />
            <el-table-column prop="expressNo" header-align="center"
                align="center" label="快递单号" width="280" />
            <el-table-column prop="expressDate" header-align="center"
                align="center" label="寄出日期" width="170" />
            <el-table-column fixed="right" header-align="center" align="center"
                width="250" label="操作">
                <template #default="scope">
                    <el-button type="text" :disabled="scope.row.disabled"
                        @click="createReportHandle(scope.row.id)">
                        生成报告
                    </el-button>
                    <el-button type="text" :disabled="scope.row.status!='已生成'"
                        @click="downloadReportHandle(scope.row.patientName, scope.row.fileUrl)">
                        下载报告
                    </el-button>
                    <el-button type="text" :disabled="scope.row.status!='已生成'"
                        @click="identifyWaybillHandle(scope.row.id,scope.row.patientName, scope.row.phone)">
                        录入运单
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

    <el-dialog title="录入运单" v-if="proxy.isAuth(['ROOT', 'CHECKUP_REPORT:UPDATE'])"
        :close-on-click-modal="false" v-model="dialog.visible" width="420px">
        <el-alert title="请上传快递运单照片，系统将自动识别运单中的信息，然后请您核对无误后，提交运单信息"
            type="success" :closable="false" />
        <el-upload :action="dialog.upload.action" name="file"
            accept=".jpg,.jpeg" :headers="dialog.upload.headers"
            :show-file-list="false"
            :before-upload="waybillBeforeUpload"
            :on-success="waybillUploadSuccess"
            :on-error="waybillUploadError">
            <el-button type="primary">上传运单</el-button>
        </el-upload>
        <el-divider border-style="dashed">认真核对下方运单信息</el-divider>
        <table class="waybill-table">
            <tr>
                <th>收件人姓名：</th>
                <td>{{dialog.data.recName}}</td>
            </tr>
            <tr>
                <th>收件人电话：</th>
                <td>{{dialog.data.recPhone}}</td>
            </tr>
            <tr>
                <th>运单号码：</th>
                <td>{{dialog.data.expressNo}}</td>
            </tr>
        </table>
        <template #footer>
            <span class="dialog-footer">
                <el-button @click="dialog.visible = false">取消</el-button>
                <el-button type="primary" :disabled="dialog.data.expressNo==null||dialog.data.patientName!=dialog.data.recName|| dialog.data.recPhone.substr(7, 4) != dialog.data.phoneEnd"
                    @click="dataFormSubmit">
                提交运单
                </el-button>
            </span>
        </template>
    </el-dialog>
</template>

<script lang="ts" setup>
    import { reactive, getCurrentInstance, ref, Ref } from 'vue';
    import { ElMessage, ElNotification } from 'element-plus'
    import { isBlank, stringIsEmpty } from '../../utils/validate';
  
    import { dayjs } from 'element-plus'
    import isSameOrBefore from 'dayjs/plugin/isSameOrBefore'
import axios from 'axios';
    dayjs.extend(isSameOrBefore)

    const { proxy } = getCurrentInstance();
  
    const dataForm = reactive({
        patientName: null,
        phone: null,
        expressNo: null,
        statusLabel: '全部',
        status: null
    });

    const data = reactive({
        dataList: [],
        pageIndex: 1,
        pageSize: 10,
        totalCount: 0,
        loading: false
    });

    const dataRule = reactive({
        patientName: [
            { required: false, pattern: '^[\u4e00-\u9fa5]{1,10}$', message: '姓名格式错误' }
        ],
        phone: [
            { required: false, pattern: '^1[1-9]\\d{9}$', message: '电话格式错误' }
        ],
        expressNo: [
            { required: false, pattern: '^[0-9a-zA-Z]{10,24}$', message: '快递单号格式错误' }
        ]
    });
  
    const upload = reactive({
        action: "/meinian-api/mis/checkup/report/importExpress",
        headers: {
            satoken : localStorage.getItem('token')
        },
    });

    const dialog = reactive({
        visible: false,
        upload: {
            action: "/meinian-api/mis/report/recognizeWaybill",
            headers: {
                satoken : localStorage.getItem('token')
            },
            data: {
                id: null
            }
        },
        data: {
            id: null,
            recName: null,
            recPhone: null,
            expressNo: null,
            patientName: null,
            phoneEnd: null
        }
    })

    async function loadPageData(){
        data.loading = true;

        if(dataForm.statusLabel == '全部'){
            dataForm.status = null;
        }else if(dataForm.statusLabel == "未生成"){
            dataForm.status = 1;
        }else if(dataForm.statusLabel == "已生成"){
            dataForm.status = 2;
        }else if(dataForm.statusLabel == "已寄出"){
            dataForm.status = 3;
        }

        let json = {
            patientName: dataForm.patientName,
            phone: dataForm.phone,
            expressNo: dataForm.expressNo,
            status: dataForm.status,
            pageNo: data.pageIndex,
            pageSize: data.pageSize
        };

        let config = {
            headers: {
                satoken: localStorage.getItem('token')
            }
        };

        let response = await axios.post("/meinian-api/mis/report/pageQuery", json, config);
        let pageResult = response.data.pageResult;
        
        let records = pageResult.records;
        console.log(records)

        let statusEnum = {
            "1": "未生成",
            "2": "已生成",
            "3": "已寄出"
        };

        for(let one of records){
            one.status = statusEnum[one.status + ""];
        }
        data.dataList = records;
        data.totalCount = pageResult.total;
        data.loading = false;
    }

    loadPageData();

    const form: Ref = ref(null);
    async function searchHandle(){
        let ok = await form.value.validate();
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

    async function createReportHandle(id) {
        ElNotification({
            title: '提示信息',
            message: '体检报告正在生成中，请稍后',
            duration: 1000
        })

        let json = { id: id }
        let config = {
            headers: {
                satoken: localStorage.getItem("token")
            }
        };
        let response = await axios.post("/meinian-api/mis/report/createReport", json, config);
        let resp = response.data;
        if (resp.result) {
            ElMessage({
                message: '体检报告创建成功',
                type: 'success',
                duration: 1200,
                onClose: () => {
                    loadPageData();
                }
            });
        }else {
            ElMessage({
                message: '体检报告创建失败',
                type: 'error',
                duration: 1200
            });
        }
    }

    async function downloadReportHandle(name, fileUrl) {
    
        let response = await axios.get(`/meinian-api/mis/report/downloadReport`, {
            params: { name, fileUrl },
            headers: { satoken: localStorage.getItem("token") },
            responseType: 'blob'
        });
        const url = window.URL.createObjectURL(new Blob([response.data]));
        const link = document.createElement('a');
        link.href = url;
        link.setAttribute('download', `${name}的体检报告.docx`);
        document.body.appendChild(link);
        link.click();
        link.remove();
    }

    function copyAddressHandle(name, phone, address){
        const info = `${name},${phone},${address}`;
        navigator.clipboard.writeText(info).then(()=>{
            ElMessage.success("已复制");
        });
    }

    function identifyWaybillHandle(id, patientName, phone) {
        dialog.data.id = id;
        dialog.data.recName = null
        dialog.data.recPhone = null
        dialog.data.expressNo = null
        dialog.data.patientName = patientName
        dialog.data.phoneEnd = phone.substr(7, 4) //保存尾号
        dialog.visible = true
    }

    function waybillBeforeUpload(file) {
        let size = file.size / 1024 / 1024;
        if (size > 5) {
            ElMessage({
                message: '图片不能超过5M大小',
                type: 'error',
                duration: 1200
            });
            return false;
        }
        ElMessage({
            message: '识别中请稍后',
            type: 'info',
            duration: 1000
        });
        return true;
    }

    function waybillUploadError(e) {
        ElMessage({
            message: "图片上传失败",
            type: 'error',
            duration: 1200
        });
        console.error(e)
    }

    function waybillUploadSuccess(resp : any, uploadFile : UploadFile, uploadFiles : UploadFiles) {
        if (resp.msg == 'success') {
            let result = resp.result;
            dialog.data.recName = result.recName
            dialog.data.recPhone = result.recPhone
            dialog.data.expressNo = result.expressNo
            if (result.recName != dialog.data.patientName || result.recPhone.substr(7, 4) != dialog.data.phoneEnd) {
                ElMessage({
                    message: '收件人姓名或者电话与体检人不符',
                    type: 'error',
                    duration: 1200
                });
            }else{
                ElMessage({
                    message: '上传运单成功',
                    type: 'success',
                    duration: 1200
                });
            }
        }
    }

    async function dataFormSubmit(){
        let expressNo = dialog.data.expressNo;
        let expressDate = dayjs().format("YYYY-MM-DD");
        if(!stringIsEmpty(expressNo)){
            let json = {
                id: dialog.data.id,
                expressNo: expressNo,
                expressDate: expressDate
            };

            let config = {
                headers: {
                    satoken: localStorage.getItem("token")
                }
            };

            let response = await axios.post("/meinian-api/mis/report/saveWaybill", json, config);
            let result = response.data.result;
            if(result){
                ElMessage({
                    message: "运单提交成功",
                    type: 'success',
                    duration: 1200,
                    onClose: () => {
                        dialog.visible = false
                        loadPageData()
                    }
                });
            }else {
                ElMessage({
                    message: "运单提交失败",
                    type: 'error',
                    duration: 1200
                });
            }
        }
    }
</script>

<style lang="less" scoped>
    @import url('checkup_report.less');
</style>