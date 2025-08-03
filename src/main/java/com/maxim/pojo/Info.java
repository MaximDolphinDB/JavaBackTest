package com.maxim.pojo;
import java.time.*;

public class Info{
    String symbol;
    LocalDate tradeDate;
    Double open;
    Double high;
    Double low;
    Double close;
    LocalDate start_date;
    LocalDate end_date;

    public Info(LocalDate tradeDate, String symbol, Double open, Double high, Double low, Double close,
                LocalDate start_date, LocalDate end_date) {
        this.tradeDate = tradeDate;
        this.symbol = symbol;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.start_date = start_date;
        this.end_date = end_date;
    }
}