package com.crudsystem.dao;

import com.crudsystem.exception.DataAccessException;
import com.crudsystem.exception.ProductNotFoundException;
import com.crudsystem.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Implementação em memória do ProductDAO usando estruturas thread-safe.
 * Simula persistência com validações robustas e tratamento de erros.
 */
public class InMemoryProductDAO implements ProductDAO {
    private static final Logger logger = LoggerFactory.getLogger(InMemoryProductDAO.class);
    
    private final Map<Long, Product> products = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    
    private static InMemoryProductDAO instance;
    
    private InMemoryProductDAO() {
        logger.info("InMemoryProductDAO inicializado");
    }
    
    /**
     * Singleton thread-safe para garantir única instância do DAO.
     */
    public static synchronized InMemoryProductDAO getInstance() {
        if (instance == null) {
            instance = new InMemoryProductDAO();
        }
        return instance;
    }

    @Override
    public Product create(Product product) throws DataAccessException {
        if (product == null) {
            logger.error("Tentativa de criar produto nulo");
            throw new DataAccessException("Produto não pode ser nulo");
        }
        
        try {
            product.validate();
            
            Long id = idGenerator.getAndIncrement();
            product.setId(id);
            product.setCreatedAt(LocalDateTime.now());
            product.setUpdatedAt(LocalDateTime.now());
            
            products.put(id, product);
            logger.info("Produto criado com sucesso: ID={}", id);
            
            return product;
        } catch (IllegalStateException e) {
            logger.error("Erro de validação ao criar produto: {}", e.getMessage());
            throw new DataAccessException("Erro de validação: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Erro inesperado ao criar produto", e);
            throw new DataAccessException("Erro ao criar produto: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Product> findById(Long id) throws DataAccessException {
        if (id == null || id <= 0) {
            logger.warn("Tentativa de buscar produto com ID inválido: {}", id);
            throw new DataAccessException("ID inválido: " + id);
        }
        
        try {
            Product product = products.get(id);
            logger.debug("Busca por ID={}: {}", id, product != null ? "encontrado" : "não encontrado");
            return Optional.ofNullable(product);
        } catch (Exception e) {
            logger.error("Erro ao buscar produto por ID={}", id, e);
            throw new DataAccessException("Erro ao buscar produto: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Product> findAll() throws DataAccessException {
        try {
            List<Product> allProducts = new ArrayList<>(products.values());
            logger.debug("Listando todos os produtos: {} encontrados", allProducts.size());
            return allProducts;
        } catch (Exception e) {
            logger.error("Erro ao listar produtos", e);
            throw new DataAccessException("Erro ao listar produtos: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Product product) throws ProductNotFoundException, DataAccessException {
        if (product == null) {
            logger.error("Tentativa de atualizar produto nulo");
            throw new DataAccessException("Produto não pode ser nulo");
        }
        
        if (product.getId() == null) {
            logger.error("Tentativa de atualizar produto sem ID");
            throw new DataAccessException("Produto deve ter um ID para ser atualizado");
        }
        
        try {
            if (!products.containsKey(product.getId())) {
                logger.warn("Tentativa de atualizar produto inexistente: ID={}", product.getId());
                throw new ProductNotFoundException(product.getId());
            }
            
            product.validate();
            product.setUpdatedAt(LocalDateTime.now());
            
            products.put(product.getId(), product);
            logger.info("Produto atualizado com sucesso: ID={}", product.getId());
            
        } catch (ProductNotFoundException e) {
            throw e;
        } catch (IllegalStateException e) {
            logger.error("Erro de validação ao atualizar produto: {}", e.getMessage());
            throw new DataAccessException("Erro de validação: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Erro inesperado ao atualizar produto", e);
            throw new DataAccessException("Erro ao atualizar produto: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(Long id) throws ProductNotFoundException, DataAccessException {
        if (id == null || id <= 0) {
            logger.error("Tentativa de deletar produto com ID inválido: {}", id);
            throw new DataAccessException("ID inválido: " + id);
        }
        
        try {
            Product removed = products.remove(id);
            if (removed == null) {
                logger.warn("Tentativa de deletar produto inexistente: ID={}", id);
                throw new ProductNotFoundException(id);
            }
            logger.info("Produto deletado com sucesso: ID={}", id);
            
        } catch (ProductNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Erro inesperado ao deletar produto", e);
            throw new DataAccessException("Erro ao deletar produto: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean exists(Long id) throws DataAccessException {
        if (id == null || id <= 0) {
            return false;
        }
        
        try {
            return products.containsKey(id);
        } catch (Exception e) {
            logger.error("Erro ao verificar existência do produto", e);
            throw new DataAccessException("Erro ao verificar produto: " + e.getMessage(), e);
        }
    }

    @Override
    public long count() throws DataAccessException {
        try {
            return products.size();
        } catch (Exception e) {
            logger.error("Erro ao contar produtos", e);
            throw new DataAccessException("Erro ao contar produtos: " + e.getMessage(), e);
        }
    }
    
    /**
     * Método auxiliar para limpar todos os dados (útil para testes).
     */
    public void clear() {
        products.clear();
        idGenerator.set(1);
        logger.info("Todos os produtos foram removidos");
    }
}
