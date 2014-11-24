package com.jelmstrom.tips.match;


import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class MatchTest {

     @Test
    public void equalsShouldCompareTwoIdendicalMatchesAsTrue(){
         Date matchStart = new Date();
         Match m1 = new Match ("A", "B", matchStart, -1L);
         m1.setMatchId(1L);
         new Result(m1, 2, 1, 0L);
         Match m2 = new Match ("A", "B", matchStart, -1L);
         m2.setMatchId(1L);
         new Result(m2, 2, 1, 0L);
         matchStart.before(Calendar.getInstance().getTime());
         assertThat(m1.equals(m2), is(true));

     }


}
