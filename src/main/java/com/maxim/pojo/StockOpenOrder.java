package com.maxim.pojo;
import java.time.*;

// 订单对象
public class StockOpenOrder extends StockOrder{
    private final static String ORDER_STATE = "open";
    private static LocalDateTime min_timestamp;
    private static LocalDateTime max_timestamp;
    private static Double static_profit;
    private static Double static_loss;
    private static Double dynamic_profit;
    private static Double dynamic_loss;
    private static Double commission;

    public StockOpenOrder(String symbol, Double vol, Double price,
                          LocalDate create_date, LocalDateTime create_timestamp, LocalDateTime min_timestamp, LocalDateTime max_timestamp,
                          LocalDateTime min_order_timestamp, LocalDateTime max_order_timestamp,
                          Double static_profit, Double static_loss, Double dynamic_profit,
                          Double dynamic_loss, Double commission, String reason) {
        super(symbol, vol, price, create_date, create_timestamp, min_order_timestamp, max_order_timestamp, reason);
        this.order_state = ORDER_STATE;
        StockOpenOrder.min_timestamp = min_timestamp;
        StockOpenOrder.max_timestamp = max_timestamp;
        StockOpenOrder.static_profit = static_profit;
        StockOpenOrder.static_loss = static_loss;
        StockOpenOrder.dynamic_profit = dynamic_profit;
        StockOpenOrder.dynamic_loss = dynamic_loss;
        StockOpenOrder.commission = commission;
    }
}