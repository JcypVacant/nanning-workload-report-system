/// <reference types="vite/client" />

/** API 基础路径 */
declare const VITE_API_BASE: string

interface ImportMetaEnv {
  readonly VITE_API_BASE: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
