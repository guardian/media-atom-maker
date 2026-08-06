import { sentryVitePlugin } from '@sentry/vite-plugin';
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react-swc';
import svgr from 'vite-plugin-svgr';

export default defineConfig({
  base: '',
  plugins: [
    react(),
    svgr(),
    sentryVitePlugin({
      org: 'the-guardian',
      project: 'media-atom-maker-dev'
    })
  ],
  publicDir: false, // This feature is deactivated because the 'public' dir also has a special meaning for the Play framework
  build: {
    sourcemap: true,
    manifest: true,
    rollupOptions: {
      input: './public/video-ui/src/app.tsx',
      output: {
        entryFileNames: `[name].js`,
        chunkFileNames: `[name].js`,
        assetFileNames: `[name].[ext]`
      }
    },
    outDir: './public/video-ui/build',
    emptyOutDir: true
  },
  server: {
    origin: 'http://localhost:5173',
    cors: {
      origin: 'https://video.local.dev-gutools.co.uk'
    },
    // We depend upon this port number in a few places, so fail fast if we cannot allocate it.
    strictPort: true,
    fs: {
      allow: ['/public/video-ui/fonts', './']
    }
  }
});
