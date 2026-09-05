import { sentryVitePlugin } from '@sentry/vite-plugin';
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react-swc';
import svgr from 'vite-plugin-svgr';

// Play serves the contents of public/ directly, so any .map file left on disk
// after the build would be publicly downloadable.
const sourceMapGlob = './public/video-ui/build/**/*.map';

export default defineConfig(({ command }) => ({
  base: '',
  plugins: [
    react(),
    svgr(),
    // Build only. The dev server has no source maps worth uploading, and
    // including this unconditionally makes `yarn client-dev` reach out to
    // Sentry on every start.
    ...(command === 'build'
      ? [
          sentryVitePlugin({
            org: 'the-guardian',
            // Single project for all stages, matching the DSN in the
            // [STAGE].public.conf files; stages are separated by `environment`
            // rather than by project. Overridable for one-off builds.
            project: process.env.SENTRY_PROJECT ?? 'media-atom-maker',
            sourcemaps: {
              filesToDeleteAfterUpload: [sourceMapGlob]
            }
          })
        ]
      : [])
  ],
  publicDir: false, // This feature is deactivated because the 'public' dir also has a special meaning for the Play framework
  build: {
    // 'hidden' still emits maps for Sentry to upload, but omits the
    // sourceMappingURL comment so browsers never request them.
    sourcemap: 'hidden',
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
}));
