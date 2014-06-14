package com.jelmstrom.tips.user;

import org.hsqldb.lib.StringUtil;
import org.springframework.util.StringUtils;

public class User {
    public final String displayName;
    public final String email;
    public final boolean admin;
    public final String token;

    public User(String displayName, String email, boolean admin, String token) {
        this.displayName = displayName;
        this.email = email;
        this.admin = admin;
        this.token = token;
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;

        User user = (User) o;

        if (!displayName.equals(user.displayName)) return false;
        if (!email.equals(user.email)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = displayName.hashCode();
        result = 31 * result + email.hashCode();
        return result;
    }

    public String toString(){
        return displayName + " " + email +  "" + (admin?"admin":"");
    }

    public boolean isValid() {
        return !invalid();
    }

    private boolean invalid() {
        return StringUtils.isEmpty(email) || StringUtils.isEmpty(displayName);
    }

    public static User emptyUser() {
       return new User("","", false, "");
    }
}