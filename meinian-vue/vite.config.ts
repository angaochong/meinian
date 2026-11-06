import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

// 配置SVG依赖库
import path from 'path';
import { createSvgIconsPlugin } from 'vite-plugin-svg-icons';

export default defineConfig({
  plugins: [
    vue(),
    // 配置按需自动加载EP控件
    AutoImport({
      resolvers: [ElementPlusResolver()],
    }),
    // 引入EP控件库
    Components({
      resolvers: [ElementPlusResolver()],
    }),
    // 引入SVG图标素材文件
    createSvgIconsPlugin({
      iconDirs: [path.resolve(process.cwd(), 'src/icons/svg')],
      symbolId: '[name]'
    })
  ],
  css: {
    preprocessorOptions: {
      scss: {
        silenceDeprecations: ['import', 'global-builtin']
      }
    }
  },
    server: {
    // 配置前端项目所在服务器的IP地址
    host: 'localhost',
    // 配置前端项目所在服务器的端口号
    port: 5173,
      
    // 配置代理
    // 所有以 /meinian-api 开头的请求路径都走vite开发服务器的代理机制。
    // 你实际的请求路径是：http://localhost:5173/meinian-api/test
    // vite开发服务器代理的结果是：https://localhost:8080/meinian-api/test
    // 这样后端就不会认为是跨域访问了。
    proxy: {
      '/meinian-api': {
        //target: 'https://localhost:8080',
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false  // vite开发服务器不验证后端服务器的证书（我们HTTPS协议使用的是自签名证书，因此这里不能让Vite开发服务器验证证书，如果验证会失败）
      }
    }
  }
})