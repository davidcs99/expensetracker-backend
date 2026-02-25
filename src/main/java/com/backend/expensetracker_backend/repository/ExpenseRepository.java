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

    // Obtener todos los gastos de un usuario ordenados por fecha descendente
    List<Expense> findByUserIdOrderByDateDesc(Long userId);

    // Obtener gastos de un usuario en un rango de fechas
    List<Expense> findByUserIdAndDateBetweenOrderByDateDesc(Long userId, LocalDate startDate, LocalDate endDate);

    // Obtener gastos de un usuario por categoría
    List<Expense> findByUserIdAndCategoryId(Long userId, Long categoryId);

    // Contar gastos del mes actual de un usuario
    @Query("SELECT COUNT(e) FROM Expense e WHERE e.user.id = :userId " +
            "AND FUNCTION('YEAR', e.date) = FUNCTION('YEAR', CURRENT_DATE) " +
            "AND FUNCTION('MONTH', e.date) = FUNCTION('MONTH', CURRENT_DATE)")
    Long countCurrentMonthExpensesByUserId(@Param("userId") Long userId);

    // Obtener gastos de un usuario en un mes específico
    @Query("SELECT e FROM Expense e WHERE e.user.id = :userId " +
            "AND FUNCTION('YEAR', e.date) = :year " +
            "AND FUNCTION('MONTH', e.date) = :month " +
            "ORDER BY e.date DESC")
    List<Expense> findByUserIdAndYearAndMonth(@Param("userId") Long userId,
                                              @Param("year") int year,
                                              @Param("month") int month);
}