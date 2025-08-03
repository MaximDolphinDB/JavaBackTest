package com.maxim.pojo;
import java.time.*;

public class StockOrder extends Order{
    protected Double static_profit;
    protected Double static_loss;
    protected Double dynamic_profit;
    protected Double dynamic_loss;
    protected boolean partialOrder; // 当前订单是否为子单

    public StockOrder(String symbol, Double vol, Double price, LocalDate create_date, LocalDateTime create_timestamp,
                      LocalDateTime min_order_timestamp, LocalDateTime max_order_timestamp, String reason){
        super(symbol, vol, price, create_date, create_timestamp, min_order_timestamp, max_order_timestamp, reason);
        this.partialOrder = false; // 默认当前订单为完整订单
    }
}
