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

    List<Expense> findByUserIdOrderByDateDesc(Long userId);

    List<Expense> findByUserIdAndDateBetweenOrderByDateDesc(Long userId, LocalDate startDate, LocalDate endDate);

    List<Expense> findByUserIdAndCategoryId(Long userId, Long categoryId);

    @Query("SELECT COUNT(e) FROM Expense e WHERE e.user.id = :userId " +
            "AND EXTRACT(YEAR FROM e.date) = EXTRACT(YEAR FROM CURRENT_DATE) " +
            "AND EXTRACT(MONTH FROM e.date) = EXTRACT(MONTH FROM CURRENT_DATE)")
    Long countCurrentMonthExpensesByUserId(@Param("userId") Long userId);

    @Query("SELECT e FROM Expense e WHERE e.user.id = :userId " +
            "AND EXTRACT(YEAR FROM e.date) = :year " +
            "AND EXTRACT(MONTH FROM e.date) = :month " +
            "ORDER BY e.date DESC")
    List<Expense> findByUserIdAndYearAndMonth(@Param("userId") Long userId,
                                              @Param("year") int year,
                                              @Param("month") int month);
}