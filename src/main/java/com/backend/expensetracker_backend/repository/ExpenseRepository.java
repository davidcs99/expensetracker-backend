package com.backend.expensetracker_backend.repository;

import com.backend.expensetracker_backend.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    // Buscar gastos de un usuario específico
    List<Expense> findByUserId(Long userId);

    // Buscar gastos de un usuario en un rango de fechas
    List<Expense> findByUserIdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate);

    // Contar gastos de un usuario
    long countByUserId(Long userId);

    // Contar gastos de un usuario en el mes actual
    @Query("SELECT COUNT(e) FROM Expense e WHERE e.user.id = :userId " +
            "AND MONTH(e.date) = MONTH(CURRENT_DATE) " +
            "AND YEAR(e.date) = YEAR(CURRENT_DATE)")
    long countCurrentMonthExpensesByUserId(@Param("userId") Long userId);
}