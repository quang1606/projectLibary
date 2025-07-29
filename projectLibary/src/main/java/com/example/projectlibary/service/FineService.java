package com.example.projectlibary.service;

import com.example.projectlibary.common.ReturnCondition;
import com.example.projectlibary.model.BookLoan;

import java.math.BigDecimal;

public interface FineService {
    void createFineForLoan(BookLoan loan, ReturnCondition condition, BigDecimal replacementCost);

}
