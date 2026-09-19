package org.example.lab_1.api;
public class ApiException extends RuntimeException {
 public final int status;
 public final String field;
 public ApiException(int status, String message) { this(status,message,null); }
 public ApiException(int status, String message, String field) { super(message); this.status=status; this.field=field; }
}
