package com.jelmstrom.tips.user;

import com.jelmstrom.tips.persistence.MongoRepository;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import org.springframework.util.StringUtils;

import java.util.List;

import static java.util.stream.Collectors.toList;


public class UserRepository extends MongoRepository {

    public static final String TOKEN = "token";
    public final DBCollection userCollection;
    public static final String EMAIL = "email";
    public static final String CREDENTIALS = "credentials";
    public static final String DISPLAY_NAME = "displayName";
    public static final String ADMIN = "isAdmin";

    public UserRepository(String context) {
        super(context);
        userCollection = getDb().getCollection("registeredUser");
    }

    public void store(User user){
            User existingUser = read(user.email);
            String token= handleEmptyToken(user.token, existingUser.token);
            BasicDBObject dbUser = new BasicDBObject(EMAIL, user.email)
                    .append(CREDENTIALS, user.credentials)
                    .append(DISPLAY_NAME, user.displayName)
                    .append(ADMIN, user.admin)
                    .append(TOKEN, token);
        if(existingUser.isValid()){
            userCollection.update(new BasicDBObject(EMAIL, user.email), dbUser);
        } else  {
            userCollection.insert(dbUser);
        }
    }

    public String handleEmptyToken(String userToken, String existingToken) {
        return StringUtils.isEmpty(existingToken)? userToken : existingToken;

    }

    public User read(String email) {
        DBObject users = userCollection.findOne(new BasicDBObject(EMAIL, email));
        if(users != null && null != users.get(EMAIL)){
            return buildUser(users);
        }
        return new User("", email,"", false, "");
    }

    private User buildUser(DBObject dbUser) {
        if(dbUser != null && null != dbUser.get(EMAIL)){
              return new User(dbUser.get(DISPLAY_NAME).toString()
                , dbUser.get(EMAIL).toString()
                , dbUser.get(CREDENTIALS).toString()
                , Boolean.parseBoolean(dbUser.get(ADMIN).toString())
                , (String) dbUser.get(TOKEN));
        } else {
            return new User("", "","", false, "");
        }
    }

    public void remove(String user) {
        userCollection.remove(new BasicDBObject(EMAIL, user));
    }

    public List<User> read() {
        return userCollection.find().toArray().stream().map(this::buildUser).collect(toList());

    }

    public User find(String displayName) {
        return buildUser(userCollection.findOne(new BasicDBObject(DISPLAY_NAME, displayName)));
    }

    public User findAdminUser() {
        return buildUser(userCollection.findOne(new BasicDBObject(ADMIN, true)));
    }


    public User findByToken(String token) {
        return buildUser(userCollection.findOne(new BasicDBObject(TOKEN, token)));
    }
}
