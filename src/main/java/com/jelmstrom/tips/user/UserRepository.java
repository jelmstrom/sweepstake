package com.jelmstrom.tips.user;

import com.jelmstrom.tips.persistence.MongoRepository;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;
import org.bson.types.ObjectId;
import org.springframework.util.StringUtils;

import java.util.List;

import static java.util.stream.Collectors.toList;


public class UserRepository extends MongoRepository {

    public static final String TOKEN = "token";
    public static final String ID = "_id";
    public final DBCollection userCollection;
    public static final String EMAIL = "email";
    public static final String CREDENTIALS = "credentials";
    public static final String DISPLAY_NAME = "displayName";
    public static final String ADMIN = "isAdmin";

    public UserRepository(String context) {
        super(context);
        userCollection = getDb().getCollection("registeredUser");
    }

    public User store(User user){
            User existingUser = read(user.id);
            String token= handleEmptyToken(user.token, existingUser.token);
            BasicDBObject dbUser = new BasicDBObject(EMAIL, user.email)
                    .append(DISPLAY_NAME, user.displayName)
                    .append(ADMIN, user.admin)
                    .append(TOKEN, token);
        if(existingUser.isValid() && existingUser.id.equals(user.id)){
            userCollection.update(new BasicDBObject(ID, new ObjectId(existingUser.id)), dbUser);
        } else  {
           userCollection.insert(dbUser);
        }
        return findByEmail(user.email);
    }

    private User read(String id) {
        if(StringUtils.isEmpty(id)) {
            return User.emptyUser();
        } else {
            return buildUser(userCollection.findOne(new BasicDBObject(ID, new ObjectId(id))));
        }
    }

    public String handleEmptyToken(String userToken, String existingToken) {
        return StringUtils.isEmpty(existingToken)? userToken : existingToken;

    }

    public User findByEmail(String email) {
        return buildUser(userCollection.findOne(new BasicDBObject(EMAIL, email)));
    }

    private User buildUser(DBObject dbUser) {
        if(dbUser != null && null != dbUser.get("_id")){
              return new User(dbUser.get("_id").toString()
                , dbUser.get(DISPLAY_NAME).toString()
                , dbUser.get(EMAIL).toString()
                , Boolean.parseBoolean(dbUser.get(ADMIN).toString())
                , (String) dbUser.get(TOKEN));
        } else {
            return User.emptyUser();
        }
    }

    public void remove(String user) {
        userCollection.remove(new BasicDBObject(EMAIL, user));
    }

    public List<User> read() {
        return userCollection.find().toArray().stream().map(this::buildUser).collect(toList());

    }

    public User findByDisplayName(String displayName) {
        return buildUser(userCollection.findOne(new BasicDBObject(DISPLAY_NAME, displayName)));
    }

    public User findAdminUser() {
        return buildUser(userCollection.findOne(new BasicDBObject(ADMIN, true)));
    }


    public User findByToken(String token) {
        return buildUser(userCollection.findOne(new BasicDBObject(TOKEN, token)));
    }
}
