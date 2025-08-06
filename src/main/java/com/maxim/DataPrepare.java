package com.maxim;
import com.maxim.service.DataLoader; // 数据导入模块
import com.maxim.service.StockKBarStruct;
import com.xxdb.DBConnection;
import java.io.IOException;

public class DataPrepare {
    private static final String DBName = "dfs://stock_cn/combination";
    private static final String TBName = "StockDailyKBar";
    private static final String HOST =  "172.16.0.184";
    private static final int PORT = 8001;
    private static final String USERNAME = "maxim";
    private static final String PASSWORD = "dyJmoc-tiznem-1figgu";
    public static void main(String[] args) throws IOException {
        DBConnection conn = new DBConnection();
        conn.connect(HOST, PORT);
        conn.login(USERNAME, PASSWORD, true);
        DataLoader dl = new DataLoader();
    }

    public static void getStockKBarJson(DBConnection conn, String DBName, String TBName, StockKBarStruct struct){

    }
}