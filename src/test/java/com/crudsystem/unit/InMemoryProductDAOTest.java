package com.crudsystem.unit;

import com.crudsystem.dao.InMemoryProductDAO;
import com.crudsystem.exception.DataAccessException;
import com.crudsystem.exception.ProductNotFoundException;
import com.crudsystem.model.Product;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para InMemoryProductDAO.
 * Testa todas as operações CRUD e cenários de erro.
 */
@DisplayName("Testes Unitários - InMemoryProductDAO")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class InMemoryProductDAOTest {

    private InMemoryProductDAO dao;

    @BeforeEach
    void setUp() {
        dao = InMemoryProductDAO.getInstance();
        dao.clear();
    }

    @Test
    @Order(1)
    @DisplayName("Deve criar produto válido")
    void shouldCreateValidProduct() throws DataAccessException {
        Product product = createTestProduct("Notebook", new BigDecimal("2500.00"), 10);
        
        Product created = dao.create(product);
        
        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals("Notebook", created.getName());
        assertEquals(new BigDecimal("2500.00"), created.getPrice());
        assertEquals(10, created.getQuantity());
        assertNotNull(created.getCreatedAt());
        assertNotNull(created.getUpdatedAt());
    }

    @Test
    @Order(2)
    @DisplayName("Deve lançar exceção ao criar produto nulo")
    void shouldThrowExceptionWhenCreatingNullProduct() {
        DataAccessException exception = assertThrows(
                DataAccessException.class,
                () -> dao.create(null)
        );
        
        assertTrue(exception.getMessage().contains("não pode ser nulo"));
    }

    @Test
    @Order(3)
    @DisplayName("Deve lançar exceção ao criar produto inválido")
    void shouldThrowExceptionWhenCreatingInvalidProduct() {
        Product product = new Product();
        // Produto sem campos obrigatórios
        
        DataAccessException exception = assertThrows(
                DataAccessException.class,
                () -> dao.create(product)
        );
        
        assertTrue(exception.getMessage().contains("validação"));
    }

    @Test
    @Order(4)
    @DisplayName("Deve buscar produto por ID")
    void shouldFindProductById() throws DataAccessException {
        Product product = createTestProduct("Mouse", new BigDecimal("50.00"), 20);
        Product created = dao.create(product);
        
        Optional<Product> found = dao.findById(created.getId());
        
        assertTrue(found.isPresent());
        assertEquals(created.getId(), found.get().getId());
        assertEquals("Mouse", found.get().getName());
    }

    @Test
    @Order(5)
    @DisplayName("Deve retornar Optional vazio para ID inexistente")
    void shouldReturnEmptyOptionalForNonExistentId() throws DataAccessException {
        Optional<Product> found = dao.findById(999L);
        
        assertFalse(found.isPresent());
    }

    @Test
    @Order(6)
    @DisplayName("Deve lançar exceção ao buscar com ID nulo")
    void shouldThrowExceptionWhenFindingByNullId() {
        DataAccessException exception = assertThrows(
                DataAccessException.class,
                () -> dao.findById(null)
        );
        
        assertTrue(exception.getMessage().contains("ID inválido"));
    }

    @Test
    @Order(7)
    @DisplayName("Deve lançar exceção ao buscar com ID negativo")
    void shouldThrowExceptionWhenFindingByNegativeId() {
        DataAccessException exception = assertThrows(
                DataAccessException.class,
                () -> dao.findById(-1L)
        );
        
        assertTrue(exception.getMessage().contains("ID inválido"));
    }

    @Test
    @Order(8)
    @DisplayName("Deve listar todos os produtos")
    void shouldFindAllProducts() throws DataAccessException {
        dao.create(createTestProduct("Produto 1", new BigDecimal("10.00"), 5));
        dao.create(createTestProduct("Produto 2", new BigDecimal("20.00"), 10));
        dao.create(createTestProduct("Produto 3", new BigDecimal("30.00"), 15));
        
        List<Product> products = dao.findAll();
        
        assertNotNull(products);
        assertEquals(3, products.size());
    }

    @Test
    @Order(9)
    @DisplayName("Deve retornar lista vazia quando não há produtos")
    void shouldReturnEmptyListWhenNoProducts() throws DataAccessException {
        List<Product> products = dao.findAll();
        
        assertNotNull(products);
        assertTrue(products.isEmpty());
    }

    @Test
    @Order(10)
    @DisplayName("Deve atualizar produto existente")
    void shouldUpdateExistingProduct() throws DataAccessException, ProductNotFoundException {
        Product product = createTestProduct("Teclado", new BigDecimal("100.00"), 25);
        Product created = dao.create(product);
        
        created.setName("Teclado Mecânico");
        created.setPrice(new BigDecimal("250.00"));
        created.setQuantity(15);
        
        dao.update(created);
        
        Optional<Product> updated = dao.findById(created.getId());
        assertTrue(updated.isPresent());
        assertEquals("Teclado Mecânico", updated.get().getName());
        assertEquals(new BigDecimal("250.00"), updated.get().getPrice());
        assertEquals(15, updated.get().getQuantity());
    }

    @Test
    @Order(11)
    @DisplayName("Deve lançar exceção ao atualizar produto nulo")
    void shouldThrowExceptionWhenUpdatingNullProduct() {
        DataAccessException exception = assertThrows(
                DataAccessException.class,
                () -> dao.update(null)
        );
        
        assertTrue(exception.getMessage().contains("não pode ser nulo"));
    }

    @Test
    @Order(12)
    @DisplayName("Deve lançar exceção ao atualizar produto sem ID")
    void shouldThrowExceptionWhenUpdatingProductWithoutId() {
        Product product = createTestProduct("Produto", new BigDecimal("10.00"), 5);
        
        DataAccessException exception = assertThrows(
                DataAccessException.class,
                () -> dao.update(product)
        );
        
        assertTrue(exception.getMessage().contains("ID"));
    }

    @Test
    @Order(13)
    @DisplayName("Deve lançar exceção ao atualizar produto inexistente")
    void shouldThrowExceptionWhenUpdatingNonExistentProduct() {
        Product product = createTestProduct("Produto", new BigDecimal("10.00"), 5);
        product.setId(999L);
        
        assertThrows(ProductNotFoundException.class, () -> dao.update(product));
    }

    @Test
    @Order(14)
    @DisplayName("Deve deletar produto existente")
    void shouldDeleteExistingProduct() throws DataAccessException, ProductNotFoundException {
        Product product = createTestProduct("Monitor", new BigDecimal("800.00"), 8);
        Product created = dao.create(product);
        
        dao.delete(created.getId());
        
        Optional<Product> deleted = dao.findById(created.getId());
        assertFalse(deleted.isPresent());
    }

    @Test
    @Order(15)
    @DisplayName("Deve lançar exceção ao deletar produto inexistente")
    void shouldThrowExceptionWhenDeletingNonExistentProduct() {
        assertThrows(ProductNotFoundException.class, () -> dao.delete(999L));
    }

    @Test
    @Order(16)
    @DisplayName("Deve lançar exceção ao deletar com ID nulo")
    void shouldThrowExceptionWhenDeletingWithNullId() {
        DataAccessException exception = assertThrows(
                DataAccessException.class,
                () -> dao.delete(null)
        );
        
        assertTrue(exception.getMessage().contains("ID inválido"));
    }

    @Test
    @Order(17)
    @DisplayName("Deve verificar se produto existe")
    void shouldCheckIfProductExists() throws DataAccessException {
        Product product = createTestProduct("Webcam", new BigDecimal("200.00"), 12);
        Product created = dao.create(product);
        
        assertTrue(dao.exists(created.getId()));
        assertFalse(dao.exists(999L));
    }

    @Test
    @Order(18)
    @DisplayName("Deve retornar false para ID nulo ao verificar existência")
    void shouldReturnFalseForNullIdWhenCheckingExistence() throws DataAccessException {
        assertFalse(dao.exists(null));
    }

    @Test
    @Order(19)
    @DisplayName("Deve contar produtos corretamente")
    void shouldCountProductsCorrectly() throws DataAccessException {
        assertEquals(0, dao.count());
        
        dao.create(createTestProduct("Produto 1", new BigDecimal("10.00"), 5));
        assertEquals(1, dao.count());
        
        dao.create(createTestProduct("Produto 2", new BigDecimal("20.00"), 10));
        assertEquals(2, dao.count());
        
        dao.create(createTestProduct("Produto 3", new BigDecimal("30.00"), 15));
        assertEquals(3, dao.count());
    }

    @Test
    @Order(20)
    @DisplayName("Deve limpar todos os produtos")
    void shouldClearAllProducts() throws DataAccessException {
        dao.create(createTestProduct("Produto 1", new BigDecimal("10.00"), 5));
        dao.create(createTestProduct("Produto 2", new BigDecimal("20.00"), 10));
        
        assertEquals(2, dao.count());
        
        dao.clear();
        
        assertEquals(0, dao.count());
        assertTrue(dao.findAll().isEmpty());
    }

    @Test
    @Order(21)
    @DisplayName("Deve gerar IDs sequenciais")
    void shouldGenerateSequentialIds() throws DataAccessException {
        Product p1 = dao.create(createTestProduct("Produto 1", new BigDecimal("10.00"), 5));
        Product p2 = dao.create(createTestProduct("Produto 2", new BigDecimal("20.00"), 10));
        Product p3 = dao.create(createTestProduct("Produto 3", new BigDecimal("30.00"), 15));
        
        assertTrue(p1.getId() < p2.getId());
        assertTrue(p2.getId() < p3.getId());
    }

    // Método auxiliar
    private Product createTestProduct(String name, BigDecimal price, Integer quantity) {
        Product product = new Product();
        product.setName(name);
        product.setDescription("Descrição de " + name);
        product.setPrice(price);
        product.setQuantity(quantity);
        return product;
    }
}
