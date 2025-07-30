package com.maxim.pojo;
import java.time.*;

// 订单对象
public class StockCloseOrder extends Order{
    private static final String order_state = "close";

    public StockCloseOrder(String symbol, Double vol, Double price, LocalDate create_date, LocalDateTime create_timestamp,
                           LocalDateTime min_order_timestamp, LocalDateTime max_order_timestamp, String reason) {
        super(symbol, vol, price, create_date, create_timestamp, min_order_timestamp, max_order_timestamp, reason);
    }
}
