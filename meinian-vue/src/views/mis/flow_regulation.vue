<template>
    <div v-if="proxy.isAuth(['ROOT', 'FLOW_REGULATION:SELECT'])">
        <el-form :inline="true" :model="dataForm" :rules="dataRule" ref="form">
            <el-form-item>
                <el-select v-model="dataForm.place" style="width: 250px" placeholder="科室名称"
                    clearable="clearable">
                    <el-option v-for="one in data.placeList" :label="one"
                        :value="one" />
                </el-select>
            </el-form-item>
            <el-form-item prop="blueUuid">
                <el-input v-model="dataForm.blueUuid" placeholder="蓝牙信标UUID"
                    maxlength="32" class="input" clearable="clearable" />
            </el-form-item>
            <el-form-item>
                <el-button type="primary" @click="searchHandle()">查询</el-button>
                <el-button type="primary"
                    :disabled="!proxy.isAuth(['ROOT', 'FLOW_REGULATION:INSERT'])"
                    @click="addHandle()">
                    新增
                </el-button>
                <el-button type="danger"
                    :disabled="!proxy.isAuth(['ROOT', 'FLOW_REGULATION:DELETE'])"
                    @click="deleteHandle()">
                    批量删除
                </el-button>
            </el-form-item>
            <el-form-item class="mold">
                <el-switch v-model="dataForm.mode" size="large"
                    active-text="自动调流" inactive-text="手动调流"
                    :before-change="changeModeHandle" />
            </el-form-item>
        </el-form>
        <el-table :data="data.dataList"
            :header-cell-style="{'background':'#f5f7fa'}" border
            v-loading="data.loading" @selection-change="selectionChangeHandle">
            <el-table-column type="selection" header-align="center"
                align="center" width="50" :selectable="selectable" />
            <el-table-column type="index" header-align="center" align="center"
                width="100" label="序号">
                <template #default="scope">
                    <span>{{ (data.pageIndex - 1) * data.pageSize + scope.$index + 1 }}</span>
                </template>
            </el-table-column>
            <el-table-column prop="departmentName" header-align="center" align="center"
                min-width="250" label="科室名称" />
            <el-table-column prop="beaconUuid" header-align="center"
                align="center" min-width="270" label="蓝牙信标UUID" />
            <el-table-column prop="currentCount" header-align="center" align="center"
                min-width="120" label="实际排队人数" />
            <el-table-column prop="maxCapacity" header-align="center" align="center"
                min-width="120" label="最大排队人数" />
            <el-table-column prop="weight" header-align="center" align="center"
                min-width="120" label="调流权重" />
            <el-table-column prop="priority" header-align="center"
                align="center" min-width="120" label="调流优先级" />
            <el-table-column header-align="center" align="center" width="150"
                label="操作">
                <template #default="scope">
                    <el-button type="text"
                        v-if="proxy.isAuth(['ROOT', 'FLOW_REGULATION:UPDATE'])"
                        :disabled="scope.row.status"
                        @click="updateHandle(scope.row.id)">
                        修改
                    </el-button>
                    <el-button type="text"
                        v-if="proxy.isAuth(['ROOT', 'FLOW_REGULATION:DELETE'])"
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

    <el-dialog :title="!dialog.dataForm.id ? '新增' : '修改'"
        v-if="proxy.isAuth(['ROOT', 'FLOW_REGULATION:INSERT', 'FLOW_REGULATION:UPDATE'])"
        :close-on-click-modal="false" v-model="dialog.visible"
        width="450px">
        <el-form :model="dialog.dataForm" ref="dialogForm"
            :rules="dialog.dataRule" label-width="80px">
            <el-form-item label="科室名称" prop="place">
                <el-input v-model="dialog.dataForm.place"
                    placeholder="输入科室名称" maxlength="40" clearable />
            </el-form-item>
            <el-form-item label="蓝牙信标" prop="blueUuid">
                <el-input v-model="dialog.dataForm.blueUuid"
                    placeholder="输入蓝牙信标UUID" maxlength="32" clearable />
            </el-form-item>
            <el-form-item label="排队上限">
                <el-input-number v-model="dialog.dataForm.maxNum" min="1"
                    max="500" :step="10" step-strictly />
            </el-form-item>
            <el-form-item label="限流权重">
                <el-slider v-model="dialog.dataForm.weight" :step="1"
                    show-stops :min="1" :max="10" />
            </el-form-item>
            <el-form-item label="优先级">
                <el-slider v-model="dialog.dataForm.priority" :step="1"
                    show-stops :min="1" :max="10" />
            </el-form-item>
        </el-form>
        <template #footer>
            <span class="dialog-footer">
                <el-button @click="dialog.visible=false">取消</el-button>
                <el-button type="primary" @click="dataFormSubmit">确定</el-button>
            </span>
        </template>
    </el-dialog>
</template>

<script lang="ts" setup>
    import axios from 'axios';
import { ElMessage, ElMessageBox } from 'element-plus';
import { reactive, getCurrentInstance, ref, Ref, nextTick } from 'vue';
    const { proxy } = getCurrentInstance();

    const dataForm = reactive({
        place: null,
        blueUuid: null,
        mode: null
    });

    const data = reactive({
        placeList: [],
        dataList: [],
        pageIndex: 1,
        pageSize: 10,
        totalCount: 0,
        loading: false,
        selections: []
    });

    const dataRule = reactive({
        blueUuid: [
            { required: false, pattern: '^[a-zA-Z0-9]{32}$', message: '蓝牙信标UUID格式错误' }
        ]
    });

    const dialog = reactive({
        visible: false,
        dataForm: {
            id: null,
            place: null,
            blueUuid: null,
            maxNum: 10,
            weight: 1,
            priority: 1
        },
        dataRule: {
            place: [
                { required: true, message: '科室名称不能为空' },
                { min: 2, message: '科室名称不能少于2个字符' },
                { pattern: '^[a-zA-Z0-9\u4e00-\u9fa5\(\)]{2,40}$', message: '科室名称格式错误' }
            ],
            blueUuid: [
                { required: true, message: '蓝牙信标UUID不能为空' },
                { pattern: '^[a-zA-Z0-9]{32}$', message: '蓝牙信标UUID格式错误' }
            ]
        }
    })

    async function loadDeptList(){
        let config = {
            headers: {
                satoken: localStorage.getItem("token")
            }
        };

        let response = await axios.get("/meinian-api/mis/flow_regulation/findDepartmentNames", config);
        let result = response.data.result;
        data.placeList = result;
    }

    loadDeptList();

    async function loadMode(){
        let config = {
            heades: {
                satoken: localStorage.getItem("token")
            }
        };

        let response = await axios.get("/meinian-api/mis/flow_regulation/fingFlowRule", config);
        dataForm.mode = response.data.result;
    }

    loadMode();

    async function loadPageData(){
        data.loading = true;

        let sendData = {
            pageNo: data.pageIndex,
            pageSize: data.pageSize,
            beaconUuid: dataForm.blueUuid,
            deptName: dataForm.place
        };

        let config = {
            headers: {
                satoken: localStorage.getItem("token")
            }
        };

        let response = await axios.post("/meinian-api/mis/flow_regulation/pageQuery", sendData, config);
        let pageResult = response.data.pageResult;

        data.totalCount = pageResult.total;
        data.dataList = pageResult.records;

        data.loading = false;
    }

    loadPageData();

    function searchHandle(){
        proxy.$refs['form'].validate(valid=>{
            if(valid){
                // 表单合法，清除残留的错误信息
                proxy.$refs['form'].clearValidate();
                // 加载分页数据
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

    function addHandle(){
        dialog.dataForm.id = null;
        dialog.dataForm.place = null;
        dialog.dataForm.blueUuid = null;
        dialog.dataForm.maxNum = 10;
        dialog.dataForm.weight = 1;
        dialog.dataForm.priority = 1;
        dialog.visible = true;
        proxy.$nextTick(()=>{
            proxy.$refs['dialogForm'].resetFields();
        });
    }

    //点击修改和新增的提交都是这个函数
    const dialogForm: Ref = ref(null);
    async function dataFormSubmit(){
        let ok = dialogForm.value.validate();
        if(ok){

            dialogForm.value.clearValidate;

            let json = {
                id: dialog.dataForm.id,
                departmentName: dialog.dataForm.place,
                beaconUuid: dialog.dataForm.blueUuid,
                maxCapacity: dialog.dataForm.maxNum,
                weight: dialog.dataForm.weight,
                priority: dialog.dataForm.priority
            };

            let config = {
                headers: {
                    satoken: localStorage.getItem("token")
                }
            };

            let response = await axios.post(`/meinian-api/mis/flow_regulation/${dialog.dataForm.id == null ? 'save' : 'modify'}`, json, config);
            let rows = response.data.rows;
            if(rows == 1){
                ElMessage({
                    type: 'success',
                    message: '操作成功',
                    duration: 1200,
                    onClose: () => {
                        dialog.visible = false;
                        loadPageData();
                        loadDeptList();
                    }
                });
            }
        }else{
            ElMessage({
                type: 'error',
                message: '操作失败',
                duration: 1200
            })
        }
    }

    //点击修改按钮展示弹窗
    async function updateHandle(id){
        dialog.dataForm.id = id;
        dialog.visible = true;

        await nextTick();
        let response = await axios.get("/meinian-api/mis/flow_regulation/findById", {
            headers: {
                satoken: localStorage.getItem("token")
            }
        }); 
        let result = response.data.result;

        dialog.dataForm.place = result.departmentName;
        dialog.dataForm.blueUuid = result.beaconUuid;
        dialog.dataForm.maxNum = result.maxCapacity;
        dialog.dataForm.weight = result.weight;
        dialog.dataForm.priority = result.priority;
    }

    function selectable(){
        return true;
    }

    function selectionChangeHandle(vals){
        data.selections = vals;
    }

    function deleteHandle(id){
        let ids = id ? [id] : data.selections.map(item => item.id);
        if(ids.length == 0){
            ElMessage({
                type: 'warning',
                message: '请选择要删除的数据',
                duration: 1200
            });
        }else{
            ElMessageBox.confirm("你确定要删除数据吗？", "提示信息", {
                confirmButtonText: '确定',
                cancelButtonText: '取消'
            }).then(async ()=>{
                let json = {ids};
                
                let config = {
                    headers: {
                        satoken: localStorage.getItem('token')
                    }
                };

                let response = await axios.post("/meinian-api/mis/flow_regulation/removeByIds", json, config);
                let rows = response.data.rows;
                if(rows > 0){
                    ElMessage({
                        message: "操作成功",
                        type: "success",
                        duration: 1200
                    });

                    loadDeptList();
                    loadPageData();
                }
            });
        }
    }

    async function changeModeHandle(){

        let json = {mode: !dataForm.mode};

        let config = {
            headers: {
                satoken: localStorage.getItem('token')
            }
        };

        let response = await axios.post("/meinian-api/mis/flow_regulation/switchFlowRegulation", json, config);
        let result = response.data.result;
        
        if(result){
            ElMessage({
                type: 'success',
                message: '切换成功',
                duration: 1200
            });
            //返回 true 按钮会切换
            return true;
        }else{
            ElMessage({
                type: 'warning',
                message: '切换失败',
                duration: 1200
            });

            //返回 false 按钮不会切换
            return false;
        }
    }
</script>

<style lang="less" scoped>
    @import url('flow_regulation.less');
</style>