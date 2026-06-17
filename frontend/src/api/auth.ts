import request from '@/utils/request'

export interface LoginPayload { email: string; password: string }
export interface RegisterPayload { email: string; password: string; nickname?: string }

export const authApi = {
  login: (data: LoginPayload) => request.post<any, { token: string; user: any }>('/auth/login', data),
  register: (data: RegisterPayload) => request.post<any, { token: string; user: any }>('/auth/register', data)
}
