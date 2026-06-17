package com.smartvocab.module.book.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartvocab.common.BizException;
import com.smartvocab.module.book.entity.Book;
import com.smartvocab.module.book.entity.UserBook;
import com.smartvocab.module.book.mapper.BookMapper;
import com.smartvocab.module.book.mapper.UserBookMapper;
import com.smartvocab.module.book.service.BookService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookServiceImpl extends ServiceImpl<BookMapper, Book> implements BookService {

    private final UserBookMapper userBookMapper;

    public BookServiceImpl(UserBookMapper userBookMapper) {
        this.userBookMapper = userBookMapper;
    }

    @Override
    public List<Book> listBooks(String category) {
        LambdaQueryWrapper<Book> q = new LambdaQueryWrapper<>();
        if (category != null && !category.isEmpty() && !"ALL".equalsIgnoreCase(category)) {
            q.eq(Book::getCategory, category);
        }
        q.orderByAsc(Book::getId);
        return list(q);
    }

    @Override
    public Book getById(Long id) {
        Book b = getOne(new LambdaQueryWrapper<Book>().eq(Book::getId, id));
        if (b == null) throw new BizException("词书不存在");
        return b;
    }

    @Override
    public void chooseBook(Long userId, Long bookId) {
        UserBook ub = userBookMapper.selectOne(new LambdaQueryWrapper<UserBook>()
                .eq(UserBook::getUserId, userId).eq(UserBook::getBookId, bookId));
        if (ub == null) {
            ub = new UserBook();
            ub.setUserId(userId);
            ub.setBookId(bookId);
            ub.setCurrentIndex(0);
            ub.setLearnedCount(0);
            ub.setMasteredCount(0);
            userBookMapper.insert(ub);
        }
    }
}
