package com.jelmstrom.tips.table;

import com.jelmstrom.tips.group.Group;
import com.jelmstrom.tips.group.GroupRepository;
import com.jelmstrom.tips.group.NeoGroupRepository;
import com.jelmstrom.tips.user.NeoUserRepository;
import com.jelmstrom.tips.user.User;
import com.jelmstrom.tips.user.UserRepository;
import org.junit.After;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class NeoTablePredictionTest {

    private TablePredictionRepository repo = new NeoTablePredictionRepository("");
    private UserRepository userRepo = new NeoUserRepository("");
    private GroupRepository groupRepository = new NeoGroupRepository("");

    @After
    public void tearDown(){
        repo.dropAll();
        userRepo.dropAll();
        groupRepository.dropAll();
    }

    @Test
    public void predictionByUserShouldReturnTwoPredictions(){
        User user = new User("test", "test", false, "token");
        user = userRepo.store(user);
        List<String> teams = Arrays.asList("a", "b");
        Group groupA = groupRepository.store(new Group("A", teams));
        Group groupB = groupRepository.store(new Group("B", teams));
        TablePrediction prediction = new TablePrediction(groupA.getGroupId(), user.id, teams);
        TablePrediction prediction2 = new TablePrediction(groupB.getGroupId(), user.id, teams);

        repo.store(prediction);
        repo.store(prediction2);

        List<TablePrediction> tablePredictions = repo.predictionsFor(user.id);
        assertThat(tablePredictions.size(), is(2));
        assertThat(tablePredictions.contains(prediction), is(true));
        assertThat(tablePredictions.contains(prediction2), is(true));



    }
}
