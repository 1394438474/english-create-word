package com.smartvocab.module.book.service;

import com.smartvocab.module.book.entity.Book;

import java.util.List;

public interface BookService {
    List<Book> listBooks(String category);
    Book getById(Long id);
    void chooseBook(Long userId, Long bookId);
}
