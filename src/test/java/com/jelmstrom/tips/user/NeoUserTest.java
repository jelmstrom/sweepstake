package com.jelmstrom.tips.user;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class NeoUserTest {

    UserRepository userRepository = new NeoUserRepository("test");
    User user;
    @Before
    public void setUp(){
        User newUser = new User("displayName", "email", false , "token");
        newUser.setTopScorer("scorer");
        newUser.setWinner("winner");
        this.user = userRepository.store(newUser);
    }

    @After
    public void tearDown(){
        userRepository.dropAll();
    }

    @Test
    public void readUserByIdShouldReturnStoredUser(){
        User read = userRepository.read(user.id);
        assertThat(read, is(equalTo(user)));
    }

    @Test
    public void findUserByEmailShouldReturnStoredUser(){
        User read = userRepository.findByEmail(user.email);
        assertThat(read, is(equalTo(user)));
    }


    @Test
    public void findUserByDisplayNameShouldReturnStoredUser(){
        User read = userRepository.findByDisplayName(user.displayName);
        assertThat(read, is(equalTo(user)));
    }

    @Test
    public void findUserByTokenNameShouldReturnStoredUser(){
        User read = userRepository.findByToken(user.token);
        assertThat(read, is(equalTo(user)));
    }


    @Test
    public void findAdminUserShouldReturnAdminUser(){
        User adminUser = userRepository.store(new User("Admin", "mail", true, "adminToken"));
        User read = userRepository.findAdminUser();
        assertThat(read, is(equalTo(adminUser)));
    }


    @Test
    public void deleteUserShouldRemoveUser(){
        User adminUser = userRepository.store(new User("Admin", "mail", true, "adminToken"));
        userRepository.remove(adminUser.id);
        assertThat(userRepository.read().size(), is(1));
    }
}
