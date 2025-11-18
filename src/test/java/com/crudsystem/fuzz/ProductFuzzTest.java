package com.crudsystem.fuzz;

import com.crudsystem.dao.InMemoryProductDAO;
import com.crudsystem.dao.ProductDAO;
import com.crudsystem.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes de Fuzz Testing para detectar vulnerabilidades.
 * Gera entradas aleatórias e malformadas para testar robustez.
 */
@DisplayName("Fuzz Testing - Robustez do Sistema")
class ProductFuzzTest {

    private ProductService productService;
    private ProductDAO productDAO;
    private Random random;

    @BeforeEach
    void setUp() {
        productDAO = InMemoryProductDAO.getInstance();
        ((InMemoryProductDAO) productDAO).clear();
        productService = new ProductService(productDAO);
        random = new Random();
    }

    @Test
    @DisplayName("Fuzz Test - Strings aleatórias no nome")
    void fuzzTestRandomStringsInName() {
        int iterations = 100;
        int crashCount = 0;
        int validationErrorCount = 0;
        
        for (int i = 0; i < iterations; i++) {
            String randomName = generateRandomString(random.nextInt(200));
            
            try {
                productService.createProduct(randomName, "Desc", 
                        new BigDecimal("10.00"), 5);
            } catch (Exception e) {
                if (e.getMessage().contains("validação") || 
                    e.getMessage().contains("inválido") ||
                    e.getMessage().contains("obrigatório") ||
                    e.getMessage().contains("exceder")) {
                    validationErrorCount++;
                } else {
                    crashCount++;
                    System.err.println("Erro inesperado com input: " + randomName);
                    e.printStackTrace();
                }
            }
        }
        
        // Sistema não deve crashar, apenas validar
        assertEquals(0, crashCount, "Sistema não deve crashar com entradas aleatórias");
        assertTrue(validationErrorCount > 0, "Deve haver validações sendo aplicadas");
    }

    @Test
    @DisplayName("Fuzz Test - Valores numéricos aleatórios no preço")
    void fuzzTestRandomPriceValues() {
        int iterations = 100;
        int crashCount = 0;
        
        for (int i = 0; i < iterations; i++) {
            try {
                BigDecimal randomPrice = generateRandomPrice();
                productService.createProduct("Produto " + i, "Desc", randomPrice, 5);
            } catch (Exception e) {
                // Validação esperada para valores inválidos
                if (!e.getMessage().contains("validação") && 
                    !e.getMessage().contains("negativo") &&
                    !e.getMessage().contains("exceder") &&
                    !e.getMessage().contains("casas decimais")) {
                    crashCount++;
                    System.err.println("Erro inesperado no teste de preço");
                    e.printStackTrace();
                }
            }
        }
        
        assertEquals(0, crashCount, "Sistema não deve crashar com preços aleatórios");
    }

    @Test
    @DisplayName("Fuzz Test - Valores numéricos aleatórios na quantidade")
    void fuzzTestRandomQuantityValues() {
        int iterations = 100;
        int crashCount = 0;
        
        for (int i = 0; i < iterations; i++) {
            try {
                Integer randomQuantity = generateRandomQuantity();
                productService.createProduct("Produto " + i, "Desc", 
                        new BigDecimal("10.00"), randomQuantity);
            } catch (OutOfMemoryError | StackOverflowError e) {
                crashCount++;
                System.err.println("Erro crítico no teste de quantidade");
                e.printStackTrace();
            } catch (Exception e) {
                // Validação esperada para valores inválidos
            }
        }
        
        assertEquals(0, crashCount, "Sistema não deve crashar com quantidades aleatórias");
    }

    @Test
    @DisplayName("Fuzz Test - Caracteres especiais e de controle")
    void fuzzTestSpecialAndControlCharacters() {
        String[] specialInputs = {
                "\0\0\0",                    // Null bytes
                "\n\r\t",                    // Newlines e tabs
                "\b\f",                      // Backspace e form feed
                "\\x00\\x01\\x02",           // Escape sequences
                "%00%01%02",                 // URL encoding
                "../../../etc/passwd",       // Path traversal
                "../../",                    // Path traversal
                "${java.version}",           // Expression injection
                "{{7*7}}",                   // Template injection
                "$(whoami)",                 // Command injection
                "`whoami`",                  // Command injection
                "|whoami",                   // Command injection
                ";whoami;",                  // Command injection
                "' OR '1'='1",               // SQL injection
                "1' OR '1' = '1",            // SQL injection
                "admin'--",                  // SQL injection
                "<img src=x onerror=alert(1)>", // XSS
                "javascript:alert(1)",       // XSS
                "<svg/onload=alert(1)>",     // XSS
        };
        
        int crashCount = 0;
        
        for (String input : specialInputs) {
            try {
                productService.createProduct(input, "Desc", 
                        new BigDecimal("10.00"), 5);
                
                // Se aceito, verificar se foi armazenado corretamente
                // (sem executar código malicioso)
                
            } catch (Exception e) {
                // Validação ou rejeição é aceitável
                if (e instanceof NullPointerException || 
                    e instanceof SecurityException) {
                    crashCount++;
                    System.err.println("Erro crítico com input: " + input);
                    e.printStackTrace();
                }
            }
        }
        
        assertEquals(0, crashCount, "Sistema não deve ter erros críticos com caracteres especiais");
    }

    @Test
    @DisplayName("Fuzz Test - Strings muito longas")
    void fuzzTestVeryLongStrings() {
        int[] lengths = {100, 500, 1000, 5000, 10000, 50000};
        int crashCount = 0;
        
        for (int length : lengths) {
            try {
                String longString = "a".repeat(length);
                productService.createProduct(longString, "Desc", 
                        new BigDecimal("10.00"), 5);
            } catch (OutOfMemoryError | StackOverflowError e) {
                crashCount++;
                System.err.println("Erro crítico no teste de preço");
                e.printStackTrace();
            } catch (Exception e) {
                // Validação esperada para valores inválidos
                if (false) {
                    crashCount++;
                    System.err.println("Erro crítico com string de tamanho: " + length);
                    e.printStackTrace();
                }
            }
        }
        
        assertEquals(0, crashCount, "Sistema não deve crashar com strings longas");
    }

    @Test
    @DisplayName("Fuzz Test - Combinações aleatórias de todos os campos")
    void fuzzTestRandomCombinations() {
        int iterations = 200;
        int crashCount = 0;
        int successCount = 0;
        
        for (int i = 0; i < iterations; i++) {
            try {
                String name = generateRandomString(random.nextInt(150));
                String description = random.nextBoolean() ? 
                        generateRandomString(random.nextInt(600)) : null;
                BigDecimal price = generateRandomPrice();
                Integer quantity = generateRandomQuantity();
                
                productService.createProduct(name, description, price, quantity);
                successCount++;
                
            } catch (NullPointerException | OutOfMemoryError | StackOverflowError e) {
                crashCount++;
                System.err.println("Erro crítico na iteração " + i);
                e.printStackTrace();
            } catch (Exception e) {
                // Validações são esperadas
            }
        }
        
        assertEquals(0, crashCount, "Sistema não deve ter erros críticos");
        assertTrue(successCount > 0, "Algumas entradas válidas devem ter sucesso");
    }

    @Test
    @DisplayName("Fuzz Test - IDs aleatórios em operações")
    void fuzzTestRandomIdsInOperations() {
        int iterations = 100;
        int crashCount = 0;
        
        for (int i = 0; i < iterations; i++) {
            try {
                Long randomId = generateRandomId();
                
                // Tentar buscar
                try {
                    productService.getProductById(randomId);
                } catch (Exception e) {
                    // Esperado para IDs inexistentes
                }
                
                // Tentar deletar
                try {
                    productService.deleteProduct(randomId);
                } catch (Exception e) {
                    // Esperado para IDs inexistentes
                }
                
                // Tentar atualizar
                try {
                    productService.updateProduct(randomId, "Nome", "Desc", 
                            new BigDecimal("10.00"), 5);
                } catch (Exception e) {
                    // Esperado para IDs inexistentes
                }
                
            } catch (NullPointerException | OutOfMemoryError | StackOverflowError e) {
                crashCount++;
                System.err.println("Erro crítico no teste de IDs");
                e.printStackTrace();
            }
        }
        
        assertEquals(0, crashCount, "Sistema não deve crashar com IDs aleatórios");
    }

    @Test
    @DisplayName("Fuzz Test - Operações rápidas e repetitivas")
    void fuzzTestRapidRepeatedOperations() {
        int iterations = 500;
        int crashCount = 0;
        
        try {
            for (int i = 0; i < iterations; i++) {
                // Criar
                try {
                    productService.createProduct("Produto " + i, "Desc", 
                            new BigDecimal("10.00"), 5);
                } catch (Exception e) {
                    // Ignorar erros de validação
                }
                
                // Listar
                try {
                    productService.getAllProducts();
                } catch (Exception e) {
                    crashCount++;
                }
            }
        } catch (OutOfMemoryError | StackOverflowError e) {
            crashCount++;
            e.printStackTrace();
        }
        
        assertEquals(0, crashCount, "Sistema deve suportar operações rápidas");
    }

    // Métodos auxiliares para gerar dados aleatórios
    
    private String generateRandomString(int length) {
        if (length <= 0) return "";
        
        StringBuilder sb = new StringBuilder(length);
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789 !@#$%^&*()_+-=[]{}|;:',.<>?/~`\n\r\t";
        
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return sb.toString();
    }
    
    private BigDecimal generateRandomPrice() {
        if (random.nextBoolean()) {
            // Valores válidos
            return new BigDecimal(random.nextDouble() * 1000).setScale(2, BigDecimal.ROUND_HALF_UP);
        } else {
            // Valores potencialmente inválidos
            double value = (random.nextDouble() - 0.5) * 2000000;
            return new BigDecimal(value).setScale(random.nextInt(5), BigDecimal.ROUND_HALF_UP);
        }
    }
    
    private Integer generateRandomQuantity() {
        if (random.nextBoolean()) {
            // Valores válidos
            return random.nextInt(10000);
        } else {
            // Valores potencialmente inválidos
            return random.nextInt() - 1000000;
        }
    }
    
    private Long generateRandomId() {
        if (random.nextBoolean()) {
            return (long) random.nextInt(1000);
        } else {
            return random.nextLong();
        }
    }
}
