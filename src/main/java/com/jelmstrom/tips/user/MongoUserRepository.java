package com.jelmstrom.tips.user;

import com.jelmstrom.tips.persistence.MongoRepository;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import org.bson.types.ObjectId;
import org.springframework.util.StringUtils;

import java.util.List;

import static java.util.stream.Collectors.toList;


public class MongoUserRepository extends MongoRepository implements UserRepository {

    public static final String TOKEN = "token";
    public static final String ID = "_id";
    public final DBCollection userCollection;
    public static final String EMAIL = "email";
    public static final String DISPLAY_NAME = "displayName";
    public static final String ADMIN = "isAdmin";
    public static final String SCORER = "topScorer";
    public static final String WINNER = "winner";

    public MongoUserRepository(String context) {
        super(context);
        userCollection = getDb().getCollection("registeredUser");
    }

    @Override
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
            userCollection.update(new BasicDBObject(ID, new ObjectId(existingUser.id.toString())), dbUser);
        } else  {
           userCollection.insert(dbUser);
        }
        return findByEmail(user.email);
    }

    @Override
    public User read(Long id) {
        if(null == id) {
            return User.emptyUser();
        } else {
            return buildUser(userCollection.findOne(new BasicDBObject(ID, new ObjectId(id.toString()))));
        }
    }


     private String handleEmptyToken(String userToken, String existingToken) {
        return StringUtils.isEmpty(existingToken)? userToken : existingToken;

    }

    @Override
    public User findByEmail(String email) {
        return buildUser(userCollection.findOne(new BasicDBObject(EMAIL, email)));
    }

    private User buildUser(DBObject dbUser) {
        if(dbUser != null && null != dbUser.get(ID)){
            User user = new User((long) dbUser.get(ID).toString().hashCode()
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

    @Override
    public void remove(Long userId) {
        userCollection.remove(new BasicDBObject(ID, new ObjectId(userId.toString())));
    }

    @Override
    public List<User> read() {
        return userCollection.find().toArray().stream().map(this::buildUser).collect(toList());

    }

    @Override
    public User findByDisplayName(String displayName) {
        return buildUser(userCollection.findOne(new BasicDBObject(DISPLAY_NAME, displayName)));
    }

    @Override
    public User findAdminUser() {
        return buildUser(userCollection.findOne(new BasicDBObject(ADMIN, true)));
    }


    @Override
    public User findByToken(String token) {
        return buildUser(userCollection.findOne(new BasicDBObject(TOKEN, token)));
    }

    @Override
    public void dropAll() {
        userCollection.drop();
    }
}
