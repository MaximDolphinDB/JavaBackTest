package com.maxim.pojo;
import java.time.*;

public class Order{
    protected String order_state;  // 后续每个inheritor的实例对象的属性值都不一样(open/close/long/short/buycall/...)
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