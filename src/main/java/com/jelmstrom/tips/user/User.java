package com.jelmstrom.tips.user;

public class User {
    public final String displayName;
    public final String email;
    public final String credentials;
    public final boolean admin;

    public User(String displayName, String email, String credentials, boolean admin) {
        this.displayName = displayName;
        this.email = email;
        this.credentials = credentials;
        this.admin = admin;
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;

        User user = (User) o;

        if (!credentials.equals(user.credentials)) return false;
        if (!displayName.equals(user.displayName)) return false;
        if (!email.equals(user.email)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = displayName.hashCode();
        result = 31 * result + email.hashCode();
        result = 31 * result + credentials.hashCode();
        return result;
    }
}
