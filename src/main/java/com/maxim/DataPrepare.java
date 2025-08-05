package com.maxim;
import com.maxim.service.DataLoader; // 数据导入模块

public class DataPrepare {
    private static final String DBName = "dfs://stock_cn/combination";
    private static final String TBName = "StockDailyKBar";
    public static void main(String[] args) {
        DataLoader dl = new DataLoader();
    }
}