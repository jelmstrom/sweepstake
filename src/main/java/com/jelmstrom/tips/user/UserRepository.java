package com.jelmstrom.tips.user;

import com.jelmstrom.tips.persistence.MongoRepository;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import java.util.List;

import static java.util.stream.Collectors.toList;


public class UserRepository extends MongoRepository {

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
            BasicDBObject dbUser = new BasicDBObject(EMAIL, user.email)
                    .append(CREDENTIALS, user.credentials)
                    .append(DISPLAY_NAME, user.displayName)
                    .append(ADMIN, user.admin);
        if(read(user.email).credentials.isEmpty()){
            userCollection.insert(dbUser);
        } else  {
            userCollection.update(new BasicDBObject(EMAIL, user.email)
                    , dbUser);
        }
    }

    public User read(String email) {
        DBObject users = userCollection.findOne(new BasicDBObject(EMAIL, email));
        if(users != null && null != users.get(EMAIL)){
            return buildUser(users);
        }
        return new User("", email,"", false);
    }

    private User buildUser(DBObject dbUser) {

        return new User(dbUser.get(DISPLAY_NAME).toString()
                , dbUser.get(EMAIL).toString()
                , dbUser.get(CREDENTIALS).toString()
                , Boolean.parseBoolean(dbUser.get(ADMIN).toString()));
    }

    public void remove(String user) {
        userCollection.remove(new BasicDBObject(EMAIL, user));
    }

    public List<User> read() {
        return userCollection.find().toArray().stream().map(this::buildUser).collect(toList());

    }

    public User find(String displayName) {
        DBObject users = userCollection.findOne(new BasicDBObject(DISPLAY_NAME, displayName));
        if(users != null && null != users.get(EMAIL)){
            return buildUser(users);
        }
        return new User(displayName, "","", false);
    }

    public User findAdminUser() {
        DBObject user = userCollection.findOne(new BasicDBObject(ADMIN, true));
        System.out.println("Admin Users " + user);
        if(user != null && null != user.get(EMAIL)){
            System.out.println("building user");
            User admin= buildUser(user);
            System.out.println("Admin Users " + admin);
            return admin;

        }
        System.out.println("building user");
        return new User("", "","", false);
    }
}
