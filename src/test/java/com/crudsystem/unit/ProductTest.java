package com.crudsystem.unit;

import com.crudsystem.model.Product;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a classe Product.
 * Cobre todas as ramificações e validações.
 */
@DisplayName("Testes Unitários - Product")
class ProductTest {

    @Test
    @DisplayName("Deve criar produto válido")
    void shouldCreateValidProduct() {
        Product product = new Product();
        product.setName("Notebook");
        product.setDescription("Notebook Dell");
        product.setPrice(new BigDecimal("2500.00"));
        product.setQuantity(10);
        
        assertNotNull(product);
        assertEquals("Notebook", product.getName());
        assertEquals("Notebook Dell", product.getDescription());
        assertEquals(new BigDecimal("2500.00"), product.getPrice());
        assertEquals(10, product.getQuantity());
        assertNotNull(product.getCreatedAt());
        assertNotNull(product.getUpdatedAt());
    }

    @Test
    @DisplayName("Deve criar produto com construtor parametrizado")
    void shouldCreateProductWithParameterizedConstructor() {
        Product product = new Product(1L, "Mouse", "Mouse USB", 
                new BigDecimal("25.50"), 100);
        
        assertEquals(1L, product.getId());
        assertEquals("Mouse", product.getName());
        assertEquals("Mouse USB", product.getDescription());
        assertEquals(new BigDecimal("25.50"), product.getPrice());
        assertEquals(100, product.getQuantity());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t", "\n"})
    @DisplayName("Deve lançar exceção para nome inválido")
    void shouldThrowExceptionForInvalidName(String invalidName) {
        Product product = new Product();
        
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> product.setName(invalidName)
        );
        
        assertTrue(exception.getMessage().contains("Nome do produto não pode ser vazio"));
    }

    @Test
    @DisplayName("Deve lançar exceção para nome muito longo")
    void shouldThrowExceptionForNameTooLong() {
        Product product = new Product();
        String longName = "a".repeat(101);
        
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> product.setName(longName)
        );
        
        assertTrue(exception.getMessage().contains("não pode exceder 100 caracteres"));
    }

    @Test
    @DisplayName("Deve aceitar nome com 100 caracteres")
    void shouldAcceptNameWith100Characters() {
        Product product = new Product();
        String name = "a".repeat(100);
        
        assertDoesNotThrow(() -> product.setName(name));
        assertEquals(name, product.getName());
    }

    @Test
    @DisplayName("Deve remover espaços em branco do nome")
    void shouldTrimNameWhitespace() {
        Product product = new Product();
        product.setName("  Produto  ");
        
        assertEquals("Produto", product.getName());
    }

    @Test
    @DisplayName("Deve aceitar descrição nula")
    void shouldAcceptNullDescription() {
        Product product = new Product();
        
        assertDoesNotThrow(() -> product.setDescription(null));
        assertNull(product.getDescription());
    }

    @Test
    @DisplayName("Deve lançar exceção para descrição muito longa")
    void shouldThrowExceptionForDescriptionTooLong() {
        Product product = new Product();
        String longDescription = "a".repeat(501);
        
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> product.setDescription(longDescription)
        );
        
        assertTrue(exception.getMessage().contains("não pode exceder 500 caracteres"));
    }

    @Test
    @DisplayName("Deve lançar exceção para preço nulo")
    void shouldThrowExceptionForNullPrice() {
        Product product = new Product();
        
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> product.setPrice(null)
        );
        
        assertTrue(exception.getMessage().contains("Preço não pode ser nulo"));
    }

    @Test
    @DisplayName("Deve lançar exceção para preço negativo")
    void shouldThrowExceptionForNegativePrice() {
        Product product = new Product();
        
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> product.setPrice(new BigDecimal("-10.00"))
        );
        
        assertTrue(exception.getMessage().contains("Preço não pode ser negativo"));
    }

    @Test
    @DisplayName("Deve aceitar preço zero")
    void shouldAcceptZeroPrice() {
        Product product = new Product();
        
        assertDoesNotThrow(() -> product.setPrice(BigDecimal.ZERO));
        assertEquals(BigDecimal.ZERO, product.getPrice());
    }

    @Test
    @DisplayName("Deve lançar exceção para preço com mais de 2 casas decimais")
    void shouldThrowExceptionForPriceWithMoreThan2Decimals() {
        Product product = new Product();
        
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> product.setPrice(new BigDecimal("10.999"))
        );
        
        assertTrue(exception.getMessage().contains("não pode ter mais de 2 casas decimais"));
    }

    @Test
    @DisplayName("Deve lançar exceção para quantidade nula")
    void shouldThrowExceptionForNullQuantity() {
        Product product = new Product();
        
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> product.setQuantity(null)
        );
        
        assertTrue(exception.getMessage().contains("Quantidade não pode ser nula"));
    }

    @Test
    @DisplayName("Deve lançar exceção para quantidade negativa")
    void shouldThrowExceptionForNegativeQuantity() {
        Product product = new Product();
        
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> product.setQuantity(-1)
        );
        
        assertTrue(exception.getMessage().contains("Quantidade não pode ser negativa"));
    }

    @Test
    @DisplayName("Deve aceitar quantidade zero")
    void shouldAcceptZeroQuantity() {
        Product product = new Product();
        
        assertDoesNotThrow(() -> product.setQuantity(0));
        assertEquals(0, product.getQuantity());
    }

    @Test
    @DisplayName("Deve validar produto completo")
    void shouldValidateCompleteProduct() {
        Product product = new Product();
        product.setName("Teclado");
        product.setPrice(new BigDecimal("150.00"));
        product.setQuantity(50);
        
        assertDoesNotThrow(() -> product.validate());
    }

    @Test
    @DisplayName("Deve lançar exceção ao validar produto sem nome")
    void shouldThrowExceptionWhenValidatingProductWithoutName() {
        Product product = new Product();
        product.setPrice(new BigDecimal("150.00"));
        product.setQuantity(50);
        
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> product.validate()
        );
        
        assertTrue(exception.getMessage().contains("nome é obrigatório"));
    }

    @Test
    @DisplayName("Deve lançar exceção ao validar produto sem preço")
    void shouldThrowExceptionWhenValidatingProductWithoutPrice() {
        Product product = new Product();
        product.setName("Teclado");
        product.setQuantity(50);
        
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> product.validate()
        );
        
        assertTrue(exception.getMessage().contains("preço é obrigatório"));
    }

    @Test
    @DisplayName("Deve lançar exceção ao validar produto sem quantidade")
    void shouldThrowExceptionWhenValidatingProductWithoutQuantity() {
        Product product = new Product();
        product.setName("Teclado");
        product.setPrice(new BigDecimal("150.00"));
        
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> product.validate()
        );
        
        assertTrue(exception.getMessage().contains("quantidade é obrigatória"));
    }

    @Test
    @DisplayName("Deve verificar igualdade entre produtos com mesmo ID")
    void shouldCheckEqualityBetweenProductsWithSameId() {
        Product product1 = new Product(1L, "Produto", "Desc", new BigDecimal("10.00"), 5);
        Product product2 = new Product(1L, "Outro", "Outra", new BigDecimal("20.00"), 10);
        
        assertEquals(product1, product2);
        assertEquals(product1.hashCode(), product2.hashCode());
    }

    @Test
    @DisplayName("Deve verificar desigualdade entre produtos com IDs diferentes")
    void shouldCheckInequalityBetweenProductsWithDifferentIds() {
        Product product1 = new Product(1L, "Produto", "Desc", new BigDecimal("10.00"), 5);
        Product product2 = new Product(2L, "Produto", "Desc", new BigDecimal("10.00"), 5);
        
        assertNotEquals(product1, product2);
    }

    @Test
    @DisplayName("Deve gerar toString válido")
    void shouldGenerateValidToString() {
        Product product = new Product(1L, "Mouse", "Mouse USB", 
                new BigDecimal("25.50"), 100);
        
        String toString = product.toString();
        
        assertNotNull(toString);
        assertTrue(toString.contains("id=1"));
        assertTrue(toString.contains("name='Mouse'"));
        assertTrue(toString.contains("price=25.50"));
        assertTrue(toString.contains("quantity=100"));
    }
}
