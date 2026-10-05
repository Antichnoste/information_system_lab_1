package org.example.lab_1.exception;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.ext.ReaderInterceptor;
import jakarta.ws.rs.ext.ReaderInterceptorContext;
import java.io.IOException;

@Provider
public class JsonRequestReader implements ReaderInterceptor {
    
    @Override
    public Object aroundReadFrom(ReaderInterceptorContext context) throws IOException {
        try {
            return context.proceed();
        } catch (IOException | BadRequestException exception) {
            throw new WebApplicationException(GlobalExceptionHandler.response(400, "Некорректный JSON: проверьте формат и типы значений"));
        }
    }
}
