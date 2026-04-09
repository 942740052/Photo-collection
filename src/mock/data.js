import dayjs from 'dayjs'

const photoUrls = [
  'https://picsum.photos/seed/p1/800/600',
  'https://picsum.photos/seed/p2/800/600',
  'https://picsum.photos/seed/p3/800/600',
  'https://picsum.photos/seed/p4/800/600',
  'https://picsum.photos/seed/p5/800/600',
  'https://picsum.photos/seed/p6/800/600',
  'https://picsum.photos/seed/p7/800/600',
  'https://picsum.photos/seed/p8/800/600',
  'https://picsum.photos/seed/p9/800/600',
  'https://picsum.photos/seed/p10/800/600',
  'https://picsum.photos/seed/p11/800/600',
  'https://picsum.photos/seed/p12/800/600',
  'https://picsum.photos/seed/p13/800/600',
  'https://picsum.photos/seed/p14/800/600',
  'https://picsum.photos/seed/p15/800/600',
  'https://picsum.photos/seed/p16/800/600',
  'https://picsum.photos/seed/p17/800/600',
  'https://picsum.photos/seed/p18/800/600',
  'https://picsum.photos/seed/p19/800/600',
  'https://picsum.photos/seed/p20/800/600',
  'https://picsum.photos/seed/p21/800/600',
  'https://picsum.photos/seed/p22/800/600',
  'https://picsum.photos/seed/p23/800/600',
  'https://picsum.photos/seed/p24/800/600',
  'https://picsum.photos/seed/p25/800/600',
  'https://picsum.photos/seed/p26/800/600',
  'https://picsum.photos/seed/p27/800/600',
  'https://picsum.photos/seed/p28/800/600',
  'https://picsum.photos/seed/p29/800/600',
  'https://picsum.photos/seed/p30/800/600'
]

const cameras = ['Canon EOS R5', 'Nikon Z7 II', 'Sony A7R V', 'iPhone 15 Pro', 'Huawei P60 Pro', 'Fujifilm X-T5']
const lenses = ['RF 24-70mm f/2.8', 'NIKKOR Z 70-200mm f/2.8', 'FE 85mm f/1.4 GM', 'iPhone 15 Pro 主摄', 'XF 23mm f/1.4 R']
const locations = ['北京·故宫', '上海·外滩', '杭州·西湖', '成都·宽窄巷子', '三亚·亚龙湾', '丽江·古城', '西安·兵马俑', '厦门·鼓浪屿', '青岛·栈桥', '大理·洱海']
const sceneTags = ['风景', '人像', '美食', '建筑', '动物', '夜景', '花卉', '街拍', '旅行', '日落', '海滩', '山脉', '城市', '乡村', '雪景']
const peopleNames = ['爸爸', '妈妈', '宝宝', '爷爷', '奶奶', '小明', '小红', '老王']

function randomItem(arr) {
  return arr[Math.floor(Math.random() * arr.length)]
}

function randomInt(min, max) {
  return Math.floor(Math.random() * (max - min + 1)) + min
}

function generatePhoto(id, dateOffset) {
  const date = dayjs().subtract(dateOffset, 'day').subtract(randomInt(0, 12), 'hour').subtract(randomInt(0, 59), 'minute')
  const hasGps = Math.random() > 0.3
  const camera = randomItem(cameras)
  const isPhone = camera.includes('iPhone') || camera.includes('Huawei')
  const width = randomInt(2000, 6000)
  const height = randomInt(1500, 4000)
  const tags = []
  const tagCount = randomInt(1, 4)
  for (let i = 0; i < tagCount; i++) {
    const tag = randomItem(sceneTags)
    if (!tags.includes(tag)) tags.push(tag)
  }

  return {
    id: `photo-${id}`,
    user_id: 'user-1',
    file_name: `IMG_${date.format('YYYYMMDD')}_${String(id).padStart(4, '0')}.jpg`,
    file_size: randomInt(2000000, 25000000),
    file_hash: `sha256_${id}_${Date.now()}`,
    mime_type: 'image/jpeg',
    width,
    height,
    orientation: 1,
    date_taken: date.format('YYYY-MM-DD HH:mm:ss'),
    date_uploaded: date.add(randomInt(0, 60), 'minute').format('YYYY-MM-DD HH:mm:ss'),
    description: '',
    rating: randomInt(0, 5),
    is_favorite: Math.random() > 0.8,
    latitude: hasGps ? (30 + Math.random() * 10).toFixed(6) : null,
    longitude: hasGps ? (100 + Math.random() * 20).toFixed(6) : null,
    altitude: hasGps ? randomInt(0, 2000) : null,
    location_name: hasGps ? randomItem(locations) : null,
    camera_make: camera.split(' ')[0],
    camera_model: camera,
    lens_model: isPhone ? '' : randomItem(lenses),
    focal_length: isPhone ? randomInt(13, 77) : randomInt(14, 200),
    aperture: (Math.random() * 4 + 1.4).toFixed(1),
    shutter_speed: `1/${randomInt(30, 4000)}`,
    iso: randomItem([100, 200, 400, 800, 1600, 3200]),
    thumbnail_small: photoUrls[(id - 1) % photoUrls.length],
    thumbnail_medium: photoUrls[(id - 1) % photoUrls.length],
    thumbnail_large: photoUrls[(id - 1) % photoUrls.length],
    is_archived: false,
    is_deleted: false,
    deleted_at: null,
    tags,
    created_at: date.format('YYYY-MM-DD HH:mm:ss'),
    updated_at: date.format('YYYY-MM-DD HH:mm:ss')
  }
}

export function generatePhotos(count = 100) {
  const photos = []
  for (let i = 1; i <= count; i++) {
    photos.push(generatePhoto(i, Math.floor((i - 1) / 3)))
  }
  return photos
}

export function generateAlbums() {
  return [
    {
      id: 'album-1',
      user_id: 'user-1',
      parent_id: null,
      name: '2025年旅行',
      description: '2025年所有旅行照片',
      cover_photo_id: 'photo-1',
      cover_url: photoUrls[0],
      type: 'normal',
      sort_order: 0,
      is_public: false,
      smart_rules: null,
      photo_count: 24,
      created_at: '2025-01-15 10:00:00',
      updated_at: '2025-03-20 14:30:00'
    },
    {
      id: 'album-2',
      user_id: 'user-1',
      parent_id: null,
      name: '家庭时光',
      description: '和家人在一起的温馨时刻',
      cover_photo_id: 'photo-5',
      cover_url: photoUrls[4],
      type: 'normal',
      sort_order: 1,
      is_public: false,
      smart_rules: null,
      photo_count: 18,
      created_at: '2025-02-01 08:00:00',
      updated_at: '2025-04-01 16:00:00'
    },
    {
      id: 'album-3',
      user_id: 'user-1',
      parent_id: null,
      name: '美食记录',
      description: '',
      cover_photo_id: 'photo-8',
      cover_url: photoUrls[7],
      type: 'normal',
      sort_order: 2,
      is_public: false,
      smart_rules: null,
      photo_count: 12,
      created_at: '2025-03-10 12:00:00',
      updated_at: '2025-04-05 19:00:00'
    },
    {
      id: 'album-4',
      user_id: 'user-1',
      parent_id: null,
      name: '风景精选',
      description: 'AI自动收集的风景照片',
      cover_photo_id: 'photo-3',
      cover_url: photoUrls[2],
      type: 'smart',
      sort_order: 3,
      is_public: false,
      smart_rules: { tags: ['风景'], logic: 'OR' },
      photo_count: 15,
      created_at: '2025-01-20 09:00:00',
      updated_at: '2025-04-08 10:00:00'
    },
    {
      id: 'album-5',
      user_id: 'user-1',
      parent_id: null,
      name: '宝宝成长',
      description: '记录宝宝成长的每一个瞬间',
      cover_photo_id: 'photo-10',
      cover_url: photoUrls[9],
      type: 'smart',
      sort_order: 4,
      is_public: false,
      smart_rules: { people: ['宝宝'] },
      photo_count: 30,
      created_at: '2024-06-01 08:00:00',
      updated_at: '2025-04-09 07:00:00'
    },
    {
      id: 'album-6',
      user_id: 'user-1',
      parent_id: null,
      name: '建筑摄影',
      description: '各地建筑摄影作品',
      cover_photo_id: 'photo-15',
      cover_url: photoUrls[14],
      type: 'normal',
      sort_order: 5,
      is_public: true,
      smart_rules: null,
      photo_count: 8,
      created_at: '2025-02-20 15:00:00',
      updated_at: '2025-03-15 11:00:00'
    }
  ]
}

export function generateTags() {
  return sceneTags.map((name, i) => ({
    id: `tag-${i + 1}`,
    name,
    parent_id: null,
    color: `#${Math.floor(Math.random() * 16777215).toString(16).padStart(6, '0')}`,
    icon: '',
    usage_count: randomInt(5, 50),
    is_ai_generated: Math.random() > 0.5,
    created_at: '2025-01-01 00:00:00'
  }))
}

export function generatePeople() {
  return peopleNames.map((name, i) => ({
    id: `person-${i + 1}`,
    name,
    cover_photo_id: `photo-${i + 1}`,
    cover_url: photoUrls[i],
    photo_count: randomInt(5, 40),
    is_hidden: false,
    created_at: '2025-01-01 00:00:00',
    updated_at: '2025-04-01 00:00:00'
  }))
}

export function generateShareLinks() {
  return [
    {
      id: 'share-1',
      user_id: 'user-1',
      token: 'abc123def456',
      resource_type: 'album',
      resource_id: 'album-1',
      resource_name: '2025年旅行',
      password: null,
      expires_at: '2025-05-01 00:00:00',
      max_views: null,
      view_count: 15,
      allow_download: true,
      is_active: true,
      created_at: '2025-03-15 10:00:00'
    },
    {
      id: 'share-2',
      user_id: 'user-1',
      token: 'xyz789ghi012',
      resource_type: 'photo',
      resource_id: 'photo-1',
      resource_name: 'IMG_20250315_0001.jpg',
      password: '1234',
      expires_at: null,
      max_views: 50,
      view_count: 8,
      allow_download: false,
      is_active: true,
      created_at: '2025-04-01 14:00:00'
    }
  ]
}

export function generateUsers() {
  return [
    {
      id: 'user-1',
      username: 'admin',
      email: 'admin@example.com',
      display_name: '管理员',
      avatar_url: '',
      role: 'admin',
      status: 'active',
      storage_quota: 107374182400,
      storage_used: 32212254720,
      two_factor_enabled: false,
      created_at: '2025-01-01 00:00:00',
      last_login_at: '2025-04-09 10:00:00'
    },
    {
      id: 'user-2',
      username: 'zhangsan',
      email: 'zhangsan@example.com',
      display_name: '张三',
      avatar_url: '',
      role: 'user',
      status: 'active',
      storage_quota: 53687091200,
      storage_used: 10737418240,
      two_factor_enabled: false,
      created_at: '2025-02-15 08:00:00',
      last_login_at: '2025-04-08 16:00:00'
    },
    {
      id: 'user-3',
      username: 'lisi',
      email: 'lisi@example.com',
      display_name: '李四',
      avatar_url: '',
      role: 'user',
      status: 'active',
      storage_quota: 53687091200,
      storage_used: 5368709120,
      two_factor_enabled: true,
      created_at: '2025-03-01 09:00:00',
      last_login_at: '2025-04-07 11:00:00'
    },
    {
      id: 'user-4',
      username: 'wangwu',
      email: 'wangwu@example.com',
      display_name: '王五',
      avatar_url: '',
      role: 'user',
      status: 'disabled',
      storage_quota: 53687091200,
      storage_used: 2147483648,
      two_factor_enabled: false,
      created_at: '2025-03-20 14:00:00',
      last_login_at: '2025-03-25 09:00:00'
    }
  ]
}

export const mockPhotos = generatePhotos(60)
export const mockAlbums = generateAlbums()
export const mockTags = generateTags()
export const mockPeople = generatePeople()
export const mockShareLinks = generateShareLinks()
export const mockUsers = generateUsers()
