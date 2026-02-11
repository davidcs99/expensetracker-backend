package com.backend.expensetracker_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;  // Ej: "Alimentación", "Transporte", "Entretenimiento"

    private String description;

    @Column(nullable = false)
    private Boolean active = true;  // Para deshabilitar categorías sin borrarlas

    // Relación con gastos
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    private List<Expense> expenses = new ArrayList<>();
}