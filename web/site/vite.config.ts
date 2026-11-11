import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import tailwindcss from '@tailwindcss/vite';

// Relative base: the built site works from any folder or subpath on the server.
export default defineConfig({ base: './', plugins: [react(), tailwindcss()] });
