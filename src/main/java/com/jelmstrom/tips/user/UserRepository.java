package com.jelmstrom.tips.user;

import com.jelmstrom.tips.persistence.MongoRepository;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import org.bson.types.ObjectId;
import org.springframework.util.StringUtils;

import java.util.List;

import static java.util.stream.Collectors.toList;


public class UserRepository extends MongoRepository {

    public static final String TOKEN = "token";
    public static final String ID = "_id";
    public final DBCollection userCollection;
    public static final String EMAIL = "email";
    public static final String DISPLAY_NAME = "displayName";
    public static final String ADMIN = "isAdmin";
    public static final String SCORER = "topScorer";
    public static final String WINNER = "winner";

    public UserRepository(String context) {
        super(context);
        userCollection = getDb().getCollection("registeredUser");
    }

    public User store(User user){
            User existingUser = read(user.id);
            String token= handleEmptyToken(user.token, existingUser.token);
            BasicDBObject dbUser = new BasicDBObject(EMAIL, user.email.trim())
                    .append(DISPLAY_NAME, user.displayName.trim())
                    .append(ADMIN, user.admin)
                    .append(TOKEN, token.trim())
                    .append(SCORER, user.getTopScorer())
                    .append(WINNER, user.getWinner());
        if(existingUser.isValid() && existingUser.id.equals(user.id)){
            userCollection.update(new BasicDBObject(ID, new ObjectId(existingUser.id)), dbUser);
        } else  {
           userCollection.insert(dbUser);
        }
        return findByEmail(user.email);
    }

    public User read(String id) {
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
        if(dbUser != null && null != dbUser.get(ID)){
            User user = new User(dbUser.get(ID).toString()
                    , dbUser.get(DISPLAY_NAME).toString()
                    , dbUser.get(EMAIL).toString()
                    , Boolean.parseBoolean(dbUser.get(ADMIN).toString())
                    , (String) dbUser.get(TOKEN));
            user.setTopScorer((String)dbUser.get(SCORER));
            user.setWinner((String)dbUser.get(WINNER));
            return user;
        } else {
            return User.emptyUser();
        }
    }

    public void remove(String userId) {
        userCollection.remove(new BasicDBObject(ID, new ObjectId(userId)));
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
