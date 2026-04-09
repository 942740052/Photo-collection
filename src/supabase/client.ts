import { createClient } from '@supabase/supabase-js'

// 示例配置，用户需要替换为自己的Supabase项目配置
const supabaseUrl = 'https://your-project.supabase.co'
const supabaseAnonKey = 'your-anon-key'

export const supabase = createClient(supabaseUrl, supabaseAnonKey)