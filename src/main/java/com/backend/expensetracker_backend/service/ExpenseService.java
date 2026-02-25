package com.backend.expensetracker_backend.service;

import com.backend.expensetracker_backend.dto.ExpenseCreateDTO;
import com.backend.expensetracker_backend.dto.ExpenseResponseDTO;
import com.backend.expensetracker_backend.dto.ExpenseUpdateDTO;

import java.time.LocalDate;
import java.util.List;

public interface ExpenseService {

    ExpenseResponseDTO createExpense(ExpenseCreateDTO dto);

    List<ExpenseResponseDTO> getMyExpenses();

    List<ExpenseResponseDTO> getMyExpensesByDateRange(LocalDate startDate, LocalDate endDate);

    ExpenseResponseDTO getExpenseById(Long id);

    ExpenseResponseDTO updateExpense(Long id, ExpenseUpdateDTO dto);

    void deleteExpense(Long id);

    // Método para validar límites de usuarios FREE
    void validateUserCanCreateExpense();
}