import request from '@/utils/request'
import type { WordCard } from './book'

export const learnApi = {
  today: (bookId: number, limit = 20) => request.get<any, WordCard[]>('/learn/today', { params: { bookId, limit } }),
  record: (data: { bookId: number; wordId: number; status: string; durationMs: number }) =>
    request.post('/learn/record', data)
}

export const reviewApi = {
  today: (limit = 30) => request.get<any, WordCard[]>('/review/today', { params: { limit } }),
  record: (data: { bookId: number; wordId: number; status: string; durationMs: number }) =>
    request.post('/review/record', data)
}
