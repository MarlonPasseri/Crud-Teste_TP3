package com.crudsystem.selenium;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes Selenium para interface web do sistema CRUD.
 * Testa interações com formulários, tabelas, botões e alertas.
 */
@DisplayName("Testes Selenium - Interface Web")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProductWebInterfaceTest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "http://localhost:8080";

    @BeforeAll
    static void setUpClass() {
        WebDriverManager.chromedriver().setup();
        
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    static void tearDownClass() {
        if (driver != null) {
            driver.quit();
        }
    }

    @BeforeEach
    void setUp() {
        driver.get(BASE_URL);
        clearAllProducts();
    }

    @Test
    @Order(1)
    @DisplayName("Deve carregar página principal corretamente")
    void shouldLoadMainPageCorrectly() {
        String title = driver.getTitle();
        assertEquals("Sistema CRUD - Gerenciamento de Produtos", title);
        
        WebElement header = driver.findElement(By.tagName("h1"));
        assertTrue(header.getText().contains("Sistema de Gerenciamento de Produtos"));
    }

    @Test
    @Order(2)
    @DisplayName("Deve verificar presença de todos os elementos do formulário")
    void shouldVerifyFormElementsPresence() {
        assertTrue(isElementPresent(By.id("product-form")));
        assertTrue(isElementPresent(By.id("product-name")));
        assertTrue(isElementPresent(By.id("product-description")));
        assertTrue(isElementPresent(By.id("product-price")));
        assertTrue(isElementPresent(By.id("product-quantity")));
        assertTrue(isElementPresent(By.id("submit-btn")));
    }

    @Test
    @Order(3)
    @DisplayName("Deve criar produto com sucesso")
    void shouldCreateProductSuccessfully() {
        fillProductForm("Notebook Dell", "Notebook i7 16GB", "2500.00", "10");
        submitForm();
        
        waitForSuccessMessage();
        assertTrue(isSuccessMessageDisplayed());
        
        // Verificar se o produto aparece na tabela
        WebElement table = driver.findElement(By.id("products-tbody"));
        List<WebElement> rows = table.findElements(By.tagName("tr"));
        
        boolean productFound = false;
        for (WebElement row : rows) {
            if (row.getText().contains("Notebook Dell")) {
                productFound = true;
                break;
            }
        }
        assertTrue(productFound, "Produto criado deve aparecer na tabela");
    }

    @Test
    @Order(4)
    @DisplayName("Deve validar campo nome obrigatório")
    void shouldValidateRequiredNameField() {
        fillProductForm("", "Descrição", "100.00", "5");
        submitForm();
        
        // Verificar mensagem de erro ou validação HTML5
        WebElement nameField = driver.findElement(By.id("product-name"));
        String validationMessage = nameField.getAttribute("validationMessage");
        assertNotNull(validationMessage);
    }

    @Test
    @Order(5)
    @DisplayName("Deve validar campo preço obrigatório")
    void shouldValidateRequiredPriceField() {
        WebElement nameField = driver.findElement(By.id("product-name"));
        WebElement priceField = driver.findElement(By.id("product-price"));
        WebElement quantityField = driver.findElement(By.id("product-quantity"));
        
        nameField.sendKeys("Produto Teste");
        priceField.clear();
        quantityField.sendKeys("10");
        
        submitForm();
        
        String validationMessage = priceField.getAttribute("validationMessage");
        assertNotNull(validationMessage);
    }

    @Test
    @Order(6)
    @DisplayName("Deve validar campo quantidade obrigatório")
    void shouldValidateRequiredQuantityField() {
        WebElement nameField = driver.findElement(By.id("product-name"));
        WebElement priceField = driver.findElement(By.id("product-price"));
        WebElement quantityField = driver.findElement(By.id("product-quantity"));
        
        nameField.sendKeys("Produto Teste");
        priceField.sendKeys("100.00");
        quantityField.clear();
        
        submitForm();
        
        String validationMessage = quantityField.getAttribute("validationMessage");
        assertNotNull(validationMessage);
    }

    @Test
    @Order(7)
    @DisplayName("Deve editar produto existente")
    void shouldEditExistingProduct() {
        // Criar produto primeiro
        fillProductForm("Mouse Logitech", "Mouse USB", "50.00", "20");
        submitForm();
        waitForSuccessMessage();
        
        // Clicar no botão Editar
        WebElement editButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(@class, 'btn-edit')]")));
        editButton.click();
        
        // Verificar se o formulário foi preenchido
        wait.until(ExpectedConditions.attributeToBeNotEmpty(
                driver.findElement(By.id("product-name")), "value"));
        
        WebElement nameField = driver.findElement(By.id("product-name"));
        assertEquals("Mouse Logitech", nameField.getAttribute("value"));
        
        // Alterar valores
        nameField.clear();
        nameField.sendKeys("Mouse Logitech MX Master");
        
        WebElement priceField = driver.findElement(By.id("product-price"));
        priceField.clear();
        priceField.sendKeys("250.00");
        
        submitForm();
        waitForSuccessMessage();
        
        // Verificar se foi atualizado na tabela
        WebElement table = driver.findElement(By.id("products-tbody"));
        assertTrue(table.getText().contains("Mouse Logitech MX Master"));
        assertTrue(table.getText().contains("250"));
    }

    @Test
    @Order(8)
    @DisplayName("Deve cancelar edição de produto")
    void shouldCancelProductEdit() {
        // Criar produto
        fillProductForm("Teclado", "Teclado mecânico", "150.00", "15");
        submitForm();
        waitForSuccessMessage();
        
        // Clicar em Editar
        WebElement editButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(@class, 'btn-edit')]")));
        editButton.click();
        
        // Verificar se botão Cancelar apareceu
        WebElement cancelButton = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.id("cancel-btn")));
        assertTrue(cancelButton.isDisplayed());
        
        // Clicar em Cancelar
        cancelButton.click();
        
        // Verificar se formulário foi resetado
        WebElement nameField = driver.findElement(By.id("product-name"));
        assertEquals("", nameField.getAttribute("value"));
        
        // Verificar se botão Cancelar foi ocultado
        assertFalse(cancelButton.isDisplayed());
    }

    @Test
    @Order(9)
    @DisplayName("Deve exibir modal de confirmação ao deletar")
    void shouldShowConfirmationModalOnDelete() {
        // Criar produto
        fillProductForm("Webcam", "Webcam HD", "200.00", "8");
        submitForm();
        waitForSuccessMessage();
        
        // Clicar no botão Excluir
        WebElement deleteButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(@class, 'btn-delete')]")));
        deleteButton.click();
        
        // Verificar se modal apareceu
        WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.id("confirm-modal")));
        assertTrue(modal.isDisplayed());
        
        // Verificar botões do modal
        assertTrue(isElementPresent(By.id("confirm-yes")));
        assertTrue(isElementPresent(By.id("confirm-no")));
    }

    @Test
    @Order(10)
    @DisplayName("Deve cancelar exclusão de produto")
    void shouldCancelProductDeletion() {
        // Criar produto
        fillProductForm("Monitor", "Monitor 24 polegadas", "800.00", "5");
        submitForm();
        waitForSuccessMessage();
        
        // Clicar em Excluir
        WebElement deleteButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(@class, 'btn-delete')]")));
        deleteButton.click();
        
        // Clicar em Cancelar no modal
        WebElement cancelButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.id("confirm-no")));
        cancelButton.click();
        
        // Verificar se modal foi fechado
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("confirm-modal")));
        
        // Verificar se produto ainda está na tabela
        WebElement table = driver.findElement(By.id("products-tbody"));
        assertTrue(table.getText().contains("Monitor"));
    }

    @Test
    @Order(11)
    @DisplayName("Deve confirmar e deletar produto")
    void shouldConfirmAndDeleteProduct() {
        // Criar produto
        fillProductForm("Impressora", "Impressora HP", "600.00", "3");
        submitForm();
        waitForSuccessMessage();
        
        // Clicar em Excluir
        WebElement deleteButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(@class, 'btn-delete')]")));
        deleteButton.click();
        
        // Confirmar exclusão
        WebElement confirmButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.id("confirm-yes")));
        confirmButton.click();
        
        // Aguardar mensagem de sucesso
        waitForSuccessMessage();
        
        // Verificar se produto foi removido da tabela
        WebElement table = driver.findElement(By.id("products-tbody"));
        assertFalse(table.getText().contains("Impressora"));
    }

    @Test
    @Order(12)
    @DisplayName("Deve listar múltiplos produtos")
    void shouldListMultipleProducts() {
        // Criar vários produtos
        String[][] products = {
                {"Produto 1", "Descrição 1", "10.00", "5"},
                {"Produto 2", "Descrição 2", "20.00", "10"},
                {"Produto 3", "Descrição 3", "30.00", "15"}
        };
        
        for (String[] product : products) {
            fillProductForm(product[0], product[1], product[2], product[3]);
            submitForm();
            waitForSuccessMessage();
            closeMessages();
        }
        
        // Verificar contador de produtos
        WebElement productCount = driver.findElement(By.id("product-count"));
        assertEquals("3", productCount.getText());
        
        // Verificar se todos aparecem na tabela
        WebElement table = driver.findElement(By.id("products-tbody"));
        String tableText = table.getText();
        
        assertTrue(tableText.contains("Produto 1"));
        assertTrue(tableText.contains("Produto 2"));
        assertTrue(tableText.contains("Produto 3"));
    }

    @Test
    @Order(13)
    @DisplayName("Deve exibir mensagem quando não há produtos")
    void shouldShowMessageWhenNoProducts() {
        WebElement noProductsRow = driver.findElement(By.id("no-products-row"));
        assertTrue(noProductsRow.isDisplayed());
        assertTrue(noProductsRow.getText().contains("Nenhum produto cadastrado"));
        
        WebElement productCount = driver.findElement(By.id("product-count"));
        assertEquals("0", productCount.getText());
    }

    @Test
    @Order(14)
    @DisplayName("Deve validar entrada de preço negativo")
    void shouldValidateNegativePrice() {
        WebElement priceField = driver.findElement(By.id("product-price"));
        priceField.sendKeys("-10");
        
        String min = priceField.getAttribute("min");
        assertEquals("0", min);
    }

    @Test
    @Order(15)
    @DisplayName("Deve validar entrada de quantidade negativa")
    void shouldValidateNegativeQuantity() {
        WebElement quantityField = driver.findElement(By.id("product-quantity"));
        quantityField.sendKeys("-5");
        
        String min = quantityField.getAttribute("min");
        assertEquals("0", min);
    }

    // Métodos auxiliares
    
    private void fillProductForm(String name, String description, String price, String quantity) {
        WebElement nameField = driver.findElement(By.id("product-name"));
        WebElement descField = driver.findElement(By.id("product-description"));
        WebElement priceField = driver.findElement(By.id("product-price"));
        WebElement quantityField = driver.findElement(By.id("product-quantity"));
        
        nameField.clear();
        nameField.sendKeys(name);
        
        descField.clear();
        descField.sendKeys(description);
        
        priceField.clear();
        priceField.sendKeys(price);
        
        quantityField.clear();
        quantityField.sendKeys(quantity);
    }
    
    private void submitForm() {
        WebElement submitButton = driver.findElement(By.id("submit-btn"));
        submitButton.click();
    }
    
    private void waitForSuccessMessage() {
        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(".message.success")));
    }
    
    private boolean isSuccessMessageDisplayed() {
        try {
            WebElement message = driver.findElement(By.cssSelector(".message.success"));
            return message.isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }
    
    private boolean isElementPresent(By locator) {
        try {
            driver.findElement(locator);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }
    
    private void closeMessages() {
        List<WebElement> closeButtons = driver.findElements(
                By.cssSelector(".message-close"));
        for (WebElement button : closeButtons) {
            try {
                button.click();
            } catch (Exception e) {
                // Ignorar se já foi fechado
            }
        }
    }
    
    private void clearAllProducts() {
        try {
            List<WebElement> deleteButtons = driver.findElements(
                    By.xpath("//button[contains(@class, 'btn-delete')]"));
            
            for (WebElement button : deleteButtons) {
                button.click();
                WebElement confirmButton = wait.until(
                        ExpectedConditions.elementToBeClickable(By.id("confirm-yes")));
                confirmButton.click();
                Thread.sleep(500);
            }
        } catch (Exception e) {
            // Sem produtos para deletar
        }
    }
}
