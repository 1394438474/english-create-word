package com.smartvocab.module.book.controller;

import com.smartvocab.common.R;
import com.smartvocab.common.SecurityContext;
import com.smartvocab.module.book.entity.Book;
import com.smartvocab.module.book.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @GetMapping
    public R<List<Book>> list(@RequestParam(required = false) String category) {
        return R.ok(bookService.listBooks(category));
    }

    @GetMapping("/{id}")
    public R<Book> detail(@PathVariable Long id) {
        return R.ok(bookService.getById(id));
    }

    @PostMapping("/{id}/choose")
    public R<Void> choose(@PathVariable Long id) {
        bookService.chooseBook(SecurityContext.getUserId(), id);
        return R.ok();
    }
}
