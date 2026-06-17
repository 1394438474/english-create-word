import request from '@/utils/request'

export interface Book {
  id: number
  name: string
  category: string
  cover: string
  color: string
  description: string
  wordCount: number
  level: string
}

export interface WordCard {
  id: number
  bookId: number
  spelling: string
  phoneticUs: string
  phoneticUk: string
  audioUrl: string
  imageUrl: string
  difficulty: string
  meanings: { pos: string; meaningZh: string; meaningEn: string }[]
  sentences: { en: string; zh: string; source: string }[]
  status: string
  stability: number
  confidence: number
  inVocab: boolean
  isError: boolean
}

export const bookApi = {
  list: (category?: string) => request.get<any, Book[]>('/books', { params: { category } }),
  detail: (id: number) => request.get<any, Book>(`/books/${id}`),
  choose: (id: number) => request.post(`/books/${id}/choose`)
}

export const wordApi = {
  pageByBook: (bookId: number, current = 1, size = 20) =>
    request.get<any, { total: number; records: WordCard[] }>('/words/book/' + bookId, { params: { current, size } }),
  detail: (id: number) => request.get<any, WordCard>(`/words/${id}`),
  batch: (ids: number[]) => request.post<any, WordCard[]>('/words/batch', ids)
}
