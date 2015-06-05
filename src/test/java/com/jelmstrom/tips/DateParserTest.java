package com.jelmstrom.tips;

import org.junit.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.time.zone.ZoneOffsetTransitionRule;
import java.util.Date;

public class DateParserTest {

    @Test
    public void pattern(){

        LocalDateTime parse = LocalDateTime.parse("06 06 2015 20:00", DateTimeFormatter.ofPattern("dd MM yyyy HH:mm"));;
        Date oldDate = new Date(parse.atZone(ZoneId.of("Europe/Stockholm")).toInstant().toEpochMilli());


        System.out.print(oldDate.toString());


    }

}
