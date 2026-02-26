package com.backend.expensetracker_backend.service.impl;

import com.backend.expensetracker_backend.dto.ExpenseCreateDTO;
import com.backend.expensetracker_backend.dto.ExpenseResponseDTO;
import com.backend.expensetracker_backend.dto.ExpenseUpdateDTO;
import com.backend.expensetracker_backend.entity.Category;
import com.backend.expensetracker_backend.entity.Expense;
import com.backend.expensetracker_backend.entity.SubscriptionType;
import com.backend.expensetracker_backend.entity.User;
import com.backend.expensetracker_backend.event.LimitAlertEvent;  // ← NUEVO
import com.backend.expensetracker_backend.repository.CategoryRepository;
import com.backend.expensetracker_backend.repository.ExpenseRepository;
import com.backend.expensetracker_backend.service.AppConfigService;
import com.backend.expensetracker_backend.service.EventPublisher;  // ← NUEVO
import com.backend.expensetracker_backend.service.ExpenseService;
import com.backend.expensetracker_backend.service.UserSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExpenseServiceImpl implements ExpenseService {

    private static final String MSG_EXPENSE_NOT_FOUND = "Gasto no encontrado con id: ";
    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final UserSyncService userSyncService;
    private final AppConfigService appConfigService;
    private final EventPublisher eventPublisher;

    @Override
    @Transactional
    public ExpenseResponseDTO createExpense(ExpenseCreateDTO dto) {
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

        // ═══════════════════════════════════════════════════════════
        // PATRÓN OBSERVER: Verificar y publicar alerta si es necesario
        // ═══════════════════════════════════════════════════════════
        checkAndPublishLimitAlert(currentUser);

        return mapToResponseDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseResponseDTO> getMyExpenses() {
        User currentUser = userSyncService.getCurrentUser();
        return expenseRepository.findByUserIdOrderByDateDesc(currentUser.getId())
                .stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseResponseDTO> getMyExpensesByDateRange(LocalDate startDate, LocalDate endDate) {
        User currentUser = userSyncService.getCurrentUser();
        return expenseRepository.findByUserIdAndDateBetweenOrderByDateDesc(
                        currentUser.getId(), startDate, endDate)
                .stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ExpenseResponseDTO getExpenseById(Long id) {
        User currentUser = userSyncService.getCurrentUser();
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(MSG_EXPENSE_NOT_FOUND + id));

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
                .orElseThrow(() -> new IllegalArgumentException(MSG_EXPENSE_NOT_FOUND + id));

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
                .orElseThrow(() -> new IllegalArgumentException(MSG_EXPENSE_NOT_FOUND + id));

        if (!expense.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("No tienes permiso para eliminar este gasto");
        }

        expenseRepository.delete(expense);
        log.info("Gasto eliminado: expenseId={}, userId={}", id, currentUser.getId());
    }

    public void validateUserCanCreateExpense() {
        User user = userSyncService.getCurrentUser();
        boolean isPremium = user.getSubscription() == SubscriptionType.PREMIUM;

        int limit = appConfigService.getFreeAccountMonthlyLimit();

        if (!isPremium) {
            Long currentCount = expenseRepository.countCurrentMonthExpensesByUserId(user.getId());

            boolean canCreate = appConfigService.canCreateMoreExpenses(currentCount.intValue(), false);

            if (!canCreate) {
                double usagePercentage = appConfigService.getUsagePercentage(currentCount.intValue(), false);

                throw new IllegalStateException(
                        String.format("Has alcanzado el límite de %d gastos mensuales " +
                                        "para cuentas FREE (%.0f%% de uso). " +
                                        "Actualiza a PREMIUM para gastos ilimitados.",
                                limit, usagePercentage)
                );
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // PATRÓN OBSERVER - MÉTODO NUEVO
    // ═══════════════════════════════════════════════════════════

    private void checkAndPublishLimitAlert(User user) {
        if (user.getSubscription() == SubscriptionType.PREMIUM) {
            return; // Premium no tiene límites
        }

        Long currentCount = expenseRepository.countCurrentMonthExpensesByUserId(user.getId());
        int limit = appConfigService.getFreeAccountMonthlyLimit();
        double usagePercentage = appConfigService.getUsagePercentage(currentCount.intValue(), false);

        // Publicar alerta si supera el 80% del límite
        if (usagePercentage >= 80.0 && usagePercentage < 100.0) {
            LimitAlertEvent event = new LimitAlertEvent(
                    user.getId(),
                    user.getEmail(),
                    currentCount.intValue(),
                    limit,
                    usagePercentage
            );

            eventPublisher.publishLimitAlert(event);
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