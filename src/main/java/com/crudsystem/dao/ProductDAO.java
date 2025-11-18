package com.crudsystem.dao;

import com.crudsystem.exception.DataAccessException;
import com.crudsystem.exception.ProductNotFoundException;
import com.crudsystem.model.Product;

import java.util.List;
import java.util.Optional;

/**
 * Interface DAO para operações de persistência de produtos.
 * Define contrato para acesso a dados com tratamento robusto de erros.
 */
public interface ProductDAO {
    
    /**
     * Cria um novo produto no sistema.
     * @param product produto a ser criado
     * @return produto criado com ID gerado
     * @throws DataAccessException se houver erro na operação
     */
    Product create(Product product) throws DataAccessException;
    
    /**
     * Busca um produto por ID.
     * @param id identificador do produto
     * @return Optional contendo o produto se encontrado
     * @throws DataAccessException se houver erro na operação
     */
    Optional<Product> findById(Long id) throws DataAccessException;
    
    /**
     * Lista todos os produtos.
     * @return lista de produtos
     * @throws DataAccessException se houver erro na operação
     */
    List<Product> findAll() throws DataAccessException;
    
    /**
     * Atualiza um produto existente.
     * @param product produto com dados atualizados
     * @throws ProductNotFoundException se o produto não existir
     * @throws DataAccessException se houver erro na operação
     */
    void update(Product product) throws ProductNotFoundException, DataAccessException;
    
    /**
     * Remove um produto por ID.
     * @param id identificador do produto
     * @throws ProductNotFoundException se o produto não existir
     * @throws DataAccessException se houver erro na operação
     */
    void delete(Long id) throws ProductNotFoundException, DataAccessException;
    
    /**
     * Verifica se um produto existe.
     * @param id identificador do produto
     * @return true se o produto existe
     * @throws DataAccessException se houver erro na operação
     */
    boolean exists(Long id) throws DataAccessException;
    
    /**
     * Conta o total de produtos.
     * @return número de produtos
     * @throws DataAccessException se houver erro na operação
     */
    long count() throws DataAccessException;
}
