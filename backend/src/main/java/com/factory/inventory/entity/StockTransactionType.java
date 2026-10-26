package com.factory.inventory.entity;

public enum StockTransactionType {
    INITIAL_STOCK,
    INWARD,
    PO_RESERVATION,
    PO_UNRESERVATION,
    DC_DISPATCH,
    MANUAL_ADJUSTMENT,
    RETURN_INWARD
}
