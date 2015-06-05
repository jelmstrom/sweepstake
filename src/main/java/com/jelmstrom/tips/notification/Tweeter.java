package com.jelmstrom.tips.notification;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class Tweeter {

    public static Twitter twitter;
    public static boolean enabled;

    @SuppressWarnings("unused")
    public boolean send(String message) {

        if (enabled) {
            System.out.println(" ############  Tweet : " + message);

            try {
                twitter.updateStatus(message);
            } catch (TwitterException e) {
                System.out.println("Tweet failed + " + e.getMessage());
                return false;
            }
        }
        return true;
    }

    public static void configure(String accessTokenSecret, String accessToken, String consumerSecret, String consumerKey, Boolean enabled){
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthAccessTokenSecret(accessTokenSecret)
                .setOAuthConsumerKey(consumerKey)
                .setOAuthAccessToken(accessToken)
                .setOAuthConsumerSecret(consumerSecret);
        TwitterFactory tf = new TwitterFactory(cb.build());
        twitter = tf.getInstance();
        Tweeter.enabled=enabled;
        System.out.println("Tweet configured" + (Tweeter.enabled ? " and enabled" : " but disabled ")) ;

    }
}
