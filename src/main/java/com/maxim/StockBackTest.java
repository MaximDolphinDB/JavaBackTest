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

        // 使用BackTestConfig类中已有的getInstance方法
        BackTestConfig config = BackTestConfig.getInstance(jsonContent);

//        // 现在config就是初始化好的单例实例, 可以通过Getter与Setter自由访问
//        config.setCash(111.0);
//        System.out.println(config.getCash());

//        // 尝试进行下单
//        TradeBehavior.orderOpenStock("000001.XSHE", 1.0, 10.0, null, null, null, null, null, null, null, null, null,"");
//        System.out.println(config.getStockCounter().keySet()); // LinkedHashMap, 是不是可以考虑换成别的, 比如TreeMap？

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
//        System.out.println(config.getStockPosition().keySet());
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