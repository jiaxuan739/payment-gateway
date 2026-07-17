package com.payment.gateway.dto;

public class PaymentResponse<T> {

    private int code;
    private String message;
    private T data;

    public PaymentResponse() {
    }

    public PaymentResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // -- Getters and Setters --

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    // -- Builder --

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public static class Builder<T> {
        private int code;
        private String message;
        private T data;

        public Builder<T> code(int code) {
            this.code = code;
            return this;
        }

        public Builder<T> message(String message) {
            this.message = message;
            return this;
        }

        public Builder<T> data(T data) {
            this.data = data;
            return this;
        }

        public PaymentResponse<T> build() {
            return new PaymentResponse<>(code, message, data);
        }
    }

    // -- Factory methods --

    public static <T> PaymentResponse<T> ok(T data) {
        return PaymentResponse.<T>builder()
                .code(200)
                .message("success")
                .data(data)
                .build();
    }

    public static <T> PaymentResponse<T> fail(int code, String message) {
        return PaymentResponse.<T>builder()
                .code(code)
                .message(message)
                .build();
    }
}
