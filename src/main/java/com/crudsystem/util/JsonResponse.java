package com.crudsystem.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Classe utilitária para construir respostas JSON padronizadas.
 * Garante feedback consistente e seguro ao usuário.
 */
public class JsonResponse {
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();
    
    private boolean success;
    private String message;
    private Object data;
    private Map<String, Object> metadata;
    
    private JsonResponse() {
        this.metadata = new HashMap<>();
    }
    
    public static JsonResponse success(String message) {
        JsonResponse response = new JsonResponse();
        response.success = true;
        response.message = message;
        return response;
    }
    
    public static JsonResponse success(String message, Object data) {
        JsonResponse response = success(message);
        response.data = data;
        return response;
    }
    
    public static JsonResponse error(String message) {
        JsonResponse response = new JsonResponse();
        response.success = false;
        response.message = sanitizeErrorMessage(message);
        return response;
    }
    
    public static JsonResponse error(String message, int statusCode) {
        JsonResponse response = error(message);
        response.addMetadata("statusCode", statusCode);
        return response;
    }
    
    public JsonResponse addMetadata(String key, Object value) {
        this.metadata.put(key, value);
        return this;
    }
    
    public String toJson() {
        return gson.toJson(this);
    }
    
    /**
     * Sanitiza mensagens de erro para evitar exposição de informações sensíveis.
     * Implementa fail gracefully removendo stack traces e detalhes internos.
     */
    private static String sanitizeErrorMessage(String message) {
        if (message == null) {
            return "Erro desconhecido";
        }
        
        // Remove stack traces
        int stackTraceIndex = message.indexOf("\n\tat ");
        if (stackTraceIndex > 0) {
            message = message.substring(0, stackTraceIndex);
        }
        
        // Remove caminhos de arquivo
        message = message.replaceAll("/[\\w/.-]+\\.java:\\d+", "[arquivo]");
        
        // Remove informações de classe interna
        message = message.replaceAll("com\\.crudsystem\\.[\\w.]+:", "");
        
        return message.trim();
    }
    
    // Getters
    public boolean isSuccess() {
        return success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public Object getData() {
        return data;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
}
