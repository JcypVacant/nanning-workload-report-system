import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
import { resolve } from 'path'

/**
 * Vite 构建配置
 * 包含：
 * - Vue 3 SFC 插件
 * - Element Plus 组件自动导入
 * - API 自动导入（ref、reactive、computed 等）
 * - 路径别名 @ -> src/
 * - 开发代理（/api -> localhost:8080）
 */
export default defineConfig({
  plugins: [
    vue(),
    // Element Plus 组件自动导入
    Components({
      resolvers: [ElementPlusResolver()],
      dts: 'src/types/components.d.ts'
    }),
    // Vue API 自动导入
    AutoImport({
      imports: ['vue', 'vue-router', 'pinia'],
      resolvers: [ElementPlusResolver()],
      dts: 'src/types/auto-imports.d.ts'
    })
  ],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  server: {
    port: 5173,
    // 代理配置：将 /api 请求转发到后端 8080 端口
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
