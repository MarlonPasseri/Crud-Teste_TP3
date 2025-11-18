package com.crudsystem.unit;

import com.crudsystem.dao.ProductDAO;
import com.crudsystem.exception.DataAccessException;
import com.crudsystem.exception.ProductNotFoundException;
import com.crudsystem.exception.ValidationException;
import com.crudsystem.model.Product;
import com.crudsystem.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para ProductService com Mockito.
 * Testa lógica de negócio e validações.
 */
@DisplayName("Testes Unitários - ProductService")
class ProductServiceTest {

    @Mock
    private ProductDAO productDAO;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        productService = new ProductService(productDAO);
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar service com DAO nulo")
    void shouldThrowExceptionWhenCreatingServiceWithNullDAO() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new ProductService(null)
        );
        
        assertTrue(exception.getMessage().contains("não pode ser nulo"));
    }

    @Test
    @DisplayName("Deve criar produto válido")
    void shouldCreateValidProduct() throws ValidationException, DataAccessException {
        Product mockProduct = createMockProduct(1L, "Notebook", new BigDecimal("2500.00"), 10);
        when(productDAO.create(any(Product.class))).thenReturn(mockProduct);
        
        Product created = productService.createProduct("Notebook", "Notebook Dell", 
                new BigDecimal("2500.00"), 10);
        
        assertNotNull(created);
        assertEquals("Notebook", created.getName());
        verify(productDAO, times(1)).create(any(Product.class));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t", "\n"})
    @DisplayName("Deve lançar exceção ao criar produto com nome inválido")
    void shouldThrowExceptionWhenCreatingProductWithInvalidName(String invalidName) throws DataAccessException {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> productService.createProduct(invalidName, "Desc", 
                        new BigDecimal("10.00"), 5)
        );
        
        assertTrue(exception.getMessage().contains("Nome"));
        verify(productDAO, never()).create(any(Product.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar produto com nome muito longo")
    void shouldThrowExceptionWhenCreatingProductWithNameTooLong() throws DataAccessException {
        String longName = "a".repeat(101);
        
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> productService.createProduct(longName, "Desc", 
                        new BigDecimal("10.00"), 5)
        );
        
        assertTrue(exception.getMessage().contains("100 caracteres"));
        verify(productDAO, never()).create(any(Product.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar produto com preço nulo")
    void shouldThrowExceptionWhenCreatingProductWithNullPrice() throws DataAccessException {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> productService.createProduct("Produto", "Desc", null, 5)
        );
        
        assertTrue(exception.getMessage().contains("Preço"));
        verify(productDAO, never()).create(any(Product.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar produto com preço negativo")
    void shouldThrowExceptionWhenCreatingProductWithNegativePrice() throws DataAccessException {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> productService.createProduct("Produto", "Desc", 
                        new BigDecimal("-10.00"), 5)
        );
        
        assertTrue(exception.getMessage().contains("negativo"));
        verify(productDAO, never()).create(any(Product.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar produto com preço muito alto")
    void shouldThrowExceptionWhenCreatingProductWithPriceTooHigh() throws DataAccessException {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> productService.createProduct("Produto", "Desc", 
                        new BigDecimal("1000000.00"), 5)
        );
        
        assertTrue(exception.getMessage().contains("999999.99"));
        verify(productDAO, never()).create(any(Product.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar produto com quantidade nula")
    void shouldThrowExceptionWhenCreatingProductWithNullQuantity() throws DataAccessException {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> productService.createProduct("Produto", "Desc", 
                        new BigDecimal("10.00"), null)
        );
        
        assertTrue(exception.getMessage().contains("Quantidade"));
        verify(productDAO, never()).create(any(Product.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar produto com quantidade negativa")
    void shouldThrowExceptionWhenCreatingProductWithNegativeQuantity() throws DataAccessException {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> productService.createProduct("Produto", "Desc", 
                        new BigDecimal("10.00"), -1)
        );
        
        assertTrue(exception.getMessage().contains("negativa"));
        verify(productDAO, never()).create(any(Product.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar produto com quantidade muito alta")
    void shouldThrowExceptionWhenCreatingProductWithQuantityTooHigh() throws DataAccessException {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> productService.createProduct("Produto", "Desc", 
                        new BigDecimal("10.00"), 1000000)
        );
        
        assertTrue(exception.getMessage().contains("999999"));
        verify(productDAO, never()).create(any(Product.class));
    }

    @Test
    @DisplayName("Deve buscar produto por ID")
    void shouldGetProductById() throws ProductNotFoundException, DataAccessException {
        Product mockProduct = createMockProduct(1L, "Mouse", new BigDecimal("50.00"), 20);
        when(productDAO.findById(1L)).thenReturn(Optional.of(mockProduct));
        
        Product found = productService.getProductById(1L);
        
        assertNotNull(found);
        assertEquals(1L, found.getId());
        assertEquals("Mouse", found.getName());
        verify(productDAO, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar produto com ID nulo")
    void shouldThrowExceptionWhenGettingProductWithNullId() throws DataAccessException {
        assertThrows(ProductNotFoundException.class, 
                () -> productService.getProductById(null));
        verify(productDAO, never()).findById(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar produto com ID negativo")
    void shouldThrowExceptionWhenGettingProductWithNegativeId() throws DataAccessException {
        assertThrows(ProductNotFoundException.class, 
                () -> productService.getProductById(-1L));
        verify(productDAO, never()).findById(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar produto inexistente")
    void shouldThrowExceptionWhenGettingNonExistentProduct() throws DataAccessException {
        when(productDAO.findById(999L)).thenReturn(Optional.empty());
        
        assertThrows(ProductNotFoundException.class, 
                () -> productService.getProductById(999L));
        verify(productDAO, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Deve listar todos os produtos")
    void shouldGetAllProducts() throws DataAccessException {
        List<Product> mockProducts = Arrays.asList(
                createMockProduct(1L, "Produto 1", new BigDecimal("10.00"), 5),
                createMockProduct(2L, "Produto 2", new BigDecimal("20.00"), 10)
        );
        when(productDAO.findAll()).thenReturn(mockProducts);
        
        List<Product> products = productService.getAllProducts();
        
        assertNotNull(products);
        assertEquals(2, products.size());
        verify(productDAO, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve atualizar produto existente")
    void shouldUpdateExistingProduct() throws Exception {
        Product mockProduct = createMockProduct(1L, "Teclado", new BigDecimal("100.00"), 25);
        when(productDAO.findById(1L)).thenReturn(Optional.of(mockProduct));
        doNothing().when(productDAO).update(any(Product.class));
        
        assertDoesNotThrow(() -> productService.updateProduct(1L, "Teclado Mecânico", 
                "Teclado RGB", new BigDecimal("250.00"), 15));
        
        verify(productDAO, times(1)).findById(1L);
        verify(productDAO, times(1)).update(any(Product.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar produto com ID inválido")
    void shouldThrowExceptionWhenUpdatingProductWithInvalidId() throws DataAccessException, ProductNotFoundException {
        assertThrows(ValidationException.class, 
                () -> productService.updateProduct(null, "Produto", "Desc", 
                        new BigDecimal("10.00"), 5));
        verify(productDAO, never()).update(any(Product.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar produto inexistente")
    void shouldThrowExceptionWhenUpdatingNonExistentProduct() throws DataAccessException, ProductNotFoundException {
        when(productDAO.findById(999L)).thenReturn(Optional.empty());
        
        assertThrows(ProductNotFoundException.class, 
                () -> productService.updateProduct(999L, "Produto", "Desc", 
                        new BigDecimal("10.00"), 5));
        verify(productDAO, never()).update(any(Product.class));
    }

    @Test
    @DisplayName("Deve deletar produto existente")
    void shouldDeleteExistingProduct() throws Exception {
        doNothing().when(productDAO).delete(1L);
        
        assertDoesNotThrow(() -> productService.deleteProduct(1L));
        
        verify(productDAO, times(1)).delete(1L);
    }

    @Test
    @DisplayName("Deve lançar exceção ao deletar produto com ID inválido")
    void shouldThrowExceptionWhenDeletingProductWithInvalidId() throws DataAccessException, ProductNotFoundException {
        assertThrows(ProductNotFoundException.class, 
                () -> productService.deleteProduct(null));
        verify(productDAO, never()).delete(any());
    }

    @Test
    @DisplayName("Deve verificar se produto existe")
    void shouldCheckIfProductExists() throws DataAccessException {
        when(productDAO.exists(1L)).thenReturn(true);
        when(productDAO.exists(999L)).thenReturn(false);
        
        assertTrue(productService.productExists(1L));
        assertFalse(productService.productExists(999L));
        
        verify(productDAO, times(1)).exists(1L);
        verify(productDAO, times(1)).exists(999L);
    }

    @Test
    @DisplayName("Deve retornar false para ID nulo ao verificar existência")
    void shouldReturnFalseForNullIdWhenCheckingExistence() throws DataAccessException {
        assertFalse(productService.productExists(null));
        verify(productDAO, never()).exists(any());
    }

    // Método auxiliar
    private Product createMockProduct(Long id, String name, BigDecimal price, Integer quantity) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setDescription("Descrição de " + name);
        product.setPrice(price);
        product.setQuantity(quantity);
        return product;
    }
}
