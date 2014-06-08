package com.jelmstrom.tips;


import com.jelmstrom.tips.match.Match;
import com.jelmstrom.tips.match.Result;
import org.junit.Test;

import java.util.Date;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class MatchTest {

     @Test
    public void equalsShouldCompareTwoIdendicalMatchesAsTrue(){
         Date matchStart = new Date();
         Match m1 = new Match ("A", "B", matchStart, "1");
         new Result(m1, 2, 1, "Johan");
         Match m2 = new Match ("A", "B", matchStart, "1");
         new Result(m2, 2, 1, "Johan");

         assertThat(m1.equals(m2), is(true));

     }


}
