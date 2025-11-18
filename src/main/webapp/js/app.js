/**
 * Aplicação CRUD de Produtos
 * Implementa fail early e fail gracefully com validações robustas
 */

// Configuração da API
const API_BASE_URL = '/api/products';

// Estado da aplicação
let isEditMode = false;
let currentProductId = null;

// Elementos do DOM
const productForm = document.getElementById('product-form');
const formTitle = document.getElementById('form-title');
const submitBtn = document.getElementById('submit-btn');
const cancelBtn = document.getElementById('cancel-btn');
const productsTbody = document.getElementById('products-tbody');
const productCount = document.getElementById('product-count');
const messageContainer = document.getElementById('message-container');
const confirmModal = document.getElementById('confirm-modal');
const confirmYes = document.getElementById('confirm-yes');
const confirmNo = document.getElementById('confirm-no');

// Inicialização
document.addEventListener('DOMContentLoaded', () => {
    loadProducts();
    setupEventListeners();
});

// Configurar event listeners
function setupEventListeners() {
    productForm.addEventListener('submit', handleFormSubmit);
    cancelBtn.addEventListener('click', resetForm);
    confirmNo.addEventListener('click', closeConfirmModal);
}

// Carregar produtos
async function loadProducts() {
    try {
        const response = await fetch(API_BASE_URL);
        
        if (!response.ok) {
            throw new Error(`Erro HTTP: ${response.status}`);
        }
        
        const result = await response.json();
        
        if (result.success && result.data) {
            renderProducts(result.data);
        } else {
            showMessage('Erro ao carregar produtos', 'error');
        }
    } catch (error) {
        console.error('Erro ao carregar produtos:', error);
        showMessage('Erro ao conectar com o servidor. Tente novamente.', 'error');
        renderProducts([]);
    }
}

// Renderizar produtos na tabela
function renderProducts(products) {
    const noProductsRow = document.getElementById('no-products-row');
    
    if (products.length === 0) {
        noProductsRow.style.display = 'table-row';
        productCount.textContent = '0';
        
        // Remover produtos existentes
        const existingRows = productsTbody.querySelectorAll('tr:not(#no-products-row)');
        existingRows.forEach(row => row.remove());
        return;
    }
    
    noProductsRow.style.display = 'none';
    productCount.textContent = products.length;
    
    // Limpar tbody exceto a linha "no-products"
    const existingRows = productsTbody.querySelectorAll('tr:not(#no-products-row)');
    existingRows.forEach(row => row.remove());
    
    // Adicionar produtos
    products.forEach(product => {
        const row = createProductRow(product);
        productsTbody.appendChild(row);
    });
}

// Criar linha de produto
function createProductRow(product) {
    const row = document.createElement('tr');
    row.setAttribute('data-product-id', product.id);
    
    row.innerHTML = `
        <td>${escapeHtml(product.id)}</td>
        <td>${escapeHtml(product.name)}</td>
        <td>${escapeHtml(product.description || '-')}</td>
        <td>R$ ${formatPrice(product.price)}</td>
        <td>${escapeHtml(product.quantity)}</td>
        <td class="actions">
            <button class="btn btn-edit" onclick="editProduct(${product.id})">Editar</button>
            <button class="btn btn-delete" onclick="confirmDelete(${product.id})">Excluir</button>
        </td>
    `;
    
    return row;
}

// Submeter formulário
async function handleFormSubmit(event) {
    event.preventDefault();
    
    // Limpar mensagens de erro anteriores
    clearFieldErrors();
    
    // Obter dados do formulário
    const formData = getFormData();
    
    // Validação client-side (fail early)
    const validationErrors = validateFormData(formData);
    if (validationErrors.length > 0) {
        displayFieldErrors(validationErrors);
        showMessage('Por favor, corrija os erros no formulário', 'error');
        return;
    }
    
    try {
        let response;
        
        if (isEditMode) {
            response = await updateProduct(currentProductId, formData);
        } else {
            response = await createProduct(formData);
        }
        
        if (response.success) {
            showMessage(response.message, 'success');
            resetForm();
            await loadProducts();
        } else {
            showMessage(response.message || 'Erro ao processar requisição', 'error');
        }
        
    } catch (error) {
        console.error('Erro ao submeter formulário:', error);
        showMessage('Erro ao conectar com o servidor. Tente novamente.', 'error');
    }
}

// Obter dados do formulário
function getFormData() {
    return {
        name: document.getElementById('product-name').value.trim(),
        description: document.getElementById('product-description').value.trim(),
        price: parseFloat(document.getElementById('product-price').value),
        quantity: parseInt(document.getElementById('product-quantity').value)
    };
}

// Validar dados do formulário (fail early)
function validateFormData(data) {
    const errors = [];
    
    if (!data.name || data.name.length === 0) {
        errors.push({ field: 'name', message: 'Nome é obrigatório' });
    } else if (data.name.length > 100) {
        errors.push({ field: 'name', message: 'Nome não pode exceder 100 caracteres' });
    }
    
    if (data.description && data.description.length > 500) {
        errors.push({ field: 'description', message: 'Descrição não pode exceder 500 caracteres' });
    }
    
    if (isNaN(data.price) || data.price < 0) {
        errors.push({ field: 'price', message: 'Preço deve ser um valor positivo' });
    } else if (data.price > 999999.99) {
        errors.push({ field: 'price', message: 'Preço não pode exceder 999999.99' });
    }
    
    if (isNaN(data.quantity) || data.quantity < 0) {
        errors.push({ field: 'quantity', message: 'Quantidade deve ser um valor positivo' });
    } else if (data.quantity > 999999) {
        errors.push({ field: 'quantity', message: 'Quantidade não pode exceder 999999' });
    }
    
    return errors;
}

// Exibir erros nos campos
function displayFieldErrors(errors) {
    errors.forEach(error => {
        const errorElement = document.getElementById(`${error.field}-error`);
        if (errorElement) {
            errorElement.textContent = error.message;
        }
    });
}

// Limpar erros dos campos
function clearFieldErrors() {
    const errorElements = document.querySelectorAll('.error-message');
    errorElements.forEach(element => {
        element.textContent = '';
    });
}

// Criar produto
async function createProduct(data) {
    try {
        const response = await fetch(API_BASE_URL, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        });
        
        return await response.json();
    } catch (error) {
        throw new Error('Erro ao criar produto: ' + error.message);
    }
}

// Atualizar produto
async function updateProduct(id, data) {
    try {
        const response = await fetch(`${API_BASE_URL}/${id}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        });
        
        return await response.json();
    } catch (error) {
        throw new Error('Erro ao atualizar produto: ' + error.message);
    }
}

// Editar produto
async function editProduct(id) {
    try {
        const response = await fetch(`${API_BASE_URL}/${id}`);
        
        if (!response.ok) {
            throw new Error(`Erro HTTP: ${response.status}`);
        }
        
        const result = await response.json();
        
        if (result.success && result.data) {
            populateForm(result.data);
        } else {
            showMessage('Erro ao carregar produto para edição', 'error');
        }
    } catch (error) {
        console.error('Erro ao editar produto:', error);
        showMessage('Erro ao carregar produto. Tente novamente.', 'error');
    }
}

// Preencher formulário para edição
function populateForm(product) {
    isEditMode = true;
    currentProductId = product.id;
    
    document.getElementById('product-id').value = product.id;
    document.getElementById('product-name').value = product.name;
    document.getElementById('product-description').value = product.description || '';
    document.getElementById('product-price').value = product.price;
    document.getElementById('product-quantity').value = product.quantity;
    
    formTitle.textContent = 'Editar Produto';
    submitBtn.textContent = 'Atualizar Produto';
    cancelBtn.style.display = 'block';
    
    // Scroll para o formulário
    document.querySelector('.form-section').scrollIntoView({ behavior: 'smooth' });
}

// Confirmar exclusão
function confirmDelete(id) {
    currentProductId = id;
    confirmModal.style.display = 'block';
    
    confirmYes.onclick = async () => {
        await deleteProduct(id);
        closeConfirmModal();
    };
}

// Fechar modal de confirmação
function closeConfirmModal() {
    confirmModal.style.display = 'none';
    currentProductId = null;
}

// Deletar produto
async function deleteProduct(id) {
    try {
        const response = await fetch(`${API_BASE_URL}/${id}`, {
            method: 'DELETE'
        });
        
        const result = await response.json();
        
        if (result.success) {
            showMessage(result.message, 'success');
            await loadProducts();
        } else {
            showMessage(result.message || 'Erro ao excluir produto', 'error');
        }
    } catch (error) {
        console.error('Erro ao deletar produto:', error);
        showMessage('Erro ao conectar com o servidor. Tente novamente.', 'error');
    }
}

// Resetar formulário
function resetForm() {
    productForm.reset();
    clearFieldErrors();
    
    isEditMode = false;
    currentProductId = null;
    
    document.getElementById('product-id').value = '';
    formTitle.textContent = 'Adicionar Novo Produto';
    submitBtn.textContent = 'Adicionar Produto';
    cancelBtn.style.display = 'none';
}

// Exibir mensagem
function showMessage(text, type) {
    const message = document.createElement('div');
    message.className = `message ${type}`;
    message.innerHTML = `
        <span>${escapeHtml(text)}</span>
        <button class="message-close" onclick="this.parentElement.remove()">&times;</button>
    `;
    
    messageContainer.appendChild(message);
    
    // Auto-remover após 5 segundos
    setTimeout(() => {
        message.remove();
    }, 5000);
}

// Formatar preço
function formatPrice(price) {
    return parseFloat(price).toFixed(2).replace('.', ',');
}

// Escape HTML para prevenir XSS (segurança)
function escapeHtml(text) {
    if (text === null || text === undefined) {
        return '';
    }
    
    const div = document.createElement('div');
    div.textContent = text.toString();
    return div.innerHTML;
}

// Fechar modal ao clicar fora
window.onclick = function(event) {
    if (event.target === confirmModal) {
        closeConfirmModal();
    }
};
