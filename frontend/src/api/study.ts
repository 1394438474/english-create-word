import request from '@/utils/request'
import type { WordCard } from './book'

export interface QuizOption { label: string; text: string; imageUrl?: string }
export interface QuizQuestion { wordId: number; spelling: string; phonetic: string; imageUrl: string; prompt: string; options: QuizOption[]; answerIndex: number }
export interface QuizDTO { type: string; bookId: number; size: number; questions: QuizQuestion[]; total?: number; correct?: number; score?: number; durationMs?: number }

export const errorBookApi = {
  list: (quizType?: string) => request.get<any, WordCard[]>('/errorbook', { params: { quizType } }),
  remove: (wordId: number) => request.post(`/errorbook/${wordId}/remove`),
  add: (wordId: number, quizType = 'TRANSLATE') => request.post('/errorbook/add', { wordId, quizType })
}

export const quizApi = {
  generate: (type: string, bookId: number, size = 10) =>
    request.get<any, QuizDTO>('/quiz/generate', { params: { type, bookId, size } }),
  submit: (data: QuizDTO) => request.post<any, QuizDTO>('/quiz/submit', data)
}

export const vocabApi = {
  list: () => request.get<any, any[]>('/vocab'),
  add: (data: { wordId: number; note?: string; star?: number; isMarked?: number }) =>
    request.post('/vocab/add', data),
  updateNote: (wordId: number, note: string) => request.post(`/vocab/${wordId}/note`, { note }),
  updateStar: (wordId: number, star: number) => request.post(`/vocab/${wordId}/star`, { star }),
  batchRemove: (wordIds: number[]) => request.delete('/vocab/batch', { data: wordIds }),
  export: () => request.get<any, { total: number; items: any[] }>('/vocab/export')
}

export const checkInApi = {
  do: (learnedCount: number, durationMs: number) =>
    request.post<any, { streakDays: number; totalDays: number; unlockedMedals: string[] }>('/checkin', { learnedCount, durationMs }),
  calendar: (year: number, month: number) => request.get<any, any>('/checkin/calendar', { params: { year, month } }),
  medals: () => request.get<any, any[]>('/checkin/medals')
}

export const statApi = {
  dashboard: () => request.get<any, any>('/stat/dashboard'),
  trend: (days = 14) => request.get<any, any[]>('/stat/trend', { params: { days } }),
  mastery: () => request.get<any, any>('/stat/mastery'),
  weak: (limit = 10) => request.get<any, any[]>('/stat/weak', { params: { limit } }),
  radar: () => request.get<any, any>('/stat/radar')
}
