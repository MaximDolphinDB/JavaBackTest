package com.maxim.pojo;
import java.time.*;

public class KBar {
    String symbol;
    LocalDate tradeDate;
    LocalDateTime tradeTime;
    Double open;
    Double high;
    Double low;
    Double close;
    Double volume;

    public KBar(String symbol, LocalDate tradeDate, LocalDateTime tradeTime,
                Double open, Double high, Double low, Double close, Double volume) {
        this.low = low;
        this.symbol = symbol;
        this.tradeDate = tradeDate;
        this.tradeTime = tradeTime;
        this.open = open;
        this.high = high;
        this.close = close;
        this.volume = volume;
    }
}
