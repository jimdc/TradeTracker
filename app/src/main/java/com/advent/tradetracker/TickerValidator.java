package com.advent.tradetracker;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TickerValidator {

    public boolean validate(String string) {
        Pattern tickerPattern = Pattern.compile(" ^\n" +
                " (?<PreXChangeCode>[a-z]{2,4}:(?![a-z\\d]+\\.))?\n" +
                " (?<Stock>[a-z]{1,4}|\\d{1,3}(?=\\.)|\\d{4,})\n" +
                " (?<PostXChangeCode>\\.[a-z]{2})?\n" +
                " $");
        Matcher matcher =tickerPattern.matcher(string);

        return matcher.find();
    }

}
