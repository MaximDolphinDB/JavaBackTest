package com.maxim;
import com.maxim.pojo.*;
import org.w3c.dom.css.Counter;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;

public class StockBackTest {
    public static void main(String[] args) throws Exception{
        // 读取JSON文件内容
        String configPath = "D:\\Maxim\\JavaBackTest\\src\\main\\java\\com\\maxim\\backtest_config.json";
        String jsonContent = new String(Files.readAllBytes(Paths.get(configPath)));
        BackTestConfig config = BackTestConfig.getInstance(jsonContent);
        // Java单例设计模式, 获取全局配置项, 回测逻辑会实时修改里面的属性

        // 尝试进行下单
        TradeBehavior.orderOpenStock("000001.XSHE", 1.0, 10.0, null, null, null, null, null, null, null, null, null,"",false);
        TradeBehavior.orderCloseStock("000001.XSHE", 1.0, 10.0, null, null, null, false);
        System.out.println(config.getStockCounter().keySet()); // LinkedHashMap, 是不是可以考虑换成别的, 比如TreeMap，在不要求排序的地方从而提高性能？

        // [待实现]: 柜台逻辑

        // 尝试加入持仓
        CounterBehavior.executeStock("000001.XSHE", 1.0, 10.0, null, null, null, null, null, null,"");
        CounterBehavior.executeStock("000002.XSHE", 3.0, 20.0, null, null, null, null, null, null,"");
        CounterBehavior.executeStock("000001.XSHE", 3.0, 10.0, null, null, null, null, null, null,"");

        // 查看股票持仓
        for (String symbol: config.getStockPosition().keySet()){
            Collection<Position> pos_list = config.getStockPosition().get(symbol);
            if (pos_list.size()>1){
                for (Position pos: pos_list){
                    System.out.println(symbol + ":" +pos.getVol());
                }
            }else{
                System.out.println(symbol + ":" + config.getStockPosition().get(symbol).get(0).getVol());
            }
        }

        // 尝试进行平仓
        CounterBehavior.closeStock("000001.XSHE", 1.0, 15.0, "");
        // 查看股票持仓
        for (String symbol: config.getStockPosition().keySet()){
            Collection<Position> pos_list = config.getStockPosition().get(symbol);
            if (pos_list.size()>1){
                for (Position pos: pos_list){
                    System.out.println(symbol + ":" +pos.getVol());
                }
            }else{
                System.out.println(symbol + ":" + config.getStockPosition().get(symbol).get(0).getVol());
            }
        }

    }
}