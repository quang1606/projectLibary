package com.example.projectlibary.service;

import com.example.projectlibary.dto.reponse.BookCopyResponse;
import com.example.projectlibary.dto.reponse.BookLoanResponse;
import com.example.projectlibary.dto.reponse.BookSummaryResponse;
import com.example.projectlibary.dto.reponse.PageResponse;
import com.example.projectlibary.dto.request.FinalizeReturnRequest;
import jakarta.validation.Valid;

public interface ReturnService {
    PageResponse<BookLoanResponse> getMyLoans(int page, int size);

    BookLoanResponse returnsBook(Long loanId);

    BookLoanResponse getBookLoanByCopy(long id);

    BookLoanResponse finalizeReturn(@Valid FinalizeReturnRequest request);
}
