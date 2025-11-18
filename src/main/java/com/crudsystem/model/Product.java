package com.crudsystem.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Modelo de domínio representando um Produto no sistema.
 * Implementa validações robustas e imutabilidade parcial.
 */
public class Product implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer quantity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Product() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Product(Long id, String name, String description, BigDecimal price, Integer quantity) {
        this();
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // Setters com validação
    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do produto não pode ser vazio");
        }
        if (name.length() > 100) {
            throw new IllegalArgumentException("Nome do produto não pode exceder 100 caracteres");
        }
        this.name = name.trim();
    }

    public void setDescription(String description) {
        if (description != null && description.length() > 500) {
            throw new IllegalArgumentException("Descrição não pode exceder 500 caracteres");
        }
        this.description = description != null ? description.trim() : null;
    }

    public void setPrice(BigDecimal price) {
        if (price == null) {
            throw new IllegalArgumentException("Preço não pode ser nulo");
        }
        if (price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Preço não pode ser negativo");
        }
        if (price.scale() > 2) {
            throw new IllegalArgumentException("Preço não pode ter mais de 2 casas decimais");
        }
        this.price = price;
    }

    public void setQuantity(Integer quantity) {
        if (quantity == null) {
            throw new IllegalArgumentException("Quantidade não pode ser nula");
        }
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantidade não pode ser negativa");
        }
        this.quantity = quantity;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Valida se o produto está em estado consistente.
     * @throws IllegalStateException se o produto estiver inválido
     */
    public void validate() {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalStateException("Produto inválido: nome é obrigatório");
        }
        if (price == null) {
            throw new IllegalStateException("Produto inválido: preço é obrigatório");
        }
        if (quantity == null) {
            throw new IllegalStateException("Produto inválido: quantidade é obrigatória");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(id, product.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", quantity=" + quantity +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
