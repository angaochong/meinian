<template>
    <div v-if="proxy.isAuth(['ROOT', 'GOODS:SELECT'])">
        <el-form :inline="true" :model="dataForm" :rules="dataRule" ref="form">
            <el-form-item prop="packageName">
                <el-input v-model="dataForm.packageName" placeholder="套餐名称"
                    maxlength="50" class="input" clearable />
            </el-form-item>
            <el-form-item prop="packageCode">
                <el-input v-model="dataForm.packageCode" placeholder="套餐编号"
                    class="input" maxlength="20" clearable />
            </el-form-item>
            <el-form-item>
                <el-select v-model="dataForm.packageType" class="input"
                    placeholder="类别" clearable>
                    <el-option label="父母体检" value="父母体检" />
                    <el-option label="入职体检" value="入职体检" />
                    <el-option label="职场白领" value="职场白领" />
                    <el-option label="个人高端" value="个人高端" />
                    <el-option label="中青年体检" value="中青年体检" />
                </el-select>
            </el-form-item>
            <el-form-item>
                <el-select v-model="dataForm.categoryId" class="input"
                    placeholder="展示区" clearable>
                    <el-option label="活动专区" value="1" />
                    <el-option label="热卖套餐" value="2" />
                    <el-option label="新品推荐" value="3" />
                    <el-option label="孝敬父母" value="4" />
                    <el-option label="白领精英" value="5" />
                </el-select>
            </el-form-item>
            <el-form-item>
                <el-button type="primary" @click="searchHandle()">查询</el-button>
                <el-button type="primary"
                    :disabled="!proxy.isAuth(['ROOT', 'GOODS:INSERT'])"
                    @click="addHandle()">
                    新增
                </el-button>
                <el-button type="danger"
                    :disabled="!proxy.isAuth(['ROOT', 'GOODS:DELETE'])"
                    @click="deleteHandle()">
                    批量删除
                </el-button>
            </el-form-item>
            <el-form-item class="mold">
                <el-radio-group v-model="dataForm.statusLabel"
                    @change="searchHandle()">
                    <el-radio-button label="全部"></el-radio-button>
                    <el-radio-button label="已上架"></el-radio-button>
                    <el-radio-button label="已下架"></el-radio-button>
                </el-radio-group>
            </el-form-item>
        </el-form>
    </div>
    <el-table :data="data.dataList" :header-cell-style="{'background':'#f5f7fa'}" border v-loading="data.loading" @selection-change="selectionChangeHandle">
        <el-table-column type="selection" header-align="center" align="center" width="50" :selectable="selectable" />
        <el-table-column type="index" header-align="center" align="center" width="100" label="序号">
            <template #default="scope">
                <span>{{ (data.pageIndex - 1) * data.pageSize + scope.$index + 1 }}</span>
            </template>
        </el-table-column>
        <el-table-column prop="packageName" header-align="left" align="left" min-width="250" label="套餐名称" />
        <el-table-column prop="packageCode" header-align="left" align="left" min-width="130" label="套餐编号" />
        
        <el-table-column header-align="center" align="center" min-width="80" label="现价">
            <!--如果你要自定义单元格中的内容，就需要单独编写下面的 template 标签-->
            <template #default="scope">
                <span>￥{{ scope.row.currentPrice }}</span>
            </template>
        </el-table-column>
        
        <el-table-column header-align="center" align="center" min-width="100" label="原价">
            <template #default="scope">
                <span>￥{{ scope.row.originalPrice }}</span>
            </template>
        </el-table-column>
        <el-table-column prop="ruleName" header-align="center" align="center" min-width="100" label="促销方案" />
        <el-table-column prop="salesVolume" header-align="center" align="center" min-width="100" label="销量" />
        <el-table-column prop="packageType" header-align="center" align="center" min-width="100" label="类型" />
        <el-table-column heaFseder-align="center" align="center" min-width="100" label="体检内容">
            <template #default="scope">
                <span :class="scope.row.hasDocument ? 'link-blue' : 'link-red'" @click="documentHandle(scope.row.id, scope.row.hasDocument)">
                    {{ scope.row.hasDocument ? '有文档' : '无文档' }}
                </span>
            </template>
        </el-table-column>
        <el-table-column prop="status" header-align="center" align="center"
            min-width="80" label="状态">
            <template #default="scope">
                <!-- v-model="scope.row.status" 为true表示上架，false表示下架 -->
                <!-- active-text="上架" 开启时显示上架-->
                <!-- inactive-text="下架" 关闭时显示下架-->
                <!-- style="--el-switch-on-color: #13ce66; --el-switch-off-color: #ff4949;" 开启时绿色，关闭时红色-->
                <!-- :disabled="!scope.row.hasCheckup" 只有当 hasCheckup 为 true 时开关才可用-->
                <!-- inline-prompt 控制文本显示的位置和方式，这个配置表示文本显示在开关的内部，如果不写它，文本显示在开关的外面-->
                <el-switch v-model="scope.row.status" inline-prompt
                    style="--el-switch-on-color: #13ce66; --el-switch-off-color: #ff4949;"
                    active-text="上架" inactive-text="下架"
                    :disabled="!scope.row.hasDocument"
                    @change="changeSwitchHandle(scope.row.id, scope.row.status)" />
            </template>
        </el-table-column>
        <el-table-column header-align="center" align="center" width="150" label="操作">
            <template #default="scope">
                <el-button type="text" :disabled="!scope.row.status"  @click="viewHandle(scope.row.id)">
                    预览
                </el-button>
                <el-button type="text"
                    v-if="proxy.isAuth(['ROOT', 'GOODS:UPDATE'])"
                    :disabled="scope.row.status"
                    @click="updateHandle(scope.row.id)">
                    修改
                </el-button>    
                <el-button type="text"
                    v-if="proxy.isAuth(['ROOT', 'GOODS:DELETE'])"
                    :disabled="scope.row.salesVolume > 0 || scope.row.status"
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

    <el-dialog :title="!goodsDialog.dataForm.id ? '新增' : '修改'"
        v-if="proxy.isAuth(['ROOT', 'GOODS:INSERT', 'GOODS:UPDATE'])"
        :close-on-click-modal="false" v-model="goodsDialog.visible"
        width="750px">
        <el-form :model="goodsDialog.dataForm" ref="dialogForm" :rules="goodsDialog.dataRule" label-width="80px">
            <el-form-item label="套餐名称" prop="packageName">
            <el-input v-model="goodsDialog.dataForm.packageName" maxlength="50" clearable />
            </el-form-item>
            <el-form-item label="套餐编号" prop="packageCode">
            <el-input v-model="goodsDialog.dataForm.packageCode" maxlength="20" clearable />
            </el-form-item>
            <el-form-item label="简介信息" prop="description">
            <el-input type="textarea" v-model="goodsDialog.dataForm.description" :rows="4" maxlength="200" clearable />
            </el-form-item>
            <el-form-item label="套餐原价" prop="originalPrice">
            <el-input v-model="goodsDialog.dataForm.originalPrice" placeholder="输入原价" class="price" maxlength="20" clearable>
                <template #append>
                元
                </template>
            </el-input>
            <span class="desc">提示：价格精确到分（小数点后两位）</span>
            </el-form-item>
            <el-form-item label="套餐现价" prop="currentPrice">
            <el-input v-model="goodsDialog.dataForm.currentPrice" placeholder="输入现价" class="price" maxlength="20" clearable>
                <template #append>
                元
                </template>
            </el-input>
            <span class="desc">提示：价格精确到分（小数点后两位）</span>
            </el-form-item>
            <el-form-item label="折扣列表">
            <el-select v-model="goodsDialog.dataForm.promotionId" placeholder="选择折扣信息" clearable>
                <el-option :label="one.ruleName" :value="one.ruleId" v-for="one in goodsDialog.ruleList" />
            </el-select>
            </el-form-item>
            <el-form-item label="封面图片" prop="coverImage">
            <!--EP组件库底层自动发送请求，不需要我们发请求：用户点击 → 弹出文件选择框 → 选择文件 → 自动发送POST请求-->
            <!-- :action="goodsDialog.upload.action" 指定上传时的请求路径 -->
            <!-- :headers="goodsDialog.upload.headers" 上传请求头（通常用于身份验证，如token） -->
            <!-- :data="goodsDialog.upload.data" 上传附加数据（如业务参数） -->
            <!-- :show-file-list="false" 不显示文件列表，适用于单文件上传，如果显示的话：每个文件会显示文件名、大小、状态 -->
            <!-- accept=".jpg,.jpeg" 只接受jpg和jpeg格式的图片 -->
            <!-- :on-success="imageUploadSuccess" 上传成功回调函数 -->
            <!-- :before-upload="imageBeforeUpload" 上传前校验函数（可用于文件类型、大小校验） -->
            <!-- :on-error="imageUploadError" 上传失败回调函数 -->
            <el-upload class="image-uploader"
                :action="goodsDialog.upload.action"
                :headers="goodsDialog.upload.headers"
                :data="goodsDialog.upload.data" :show-file-list="false"
                accept=".jpg,.jpeg" :on-success="imageUploadSuccess"
                :before-upload="imageBeforeUpload"
                :on-error="imageUploadError">
                <!-- 如果已上传图片，显示预览图 -->
                <img v-if="goodsDialog.imageUrl" :src="goodsDialog.imageUrl" class="image" />
                <!-- 未上传图片时显示上传图标 -->
                <el-icon v-else class="image-uploader-icon"><Plus /></el-icon>
            </el-upload>
            </el-form-item>
            <el-form-item label="套餐类别" prop="packageType">
                <el-select v-model="goodsDialog.dataForm.packageType" placeholder="检查类别" clearable>
                    <el-option label="父母体检" value="父母体检" />
                    <el-option label="入职体检" value="入职体检" />
                    <el-option label="职场白领" value="职场白领" />
                    <el-option label="个人高端" value="个人高端" />
                    <el-option label="中青年体检" value="中青年体检" />
                </el-select>
            </el-form-item>
            <el-form-item label="特征标签">
                <div class="tag-row">
                    <el-input class="tag-input" v-model="goodsDialog.newTag" @keyup.enter="enterTag" clearable />
                    <span class="desc">提示：输入标签后按回车键</span>
                </div>
                <div class="tags">
                    <el-tag v-for="one in goodsDialog.dataForm.tags" closable :disable-transitions="false" @close="closeTag(one)">
                        {{ one }}
                    </el-tag>
                </div>
            </el-form-item>
            <el-form-item label="展示区" prop="categoryId">
                <el-select v-model="goodsDialog.dataForm.categoryId" placeholder="选择展示区" clearable>
                    <el-option label="活动专区" value="1" />
                    <el-option label="热卖套餐" value="2" />
                    <el-option label="新品推荐" value="3" />
                    <el-option label="孝敬父母" value="4" />
                    <el-option label="白领精英" value="5" />
                </el-select>
            </el-form-item>
            <el-form-item label="体检内容">
                <!-- 
                商品体检项目动态表单行
                功能：循环渲染体检项目配置，每行包含类别选择、项目名称、内容和删除操作
                结构说明：
                    - :gutter="10" 列间距10像素
                    - v-for 遍历goodsDialog.item数组，动态生成多个配置行
                    - :key="$index" 使用索引作为key
                
                el-col 列布局(EP的栅格布局，EP规定一行共24个栅格：把一行等分成24份)：
                    - 第1列(span=6，6就是表示占6份)：检查类别下拉选择，包含科室/实验室/医技/其他检查
                    - 第2列(span=6)：体检项目名称输入框，最多50字符
                    - 第3列(span=11)：体检内容详细说明输入框，最多500字符  
                    - 第4列(span=1)：删除按钮，点击移除当前行配置
                -->
                <el-row :gutter="10" class="item-row" v-for="(one, $index) in goodsDialog.item" :key="$index">
                    <el-col :span="6">
                        <el-select v-model="one.type" placeholder="检查类别" clearable>
                            <el-option label="科室检查" value="科室检查" />
                            <el-option label="实验室检查" value="实验室检查" />
                            <el-option label="医技检查" value="医技检查" />
                            <el-option label="其他检查" value="其他检查" />
                        </el-select>
                    </el-col>
                    <el-col :span="6">
                        <el-input v-model="one.title" placeholder="体检项目" maxlength="50" clearable />
                    </el-col>
                    <el-col :span="11">
                        <el-input v-model="one.content" placeholder="体检内容" maxlength="500" clearable />
                    </el-col>
                    <el-col :span="1">
                        <el-button type="primary" :icon="Delete" @click="deleteItem($index)" />
                    </el-col>
                </el-row>
            </el-form-item>
        </el-form>
        <template #footer>
            <span class="dialog-footer">
                <el-button type="danger" @click="addItem">添加项目</el-button>
                <el-button @click="goodsDialog.visible=false">取消</el-button>
                <el-button type="primary" @click="dataFormSubmit">确定</el-button>
            </span>
        </template>
    </el-dialog>  
    
    <el-dialog title="提示信息"
        v-if="proxy.isAuth(['ROOT', 'GOODS:INSERT', 'GOODS:UPDATE'])"
        v-model="documentDialog.visible" width="350px">
        <div class="message-content">
            <el-icon :size="18" class="icon">
                <WarningFilled />
            </el-icon>
            <p>请您选择【上传】或者【下载】体检内容文档？如果未上传体检内容文档，则体检套餐将无法上架。</p>
        </div>
            <!-- 
            1. 上传功能：
                - 使用el-upload组件实现Excel文件上传
                - 上传地址：documentDialog.upload.action
                - 附加数据：documentDialog.data
                - 请求头：documentDialog.upload.headers（通常包含认证信息）
                - 只接受.xlsx格式文件
                - 上传前校验：documentBeforeUpload（校验文件格式、大小等）
                - 上传成功回调：documentUploadSuccess（处理上传结果）
                - 上传失败回调：documentUploadError（错误处理）
                - 显示为带Upload图标的成功样式按钮
            
            2. 下载功能：
                - 使用el-button实现文件下载
                - 带Download图标的主样式按钮
                - 禁用条件：!documentDialog.data.hasCheckup（未通过审核时禁用下载）
                - 点击事件：documentDownloadHandle（触发下载操作）
            -->
        <template #footer>
            <span class="document-dialog-footer">
                <el-upload :action="documentDialog.upload.action"
                    :data="documentDialog.data" :show-file-list="false"
                    :headers="documentDialog.upload.headers" accept=".xlsx"
                    :before-upload="documentBeforeUpload"
                    :on-success="documentUploadSuccess"
                    :on-error="documentUploadError">
                    <el-button type="success" :icon="Upload"
                        class="uploadBtn">上传</el-button>
                </el-upload>
                <el-button type="primary" :icon="Download"
                    class="downloadBtn"
                    :disabled="!documentDialog.data.hasDocument"
                    @click="documentDownloadHandle">
                    下载
                </el-button>
            </span>
        </template>
    </el-dialog>
    
</template>

<script lang="ts" setup>
    import { reactive, getCurrentInstance, ref, onMounted, type Ref, nextTick } from 'vue';
    //引入ElementPlus组件库中的三个图标，要给按钮设置图标
    import { Delete, Download, Upload } from '@element-plus/icons-vue';
    import router from '../../router/index';
import axios from 'axios';
import { ElMessage, type UploadFile, type UploadFiles } from 'element-plus';
    const { proxy } = getCurrentInstance();

    const dataForm = reactive({
        packageName: null,
        packageCode: null,
        packageType: null,
        categoryId: null,
        statusLabel: '全部',
        status: null
    });

    const dataRule = reactive({
        packageName: [{ pattern: '^[a-zA-Z0-9\u4e00-\u9fa5]{1,50}$', message: '关键字内容不正确' }],
        packageCode: [{ min: 6, message: '编号不能少于6个字符' }, { pattern: '^[a-zA-Z0-9]{6,20}$', message: '编号格式错误' }]
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
        // 显示页面加载进度条
        data.loading = true;

        // 准备数据
        if(dataForm.statusLabel == '全部'){
            dataForm.status = null;
        }else if(dataForm.statusLabel == '已上架'){
            dataForm.status = true;
        }else{
            dataForm.status = false;
        }

        const json = {
            packageName: dataForm.packageName,
            packageCode: dataForm.packageCode,
            packageType: dataForm.packageType,
            categoryId: dataForm.categoryId,
            status: dataForm.status,
            pageNo: data.pageIndex,
            pageSize: data.pageSize
        };

        // 准备配置
        const config = {
            headers: {
                satoken: localStorage.getItem('token')
            }
        };
        // 发送ajax请求
        const response = await axios.post('/meinian-api/mis/goods/pageQuery', json, config);
        // 获取响应的数据
        const pageResult = response.data.pageResult;
        // 填充 dataList
        data.dataList = pageResult.records;
        // 把数据库返回的数字1和0转换为true和false。因为前端页面需要布尔值。
        for(let one of data.dataList){
            let goods = one as any;
            goods.status = goods.status ? true : false
        }
        // 更新总记录条数
        data.totalCount = pageResult.total;
        // 隐藏加载进度条
        data.loading = false;
    }

    // 页面第一次渲染时，就加载第一页数据。
    loadPageData();    

    // 点击 查询 或者 点击 全部/已上架/已下架 执行的回调函数。
    function searchHandle(){
        // 1. 校验表单数据
        proxy.$refs['form'].validate((valid: any) => {
            if(valid){
                // 清空之前的错误提示信息
                proxy.$refs['form'].clearValidate();
                // 2. 将页码定位到第一页
                data.pageIndex = 1;
                // 3. 分页查询
                loadPageData();
            }else{
                return false;
            }
        })
    }    

    function sizeChangeHandle(val: any){
        data.pageIndex = 1;
        data.pageSize = val;
        loadPageData();
    }

    function currentChangeHandle(val: any){
        data.pageIndex = val;
        loadPageData();
    }

    function imageBeforeUpload(file: any) {
        let size = file.size / 1024 / 1024;
        if (size > 2) {
            proxy.$message({
                message: '图片不能超过2M大小',
                type: 'error',
                duration: 1200
            });
            return false;
        }
        return true;
    }

    function imageUploadSuccess(resp : any, uploadFile : UploadFile, UploadFiles : UploadFiles){
        if(resp.msg = 'success'){
            let path = resp.result;
            //往数据库存储的地址，相对路径
            goodsDialog.dataForm.coverImage = path;
            //上传窗口的图片，绝对路径
            goodsDialog.imageUrl = `${proxy.$minioUrl}${path}`;
            dialogForm.value.clearValidate(["coverImage"]);
        }
    }

    function imageUploadError(e) {
        proxy.$message({
            message: "图片上传失败",
            type: 'error',
            duration: 1200
        });
        console.error(e)
    }


    const goodsDialog = reactive({
        visible: false,
        newTag: null,
        item: [{}],
        imageUrl: null,
        ruleList: [],
        dataForm: {
            id: null,
            packageName: null,
            packageCode: null,
            description: null,
            originalPrice: null,
            currentPrice: null,
            promotionId: null,
            coverImage: null,
            packageType: null,
            tags: [],
            categoryId: null
        },
        upload: {
            action: '/meinian-api/mis/goods/uploadImage',
            headers: {
                'satoken': localStorage.getItem('token')
            },
            data: {
                id: null
            },
        },

        dataRule: {
            packageName: [
                { required: true, message: '名称不能为空' },
                { min: 2, message: '名称不能少于2个字符' },
                { pattern: '^[a-zA-Z0-9\u4e00-\u9fa5]{2,50}$', message: '名称格式错误' }
            ],
            packageCode: [
                { required: true, message: '编号不能为空' },
                { min: 6, message: '编号不能少于6个字符' },
                { pattern: '^[a-zA-Z0-9]{6,20}$', message: '编号格式错误' }
            ],
            description: [{ required: true, message: '简介信息不能为空' }],
            originalPrice: [
                {
                    required: true,
                    message: '价格不能为空'
                },
                {
                    pattern: '(^[1-9]([0-9]+)?(\\.[0-9]{1,2})?$)|(^(0){1}$)|(^[0-9]\\.[0-9]([0-9])?$)',
                    message: '价格不正确'
                }
            ],
            currentPrice: [
                {
                    required: true,
                    message: '价格不能为空'
                },
                {
                    pattern: '(^[1-9]([0-9]+)?(\\.[0-9]{1,2})?$)|(^(0){1}$)|(^[0-9]\\.[0-9]([0-9])?$)',
                    message: '价格不正确'
                }
            ],
            coverImage: [
                {
                    required: true,
                    message: '没有上传封面图片'
                }
            ],
            packageType: [
                {
                    required: true,
                    message: '没有选择套餐类别'
                }
            ]
        }
    });    

    // 加载折扣列表
    async function loadRuleList(){
        // 配置信息
        const config = {
            headers: {
                satoken: localStorage.getItem('token')
            }
        };
        // 发送ajax get请求
        const response = await axios.get('/meinian-api/mis/rule/list', config);
        const result = response.data.result;
        // 获取响应数据并设置到响应对象的属性上
        goodsDialog.ruleList = result;
    }
    
    const documentDialog = reactive({
        visible: false, // 记得调试完改成false
        upload: {
            action: '/meinian-api/mis/goods/uploadExamItems',
            headers: {
                'satoken': localStorage.getItem('token')
            }
        },
        data: {
            id: null,
            hasCheckup: null
        }
    });    

    const dialogForm: Ref = ref(null);
    async function addHandle(){
        goodsDialog.visible = true;
        // 等页面显示出来
        await nextTick();
        // 恢复初始值
        dialogForm.value.resetFields();
        // 新增时id肯定是要置空的
        goodsDialog.dataForm.id = null;
    
        // 看看页面上有什么残留，就清空哪个
        goodsDialog.dataForm.promotionId = null;
        goodsDialog.imageUrl = null;
        goodsDialog.dataForm.coverImage = null;
        //解决特征标签输入框里的文字残留
        goodsDialog.newTag = null;
        goodsDialog.dataForm.tags = [];
        goodsDialog.item = [{}];
    
        // 加载折扣列表
        loadRuleList();
    }

    function enterTag() {
        //判断文本框输入的内容是不是空值或者空字符串
        if (goodsDialog.newTag != null && goodsDialog.newTag != '') {
            //判断newTag数组是否有文本框输入的内容相同的元素，如果有就不需要创建新的Tag控件
            if (goodsDialog.dataForm.tags.includes(goodsDialog.newTag)) {
                proxy.$message({
                    message: '不能添加重复标签',
                    type: 'warning',
                    duration: 1200
                });
            } else {
                goodsDialog.dataForm.tags.push(goodsDialog.newTag);
                //清空文本框内容
                goodsDialog.newTag = null;
            }
        }
    }

    function closeTag(tag) {
        //计算要关闭的Tag是数组中的哪个元素
        let i = goodsDialog.dataForm.tags.indexOf(tag);
        //删除数组中某个元素
        goodsDialog.dataForm.tags.splice(i, 1);
    }

    function addItem(){
        goodsDialog.item.push({});
    }

    function deleteItem(index){
        if(goodsDialog.item.length == 1){
            proxy.$message({
                message: '不能全部删除',
                type: 'error',
                duration: 1200
            });
        }else{
            goodsDialog.item.splice(index, 1);
        }
    }

    async function dataFormSubmit() {
        // 校验表单数据（异步）
        let ok = await dialogForm.value.validate();
        if(ok){
            try{
                // 转换体检内容为最终要提交的数据
                let departmentExam: any = [];
                let labExam: any = [];
                let medicalExam: any = [];
                let otherExam: any = [];
                let items = goodsDialog.item;
                for(let one of items){
                    let goods = one as any;
                    if(goods.type == undefined || goods.type == ""){
                        ElMessage({
                            type: "error",
                            message: "体检类别不能为空",
                            duration: 1200
                        });
                        return;
                    }
                    if(goods.title == undefined || goods.title == ""){
                        ElMessage({
                            type: "error",
                            message: "体检项目不能为空",
                            duration: 1200
                        });
                        return;
                    }
                    if(goods.content == undefined || goods.content == ""){
                        ElMessage({
                            type: "error",
                            message: "体检内容不能为空",
                            duration: 1200
                        });
                        return;
                    }
                    if(goods.type == "科室检查"){
                        departmentExam.push({title: goods.title,content:goods.content});
                    }else if(goods.type == "实验室检查"){
                        labExam.push({title: goods.title,content:goods.content});
                    }else if(goods.type == "医技检查"){
                        medicalExam.push({title: goods.title,content:goods.content});
                    }else if(goods.type == "其他检查"){
                        otherExam.push({title: goods.title,content:goods.content});
                    }
                }
                // 准备数据
                let sendData = {
                    id: goodsDialog.dataForm.id,
                    packageCode: goodsDialog.dataForm.packageCode,
                    packageName: goodsDialog.dataForm.packageName,
                    description: goodsDialog.dataForm.description,
                    departmentExam: departmentExam,
                    labExam: labExam,
                    medicalExam: medicalExam,
                    otherExam: otherExam,
                    coverImage: goodsDialog.dataForm.coverImage,
                    originalPrice: goodsDialog.dataForm.originalPrice,
                    currentPrice: goodsDialog.dataForm.currentPrice,
                    packageType: goodsDialog.dataForm.packageType,
                    tags: goodsDialog.dataForm.tags,
                    categoryId: goodsDialog.dataForm.categoryId,
                    promotionId: goodsDialog.dataForm.promotionId
                };
                // 准备token
                let config = {
                    headers: {
                        satoken: localStorage.getItem("token")
                    }
                };
                // 发送ajax post请求
                let response = await axios.post(`/meinian-api/mis/goods/${goodsDialog.dataForm.id ? "modify":"save"}`, sendData, config);
                if(response.data.rows == 1){
                    ElMessage({
                        type: "success",
                        message: "操作成功",
                        duration: 1200
                    });
                    // 隐藏modal窗口
                    goodsDialog.visible = false;
                    // 重新加载分页数据
                    loadPageData();
                }else{
                    ElMessage({
                        type: "error",
                        message: "操作失败",
                        duration: 1200
                    });
                }
            }catch(e){
                console.log(e);
                ElMessage({
                    type: "error",
                    message: "操作失败",
                    duration: 1200
                });
            }
        }
    }

    
   // 弹出修改的modal窗口
    async function updateHandle(id){

        // 清空newTag
        goodsDialog.newTag = '';
        goodsDialog.item = [];

        // 设置id
        goodsDialog.dataForm.id = id;
        // 渲染窗口
        goodsDialog.visible = true;
        // 等待渲染完毕
        await nextTick();
        
        // 发送ajax请求，获取数据
        let response = await axios.post("/meinian-api/mis/goods/findById", {id}, {
            headers: {
                satoken: localStorage.getItem("token")
            }
        });

        // 获取返回的数据
        let result = response.data.result;

        // 加载折扣列表
        loadRuleList();

        // 填充数据
        goodsDialog.dataForm.packageName = result.packageName;
        goodsDialog.dataForm.packageCode = result.packageCode;
        goodsDialog.dataForm.description = result.description;
        goodsDialog.dataForm.originalPrice = result.originalPrice;
        goodsDialog.dataForm.currentPrice = result.currentPrice;
        goodsDialog.dataForm.currentPrice = result.currentPrice;
        goodsDialog.dataForm.promotionId = result.ruleId;
        goodsDialog.dataForm.coverImage = result.coverImage;
        goodsDialog.imageUrl = `${proxy.$minioUrl}${result.coverImage}`;
        goodsDialog.dataForm.packageType = result.packageType;
        goodsDialog.dataForm.tags = result.tags ? result.tags : [];
        goodsDialog.dataForm.categoryId = result.categoryId ? result.categoryId + "" : null;
        
        // 把数据库查询回来的体检内容转换成前端的item数组。
        let temp = {
                departmentExam: "科室检查",
                labExam: "实验室检查",
                medicalExam: "医技检查",
                otherExam: "其他检查"
        };
        for(let key in temp){
                if(result.hasOwnProperty(key)){
                    for(let one of result[key]){
                        goodsDialog.item.push({type: temp[key], title: one.title, content: one.content});
                    }
                }
        }

    }

    function documentHandle(id, hasDocument) {
        documentDialog.data = {
            // id是最终要传递给后端的，这是套餐id
            id: id,
            // 这个也会提交给后端，但后端form里没有写对应属性，因此也不用担心后端能接收到。
            // 这个作用是来控制下载按钮是否可用的。如果没有文档，说明用户没有上传过，自然也下载不了。
            hasDocument: hasDocument
        };
        documentDialog.visible = true;
    }

    function documentBeforeUpload(file) {
        let size = file.size / 1024 / 1024;
        if (size > 20) {
            proxy.$message({
                message: '文件不能超过20M大小',
                type: 'error',
                duration: 1200
            });
            return false;
        }
        return true;
    }

    function documentUploadSuccess() {
        documentDialog.visible = false;
        proxy.$message({
            message: '文件上传成功',
            type: 'success',
            duration: 1200,
            onClose: () => {
                loadPageData()
            }
        });
    }

    function documentUploadError(e) {
        proxy.$message({
            message: "文件上传失败",
            type: 'error',
            duration: 1200
        });
        console.error(e);
    }

    async function documentDownloadHandle() {
        const response = await axios.post('/meinian-api/mis/goods/downloadExamItems', {
            id: documentDialog.data.id
        }, {
            responseType: 'blob', // 必须写上，告诉axios，响应的内容类型是blob 二进制数据
            headers: {
                satoken: localStorage.getItem('token')
            }
        });
        
        // 直接使用后端返回的文件名
        const blobUrl = URL.createObjectURL(new Blob([response.data]));
        const link = document.createElement('a');
        link.href = blobUrl;
        link.download = `${documentDialog.data.id}.xlsx`; // 后端会返回这个文件名
        link.click();
        // 释放资源
        setTimeout(() => URL.revokeObjectURL(blobUrl), 100);
        // 隐藏弹窗
        setTimeout(() => documentDialog.visible = false, 1000);
    }

    async function changeSwitchHandle(id, status) {
        // 准备数据
        const sendData = {
            id: id,
            status: status
        };
        // 准备配置
        const config = {
            headers:{
                satoken: localStorage.getItem('token')
            }
        }
        // 发送ajax post请求
        const response = await axios.post('/meinian-api/mis/goods/modifyStatus', sendData, config);
        const result = response.data.result;
        // 提示成功信息
        if(result){
            proxy.$message({
                message: '操作成功',
                type: 'success',
                duration: 1200
            });
        }
    }

    function selectable(row, index) {
        if (row.salesVolume > 0 || row.status == true) {
            return false;
        }
        return true;
    }

    function selectionChangeHandle(val){
        data.selections = val
    }

    function deleteHandle(id) {
        // 如果这个函数的 id 参数有值，表示用户删除一个，如果该函数的id参数没有值，表示批量删除。
        let ids = id ? [id] : data.selections.map(item => { return item.id; });
        if (ids.length == 0) {
            proxy.$message({
                message: '没有选中记录',
                type: 'warning',
                duration: 1200
            });
        } else {
            proxy.$confirm(`确定要删除选中的记录？`, '提示', { 
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(async () => {
                const json = { ids: ids };
                const config = {
                    headers:{
                        satoken: localStorage.getItem('token')
                    }
                };
                const response = await axios.post('/meinian-api/mis/goods/removeByIds', json, config);
                const rows = response.data.rows;
                if (rows > 0) {
                    proxy.$message({
                        message: `刪除了${rows}条记录`,
                        type: 'success',
                        duration: 1200,
                        onClose: () => {
                            loadPageData();
                        }
                    });
                } else {
                    proxy.$message({
                        message: '未能删除记录',
                        type: 'warning',
                        duration: 1200
                    });
                }
            });
        }
    }
    
    function viewHandle(id){
        // 在当前页面中显示
        router.push({
            name: 'FrontGoods',
            params: {
                id: id
            }
        });
        // 在新窗口中显示
        /*
        const route = router.resolve({
            name: "FrontGoods",
            params: {
                id: id
            }
        });
        window.open(route.href, "_blank");
        */
    }
</script>

<style lang="less" scoped>
    @import url('goods.less');
</style>