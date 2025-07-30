package com.maxim.pojo;
import java.time.*;

public class Order{
    String symbol; // 标的名称
    Double vol;
    Double price;
    LocalDate create_date;
    LocalDateTime create_timestamp;
    LocalDateTime min_order_timestamp;
    LocalDateTime max_order_timestamp;
    String reason;

    public Order(String symbol, Double vol, Double price, LocalDate create_date, LocalDateTime create_timestamp, LocalDateTime min_order_timestamp, LocalDateTime max_order_timestamp, String reason) {
        this.symbol = symbol;
        this.vol = vol;
        this.price = price;
        this.create_date = create_date;
        this.create_timestamp = create_timestamp;
        this.min_order_timestamp = min_order_timestamp;
        this.max_order_timestamp = max_order_timestamp;
        this.reason = reason;
    }
}