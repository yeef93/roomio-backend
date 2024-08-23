package com.finpro.roomio_backend.responses;

import lombok.Data;
import lombok.ToString;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

@Data
@ToString
public class Response<T> {

  private int statusCode;
  private boolean success = false;
  private String statusMessage;
  private T data;

  // constructor
  public Response(int statusCode, String statusMessage) {
    this.statusCode = statusCode;
    this.statusMessage = statusMessage;
    if (statusCode == HttpStatus.OK.value()) {
      success = true;
    }
  }

  // Base failedResponse method
  public static <T> ResponseEntity<Response<T>> failedResponse(int statusCode, String statusMessage, T data) {
    Response<T> response = new Response<>(statusCode, statusMessage);
    response.setSuccess(false);
    response.setData(data);
    return ResponseEntity.status(statusCode).body(response);
  }

  // Overloaded failedResponse methods
  public static ResponseEntity<Response<Object>> failedResponse(int statusCode, String statusMessage) {
    return failedResponse(statusCode, statusMessage, null);
  }

  public static <T> ResponseEntity<Response<T>> failedResponse(String statusMessage) {
    return failedResponse(HttpStatus.BAD_REQUEST.value(), statusMessage, null);
  }

  // Base successfulResponse method
  public static <T> ResponseEntity<Response<T>> successfulResponse(int statusCode, String statusMessage, T data) {
    Response<T> response = new Response<>(statusCode, statusMessage);
    response.setSuccess(true);
    response.setData(data);
    return ResponseEntity.status(statusCode).body(response);
  }

  // Overloaded successfulResponse methods
  public static <T> ResponseEntity<Response<T>> successfulResponse(String statusMessage, T data) {
    return successfulResponse(HttpStatus.OK.value(), statusMessage, data);
  }

  public static <T> ResponseEntity<Response<T>> successfulResponse(T data) {
    return successfulResponse(HttpStatus.OK.value(), "Process has executed successfully", data);
  }

  // Method for handling paginated responses
  public static ResponseEntity<Response<Map<String, Object>>> responseMapper(int statusCode, String message,
                                                                             Page<?> page) {
    if (page != null) {
      Map<String, Object> response = new HashMap<>();
      response.put("currentPage", page.getNumber());
      response.put("totalPages", page.getTotalPages());
      response.put("totalElements", page.getTotalElements());
      response.put("events", page.getContent());
      return Response.successfulResponse(statusCode, message, response);
    } else {
      return Response.failedResponse("No data found!");
    }
  }
}