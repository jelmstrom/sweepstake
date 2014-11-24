package com.jelmstrom.tips.user;

import java.util.List;

public interface UserRepository {
    User store(User user);

    User read(Long id);

    User findByEmail(String email);

    void remove(Long userId);

    List<User> read();

    User findByDisplayName(String displayName);

    User findAdminUser();

    User findByToken(String token);

    void dropAll();
}
