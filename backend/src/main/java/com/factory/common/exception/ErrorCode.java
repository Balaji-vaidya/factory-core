package com.factory.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    // 400 Bad Request
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "The request parameters are invalid."),
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "Input validation failed."),
    INVALID_STATUS_TRANSITION(HttpStatus.BAD_REQUEST, "Invalid status transition."),

    // 401 Unauthorized
    UNAUTHENTICATED(HttpStatus.UNAUTHORIZED, "Authentication credentials are required."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Invalid email or password."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "Security token has expired."),

    // 403 Forbidden
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "You do not have permission to perform this action."),
    OFFICE_ACCESS_RESTRICTED(HttpStatus.FORBIDDEN, "Access to this office or state data is restricted."),

    // 404 Not Found
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "The requested resource was not found."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "User not found."),
    PO_NOT_FOUND(HttpStatus.NOT_FOUND, "Purchase order not found."),
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "Product not found."),
    CLIENT_NOT_FOUND(HttpStatus.NOT_FOUND, "Client not found."),
    INVENTORY_NOT_FOUND(HttpStatus.NOT_FOUND, "Inventory record not found."),
    DC_NOT_FOUND(HttpStatus.NOT_FOUND, "Delivery challan not found."),

    // 409 Conflict
    DUPLICATE_RESOURCE(HttpStatus.CONFLICT, "Resource already exists with identical unique identifier."),
    DUPLICATE_GSTIN(HttpStatus.CONFLICT, "A client with this GSTIN already exists."),
    DUPLICATE_SKU(HttpStatus.CONFLICT, "A machinery product with this SKU already exists."),
    DUPLICATE_PO_PRODUCT(HttpStatus.CONFLICT, "A purchase order cannot contain duplicate product line items."),
    SKU_IMMUTABLE(HttpStatus.CONFLICT, "Product SKU is immutable because historical orders or inventory reference this item."),
    INSUFFICIENT_STOCK(HttpStatus.CONFLICT, "Insufficient available stock to complete the operation."),
    INVENTORY_INVARIANT_VIOLATION(HttpStatus.CONFLICT, "Operation violates inventory balance invariant (AVAILABLE = ON_HAND - RESERVED, RESERVED <= ON_HAND)."),
    RESERVATION_CONFLICT(HttpStatus.CONFLICT, "Stock reservation conflict detected."),
    DISPATCH_LIMIT_EXCEEDED(HttpStatus.CONFLICT, "Dispatch quantity exceeds eligible reserved quantity."),
    OPTIMISTIC_LOCK_CONFLICT(HttpStatus.CONFLICT, "The record was updated by another transaction. Please reload."),

    // 500 Internal Error
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected internal server error occurred.");

    private final HttpStatus httpStatus;
    private final String defaultMessage;

    ErrorCode(HttpStatus httpStatus, String defaultMessage) {
        this.httpStatus = httpStatus;
        this.defaultMessage = defaultMessage;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
