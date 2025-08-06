package com.maxim.service;
import com.xxdb.DBConnection;
import com.xxdb.data.*;

import java.io.IOException;
import java.lang.Void;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.io.File;
import java.io.FileWriter;
import com.alibaba.fastjson2.JSONObject;

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
        String savePath = "D:\\FactorBrick\\JavaBackTest\\data\\stock_cn\\kbar";

        String symbolCol = "code";
        String dateCol = "tradeDate";
        String timeCol = "tradeTime";
        String openCol = "open";
        String highCol = "high";
        String lowCol = "low";
        String closeCol = "close";
        String volumeCol= "volume";
        String start_date = "2020.01.01";
        String end_date = "2020.01.03";

        // 定义KBar结构体
        StockKBarStruct struct = new StockKBarStruct(symbolCol, dateCol, timeCol, openCol, highCol, lowCol, closeCol, volumeCol);
        // 指定股票列表
        ArrayList<String> symbol_list = new ArrayList<>(Arrays.asList("000001.SZ", "000002.SZ", "000004.SZ", "000005.SZ", "000006.SZ", "000007.SZ", "000008.SZ"));
        // 多线程获取KBar数据
        ConcurrentHashMap<LocalDate, BasicTable> KBarMap = getStockDailyKBar(conn, DBName, TBName, savePath, start_date, end_date, struct, symbol_list);
    }
    public static ConcurrentHashMap<LocalDate, BasicTable> getStockDailyKBar(DBConnection conn, String DBName, String TBName, String savePath,
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

                    // 写入到本地Json文件
                    saveDataToJson(data, savePath, struct, tradeDate);

                    // 保存到结果集
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

        return resultMap;
    }

    public static void saveDataToJson(BasicTable data, String savePath, StockKBarStruct struct, LocalDate tradeDate){
        String dateStr = tradeDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String datePath = savePath + File.separator + dateStr; // Linux & Windows 兼容方法
        File dateDir = new File(datePath);  // 目录
        if (!dateDir.exists()){
            Boolean saveState = dateDir.mkdirs(); // 创建目录
        }

        // 按照股票代码分组并保存数据
        int rowCount = data.rows(); // 行数
        ConcurrentHashMap<String, JSONObject> dataMap = new ConcurrentHashMap<>();

        for (int i = 0; i < rowCount; i++){
            String symbol = data.getColumn(struct.symbolCol).get(i).toString();
            BasicDate date = (BasicDate) data.getColumn(struct.dateCol).get(i);
            double open = Double.parseDouble(data.getColumn(struct.openCol).get(i).getString());
            double high = Double.parseDouble(data.getColumn(struct.highCol).get(i).getString());
            double low = Double.parseDouble(data.getColumn(struct.lowCol).get(i).getString());
            double close = Double.parseDouble(data.getColumn(struct.closeCol).get(i).getString());
            double volume = Double.parseDouble(data.getColumn(struct.volumeCol).get(i).getString());

            // 构造时间戳
            LocalDateTime dateTime = LocalDateTime.of(date.getDate(), LocalTime.of(15, 0));
            String timestamp = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            // 创建1500分钟数据
            JSONObject minuteData = new JSONObject();
            minuteData.put("minute", 1500);
            minuteData.put("timestamp", timestamp);
            minuteData.put("open", open);
            minuteData.put("high", high);
            minuteData.put("low", low);
            minuteData.put("close", close);
            minuteData.put("volume", volume);

            // 将数据添加到对应股票代码的JSON对象中
            dataMap.computeIfAbsent(symbol, k -> new JSONObject()).put("1500", minuteData);

        }
        // 保存每个股票的数据到JSON文件
        dataMap.forEach((symbol, jsonData) -> {
            try {
                String fileName = datePath + File.separator + symbol + ".json";
                try (FileWriter fileWriter = new FileWriter(fileName)) {
                    fileWriter.write(jsonData.toJSONString());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}


class Utils{
    public static boolean deleteFileDir(String fileDir){
        // 删除文件夹
        File file = new File(fileDir);
        if (file.exists() && file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isDirectory()) {
                        deleteFileDir(f.getAbsolutePath());
                    } else {
                        f.delete();
                    }
                }
            }
            return file.delete();
        }
        return false;
    }
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