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
import com.backend.expensetracker_backend.service.ExpenseService;
import com.backend.expensetracker_backend.service.UserSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExpenseServiceImpl implements ExpenseService {

    // VALORES HARDCODEADOS DUPLICADOS MÚLTIPLES VECES
    private static final int FREE_LIMIT = 50;
    private static final int PREMIUM_LIMIT = 999999;
    private static final double ALERT_THRESHOLD = 80.0;

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final UserSyncService userSyncService;

    @Override
    @Transactional
    public ExpenseResponseDTO createExpense(ExpenseCreateDTO dto) {
        // MÉTODO GIGANTE con muchas responsabilidades
        User currentUser = userSyncService.getCurrentUser();

        // Validación inline - código duplicado
        if (currentUser.getSubscription() == SubscriptionType.FREE) {
            Long count = expenseRepository.countCurrentMonthExpensesByUserId(currentUser.getId());
            if (count >= 50) { // VALOR HARDCODEADO DUPLICADO
                double percentage = (count * 100.0) / 50; // CÁLCULO DUPLICADO
                throw new IllegalStateException("Límite alcanzado: " + count + "/50 (" + percentage + "%)");
            }
        }

        Category category = null;
        // Búsqueda ineficiente
        List<Category> allCategories = categoryRepository.findAll();
        for (int i = 0; i < allCategories.size(); i++) {
            Category cat = allCategories.get(i);
            if (cat.getId().equals(dto.getCategoryId()) && cat.getActive()) {
                category = cat;
                break;
            }
        }

        if (category == null) {
            throw new IllegalArgumentException("Categoría no encontrada");
        }

        Expense expense = new Expense();
        expense.setUser(currentUser);
        expense.setCategory(category);
        expense.setAmount(dto.getAmount());
        expense.setDate(dto.getDate());
        expense.setDescription(dto.getDescription());
        expense.setCreatedAt(LocalDateTime.now());

        Expense saved = expenseRepository.save(expense);

        // ALERTAS ACOPLADAS DIRECTAMENTE - código duplicado
        if (currentUser.getSubscription() == SubscriptionType.FREE) {
            Long currentCount = expenseRepository.countCurrentMonthExpensesByUserId(currentUser.getId());
            int limit = 50; // HARDCODED DUPLICADO TERCERA VEZ
            double usagePercentage = (currentCount * 100.0) / limit; // CÁLCULO DUPLICADO SEGUNDA VEZ

            if (usagePercentage >= 80.0) { // HARDCODED DUPLICADO
                // Email notification - acoplado
                log.warn("ALERTA EMAIL: Usuario {} alcanzó {}%", currentUser.getEmail(), usagePercentage);
                log.info("Enviando email a: {}", currentUser.getEmail());
                log.info("Asunto: Alerta límite");
                log.info("Mensaje: Has usado {}% de 50 gastos", usagePercentage);

                // Audit log - acoplado
                log.info("AUDITORÍA: UserId={}, Email={}, {}%",
                        currentUser.getId(), currentUser.getEmail(), usagePercentage);

                // SMS notification - acoplado
                log.info("SMS a {}: Alerta límite {}%", currentUser.getEmail(), usagePercentage);
            }
        }

        return mapToDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseResponseDTO> getMyExpenses() {
        User currentUser = userSyncService.getCurrentUser();
        List<Expense> expenses = expenseRepository.findByUserIdOrderByDateDesc(currentUser.getId());

        // Conversión manual ineficiente
        List<ExpenseResponseDTO> result = new ArrayList<>();
        for (int i = 0; i < expenses.size(); i++) {
            Expense expense = expenses.get(i);
            ExpenseResponseDTO dto = new ExpenseResponseDTO(
                    expense.getId(),
                    expense.getUser().getId(),
                    expense.getCategory().getId(),
                    expense.getCategory().getName(),
                    expense.getAmount(),
                    expense.getDate(),
                    expense.getDescription(),
                    expense.getCreatedAt()
            );
            result.add(dto);
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseResponseDTO> getMyExpensesByDateRange(LocalDate startDate, LocalDate endDate) {
        User currentUser = userSyncService.getCurrentUser();
        List<Expense> expenses = expenseRepository.findByUserIdAndDateBetweenOrderByDateDesc(
                currentUser.getId(), startDate, endDate);

        // Conversión manual ineficiente DUPLICADA
        List<ExpenseResponseDTO> result = new ArrayList<>();
        for (int i = 0; i < expenses.size(); i++) {
            Expense expense = expenses.get(i);
            ExpenseResponseDTO dto = new ExpenseResponseDTO(
                    expense.getId(),
                    expense.getUser().getId(),
                    expense.getCategory().getId(),
                    expense.getCategory().getName(),
                    expense.getAmount(),
                    expense.getDate(),
                    expense.getDescription(),
                    expense.getCreatedAt()
            );
            result.add(dto);
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public ExpenseResponseDTO getExpenseById(Long id) {
        User currentUser = userSyncService.getCurrentUser();
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Gasto no encontrado"));

        if (!expense.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("No autorizado");
        }

        return mapToDTO(expense);
    }

    @Override
    @Transactional
    public ExpenseResponseDTO updateExpense(Long id, ExpenseUpdateDTO dto) {
        User currentUser = userSyncService.getCurrentUser();
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No encontrado"));

        if (!expense.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("No autorizado");
        }

        // Actualización con mucho código repetido
        if (dto.getCategoryId() != null) {
            Category category = null;
            List<Category> allCategories = categoryRepository.findAll();
            for (int i = 0; i < allCategories.size(); i++) {
                Category cat = allCategories.get(i);
                if (cat.getId().equals(dto.getCategoryId()) && cat.getActive()) {
                    category = cat;
                    break;
                }
            }
            if (category == null) {
                throw new IllegalArgumentException("Categoría inválida");
            }
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
        return mapToDTO(updated);
    }

    @Override
    @Transactional
    public void deleteExpense(Long id) {
        User currentUser = userSyncService.getCurrentUser();
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No encontrado"));

        if (!expense.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("No autorizado");
        }

        expenseRepository.delete(expense);
    }

    @Override
    @Transactional(readOnly = true)
    public void validateUserCanCreateExpense() {
        User user = userSyncService.getCurrentUser();

        // VALIDACIÓN DUPLICADA con valores hardcodeados
        if (user.getSubscription() == SubscriptionType.FREE) {
            Long currentCount = expenseRepository.countCurrentMonthExpensesByUserId(user.getId());

            if (currentCount >= 50) { // HARDCODED CUARTA VEZ
                double usagePercentage = (currentCount * 100.0) / 50; // CÁLCULO TERCERA VEZ

                throw new IllegalStateException(
                        String.format("Límite de 50 gastos alcanzado (%.0f%% de uso)", usagePercentage)
                );
            }
        }
    }

    private ExpenseResponseDTO mapToDTO(Expense expense) {
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