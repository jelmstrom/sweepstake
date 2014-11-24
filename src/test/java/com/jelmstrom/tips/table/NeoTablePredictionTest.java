package com.jelmstrom.tips.table;

import com.jelmstrom.tips.user.NeoUserRepository;
import com.jelmstrom.tips.user.User;
import com.jelmstrom.tips.user.UserRepository;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class NeoTablePredictionTest {

    private TablePredictionRepository repo = new NeoTablePredictionRepository("");
    private UserRepository userRepo = new NeoUserRepository("");

    @Test
    public void predictionByUserSHoudReturnTwoPredictions(){
        User user = new User("test", "test", false, "token");
        user = userRepo.store(user);
        TablePrediction prediction = new TablePrediction("A", user.id, Arrays.asList("a", "b"));
        TablePrediction prediction2 = new TablePrediction("B", user.id, Arrays.asList("a", "b"));

        repo.store(prediction);
        repo.store(prediction2);

        List<TablePrediction> tablePredictions = repo.predictionsFor(user.id);
        assertThat(tablePredictions.size(), is(2));
        assertThat(tablePredictions.contains(prediction), is(true));
        assertThat(tablePredictions.contains(prediction2), is(true));



    }
}
