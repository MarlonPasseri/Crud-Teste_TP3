package com.crudsystem.integration;

import com.crudsystem.dao.InMemoryProductDAO;
import com.crudsystem.dao.ProductDAO;
import com.crudsystem.exception.DataAccessException;
import com.crudsystem.exception.ProductNotFoundException;
import com.crudsystem.exception.ValidationException;
import com.crudsystem.model.Product;
import com.crudsystem.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes de cenários de falha e situações adversas.
 * Simula erros de rede, timeouts, entradas inválidas e sobrecarga.
 */
@DisplayName("Testes de Falhas e Cenários Adversos")
class FailureScenarioTest {

    private ProductService productService;
    private ProductDAO productDAO;

    @BeforeEach
    void setUp() {
        productDAO = InMemoryProductDAO.getInstance();
        ((InMemoryProductDAO) productDAO).clear();
        productService = new ProductService(productDAO);
    }

    @Test
    @DisplayName("Deve lidar com entrada maliciosa - SQL Injection")
    void shouldHandleSQLInjectionAttempt() throws Exception {
        String maliciousInput = "'; DROP TABLE products; --";
        
        // Sistema deve aceitar mas armazenar como string literal
        // (proteção contra SQL injection é responsabilidade do DAO)
        Product product = productService.createProduct(maliciousInput, "Desc", 
                new BigDecimal("10.00"), 5);
        
        assertNotNull(product);
        assertEquals(maliciousInput, product.getName());
    }

    @Test
    @DisplayName("Deve lidar com entrada maliciosa - XSS")
    void shouldHandleXSSAttempt() throws Exception {
        String xssInput = "<script>alert('XSS')</script>";
        
        // O sistema deve aceitar mas sanitizar na saída
        Product product = productService.createProduct(xssInput, "Desc", 
                new BigDecimal("10.00"), 5);
        
        assertNotNull(product);
        assertEquals(xssInput, product.getName()); // Armazenado como está
        // A sanitização deve ocorrer na camada de apresentação
    }

    @Test
    @DisplayName("Deve lidar com entrada extremamente longa")
    void shouldHandleExtremelyLongInput() {
        String longInput = "a".repeat(10000);
        
        assertThrows(ValidationException.class, 
                () -> productService.createProduct(longInput, "Desc", 
                        new BigDecimal("10.00"), 5));
    }

    @Test
    @DisplayName("Deve lidar com caracteres especiais")
    void shouldHandleSpecialCharacters() throws Exception {
        String specialChars = "Produto @#$%^&*()_+-=[]{}|;:',.<>?/~`";
        
        Product product = productService.createProduct(specialChars, "Desc", 
                new BigDecimal("10.00"), 5);
        
        assertNotNull(product);
        assertEquals(specialChars, product.getName());
    }

    @Test
    @DisplayName("Deve lidar com caracteres Unicode")
    void shouldHandleUnicodeCharacters() throws Exception {
        String unicode = "Produto 中文 العربية עברית 日本語 한국어";
        
        Product product = productService.createProduct(unicode, "Desc", 
                new BigDecimal("10.00"), 5);
        
        assertNotNull(product);
        assertEquals(unicode, product.getName());
    }

    @Test
    @DisplayName("Deve lidar com valores numéricos extremos")
    void shouldHandleExtremeNumericValues() {
        // Preço máximo permitido
        assertThrows(ValidationException.class, 
                () -> productService.createProduct("Produto", "Desc", 
                        new BigDecimal("1000000.00"), 5));
        
        // Quantidade máxima permitida
        assertThrows(ValidationException.class, 
                () -> productService.createProduct("Produto", "Desc", 
                        new BigDecimal("10.00"), 1000000));
    }

    @Test
    @DisplayName("Deve lidar com valores numéricos no limite")
    void shouldHandleBoundaryNumericValues() throws Exception {
        // Valores no limite devem ser aceitos
        Product product1 = productService.createProduct("Produto 1", "Desc", 
                new BigDecimal("999999.99"), 5);
        assertNotNull(product1);
        
        Product product2 = productService.createProduct("Produto 2", "Desc", 
                new BigDecimal("10.00"), 999999);
        assertNotNull(product2);
        
        // Valores zero devem ser aceitos
        Product product3 = productService.createProduct("Produto 3", "Desc", 
                BigDecimal.ZERO, 0);
        assertNotNull(product3);
    }

    @Test
    @DisplayName("Deve lidar com múltiplas operações simultâneas - Concorrência")
    void shouldHandleConcurrentOperations() throws InterruptedException {
        int threadCount = 50;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    productService.createProduct(
                            "Produto " + index,
                            "Descrição " + index,
                            new BigDecimal("10.00"),
                            5
                    );
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();
        
        // Todas as operações devem ter sucesso
        assertEquals(threadCount, successCount.get());
        assertEquals(0, errorCount.get());
    }

    @Test
    @DisplayName("Deve lidar com operações de leitura e escrita simultâneas")
    void shouldHandleConcurrentReadWriteOperations() throws Exception {
        // Criar produto inicial
        Product initial = productService.createProduct("Produto Inicial", "Desc", 
                new BigDecimal("100.00"), 10);
        
        int threadCount = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        // Metade lê, metade escreve
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    if (index % 2 == 0) {
                        // Operação de leitura
                        productService.getProductById(initial.getId());
                    } else {
                        // Operação de escrita
                        productService.createProduct(
                                "Produto " + index,
                                "Descrição",
                                new BigDecimal("50.00"),
                                5
                        );
                    }
                } catch (Exception e) {
                    // Ignorar erros esperados
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();
        
        // Sistema deve permanecer consistente
        assertTrue(productService.productExists(initial.getId()));
    }

    @Test
    @DisplayName("Deve lidar com sobrecarga - Muitas requisições")
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void shouldHandleHighLoad() throws Exception {
        int requestCount = 1000;
        
        for (int i = 0; i < requestCount; i++) {
            productService.createProduct(
                    "Produto " + i,
                    "Descrição",
                    new BigDecimal("10.00"),
                    5
            );
        }
        
        // Verificar se todos foram criados
        assertEquals(requestCount, productDAO.count());
    }

    @Test
    @DisplayName("Deve lidar com operação em produto inexistente")
    void shouldHandleOperationOnNonExistentProduct() {
        assertThrows(ProductNotFoundException.class, 
                () -> productService.getProductById(999999L));
        
        assertThrows(ProductNotFoundException.class, 
                () -> productService.updateProduct(999999L, "Nome", "Desc", 
                        new BigDecimal("10.00"), 5));
        
        assertThrows(ProductNotFoundException.class, 
                () -> productService.deleteProduct(999999L));
    }

    @Test
    @DisplayName("Deve lidar com IDs inválidos")
    void shouldHandleInvalidIds() {
        assertThrows(ProductNotFoundException.class, 
                () -> productService.getProductById(null));
        
        assertThrows(ProductNotFoundException.class, 
                () -> productService.getProductById(-1L));
        
        assertThrows(ProductNotFoundException.class, 
                () -> productService.getProductById(0L));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "0.001", "0.999", "10.123", "99.9999"
    })
    @DisplayName("Deve rejeitar preços com mais de 2 casas decimais")
    void shouldRejectPricesWithMoreThan2Decimals(String price) {
        assertThrows(Exception.class, 
                () -> productService.createProduct("Produto", "Desc", 
                        new BigDecimal(price), 5));
    }

    @Test
    @DisplayName("Deve lidar com atualização de produto deletado")
    void shouldHandleUpdateOfDeletedProduct() throws Exception {
        Product product = productService.createProduct("Produto", "Desc", 
                new BigDecimal("10.00"), 5);
        Long id = product.getId();
        
        productService.deleteProduct(id);
        
        assertThrows(ProductNotFoundException.class, 
                () -> productService.updateProduct(id, "Novo Nome", "Nova Desc", 
                        new BigDecimal("20.00"), 10));
    }

    @Test
    @DisplayName("Deve lidar com múltiplas deleções do mesmo produto")
    void shouldHandleMultipleDeletionsOfSameProduct() throws Exception {
        Product product = productService.createProduct("Produto", "Desc", 
                new BigDecimal("10.00"), 5);
        Long id = product.getId();
        
        productService.deleteProduct(id);
        
        assertThrows(ProductNotFoundException.class, 
                () -> productService.deleteProduct(id));
    }

    @Test
    @DisplayName("Deve lidar com espaços em branco excessivos")
    void shouldHandleExcessiveWhitespace() throws Exception {
        String nameWithSpaces = "   Produto   Com   Espaços   ";
        
        Product product = productService.createProduct(nameWithSpaces, "Desc", 
                new BigDecimal("10.00"), 5);
        
        // Nome deve ser trimmed
        assertEquals("Produto   Com   Espaços", product.getName());
    }

    @Test
    @DisplayName("Deve lidar com descrição nula")
    void shouldHandleNullDescription() throws Exception {
        Product product = productService.createProduct("Produto", null, 
                new BigDecimal("10.00"), 5);
        
        assertNotNull(product);
        assertNull(product.getDescription());
    }

    @Test
    @DisplayName("Deve lidar com recuperação após erro")
    void shouldRecoverAfterError() throws Exception {
        // Tentar criar produto inválido
        try {
            productService.createProduct("", "Desc", new BigDecimal("10.00"), 5);
        } catch (ValidationException e) {
            // Esperado
        }
        
        // Sistema deve continuar funcionando
        Product product = productService.createProduct("Produto Válido", "Desc", 
                new BigDecimal("10.00"), 5);
        
        assertNotNull(product);
        assertEquals(1, productDAO.count());
    }

    @Test
    @DisplayName("Deve manter consistência após múltiplos erros")
    void shouldMaintainConsistencyAfterMultipleErrors() throws Exception {
        // Criar produto válido
        Product valid = productService.createProduct("Produto 1", "Desc", 
                new BigDecimal("10.00"), 5);
        
        // Tentar várias operações inválidas
        for (int i = 0; i < 10; i++) {
            try {
                productService.createProduct("", "Desc", new BigDecimal("10.00"), 5);
            } catch (ValidationException e) {
                // Esperado
            }
        }
        
        // Verificar consistência
        assertEquals(1, productDAO.count());
        assertTrue(productService.productExists(valid.getId()));
        
        // Criar outro produto válido
        Product valid2 = productService.createProduct("Produto 2", "Desc", 
                new BigDecimal("20.00"), 10);
        
        assertEquals(2, productDAO.count());
        assertNotNull(valid2);
    }
}
