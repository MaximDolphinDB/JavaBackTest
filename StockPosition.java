package com.maxim.pojo.position;
import java.time.LocalDateTime;

public class StockPosition extends Position{
    public Double profit; // 持仓利润
    public Double pre_price;  // 上一个K线的价格, 不会在创建K线的时候赋值, 而会在monitor的时候赋值
    public StockPosition(Double price, Double vol, LocalDateTime min_timestamp, LocalDateTime max_timestamp, Integer time_monitor) {
        super(price, vol, min_timestamp, max_timestamp, time_monitor);
        this.pre_price = price;
        this.profit = 0.0;  // 一开始买入的利润一定为0
    }
}
