package com.crudsystem.service;

import com.crudsystem.dao.ProductDAO;
import com.crudsystem.exception.DataAccessException;
import com.crudsystem.exception.ProductNotFoundException;
import com.crudsystem.exception.ValidationException;
import com.crudsystem.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;

/**
 * Camada de serviço para lógica de negócio relacionada a produtos.
 * Implementa validações adicionais e tratamento robusto de erros.
 */
public class ProductService {
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);
    
    private final ProductDAO productDAO;
    
    public ProductService(ProductDAO productDAO) {
        if (productDAO == null) {
            throw new IllegalArgumentException("ProductDAO não pode ser nulo");
        }
        this.productDAO = productDAO;
        logger.info("ProductService inicializado");
    }
    
    /**
     * Cria um novo produto com validações de negócio.
     */
    public Product createProduct(String name, String description, BigDecimal price, Integer quantity) 
            throws ValidationException, DataAccessException {
        
        logger.debug("Criando produto: name={}, price={}, quantity={}", name, price, quantity);
        
        // Fail early: validação de entrada
        validateProductInput(name, price, quantity);
        
        try {
            Product product = new Product();
            product.setName(name);
            product.setDescription(description);
            product.setPrice(price);
            product.setQuantity(quantity);
            
            Product created = productDAO.create(product);
            logger.info("Produto criado com sucesso: ID={}", created.getId());
            return created;
            
        } catch (IllegalArgumentException e) {
            logger.error("Erro de validação ao criar produto: {}", e.getMessage());
            throw new ValidationException("Dados inválidos: " + e.getMessage(), e);
        } catch (DataAccessException e) {
            logger.error("Erro ao acessar dados ao criar produto", e);
            throw e;
        } catch (Exception e) {
            logger.error("Erro inesperado ao criar produto", e);
            throw new DataAccessException("Erro inesperado: " + e.getMessage(), e);
        }
    }
    
    /**
     * Busca um produto por ID.
     */
    public Product getProductById(Long id) throws ProductNotFoundException, DataAccessException {
        if (id == null || id <= 0) {
            logger.warn("Tentativa de buscar produto com ID inválido: {}", id);
            throw new ProductNotFoundException("ID inválido: " + id);
        }
        
        try {
            return productDAO.findById(id)
                    .orElseThrow(() -> new ProductNotFoundException(id));
        } catch (ProductNotFoundException e) {
            logger.warn("Produto não encontrado: ID={}", id);
            throw e;
        } catch (DataAccessException e) {
            logger.error("Erro ao buscar produto: ID={}", id, e);
            throw e;
        }
    }
    
    /**
     * Lista todos os produtos.
     */
    public List<Product> getAllProducts() throws DataAccessException {
        try {
            List<Product> products = productDAO.findAll();
            logger.debug("Listados {} produtos", products.size());
            return products;
        } catch (DataAccessException e) {
            logger.error("Erro ao listar produtos", e);
            throw e;
        }
    }
    
    /**
     * Atualiza um produto existente.
     */
    public void updateProduct(Long id, String name, String description, BigDecimal price, Integer quantity) 
            throws ProductNotFoundException, ValidationException, DataAccessException {
        
        logger.debug("Atualizando produto: ID={}", id);
        
        if (id == null || id <= 0) {
            throw new ValidationException("ID inválido: " + id);
        }
        
        // Fail early: validação de entrada
        validateProductInput(name, price, quantity);
        
        try {
            // Verifica se o produto existe
            Product product = getProductById(id);
            
            // Atualiza os campos
            product.setName(name);
            product.setDescription(description);
            product.setPrice(price);
            product.setQuantity(quantity);
            
            productDAO.update(product);
            logger.info("Produto atualizado com sucesso: ID={}", id);
            
        } catch (ProductNotFoundException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            logger.error("Erro de validação ao atualizar produto: {}", e.getMessage());
            throw new ValidationException("Dados inválidos: " + e.getMessage(), e);
        } catch (DataAccessException e) {
            logger.error("Erro ao acessar dados ao atualizar produto", e);
            throw e;
        } catch (Exception e) {
            logger.error("Erro inesperado ao atualizar produto", e);
            throw new DataAccessException("Erro inesperado: " + e.getMessage(), e);
        }
    }
    
    /**
     * Remove um produto.
     */
    public void deleteProduct(Long id) throws ProductNotFoundException, DataAccessException {
        if (id == null || id <= 0) {
            logger.warn("Tentativa de deletar produto com ID inválido: {}", id);
            throw new ProductNotFoundException("ID inválido: " + id);
        }
        
        try {
            productDAO.delete(id);
            logger.info("Produto deletado com sucesso: ID={}", id);
        } catch (ProductNotFoundException e) {
            logger.warn("Produto não encontrado para deleção: ID={}", id);
            throw e;
        } catch (DataAccessException e) {
            logger.error("Erro ao deletar produto: ID={}", id, e);
            throw e;
        }
    }
    
    /**
     * Verifica se um produto existe.
     */
    public boolean productExists(Long id) throws DataAccessException {
        if (id == null || id <= 0) {
            return false;
        }
        
        try {
            return productDAO.exists(id);
        } catch (DataAccessException e) {
            logger.error("Erro ao verificar existência do produto: ID={}", id, e);
            throw e;
        }
    }
    
    /**
     * Valida entrada de dados do produto (fail early).
     */
    private void validateProductInput(String name, BigDecimal price, Integer quantity) 
            throws ValidationException {
        
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Nome do produto é obrigatório");
        }
        
        if (name.length() > 100) {
            throw new ValidationException("Nome do produto não pode exceder 100 caracteres");
        }
        
        if (price == null) {
            throw new ValidationException("Preço é obrigatório");
        }
        
        if (price.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Preço não pode ser negativo");
        }
        
        if (price.compareTo(new BigDecimal("999999.99")) > 0) {
            throw new ValidationException("Preço não pode exceder 999999.99");
        }
        
        if (quantity == null) {
            throw new ValidationException("Quantidade é obrigatória");
        }
        
        if (quantity < 0) {
            throw new ValidationException("Quantidade não pode ser negativa");
        }
        
        if (quantity > 999999) {
            throw new ValidationException("Quantidade não pode exceder 999999");
        }
    }
}
