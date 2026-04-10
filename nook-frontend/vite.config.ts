import { defineConfig } from 'vitest/config'
import react from '@vitejs/plugin-react'

export default defineConfig({
  server: {
    host: '127.0.0.1',
    port: 5173,
  },
  plugins: [react()],

  test: {
  environment: "jsdom",
  globals: true,
  setupFiles: './src/setupTests.ts',
}

})

