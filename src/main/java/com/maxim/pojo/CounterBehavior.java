package com.maxim.pojo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xxdb.DBConnection;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.time.*;
import java.util.LinkedHashMap;

public class CounterBehavior extends TradeBehavior{
    public CounterBehavior(){
        super();
    }

    public static void executeStock(String symbol, Double price, Double vol,
                                    Double static_profit, Double static_loss,
                                    Double dynamic_profit, Double dynamic_loss,
                                    LocalDateTime min_timestamp, LocalDateTime max_timestamp,
                                    String reason){
        /*
          """
        【核心函数】股票开仓/加仓(默认无手续费)
        min_price:平仓最小价格(止损)
        max_price:平仓最大价格(止盈)
        max_timestamp:持仓最大时间戳
        """
        */

        // 获取BackTestConfig实例
        BackTestConfig config = BackTestConfig.getInstance();

        // 初始化Summary视图对象
        Position pos = new StockPosition(price, vol, min_timestamp, max_timestamp, 0);
        if (!config.getStockPosition().containsKey(symbol)){
            // 说明当前没有该股票的持仓
            ArrayList<Position> pos_list = new ArrayList<>();
            pos_list.add(pos);
            config.stockPosition.put(symbol, pos_list); // 新增该股票的持仓
        }else{
            config.stockPosition.get(symbol).add(pos); // 新增该股票的持仓
        }

        if (!config.getStockSummary().containsKey(symbol)){
            StockSummary summary = new StockSummary(price, vol, static_profit, static_loss, dynamic_profit, dynamic_loss, price, price); // 这里history_min & history_max 都是当前价格
            config.stockSummary.put(symbol, summary);
        }else{
            // 需要先更新止盈止损属性
            StockSummary summary = config.stockSummary.get(symbol);
            StockSummary summary_updated = summary.update(summary, price, vol, static_profit, static_loss, dynamic_profit, dynamic_loss);
            config.stockSummary.put(symbol, summary_updated);
        }

        // 在StockRecord中记录
        StockRecord R = new StockRecord("close", reason, config.currentDate, config.currentMinute, config.currentTimeStamp, symbol, price, vol, 0.0);
        config.stockRecord.add(R);

        config.cash-=vol*price; // 减去股票购买成本
    }

    public static void closeStock(String symbol, Double price, Double vol, String reason){
        Double profit = 0.0; // 该笔交易获得的盈利
        Double margin = 0.0; // 该笔交易获得的保证金
        BackTestConfig config = BackTestConfig.getInstance(); // 获取BackTestConfig示例
        ArrayList<Position> position = config.stockPosition.get(symbol);
        LinkedHashMap<String, StockSummary> summary = config.stockSummary; // 获取当前股票的持仓视图

        if (!config.stockPosition.isEmpty()){
            if (!config.stockPosition.containsKey(symbol)){
                System.out.printf("当前%s没有持仓%n", symbol);
            }else{
                // 当前股票有持仓
                // 获取该股票的持仓
                ArrayList<Position> pos_list = config.stockPosition.get(symbol);
                ArrayList<Double> current_vol_list = new ArrayList<>();
                ArrayList<Double> ori_price_list = new ArrayList<>();
                ArrayList<Integer> time_monitor_list = new ArrayList<>();
                for (Position pos : pos_list) {
                    current_vol_list.add(pos.getVol());
                    ori_price_list.add(pos.getPrice());
                    time_monitor_list.add(pos.getTime_monitor());
                }

                // 获取允许平仓的最大数量
                double current_vol;
                boolean state;
                if (time_monitor_list.contains(-2)){
                    // 说明当前持仓队列中存在禁止卖出的股票
                    int index = time_monitor_list.indexOf(-2);
                    current_vol = current_vol_list.stream()
                            .limit(index)
                            .mapToDouble(Double::doubleValue)
                            .sum();
                    state = false;
                }else{
                    // 说明当前持仓队列中所有股票均可以卖出
                    current_vol = current_vol_list.stream()
                            .mapToDouble(Double::doubleValue)
                            .sum();
                    state = true;
                }
                if (current_vol == 0.0){
                    return ; // 说明当前无法平仓
                }
                // 获取当前可以平仓的最大数量
                double max_vol = Math.min(current_vol, vol);
                double record_vol = max_vol;
                if (max_vol >= current_vol && state){  // 说明都可以平仓
                    // 先对视图进行批处理
                    summary.remove(symbol); // 删除该股票的持仓视图
                    // 逐笔计算盈亏
                    for (int i = 0; i < current_vol_list.size(); i++) {
                        Double position_vol = current_vol_list.get(i);
                        Double ori_price = ori_price_list.get(i);
                        margin += price * position_vol;
                        profit += (price - ori_price) * position_vol;  // 逐笔盈亏
                    }
                    // 再对持仓进行处理
                    position.remove(symbol); // 直接删除该股票的持有
                }else{  // 说明只有部分仓位可以被平
                    // 先对视图进行批处理
                    double vol0, amount0, vol1, amount1;
                    vol0 = summary.get(symbol).total_vol;
                    amount0 = summary.get(symbol).total_vol * summary.get(symbol).ori_price;
                    vol1 = vol0 + vol;
                    amount1 = amount0 + vol * price;
                    summary.get(symbol).ori_price = amount1 / vol1;
                    // 再对持仓进行处理
                    for (int i=0; i<current_vol_list.size(); i++){
                        Double posVol = current_vol_list.get(i);
                        Double posPrice = ori_price_list.get(i);
                        if (max_vol >= posVol){ // 当前订单全部平仓
                            margin += price*posVol;
                            profit += (price - posPrice) * posVol;
                            position.remove(0); // FIFO Queue
                            max_vol -= posVol;
                        }else{ // 当前订单部分平仓
                            margin += price*max_vol;
                            profit += (price - posPrice) * max_vol;
                            position.get(0).vol = posVol - max_vol; // FIFO Queue
                            break;
                        }
                    }
                    // 记录本次交易
                    StockRecord R = new StockRecord("close", reason, config.currentDate, config.currentMinute, config.currentTimeStamp,
                            symbol, price, record_vol, profit);
                    config.stockRecord.add(R);

                    // 结算
                    config.profit += profit;
                    config.cash += margin;  // 股票交易中一开始付出的现金可以理解为100%保证金
                    config.stockPosition.put(symbol, position); // 更新股票持仓
                    config.stockSummary.put(symbol, summary.get(symbol));
                }

            }

        }else{
            // 当前一只票也没有持仓, 怎么平仓呢???
        }
    }
}