<template>

    <div v-if="proxy.isAuth(['ROOT', 'RULE:SELECT'])">
        <el-form :inline="true" :model="dataForm" :rules="dataRule" ref="form">
            <el-form-item prop="ruleName">
                <el-input v-model="dataForm.ruleName" placeholder="规则名称"
                    maxlength="20" class="input" clearable="clearable" />
            </el-form-item>
            <el-form-item>
                <el-button type="primary" @click="searchHandle()">查询</el-button>
                <el-button type="primary" :disabled="!proxy.isAuth(['ROOT', 'RULE:INSERT'])"
                    @click="addHandle()">
                    新增
                </el-button>
            </el-form-item>
        </el-form>
        <el-table :data="data.dataList"
            :header-cell-style="{'background':'#f5f7fa'}" border
            v-loading="data.loading">
            <el-table-column type="index" header-align="center" align="center"
                width="100" label="序号">
                <template #default="scope">
                    <span>{{ (data.pageIndex - 1) * data.pageSize + scope.$index + 1 }}</span>
                </template>
            </el-table-column>
            <el-table-column prop="ruleName" header-align="left" align="left"
                min-width="150" label="规则名称" />
            <el-table-column prop="remark" header-align="left" align="left"
                min-width="350" label="备注信息" />
            <el-table-column prop="count" header-align="center" align="center"
                min-width="80" label="关联套餐数量" />
            <el-table-column header-align="center" align="center" width="150"
                label="操作">
                <template #default="scope">
                    <el-button type="text"
                        v-if="proxy.isAuth(['ROOT', 'RULE:UPDATE'])"
                        @click="updateHandle(scope.row.ruleId)">
                        修改
                    </el-button>
                    <el-button type="text" :disabled="scope.row.count>0"
                        v-if="proxy.isAuth(['ROOT', 'RULE:DELETE'])"
                        @click="deleteHandle(scope.row.ruleId)">
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

    <!-- dialog.dataForm.id 没有值就是新增，有值就是修改 -->
    <!-- :close-on-click-modal="false"表示点击遮罩层时，不关闭模态窗口 -->
    <!-- v-model="dialog.visible" visible决定模态窗口可见性 -->
    <el-dialog :title="!dialog.dataForm.ruleId ? '新增' : '修改'"
        v-if="proxy.isAuth(['ROOT', 'RULE:INSERT', 'RULE:UPDATE'])"
        :close-on-click-modal="false" v-model="dialog.visible"
        custom-class="dialog" width="500px">

        <!-- 保存或修改的form表单 -->
        <!-- 
            四要素：
                :model="dialog.dataForm" - 为表单提供数据上下文
                v-model="dialog.dataForm.xxx" - 单个字段的双向绑定
                :rules="dialog.dataRule" - 验证规则
                prop="fieldName" - 指定验证的字段名
                这四个要素缺一不可，它们共同构成了 Element Plus 表单的完整验证体系。
        -->
        <el-form :model="dialog.dataForm" ref="dialogForm" :rules="dialog.dataRule" label-width="80px">
            <el-form-item label="规则名称" prop="ruleName">
                <el-input v-model="dialog.dataForm.ruleName" maxlength="20" clearable />
            </el-form-item>
            <el-form-item label="规则内容" prop="ruleContent">
                <el-input v-model="dialog.dataForm.ruleContent" type="textarea" rows="10" clearable />
            </el-form-item>
            <el-form-item label="备注信息">
                <el-input v-model="dialog.dataForm.remark" type="textarea" rows="3" clearable />
            </el-form-item>
        </el-form>

        <template #footer>
            <span class="dialog-footer">
                <el-button @click="dialog.visible = false">取消</el-button>
                <el-button type="primary" @click="dataFormSubmit">确定</el-button>
            </span>
        </template>
        
    </el-dialog>

</template>

<script lang="ts" setup>
    import { reactive, getCurrentInstance, ref, onMounted, Ref } from 'vue';
    import { Delete } from '@element-plus/icons-vue';
    import router from '../../router/index';
import axios from 'axios';
import { ElMessage, ElMessageBox } from 'element-plus';
    const { proxy } = getCurrentInstance();
  
    const dataForm = reactive({
        ruleName: null
    });
  
    const dataRule = reactive({
        ruleName: [
          { required: false, pattern: '^[a-zA-Z0-9\u4e00-\u9fa5]{1,20}$', message: '规则名称不正确' }
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

        const sendData = {
            ruleName: dataForm.ruleName,
            pageNo : data.pageIndex,
            pageSize : data.pageSize
        };

        const config = {
            headers: {
                satoken: localStorage.getItem('token')
            }
        };

        const response = await axios.post('/meinian-api/mis/rule/pageQuery', sendData, config);
        const pageResult = response.data.pageResult;
        data.dataList = pageResult.records;
        data.totalCount = pageResult.total;
        data.loading = false;
    }

    loadPageData();

    const form: Ref = ref(null);

    function searchHandle(){
        const ok = form.value.validate();
        if(ok){
            form.value.clearValidate();
            data.pageIndex = 1;
            loadPageData();
        }
    }

    // 专门为弹窗准备的 dataForm 和 dataRule
    // 为什么要放到 dialog 里，因为直接写 dataForm 和 dataRule会和页面中其他的dataForm 和 dataRule冲突。
    const dialog = reactive({
        visible: false,
        dataForm: {
            ruleId: null,
            ruleName: null,
            remark: null,
            ruleContent: null
        },
        dataRule: {
            ruleName: [
                { required: true, message: '规则名称不能为空' },
                { pattern: '^[a-zA-Z0-9\u4e00-\u9fa5]{1,20}$', message: '规则名称不正确' }
            ],
            ruleContent: [{ required: true, trigger: 'blur', message: '规则内容不能为空' }]
        }
    });

    function addHandle(){
        // 显示为新增效果时，ruleId必须是null
        dialog.dataForm.ruleId = null;
        dialog.dataForm.remark = null;
        dialog.dataForm.ruleContent = null;
        dialog.dataForm.ruleName = null;
        dialog.visible = true;
        proxy.$nextTick(() => {
            // 清除所有验证状态和错误信息
            proxy.$refs['dialogForm'].resetFields();
        });
    }

    function dataFormSubmit(){
        proxy.$refs['dialogForm'].validate(async (valid) => {
            if(valid){
                // 准备数据（四个数据都需要准备，因为这个方法是一个通用的方法，既是保存方法，又是修改的方法。）
                const json = {
                    ruleId: dialog.dataForm.ruleId,
                    ruleName: dialog.dataForm.ruleName,
                    ruleContent: dialog.dataForm.ruleContent,
                    remark: dialog.dataForm.remark
                };
                // 准备配置
                const config = {
                    headers: {
                        satoken: localStorage.getItem('token')
                    }
                }
                // 发送ajax post请求
                // 注意请求路径的动态效果。
                const response = await axios.post(`/meinian-api/mis/rule/${dialog.dataForm.ruleId == null ? 'save' : 'modify'}`, json, config);
                const rows = response.data.rows;
                if(rows == 1){
                    // 保存成功
                    ElMessage({
                        message: "保存成功",
                        type: "success",
                        duration: 1200,
                        onClose: ()=>{
                            // 隐藏弹窗
                            dialog.visible = false;
                            // 加载当前分页数据
                            loadPageData();
                        }
                    })
                }else{
                    // 保存失败
                    ElMessage({
                        message: "保存失败",
                        type: "error",
                        duration: 1200
                    })
                }
            }
        });
    }

    async function updateHandle(ruleId){
        // 准备数据
        const json = {
            ruleId: ruleId
        }
        // 准备配置
        const config = {
            headers: {
                satoken: localStorage.getItem('token')
            }
        };
        // 发送ajax请求
        const response = await axios.post("/meinian-api/mis/rule/findById", json, config);
        const result = response.data.result;

        dialog.dataForm.ruleId = result.ruleId;
        dialog.dataForm.ruleName = result.ruleName;
        dialog.dataForm.ruleContent = result.ruleContent;
        dialog.dataForm.remark = result.remark;

        dialog.visible = true;
    }

    function deleteHandle(ruleId){
        ElMessageBox.confirm('您确定要删除该促销规则吗？', '提示信息', {
            confirmButtonText: "确定",
            cancelButtonText: "取消"
        }).then(async () => {
            // 准备数据
            const json = {
                ruleId: ruleId
            };
            // 准备配置
            const config = {
                headers: {
                    satoken: localStorage.getItem('token')
                }
            };
            // 发送post请求删除数据
            const response = await axios.post('/meinian-api/mis/rule/removeById', json, config);
            const rows = response.data.rows;
            if(rows == 1){
                ElMessage({
                    message: "删除成功",
                    type: "success",
                    duration: 1200,
                    onClose: () => {
                        loadPageData();
                    }
                });
            }else{
                ElMessage({
                    message: "删除失败",
                    type: "error",
                    duration: 1200
                });
            }
        });
    }

</script>

<style lang="less" scoped>
    
</style>