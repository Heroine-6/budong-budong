import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

function cleanUrlPlugin() {
  return {
    name: 'clean-url-rewrite',
    configureServer(server) {
      server.middlewares.use((req, res, next) => {
        if (req.url === '/budongbudong' || req.url === '/budongbudong/') req.url = '/index.html'
        if (req.url === '/signin' || req.url === '/signin/') req.url = '/signin.html'
        if (req.url === '/signup' || req.url === '/signup/') req.url = '/signup.html'
        next()
      })
    },
  }
}

export default defineConfig({
  plugins: [vue(), cleanUrlPlugin()],
  root: 'src',
  build: {
    outDir: resolve(__dirname, '../src/main/resources/static'),
    emptyOutDir: false,
    rollupOptions: {
      input: {
        index: resolve(__dirname, 'src/index.html'),
        signin: resolve(__dirname, 'src/signin.html'),
        signup: resolve(__dirname, 'src/signup.html'),
      },
    },
  },
  server: {
    port: 3000,
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
})
