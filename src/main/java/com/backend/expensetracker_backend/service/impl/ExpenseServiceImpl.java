package com.backend.expensetracker_backend.service.impl;

import com.backend.expensetracker_backend.dto.ExpenseCreateDTO;
import com.backend.expensetracker_backend.dto.ExpenseResponseDTO;
import com.backend.expensetracker_backend.dto.ExpenseUpdateDTO;
import com.backend.expensetracker_backend.entity.Category;
import com.backend.expensetracker_backend.entity.Expense;
import com.backend.expensetracker_backend.entity.SubscriptionType;
import com.backend.expensetracker_backend.entity.User;
import com.backend.expensetracker_backend.repository.CategoryRepository;
import com.backend.expensetracker_backend.repository.ExpenseRepository;
import com.backend.expensetracker_backend.service.AppConfigService;
import com.backend.expensetracker_backend.service.ExpenseService;
import com.backend.expensetracker_backend.service.UserSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final UserSyncService userSyncService;
    private final AppConfigService appConfigService; // ← Singleton inyectado

    @Override
    @Transactional
    public ExpenseResponseDTO createExpense(ExpenseCreateDTO dto) {
        // Validar límites usando el Singleton
        validateUserCanCreateExpense();

        User currentUser = userSyncService.getCurrentUser();

        Category category = categoryRepository.findById(dto.getCategoryId())
                .filter(Category::getActive)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada o inactiva"));

        Expense expense = new Expense();
        expense.setUser(currentUser);
        expense.setCategory(category);
        expense.setAmount(dto.getAmount());
        expense.setDate(dto.getDate());
        expense.setDescription(dto.getDescription());
        expense.setCreatedAt(LocalDateTime.now());

        Expense saved = expenseRepository.save(expense);

        log.info("Gasto creado: userId={}, amount={}, categoryId={}",
                currentUser.getId(), dto.getAmount(), dto.getCategoryId());

        return mapToResponseDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseResponseDTO> getMyExpenses() {
        User currentUser = userSyncService.getCurrentUser();
        return expenseRepository.findByUserIdOrderByDateDesc(currentUser.getId())
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseResponseDTO> getMyExpensesByDateRange(LocalDate startDate, LocalDate endDate) {
        User currentUser = userSyncService.getCurrentUser();
        return expenseRepository.findByUserIdAndDateBetweenOrderByDateDesc(
                        currentUser.getId(), startDate, endDate)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ExpenseResponseDTO getExpenseById(Long id) {
        User currentUser = userSyncService.getCurrentUser();
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Gasto no encontrado con id: " + id));

        if (!expense.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("No tienes permiso para ver este gasto");
        }

        return mapToResponseDTO(expense);
    }

    @Override
    @Transactional
    public ExpenseResponseDTO updateExpense(Long id, ExpenseUpdateDTO dto) {
        User currentUser = userSyncService.getCurrentUser();
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Gasto no encontrado con id: " + id));

        if (!expense.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("No tienes permiso para modificar este gasto");
        }

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .filter(Category::getActive)
                    .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada o inactiva"));
            expense.setCategory(category);
        }

        if (dto.getAmount() != null) {
            expense.setAmount(dto.getAmount());
        }

        if (dto.getDate() != null) {
            expense.setDate(dto.getDate());
        }

        if (dto.getDescription() != null) {
            expense.setDescription(dto.getDescription());
        }

        Expense updated = expenseRepository.save(expense);
        log.info("Gasto actualizado: expenseId={}, userId={}", id, currentUser.getId());

        return mapToResponseDTO(updated);
    }

    @Override
    @Transactional
    public void deleteExpense(Long id) {
        User currentUser = userSyncService.getCurrentUser();
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Gasto no encontrado con id: " + id));

        if (!expense.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("No tienes permiso para eliminar este gasto");
        }

        expenseRepository.delete(expense);
        log.info("Gasto eliminado: expenseId={}, userId={}", id, currentUser.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public void validateUserCanCreateExpense() {
        User currentUser = userSyncService.getCurrentUser();
        boolean isPremium = currentUser.getSubscription() == SubscriptionType.PREMIUM;

        int limit = appConfigService.getFreeAccountMonthlyLimit();

        if (!isPremium) {
            Long currentMonthCount = expenseRepository.countCurrentMonthExpensesByUserId(currentUser.getId());

            boolean canCreate = appConfigService.canCreateMoreExpenses(currentMonthCount.intValue(), false);

            if (!canCreate) {
                double usagePercentage = appConfigService.getUsagePercentage(currentMonthCount.intValue(), false);

                throw new IllegalStateException(
                        String.format("Has alcanzado el límite de %d gastos mensuales para cuentas FREE (%.0f%% de uso). " +
                                "Actualiza a PREMIUM para gastos ilimitados.", limit, usagePercentage)
                );
            }
        }
    }

    private ExpenseResponseDTO mapToResponseDTO(Expense expense) {
        return new ExpenseResponseDTO(
                expense.getId(),
                expense.getUser().getId(),
                expense.getCategory().getId(),
                expense.getCategory().getName(),
                expense.getAmount(),
                expense.getDate(),
                expense.getDescription(),
                expense.getCreatedAt()
        );
    }
}