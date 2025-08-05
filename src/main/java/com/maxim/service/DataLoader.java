package com.maxim.service;
import com.xxdb.DBConnection;
import com.xxdb.data.BasicDateVector;
import com.xxdb.data.BasicTable;
import com.xxdb.data.Entity;

import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class DataLoader {
    // 连接DolphinDB并获取结构体数据对象, 返回JSON格式
    protected String HOST;
    protected Integer PORT;
    protected String USERNAME;
    protected String PASSWORD;
    protected Integer ThreadCount; // 线程数量

    public static void main(String[] args) throws IOException {
        // 创建DolphinDB连接对象 + 数据库连接池
        DBConnection conn = new DBConnection();
        String HOST = "172.16.0.184";
        int PORT = 8001;
        String USERNAME = "maxim";
        String PASSWORD = "dyJmoc-tiznem-1figgu";
        conn.connect(HOST, PORT);
        conn.login(USERNAME, PASSWORD, true);
        String DBName = "dfs://MinKDB";
        String TBName = "Min1K";
        String symbolCol = "code";
        String dateCol = "tradeDate";
        String timeCol = "tradeTime";
        String openCol = "open";
        String highCol = "high";
        String lowCol = "low";
        String closeCol = "close";
        String volumeCol= "volume";
        String start_date = "2020.01.01";
        String end_date = "2020.01.31";

        // 定义KBar结构体
        StockKBarStruct struct = new StockKBarStruct(symbolCol, dateCol, timeCol, openCol, highCol, lowCol, closeCol, volumeCol);
        // 多线程获取KBar数据
        DataLoader loader = new DataLoader(); // 创建DataLoader对象
        // 指定股票列表
        ArrayList<String> symbol_list = new ArrayList<>(Arrays.asList("000001.SZ", "000002.SZ", "000004.SZ", "000005.SZ", "000006.SZZ", "000007.SZ", "000008.SZ"));
        ConcurrentHashMap<LocalDate, BasicTable> KBarMap = getStockDailyKBar(conn, DBName, TBName, start_date, end_date, struct, symbol_list);
    }
    public static ConcurrentHashMap<LocalDate, BasicTable> getStockDailyKBar(DBConnection conn, String DBName, String TBName,
                                                      String start_date, String end_date, StockKBarStruct struct, ArrayList<String> symbol_list) throws IOException {
        /*
        start_date：like 2020.01.01
        end_date：like 2020.01.02
        */
        // 多线程从DolphinDB数据库中取数，返回BasicTable
        // 转换字符串为DolphinDB List str
        String symbol_list_str = Utils.arrayToDolphinDBString(symbol_list);
        System.out.println("symbolList: " + symbol_list_str);

        // 获取所有事件
        BasicDateVector date_list = (BasicDateVector) conn.run("""
                t = select count(*) as count from loadTable("%s", "%s") where %s between date(%s) and date(%s) group by %s order by %s; 
                exec %s from t
                """.formatted(DBName, TBName, struct.dateCol, start_date, end_date, struct.dateCol, struct.dateCol, struct.dateCol));
        System.out.println("dateList: " + date_list.getString());

        // 创建多线程结果接收集合
        ConcurrentHashMap<LocalDate, BasicTable> resultMap = new ConcurrentHashMap<>();
        Collection<CompletableFuture<Void>> futures = new ArrayList<>();

        for (int i=0; i<date_list.rows(); i++) {
            LocalDate tradeDate = date_list.getDate(i);
            String tradeDateStr = tradeDate.format(DateTimeFormatter.ofPattern("yyyy.MM.dd")); // 2020.01.01
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    String script = """
                    select %s,%s,%s,
                    %s,%s,%s,%s,%s from loadTable("%s","%s") where %s == date(%s)
                    and %s in %s
                    """.formatted(struct.symbolCol, struct.dateCol, struct.timeCol,
                            struct.openCol, struct.highCol, struct.lowCol, struct.closeCol, struct.volumeCol,
                            DBName, TBName, struct.dateCol, tradeDateStr, struct.symbolCol, symbol_list_str);
                    System.out.println(script);
                    BasicTable data = (BasicTable) conn.run(script,4,4);
                    resultMap.put(tradeDate, data);
                    System.out.println("Date: " + tradeDate);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            futures.add(future);
        }

        // 等待所有任务完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // 输出结果
        resultMap.forEach((date, table) -> {
            System.out.println("Date: " + date);
            System.out.println(table.getString());
        });
        return resultMap;
    }
}

class StockKBarStruct {
    protected String symbolCol;
    protected String dateCol;
    protected String timeCol; // LocalDateTime Format(DolphinDB Timestamp)
    protected String openCol;
    protected String highCol;
    protected String lowCol;
    protected String closeCol;
    protected String volumeCol;

    // 构造方式一: date + time
    public StockKBarStruct(String symbolCol, String dateCol, String timeCol, String openCol, String highCol, String lowCol, String closeCol, String volumeCol) {
        this.symbolCol = symbolCol;
        this.dateCol = dateCol;
        this.timeCol = timeCol;
        this.openCol = openCol;
        this.highCol = highCol;
        this.lowCol = lowCol;
        this.closeCol = closeCol;
        this.volumeCol = volumeCol;
    }

    // 构造方式二：仅 time
    public StockKBarStruct(String symbolCol, String timeCol, String openCol, String highCol, String lowCol, String closeCol, String volumeCol) {
        this.symbolCol = symbolCol;
        this.timeCol = timeCol;
        this.openCol = openCol;
        this.highCol = highCol;
        this.lowCol = lowCol;
        this.closeCol = closeCol;
        this.volumeCol = volumeCol;
    }
}

class StockInfoStruct{
    protected String symbolCol;
    protected String timeCol;
    protected String openCol;
    protected String highCol;
    protected String lowCol;
    protected String startDate;
    protected String endDate;
}

class Utils{
    public static String arrayToDolphinDBString(ArrayList<String> array) {
        /*
        输入：String[] arr = {"c1","c2"}
        输出：["c1","c2"]
        */
        String[] arr = new String[array.size()];
        for (int i=0; i<array.size(); i++){
            arr[i] = array.get(i);
        }
        return "[" + Arrays.stream(arr)
                .map(s -> "'" + s + "'")
                .collect(Collectors.joining(", ")) + "]";
    }
}