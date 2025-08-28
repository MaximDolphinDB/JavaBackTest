package com.maxim.pojo.position;

import java.time.LocalDateTime;

public class FuturePosition extends Position{
    public Double profit; // 持仓利润
    public Double margin; // 保证金金额
    public Double margin_rate; // 保证金比率
    public Integer hold_days; // 用于平仓时计算收益
    public Double pre_settle; // 昨结算价
    public Double pre_price;  // 上一个K线的价格

    public FuturePosition(Double price, Double vol, Double margin_rate, Double pre_settle, LocalDateTime min_timestamp, LocalDateTime max_timestamp, Integer time_monitor) {
        super(price, vol, min_timestamp, max_timestamp, time_monitor);
        this.profit = 0.0;  // 一开始买入的时候利润一定为0.0
        this.margin_rate = margin_rate;
        this.margin = margin_rate * vol * price;  // TODO: 增加一个参数,使得能够根据这个参数选择对应的保证金计算方式
        this.hold_days = 0;
        this.pre_settle = pre_settle;
        this.pre_price = price; // 创建的时候就是开仓价， 后续在afterBar的中会更新该属性
    }
}
