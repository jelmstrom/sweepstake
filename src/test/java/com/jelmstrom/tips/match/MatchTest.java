package com.jelmstrom.tips.match;


import com.jelmstrom.tips.configuration.Config;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class MatchTest {

     @Test
    public void equalsShouldCompareTwoIdendicalMatchesAsTrue(){

          ZonedDateTime matchStart = ZonedDateTime.now(Config.STOCKHOLM);
          Match m1 = new Match ("A", "B", matchStart, -1L);
          m1.setId(1L);
          new Result(m1, 2, 1, 0L);
          Match m2 = new Match ("A", "B", matchStart, -1L);
          m2.setId(1L);
          new Result(m2, 2, 1, 0L);
          matchStart.isBefore(ZonedDateTime.now(Config.STOCKHOLM));
          assertThat(m1.equals(m2), is(true));

     }


     @Test
     public void ZonedDateTimeTest(){
          ZonedDateTime now = ZonedDateTime.now(Config.STOCKHOLM);
          System.out.println(now.format(DateTimeFormatter.ISO_ZONED_DATE_TIME));

          ZonedDateTime again = ZonedDateTime.parse(now.format(DateTimeFormatter.ISO_ZONED_DATE_TIME)
                                                       , DateTimeFormatter.ISO_ZONED_DATE_TIME);
          assertThat(again, equalTo(now));

          System.out.println(now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

     }

     @Test
     public void parseDateInputString() {
          String date = "2016-01-10T10:00";
          java.time.LocalDateTime dt = LocalDateTime.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
          ZonedDateTime zdt = ZonedDateTime.of(dt, Config.STOCKHOLM);
          assertThat(zdt.getHour(), is(10));
     }


}
