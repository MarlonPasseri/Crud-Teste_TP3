package com.crudsystem.servlet;

import com.crudsystem.dao.InMemoryProductDAO;
import com.crudsystem.exception.DataAccessException;
import com.crudsystem.exception.ProductNotFoundException;
import com.crudsystem.exception.ValidationException;
import com.crudsystem.model.Product;
import com.crudsystem.service.ProductService;
import com.crudsystem.util.JsonResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.crudsystem.util.LocalDateTimeAdapter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Servlet REST para operações CRUD de produtos.
 * Implementa tratamento robusto de erros e validações.
 */
@WebServlet(name = "ProductServlet", urlPatterns = {"/api/products", "/api/products/*"})
public class ProductServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(ProductServlet.class);
    private static final long serialVersionUID = 1L;
    
    private ProductService productService;
    private Gson gson;
    
    @Override
    public void init() throws ServletException {
        super.init();
        productService = new ProductService(InMemoryProductDAO.getInstance());
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
        logger.info("ProductServlet inicializado");
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        setCommonHeaders(response);
        
        try {
            String pathInfo = request.getPathInfo();
            
            if (pathInfo == null || pathInfo.equals("/")) {
                // Listar todos os produtos
                handleGetAll(response);
            } else {
                // Buscar produto por ID
                handleGetById(pathInfo, response);
            }
            
        } catch (Exception e) {
            logger.error("Erro inesperado no GET", e);
            sendErrorResponse(response, "Erro interno do servidor", 
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        setCommonHeaders(response);
        
        try {
            // Lê o corpo da requisição
            String requestBody = request.getReader().lines()
                    .reduce("", (accumulator, actual) -> accumulator + actual);
            
            if (requestBody.trim().isEmpty()) {
                sendErrorResponse(response, "Corpo da requisição vazio", 
                        HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            
            // Parse do JSON
            ProductRequest productRequest = gson.fromJson(requestBody, ProductRequest.class);
            
            // Validação básica
            if (productRequest == null) {
                sendErrorResponse(response, "Dados inválidos", 
                        HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            
            // Cria o produto
            Product created = productService.createProduct(
                    productRequest.getName(),
                    productRequest.getDescription(),
                    productRequest.getPrice(),
                    productRequest.getQuantity()
            );
            
            response.setStatus(HttpServletResponse.SC_CREATED);
            String jsonResponse = JsonResponse.success("Produto criado com sucesso", created).toJson();
            response.getWriter().write(jsonResponse);
            
        } catch (ValidationException e) {
            logger.warn("Erro de validação no POST: {}", e.getMessage());
            sendErrorResponse(response, e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
        } catch (DataAccessException e) {
            logger.error("Erro de acesso a dados no POST", e);
            sendErrorResponse(response, "Erro ao criar produto", 
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            logger.error("Erro inesperado no POST", e);
            sendErrorResponse(response, "Erro interno do servidor", 
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        setCommonHeaders(response);
        
        try {
            String pathInfo = request.getPathInfo();
            
            if (pathInfo == null || pathInfo.equals("/")) {
                sendErrorResponse(response, "ID do produto é obrigatório", 
                        HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            
            Long id = extractIdFromPath(pathInfo);
            
            // Lê o corpo da requisição
            String requestBody = request.getReader().lines()
                    .reduce("", (accumulator, actual) -> accumulator + actual);
            
            if (requestBody.trim().isEmpty()) {
                sendErrorResponse(response, "Corpo da requisição vazio", 
                        HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            
            // Parse do JSON
            ProductRequest productRequest = gson.fromJson(requestBody, ProductRequest.class);
            
            if (productRequest == null) {
                sendErrorResponse(response, "Dados inválidos", 
                        HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            
            // Atualiza o produto
            productService.updateProduct(
                    id,
                    productRequest.getName(),
                    productRequest.getDescription(),
                    productRequest.getPrice(),
                    productRequest.getQuantity()
            );
            
            response.setStatus(HttpServletResponse.SC_OK);
            String jsonResponse = JsonResponse.success("Produto atualizado com sucesso").toJson();
            response.getWriter().write(jsonResponse);
            
        } catch (ProductNotFoundException e) {
            logger.warn("Produto não encontrado no PUT: {}", e.getMessage());
            sendErrorResponse(response, e.getMessage(), HttpServletResponse.SC_NOT_FOUND);
        } catch (ValidationException e) {
            logger.warn("Erro de validação no PUT: {}", e.getMessage());
            sendErrorResponse(response, e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
        } catch (DataAccessException e) {
            logger.error("Erro de acesso a dados no PUT", e);
            sendErrorResponse(response, "Erro ao atualizar produto", 
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            logger.error("Erro inesperado no PUT", e);
            sendErrorResponse(response, "Erro interno do servidor", 
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        setCommonHeaders(response);
        
        try {
            String pathInfo = request.getPathInfo();
            
            if (pathInfo == null || pathInfo.equals("/")) {
                sendErrorResponse(response, "ID do produto é obrigatório", 
                        HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            
            Long id = extractIdFromPath(pathInfo);
            
            productService.deleteProduct(id);
            
            response.setStatus(HttpServletResponse.SC_OK);
            String jsonResponse = JsonResponse.success("Produto deletado com sucesso").toJson();
            response.getWriter().write(jsonResponse);
            
        } catch (ProductNotFoundException e) {
            logger.warn("Produto não encontrado no DELETE: {}", e.getMessage());
            sendErrorResponse(response, e.getMessage(), HttpServletResponse.SC_NOT_FOUND);
        } catch (DataAccessException e) {
            logger.error("Erro de acesso a dados no DELETE", e);
            sendErrorResponse(response, "Erro ao deletar produto", 
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            logger.error("Erro inesperado no DELETE", e);
            sendErrorResponse(response, "Erro interno do servidor", 
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
    private void handleGetAll(HttpServletResponse response) throws IOException {
        try {
            List<Product> products = productService.getAllProducts();
            response.setStatus(HttpServletResponse.SC_OK);
            String jsonResponse = JsonResponse.success("Produtos listados com sucesso", products).toJson();
            response.getWriter().write(jsonResponse);
        } catch (DataAccessException e) {
            logger.error("Erro ao listar produtos", e);
            sendErrorResponse(response, "Erro ao listar produtos", 
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
    private void handleGetById(String pathInfo, HttpServletResponse response) throws IOException {
        try {
            Long id = extractIdFromPath(pathInfo);
            Product product = productService.getProductById(id);
            response.setStatus(HttpServletResponse.SC_OK);
            String jsonResponse = JsonResponse.success("Produto encontrado", product).toJson();
            response.getWriter().write(jsonResponse);
        } catch (ProductNotFoundException e) {
            logger.warn("Produto não encontrado: {}", e.getMessage());
            sendErrorResponse(response, e.getMessage(), HttpServletResponse.SC_NOT_FOUND);
        } catch (DataAccessException e) {
            logger.error("Erro ao buscar produto", e);
            sendErrorResponse(response, "Erro ao buscar produto", 
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (NumberFormatException e) {
            logger.warn("ID inválido no path: {}", pathInfo);
            sendErrorResponse(response, "ID inválido", HttpServletResponse.SC_BAD_REQUEST);
        }
    }
    
    private Long extractIdFromPath(String pathInfo) {
        String idStr = pathInfo.substring(1); // Remove a barra inicial
        return Long.parseLong(idStr);
    }
    
    private void setCommonHeaders(HttpServletResponse response) {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
    }
    
    private void sendErrorResponse(HttpServletResponse response, String message, int statusCode) 
            throws IOException {
        response.setStatus(statusCode);
        String jsonResponse = JsonResponse.error(message, statusCode).toJson();
        response.getWriter().write(jsonResponse);
    }
    
    /**
     * Classe interna para deserialização de requisições JSON.
     */
    private static class ProductRequest {
        private String name;
        private String description;
        private BigDecimal price;
        private Integer quantity;
        
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
    }
}
