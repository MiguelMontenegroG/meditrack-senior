import js from '@eslint/js'
import globals from 'globals'
import tseslint from 'typescript-eslint'
import reactHooks from 'eslint-plugin-react-hooks'
import reactRefresh from 'eslint-plugin-react-refresh'

// Configuracion minima de ESLint (flat config) para React + TypeScript.
// Paquetes y su justificacion:
// - eslint + @eslint/js: motor y reglas base recomendadas.
// - typescript-eslint: parser y reglas para TypeScript.
// - eslint-plugin-react-hooks: correcto uso de hooks de React.
// - eslint-plugin-react-refresh: compatibilidad del HMR con Vite.
// - globals: define las variables globales de navegador/servidor.
export default tseslint.config(
  { ignores: ['dist', 'node_modules'] },
  {
    extends: [js.configs.recommended, ...tseslint.configs.recommended],
    files: ['**/*.{ts,tsx}'],
    languageOptions: {
      ecmaVersion: 2022,
      globals: globals.browser,
    },
    plugins: {
      'react-hooks': reactHooks,
      'react-refresh': reactRefresh,
    },
    rules: {
      ...reactHooks.configs.recommended.rules,
      'react-refresh/only-export-components': ['warn', { allowConstantExport: true }],
    },
  },
)