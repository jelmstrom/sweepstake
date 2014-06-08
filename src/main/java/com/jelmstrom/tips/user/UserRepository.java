package com.jelmstrom.tips.user;

import com.jelmstrom.tips.persistence.MongoRepository;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import java.util.List;

import static java.util.stream.Collectors.toList;


public class UserRepository extends MongoRepository {

    public static final DBCollection userCollection = getDb().getCollection("registeredUser");
    public static final String EMAIL = "email";
    public static final String CREDENTIALS = "credentials";
    public static final String DISPLAY_NAME = "displayName";
    public static final String ADMIN = "isAdmin";

    public static void store(User user){
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

    public static User read(String email) {
        DBObject users = userCollection.findOne(new BasicDBObject(EMAIL, email));
        if(users != null && null != users.get(EMAIL)){
            return buildUser(users);
        }
        return new User("", email,"", false);
    }

    private static User buildUser(DBObject dbUser) {

        return new User(dbUser.get(DISPLAY_NAME).toString()
                , dbUser.get(EMAIL).toString()
                , dbUser.get(CREDENTIALS).toString(), false);
    }

    public static void remove(String user) {
        userCollection.remove(new BasicDBObject(EMAIL, user));
    }

    public static List<User> read() {
        return userCollection.find().toArray().stream().map(UserRepository::buildUser).collect(toList());

    }

    public static User find(String displayName) {
        DBObject users = userCollection.findOne(new BasicDBObject(DISPLAY_NAME, displayName));
        if(users != null && null != users.get(EMAIL)){
            return buildUser(users);
        }
        return new User(displayName, "","", false);
    }

    public static User findAdminUser() {
        DBObject users = userCollection.findOne(new BasicDBObject(ADMIN, true));
        if(users != null && null != users.get(EMAIL)){
            return buildUser(users);
        }
        return new User("", "","", false);
    }
}
