package com.jelmstrom.tips.user;

import com.jelmstrom.tips.persistence.NeoRepository;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class NeoUserRepository extends NeoRepository implements UserRepository {

    private static final String SEARCH_STRING = "MATCH (n:%s {%s : '%s'}) return n";

    public NeoUserRepository(String test) {
        super();
    }

    @Override
    public void dropAll() {
        dropAll(USER_LABEL);
    }

    @Override
    public User store(User user) {
        try(Transaction tx = vmTips.beginTx()){
            Node userNode;
            User returnValue;
            if(StringUtils.isEmpty(user.id)){
                userNode = vmTips.createNode(USER_LABEL);
                returnValue = new User(Long.toString(userNode.getId()),
                        user.displayName,
                        user.email,
                        user.admin,
                        user.token);
                returnValue.setTopScorer(user.getTopScorer());
                returnValue.setWinner(user.getWinner());
            } else {
                userNode = vmTips.getNodeById(Long.parseLong(user.id));
                returnValue = user;
            }
            userNode.setProperty("displayName",user.displayName );
            userNode.setProperty("email",user.email );
            userNode.setProperty("token",user.token );
            userNode.setProperty("admin",Boolean.toString(user.admin));

            if(null != user.getTopScorer()){
                userNode.setProperty("topScorer",user.getTopScorer() );
            }

            if(null != user.getWinner()){
                userNode.setProperty("winningTeam",user.getWinner() );
            }

            tx.success();
            return returnValue;
        }
    }

    @Override
    public User read(String id) {
        try(Transaction tx = vmTips.beginTx()){
            Node userNode = vmTips.getNodeById(Long.parseLong(id));

            User user = buildUser(userNode);

            tx.success();
            return user;
        }
    }

    public User buildUser(Node userNode) {
        User user = new User(Long.toString(userNode.getId()),
                userNode.getProperty("displayName").toString(),
                userNode.getProperty("email").toString(),
                Boolean.parseBoolean(userNode.getProperty("admin").toString()),
                userNode.getProperty("token").toString());
        if(userNode.hasProperty("topScorer")){
            user.setTopScorer(userNode.getProperty("topScorer").toString());
        }
        if(userNode.hasProperty("winningTeam")){
            user.setWinner(userNode.getProperty("winningTeam").toString());
        }
        return user;
    }

    @Override
    public User findByEmail(String email) {
        List<User> users = findUsersByKeyValue("email", email);
        if(users.isEmpty()){
            return new User("", email, false, "");
        } else if(users.size() > 1) {
            throw new IllegalStateException(String.format("Duplicate users with email %s", email));
        } else {
            return users.get(0);
        }
    }

    private List<User>  findUsersByKeyValue(String key, String value) {
        List<User> users = new ArrayList<>();
        try(Transaction tx = vmTips.beginTx()){
            ResourceIterator<Node> nodes = engine.execute(String.format(SEARCH_STRING, USER_LABEL.name(), key, value)).columnAs("n");
            nodes.forEachRemaining(node -> users.add(buildUser(node)));
            tx.success();
        }
        return users;
    }

    @Override
    public void remove(String userId) {
        try(Transaction tx = vmTips.beginTx()){
            Node userNode = vmTips.getNodeById(Long.parseLong(userId));
            userNode.delete();
            tx.success();
        }
    }

    @Override
    public List<User> read() {
        List<User> users = new ArrayList<>();
        try(Transaction tx = vmTips.beginTx()){
            ResourceIterator<Node> nodes = engine.execute(String.format("MATCH (n:%s) return n", USER_LABEL.name())).columnAs("n");
            nodes.forEachRemaining(node -> users.add(buildUser(node)));
            tx.success();
        }
        return users;
    }

    @Override
    public User findByDisplayName(String displayName) {
        List<User> users = findUsersByKeyValue("displayName", displayName);
        if(users.isEmpty()){
            return new User(displayName, "", false, "");
        } else if(users.size() > 1) {
            throw new IllegalStateException(String.format("Duplicate users with displayName %s", displayName));
        } else {
            return users.get(0);
        }
    }

    @Override
    public User findAdminUser() {
        List<User> users = findUsersByKeyValue("admin", "true");
        if(users.isEmpty()) {
            return new User("", "", false, "");
        } else if(users.size() > 1) {
            throw new IllegalStateException("Duplicate admin users found");
        } else {
            return users.get(0);
        }
    }

    @Override
    public User findByToken(String token) {
        List<User> users = findUsersByKeyValue("token", token);
        if(users.isEmpty()){
            return new User("", "", false, token);
        } else if(users.size() > 1) {
            throw new IllegalStateException(String.format("Duplicate users with token %s", token));
        } else {
            return users.get(0);
        }
    }
}
