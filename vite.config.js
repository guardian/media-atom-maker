import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react-swc'

export default defineConfig({
  plugins: [react()],
  build: {
    manifest: true,
    rollupOptions: {
      input: './video-ui/src/app.jsx',
      output: {
        entryFileNames: `build/[name].js`,
        chunkFileNames: `build/[name].js`,
        assetFileNames: `build/[name].[ext]`
      }
    },
    outDir: "./public/video-ui/",
    emptyOutDir: true
  },
  server: {
    origin: 'http://localhost:5173',
    // We depend upon this port number in a few places, so fail fast if we cannot allocate it.
    strictPort: true,
    fs: {
      allow: ['/video-ui/fonts', './']
    }
  }
})
