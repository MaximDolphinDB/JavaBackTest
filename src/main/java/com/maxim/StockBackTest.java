package com.maxim;
import com.maxim.pojo.*;

import java.nio.file.Files;
import java.nio.file.Paths;

public class StockBackTest {
    public static void main(String[] args) throws Exception{
        // 读取JSON文件内容
        String configPath = "D:\\FactorBrick\\BackTest\\src\\main\\java\\com\\maxim\\backtest_config.json";
        String jsonContent = new String(Files.readAllBytes(Paths.get(configPath)));

        // 使用BackTestConfig类中已有的getInstance方法
        BackTestConfig config = BackTestConfig.getInstance(jsonContent);

        // 现在config就是初始化好的单例实例, 可以通过Getter与Setter自由访问
        config.setCash(111.0);
        System.out.println(config.getCash());
    }
}