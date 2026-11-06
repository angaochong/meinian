<template>
    <div v-if="proxy.isAuth(['ROOT','USER:SELECT'])">
        <!-- 没有el-form同样可以显示页面，为什么用它？主要是用它来完成表单验证功能 -->
        <!-- :inline="true" 表示控件排成一行 -->
        <!-- :model="dataForm" 表单数据绑定 -->
        <!-- :rules="dataRule" 关联验证规则 -->
        <!-- ref="form" 这个给表单起个名，以后获取表单通过名字来获取 -->
        <el-form :inline="true" :model="dataForm" :rules="dataRule" ref="form">
            <!-- prop="name" 对应的就是 dataRule 中的 name 属性，这样就关联上验证了 -->
            <el-form-item prop="name">
                <!-- class="input" 是自己设置的样式，用来设置表单控件宽度，因为默认情况下，表单控件太宽了 -->
                <el-input v-model="dataForm.name" placeholder="姓名"
                    maxlength="10" class="input" clearable />
            </el-form-item>
            <el-form-item>
                <el-select v-model="dataForm.sex" class="input" placeholder="性别"
                    clearable>
                    <el-option label="男" value="男" />
                    <el-option label="女" value="女" />
                </el-select>
            </el-form-item>
            <el-form-item>
                <el-select v-model="dataForm.role" class="input"
                    placeholder="角色" clearable>
                    <!-- 这个位置将来模型层有数据了，自然就显示了 -->
                    <el-option v-for="one in dataForm.roleList"
                        :label="one.roleName" :value="one.roleId" />
                </el-select>
            </el-form-item>
            <el-form-item>
                <el-select v-model="dataForm.deptId" class="input"
                    placeholder="部门" clearable>
                    <!-- 这个位置也是这样，将来发送ajax请求之后就有数据了 -->
                    <el-option v-for="one in dataForm.deptList"
                        :label="one.deptName" :value="one.deptId" />
                </el-select>
            </el-form-item>
            <el-form-item>
                <el-select v-model="dataForm.status" class="input"
                    placeholder="状态" clearable>
                    <el-option label="在职" value="1" />
                    <el-option label="离职" value="2" />
                </el-select>
            </el-form-item>
            <el-form-item>
                <!-- 这些事件对应的函数还没有写，后面写 -->
                <el-button type="primary" @click="searchHandle()">查询</el-button>
                <!-- 当前用户至少有 ROOM或者USER:INSERT中的任何一个权限，这个按钮才能显示 -->
                <el-button type="primary"
                    :disabled="!proxy.isAuth(['ROOT', 'USER:INSERT'])"
                    @click="addHandle()">
                    新增
                </el-button>
                <!-- 当前用户至少有 ROOT或者USER:DELETE中的任何一个权限，这个按钮才能显示 -->
                <el-button type="danger"
                    :disabled="!proxy.isAuth(['ROOT', 'USER:DELETE'])"
                    @click="deleteHandle()">
                    批量删除
                </el-button>
            </el-form-item>
        </el-form>
        
        <!-- :data="data.dataList" 为表格提供数据，现代开发不需要写v-for，
        直接底层自动从数组中取出数据遍历生成表格行，data.dataList存储的是
        当前页数据，不是数据库中全部数据 -->
        <!-- :header-cell-style 设置表头背景色 -->
        <!-- border 表示表格的每个单元格都有边框 -->
        <!-- v-loading="data.loading" 决定是否显示加载动画 -->
        <!-- @selection-change="selectionChangeHandle" 勾选复选框时的回调 -->
        <el-table :data="data.dataList"
        :header-cell-style="{'background':'#f5f7fa'}" border
        v-loading="data.loading" @selection-change="selectionChangeHandle">

            <!-- type="selection" 规定这一列是复选框列 -->
            <!-- header-align="center" 表示表头内容居中 -->
            <!-- align="center" 表示列中数据居中 -->
            <el-table-column type="selection" header-align="center" :selectable="selectable"
                align="center" width="50" />
            
            <!-- 这一列是序号，不是主键值哈。 -->
            <!-- type="index" 告诉EP组件，这一列不用绑定数据，请自动帮我生成行号 -->
            <el-table-column type="index" header-align="center" align="center"
                width="100" label="序号">
                <!-- 
                    scope 是一个回调参数，它包含了当前这行表格数据的所有信息。
                    可以把它理解成一个数据快递包，ElTable 会自动把这一行的数据
                    “打包”好，通过 scope 传递给你。 
                    scope.$index: 当前行在【当前页面】的索引（从0开始）
                    scope.row: 当前行的完整数据对象 
                    scope.column: 当前列的信息
                    scope.store: 表格的状态管理
                -->
                <template #default="scope">
                    <!-- 这是序号的算法 -->
                    <span>{{ (data.pageIndex - 1) * data.pageSize + scope.$index + 1 }}</span>
                </template>
            </el-table-column>
            <!-- prop="name" 这个 name是后端java对象的属性名 -->
            <el-table-column prop="realName" header-align="center" align="center"
                min-width="100" label="姓名" />
            <el-table-column prop="gender" header-align="center" align="center"
                min-width="60" label="性别" />
            <el-table-column prop="mobile" header-align="center" align="center"
                min-width="130" label="电话" />
            <!-- :show-overflow-tooltip="true" 设置为true表示采用缩略显示 -->
            <el-table-column prop="email" header-align="center" align="center"
                min-width="240" label="邮箱" :show-overflow-tooltip="true" />
            <el-table-column prop="hireDate" header-align="center"
                align="center" min-width="130" label="入职日期" />
            <el-table-column prop="roles" header-align="center" align="center"
                min-width="150" label="角色" :show-overflow-tooltip="true" />
            <el-table-column prop="deptName" header-align="center" align="center"
                min-width="120" label="部门" />
            <el-table-column prop="userStatus" header-align="center" align="center"
                min-width="100" label="状态" />
            <el-table-column header-align="center" align="center" width="150"
                label="操作">
                <template #default="scope">
                    <!-- type="text" 表示按钮没有边框，看起来像文字。 -->
                    <el-button type="text"
                        v-if="proxy.isAuth(['ROOT', 'USER:UPDATE'])"
                        @click="updateHandle(scope.row.userId)">
                        修改
                    </el-button>
                    <el-button type="text"
                        v-if="proxy.isAuth(['ROOT', 'USER:UPDATE'])"
                        :disabled="scope.row.userStatus == '离职' || scope.row.isSuperAdmin"
                        @click="dismissHandle(scope.row.userId)">
                        离职
                    </el-button>
                    <el-button type="text" :disabled="scope.row.isSuperAdmin"
                        v-if="proxy.isAuth(['ROOT', 'USER:DELETE'])"
                        @click="deleteHandle(scope.row.userId)">
                        删除
                    </el-button>
                </template>
            </el-table-column>
        </el-table>
        <!-- EP组件库的分页组件 -->
        <!-- :current-page 当前页码 -->
        <!-- :page-size 每页显示多少条记录 -->
        <!-- :total 总记录条数 -->
        <!-- :page-sizes  它定义了一个下拉选择框，让用户可以快速切换每页显示的数据条数-->
        <!-- layout 分页最下面一行显示什么内容，顺序是怎样的 -->
        <el-pagination @size-change="sizeChangeHandle"
            @current-change="currentChangeHandle" :current-page="data.pageIndex"
            :page-sizes="[10, 20, 50]" :page-size="data.pageSize"
            :total="data.totalCount"
            layout="total, sizes, prev, pager, next, jumper"></el-pagination>        
    </div>
    <!-- :title 是动态的，是新增还是修改，看id是不是null，是null就是新增 -->
    <!-- :close-on-click-modal="false" 设置点击模态窗口之外的阴影是否隐藏模态窗口，false不隐藏 -->
    <!-- v-model="dialog.visible" 控制模态窗口隐藏/显示 -->
    <el-dialog :title="!dialog.dataForm.id ? '新增' : '修改'"
        v-if="proxy.isAuth(['ROOT','USER.INSERT','USER:UPDATE'])"
        :close-on-click-modal="false" v-model="dialog.visible" width="450px">
        <!-- :model="dialog.dataForm" 表单上绑定的数据 -->
        <!-- ref="dialogForm" 给表单起个名字 -->
        <!-- :rules="dialog.dataRule" 给表单关联校验规则 -->
        <el-form :model="dialog.dataForm" ref="dialogForm"
            :rules="dialog.dataRule" label-width="80px">
            <!-- prop="username"是将username的校验规则绑定到表单项上 -->
            <el-form-item label="用户名" prop="username" v-if="!dialog.update">
                <el-input v-model="dialog.dataForm.username" maxlength="20"
                    clearable />
            </el-form-item>
            <!-- v-if="!dialog.update" 用来决定密码框显示还是隐藏 -->
            <el-form-item label="密码" prop="password" v-if="!dialog.update">
                <el-input type="password" v-model="dialog.dataForm.password"
                    maxlength="20" clearable />
            </el-form-item>

            <el-form-item label="姓名" prop="realName">
                <el-input v-model="dialog.dataForm.realName" maxlength="10"
                    clearable />
            </el-form-item>
            <el-form-item label="性别" prop="gender">
                <el-select v-model="dialog.dataForm.gender" clearable>
                    <el-option label="男" value="男"></el-option>
                    <el-option label="女" value="女"></el-option>
                </el-select>
            </el-form-item>
            <el-form-item label="电话" prop="mobile">
                <el-input v-model="dialog.dataForm.mobile" maxlength="11"
                    clearable />
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
                <el-input v-model="dialog.dataForm.email" maxlength="200"
                    clearable />
            </el-form-item>
            <el-form-item label="入职日期" prop="hireDate">
                <!-- type="date" 表示日期的格式只需要年月日 -->
                <!-- :editable="false" 表示用户不可输入，只能鼠标操作 -->
                <!-- value-format="YYYY-MM-DD" 表示js代码读取日期时的日期格式 -->
                <el-date-picker v-model="dialog.dataForm.hireDate" type="date"
                    placeholder="选择日期" :editable="false" format="YYYY-MM-DD"
                    value-format="YYYY-MM-DD" />
            </el-form-item>
            <el-form-item label="角色" prop="role">
                <!-- multiple 表示可以多选 -->
                <el-select v-model="dialog.dataForm.role" placeholder="选择角色"
                    class="dialog-input" multiple clearable>
                    <el-option v-for="one in dataForm.roleList" :key="one.roleId"
                        :label="one.roleName" :value="one.roleId"
                        :disabled="one.roleName == '超级管理员'"></el-option>
                </el-select>
            </el-form-item>
            <el-form-item label="部门">
                <el-select v-model="dialog.dataForm.deptId" placeholder="选择部门"
                    class="dialog-input" clearable>
                    <el-option v-for="one in dataForm.deptList" :key="one.deptId"
                        :label="one.deptName" :value="one.deptId" />
                </el-select>
            </el-form-item>
        </el-form>
        <!-- 页脚区 -->
        <template #footer>
            <span class="dialog-footer">
                <!-- 直接将 dialog.visible 设置为false 就隐藏了。 -->
                <el-button @click="dialog.visible = false">取消</el-button>
                <el-button type="primary" @click="dataFormSubmit">确定</el-button>
            </span>
        </template>
    </el-dialog>
</template>

<script lang="ts" setup>
    import { reactive, getCurrentInstance, ref, onMounted, type Ref, nextTick } from 'vue';
    const { proxy } = getCurrentInstance();
    import { dayjs, ElMessage, SCOPE } from 'element-plus';
    import axios from 'axios';
    const dialogForm: Ref = ref(null);

    // 给表单绑定数据
    const dataForm = reactive({
        name: '',
        sex: '',
        role: '',
        deptId: '',
        status: '',
        roleList: [],
        deptList: []
    });

    async function loadRoleList(){
        const response = await axios.get('/meinian-api/mis/role/listRoles');
        const data = response.data;
        dataForm.roleList = data.list;
    }
    // 加载角色列表
    loadRoleList();

    async function loadDeptList(){
        const response = await axios.get('/meinian-api/mis/dept/listDepts');
        const data = response.data;
        dataForm.deptList = data.list;
    }
    // 加载部门列表
    loadDeptList(); 

    // 数据校验规则
    const dataRule = reactive({
        name: [{ required: false, pattern: '^[\u4e00-\u9fa5]{1,10}$', message: '姓名格式错误' }]
    });

    const data = reactive({
        // 为表格准备的数据
        dataList: [],
        // 页码
        pageIndex: 1,
        // 每页显示多少条记录
        pageSize: 10,
        // 总记录条数
        totalCount: 0,
        // 控制是否显示加载动画：true表示显示加载动画，false表示不显示加载动画
        loading: false,
        // 被选中的记录
        selections: []
    });

    // 这种方式也可以
    function selectable(row: any){
        return !row.isSuperAdmin;
    }    

    function selectionChangeHandle(values: any){
        // 这个values EP组件库自动传递过来的最新的选中记录。
        // 将这个数据赋值给我们之前定义的响应式对象。
        data.selections = values;
    }

    function deleteHandle(userId){
        try{
            // 不管是用户是进行单个删除，还是批量删除，将userId封装到数组中，用于提交数据。
            const userIds = userId ? [userId] : data.selections.map(item => {
                return item.userId;
            });
            if(userIds.length == 0){
                proxy.$message({
                    message: '请选择要删除的记录',
                    type: 'warning',
                    duration: 1200
                });
            }else{
                proxy.$confirm('确定要删除选中的记录吗?', '提示', {
                    confirmButtonText: "确定",
                    cancelButtonText: "取消",
                    type: 'warning'
                }).then(async () => {
                    // 发送ajax请求删除数据(发送delete请求和之前的get和post请求有区别。)
                    const response = await axios.delete('/meinian-api/mis/user/remove', {
                        data: {
                            userIds: userIds
                        },
                        headers: {
                            'satoken': localStorage.getItem("token")
                        }
                    });
                    const rows = response.data.rows;
                    if(rows > 0){
                        proxy.$message({
                            message: "操作成功",
                            type: 'success',
                            duration: 1200,
                            onClose: () => {
                                // 重新加载当前分页数据
                                loadPageData();
                            }
                        });
                    }else{
                        proxy.$message({
                            message: "删除失败",
                            type: 'warning',
                            duration: 1200,
                        });
                    }
                });
            }
        }catch{
            proxy.$message({
                message: "删除失败",
                type: 'warning',
                duration: 1200,
            });
        }
    }   
    
    function dismissHandle(userId: Number){
        proxy.$confirm("确定要给当前人员办理离职吗？", "提示", {
            confirmButtonText: "确定",
            cancelButtonText: "取消",
            type: "warning"
        }).then(async () => {
            // 发送ajax请求
            const json = {
                userId: userId
            };
            const config = {
                headers: {
                    'satoken': localStorage.getItem('token')
                }
            };
            const response = await axios.put('/meinian-api/mis/user/dismiss', json, config);
            const rows = response.data.rows;
            if(rows > 0){
                proxy.$message({
                    message: '离职成功',
                    type: 'success',
                    duration: 1200,
                    onClose: () => {
                        // 重新加载分页数据
                        loadPageData();
                    }
                });
            }else{
                proxy.$message({
                    message: '离职失败',
                    type: 'warning',
                    duration: 1200
                });
            }
        });
    }    

    // 分页查询
    const form: Ref = ref(null);
    async function searchHandle() {
        let valid = await form.value.validate();
        if (valid) {
            // 清理验证结果（清除验证错误提示）
            form.value.clearValidate();
            data.pageIndex = 1;
            //加载分页记录
            loadPageData();
        }
    }

    function sizeChangeHandle(pageSize: number) {
        data.pageSize = pageSize;
        data.pageIndex = 1;
        loadPageData();
    }
    function currentChangeHandle(pageNo: number) {
        data.pageIndex = pageNo;
        loadPageData();
    }   

    // 加载分页数据
    async function loadPageData(){
        try{
            // 显示加载进度条
            data.loading = true;

            // 加载用户分页数据
            // 查询条件：从另一个响应式对象 dataForm 上获取数据封装查询条件
            const queryCondition = {
                pageNo: data.pageIndex,
                pageSize: data.pageSize,
                realName: dataForm.name,
                gender: dataForm.sex,
                roleId: dataForm.role,
                deptId: dataForm.deptId,
                userStatus: dataForm.status
            }

            // token放到headers中
            const config = {
                headers: {
                    'satoken': localStorage.getItem("token")
                }
            };

            // 发送ajax post请求
            const response = await axios.post('/meinian-api/mis/user/pageQuery', queryCondition, config);

            // 将响应回来的用户列表数据放到响应式对象的对应属性上
            const records = response.data.pageResult.records;
            for(let record of records){
                if(record.userStatus == 1){
                    record.userStatus = '在职';
                }else if(record.userStatus == 2){
                    record.userStatus = "离职";
                }
            }
            data.dataList = records;
            // 更新总记录条数
            data.totalCount = response.data.pageResult.total;

            // 隐藏加载进度条
            data.loading = false;
        }catch(err){
            console.error("接口请求失败！", err.response || err);
        }
    }

    // 页面打开的时候就加载。
    loadPageData();     

    async function addHandle(){
        dialog.update = false;
        dialog.dataForm.id = null;
        dialog.visible = true;
        await nextTick();
        // 以下代码让数据恢复到初始状态。清空错误提示信息，并且重置表单中的数据
        // el-form 的 resetFields() 方法只会重置那些绑定了 prop 属性并且在 rules 中有对应验证规则的字段。
        // 因此我们的部门不会重置
        dialogForm.value.resetFields();
        // 恢复部门的初始值
        dialog.dataForm.deptId = null;
    }

    // 注意：保存和修改都会走这个回调函数。
    async function dataFormSubmit() {
        try{
            let valid = await dialogForm.value.validate();
            if (valid) {
                // 校验通过准备数据
                let json = {
                    // 这个是给修改准备的，保存不需要提交，不过写上也没关系，因为保存的后台逻辑是：主键自动生成。
                    userId: dialog.dataForm.id,
                    username: dialog.dataForm.username,
                    // 修改不需要传密码，不过传过去也没有关系，修改时，后端form中没有该属性。
                    password: dialog.dataForm.password,
                    realName: dialog.dataForm.realName,
                    gender: dialog.dataForm.gender,
                    mobile: dialog.dataForm.mobile,
                    email: dialog.dataForm.email,
                    hireDate: dialog.dataForm.hireDate,
                    // 下拉列表多选后本身就是一个数组，传给后端之后使用 Integer[] 数组接收。
                    roleIds: dialog.dataForm.role,
                    deptId: dialog.dataForm.deptId,
                };

                const config = {
                    headers: {
                        'satoken': localStorage.getItem('token')
                    }
                };

                // 发送ajax请求：注意，发送的请求可能是保存，也可能是修改。请求路径需要动态。
                const response = await axios.post(`/meinian-api/mis/user/${dialog.dataForm.id == null ? 'save' : 'modify'}`, json, config);
                const result = response.data;
                if (result.rows == 1) {
                    ElMessage({
                        message: '操作成功',
                        type: 'success',
                        duration: 1200,
                        onClose: () => {
                            dialog.visible = false;
                            //重新加载分页数据
                            loadPageData();
                        }
                    });
                } else {
                    ElMessage({
                        message: '操作失败',
                        type: 'error',
                        duration: 1200
                    });
                }
            }
        }catch{
            ElMessage({
                message: '操作失败',
                type: 'error',
                duration: 1200
            });
        }
    }

    async function updateHandle(userId: any){
        dialog.dataForm.id = userId;
        dialog.update = true;

        // 显示弹窗（一定要先渲染窗口，后填充数据）
        dialog.visible = true;
        await nextTick();
        
        // 准备数据
        const json = {
            userId: userId
        };
        // 准备token
        const config = {
            headers: {
                'satoken': localStorage.getItem('token')
            }
        };
        // 发送ajax请求
        const response = await axios.post('/meinian-api/mis/user/findUserById', json, config);
        const result = response.data.result;
        dialog.dataForm.realName = result.realName;
        dialog.dataForm.gender = result.gender;
        dialog.dataForm.mobile = result.mobile;
        dialog.dataForm.email = result.email;
        dialog.dataForm.hireDate = result.hireDate;
        // 将后端返回的json字符串转换成对象。
        dialog.dataForm.role = JSON.parse(result.roleIds);
        dialog.dataForm.deptId = result.deptId;
    }    

    const dialog = reactive({
        // 控制模态窗口显示/隐藏
        visible: false, 
        // 区分是修改还是新增的标记
        update: false,
        // 给表单绑定数据
        dataForm: {
            // 新增时不需要id，修改时需要id
            id: null,
            username: null,
            // 新增员工时需要密码框，修改时不需要密码框，因为密码只能员工自己修改
            password: null,
            realName: null,
            gender: null,
            mobile: null,
            email: null,
            // 入职日期希望有默认值，使用了dayjs库
            hireDate: dayjs(new Date()).format('YYYY-MM-DD'),
            // 对应选中的角色
            role: null,
            // 对应选中的部门
            deptId: null,
            // 默认值1表示在职
            userStatus: 1
        },
        dataRule: {
            // 校验规则
            username: [
                { required: true, message: '用户名不能为空' },
                { pattern: '^[a-zA-Z0-9]{5,20}$', message: '用户名格式错误' }
            ],
            password: [
                { required: true, message: '密码不能为空' },
                { pattern: '^[a-zA-Z0-9]{6,20}$', message: '密码格式错误' }
            ],
            realName: [
                { required: true, message: '姓名不能为空' },
                { pattern: '^[\u4e00-\u9fa5]{2,10}$', message: '姓名格式错误' }
            ],
            gender: [{ required: true, message: '性别不能为空' }],
            mobile: [{ required: true, message: '电话不能为空' }, { pattern: '^1[1-9]\\d{9}$', message: '电话格式错误' }],
            email: [
                { required: true, message: '邮箱不能为空' },
                { pattern: '^([a-zA-Z]|[0-9])(\\w|\\-)+@[a-zA-Z0-9]+\\.([a-zA-Z]{2,4})$', message: '邮箱格式错误' }
            ],
            hireDate: [{ required: true, message: '入职日期不能为空' }],
            role: [{ required: true, message: '角色不能为空' }],
            userStatus: [{ required: true, message: '状态不能为空' }]
        }
    });


    
</script>

<style lang="less" scoped>
    .dialog-input {
        width: 100% !important;
    }
</style>