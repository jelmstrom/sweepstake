package com.jelmstrom.tips.user;

import org.springframework.util.StringUtils;

public class User {
    public final String displayName;
    public final String email;
    public final boolean admin;
    public final String token;
    public final String id;
    private String topScorer;
    private String winner;

    public User(String displayName, String email, boolean admin, String token) {
        this.displayName = displayName;
        this.email = email;
        this.admin = admin;
        this.token = token;
        this.id = "";
    }

     public User(String id, String displayName, String email, boolean admin, String token) {
        this.displayName = displayName;
        this.email = email;
        this.admin = admin;
        this.token = token;
        this.id = id;
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;

        User user = (User) o;

        if (!displayName.equals(user.displayName)) return false;
        if (!email.equals(user.email)) return false;
        if (!id.equals(user.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = displayName.hashCode();
        result = 31 * result + email.hashCode();
        result = 31 * result + id.hashCode();
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

    public void setTopScorer(String topScorer) {
        this.topScorer = topScorer;
    }

    public String getTopScorer() {
        return topScorer;
    }

    public int score(User adminUser) {
        int score = 0;

        if(!StringUtils.isEmpty(topScorer)){
            score += topScorer.equals(adminUser.topScorer)?10:0;
        }

        if(!StringUtils.isEmpty(winner)){
            score += winner.equals(adminUser.winner)?10:0;
        }

        return score;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    public String getWinner() {
        return winner;
    }
}
