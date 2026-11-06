import { createApp } from 'vue';
import App from './App.vue';

const app = createApp(App);

// 导入路由配置
import router from './router';
app.use(router);

// 导入SVG依赖库，可以在页面上显示SVG图片
import 'virtual:svg-icons-register';

// 引用ElementPlus的CSS文件，否则MacOS系统会出现控件丢失样式
import 'element-plus/dist/index.css';

// 导入ElementUI组件库
import ElementPlus from 'element-plus';

// 让日历控件显示中文，导入简体中文场景
import zhCn from 'element-plus/es/locale/lang/zh-cn';

// 将ElementUI组件库整合到Vue对象上
app.use(ElementPlus, { locale: zhCn });

// 使用ElementPlus自带的图标库
import * as ElementPlusIconsVue from '@element-plus/icons-vue';
for(const [key, component] of Object.entries(ElementPlusIconsVue)){
  app.component(key, component);
}

// permissions 参数是什么？需要的权限列表（当前用户至少拥有权限列表当中的1个权限，即可访问）
// permissionsJson 是什么？当前用户自己拥有的权限列表。json字符串。
app.config.globalProperties.isAuth = function(permissions: string[]){
  try{
    let permissionsJson = localStorage.getItem('permissions');
    if(permissionsJson){ // 如果从本地存储中获取不到任何权限，不用说了，直接返回false。
      // 把json字符串转换成对象（对象是一个数组）
      const localPermissions = JSON.parse(permissionsJson);
      for(let mustPermission of permissions){
        if(localPermissions.includes(mustPermission)){
          return true;
        }
      }
    }
    return false;
  }catch{
    // 出现错误就是没有权限。
    return false;
  }
}

app.config.globalProperties.$minioUrl = 'http://192.168.91.200:9000/meinian-his/';

// 配置WebSocket
import VueNativeSock from 'vue-native-websocket-vue3';

app.use(VueNativeSock, 'ws://localhost:8080/meinian-api/websocket/push/message', {
    format: 'json',
    // 如果WebSocket连接长时间不收发请求，会被服务端切断连接，下面的设置可以自动重连
    reconnection: true
});

import html2Canvas from 'html2canvas';
import jsPDF from 'jspdf';

function isSplit(nodes, index, pageHeight) {
    // 计算当前这块dom是否跨越了a4大小，以此分割
    if (
        nodes[index].offsetTop + nodes[index].offsetHeight < pageHeight &&
        nodes[index + 1] &&
        nodes[index + 1].offsetTop + nodes[index + 1].offsetHeight > pageHeight
    ) {
        return true;
    }
    return false;
}

app.config.globalProperties.getPdf = function () {
    var title = this.htmlTitle; //PDF标题
    let ST = document.documentElement.scrollTop || document.body.scrollTop;
    let SL = document.documentElement.scrollLeft || document.body.scrollLeft;
    document.documentElement.scrollTop = 0;
    document.documentElement.scrollLeft = 0;
    document.body.scrollTop = 0;
    document.body.scrollLeft = 0;
    //获取滚动条的位置并赋值为0，因为是el-dialog弹框，并且内容较多出现了纵向的滚动条,截图出来的效果只能截取到视图窗口显示的部分,超出窗口部分则无法生成。所以先将滚动条置顶
    const A4_WIDTH = 592.28;
    const A4_HEIGHT = 841.89;
    let imageWrapper = document.querySelector('#pdfDom'); // 获取DOM
    var title = imageWrapper.getAttribute('name'); //PDF标题
    let pageHeight = (imageWrapper.scrollWidth / A4_WIDTH) * A4_HEIGHT;
    let lableListID = imageWrapper.querySelectorAll('p');
    // 进行分割操作，当dom内容已超出a4的高度，则将该dom前插入一个空dom，把他挤下去，分割
    for (let i = 0; i < lableListID.length; i++) {
        let multiple = Math.ceil((lableListID[i].offsetTop + lableListID[i].offsetHeight) / pageHeight);
        if (isSplit(lableListID, i, multiple * pageHeight)) {
            let divParent = lableListID[i].parentNode; // 获取该div的父节点
            let newNode = document.createElement('div');
            newNode.className = 'emptyDiv';
            newNode.style.background = '#ffffff';
            let _H = multiple * pageHeight - (lableListID[i].offsetTop + lableListID[i].offsetHeight);
            //留白
            newNode.style.height = _H + 30 + 'px';
            newNode.style.width = '100%';
            let next = lableListID[i].nextSibling; // 获取div的下一个兄弟节点
            // 判断兄弟节点是否存在
            if (next) {
                // 存在则将新节点插入到div的下一个兄弟节点之前，即div之后
                divParent.insertBefore(newNode, next);
            } else {
                // 不存在则直接添加到最后,appendChild默认添加到divParent的最后
                divParent.appendChild(newNode);
            }
        }
    }
    //接下来开始截图
    this.$nextTick(() => {
        // nexttick可以保证要截图的部分全部执行完毕，具体用法自行百度...
        html2Canvas(imageWrapper, {
            allowTaint: true,
            taintTest: false,
            useCORS: true,
            //width:960,
            //height:5072,
            dpi: window.devicePixelRatio * 4, //将分辨率提高到特定的DPI 提高四倍
            scale: 4 //按比例增加分辨率
        }).then(canvas => {
            let pdf = new jsPDF('p', 'mm', 'a4'); //A4纸，纵向
            let ctx = canvas.getContext('2d'),
                a4w = 190,
                a4h = 277, //A4大小，210mm x 297mm，四边各保留10mm的边距，显示区域190x277
                imgHeight = Math.floor((a4h * canvas.width) / a4w), //按A4显示比例换算一页图像的像素高度
                renderedHeight = 0;

            while (renderedHeight < canvas.height) {
                let page = document.createElement('canvas');
                page.width = canvas.width;
                page.height = Math.min(imgHeight, canvas.height - renderedHeight); //可能内容不足一页
                //用getImageData剪裁指定区域，并画到前面创建的canvas对象中
                page.getContext('2d').putImageData(
                    ctx.getImageData(
                        0,
                        renderedHeight,
                        canvas.width,
                        Math.min(imgHeight, canvas.height - renderedHeight)
                    ),
                    0,
                    0
                );
                pdf.addImage(
                    page.toDataURL('image/jpeg', 1.0), 'JPEG', 10, 10, a4w, Math.min(a4h, (a4w * page.height) / page.width)
                ); //添加图像到页面，保留10mm边距
                renderedHeight += imgHeight;
                if (renderedHeight < canvas.height) pdf.addPage(); //如果后面还有内容，添加一个空页
            }
            pdf.save(`${title}.pdf`);
        });
    });
};

app.mount('#app');