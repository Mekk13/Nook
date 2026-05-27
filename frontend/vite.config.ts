import { defineConfig } from 'vitest/config'
import react from '@vitejs/plugin-react'
import basicSsl from '@vitejs/plugin-basic-ssl'

export default defineConfig({
  server: {
    host: '0.0.0.0',
    port: 5173,
  },
  plugins: [
    react(),
    basicSsl()
  ],

  define: {
    global: 'window',
  },

  test: {
  environment: "jsdom",
  globals: true,
  setupFiles: './src/setupTests.ts',
}

})

