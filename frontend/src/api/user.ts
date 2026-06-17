import request from '@/utils/request'

export interface UserPayload { nickname?: string; bio?: string; avatar?: string }

export const userApi = {
  me: () => request.get<any, any>('/user/me'),
  update: (data: UserPayload) => request.put('/user/me', data),
  changePassword: (data: { oldPwd: string; newPwd: string }) => request.post('/user/password', data),
  uploadAvatar: (file: File) => {
    const fd = new FormData()
    fd.append('file', file)
    return request.post<any, { url: string }>('/upload/avatar', fd, { headers: { 'Content-Type': 'multipart/form-data' } })
  }
}
