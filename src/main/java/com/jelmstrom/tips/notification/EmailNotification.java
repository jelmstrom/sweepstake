package com.jelmstrom.tips.notification;

import com.jelmstrom.tips.user.User;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailNotification {

    private Session session;

    private final InternetAddress internetAddress;


    public EmailNotification() {

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication("chrilles.vmtips@gmail.com", "vmtips 2014");
                    }
                });
        InternetAddress local = null;
        try {
            local = new InternetAddress("chrilles.vmtips@gmail.com");
        } catch (AddressException e) {
            System.out.println("Failed to create user for FROM address");
        }
        internetAddress = local;
    }

    public void sendMail(User user) {

        try {
            Message message = new MimeMessage(session);
            message.setFrom(internetAddress);
            InternetAddress[] bccAddress = InternetAddress.parse("johan.elmstrom@gmail.com");
            InternetAddress[] toAddress = InternetAddress.parse(user.email);
            message.setRecipients(Message.RecipientType.TO, toAddress);
            message.setRecipients(Message.RecipientType.BCC, bccAddress);
            message.setSubject("Uppdatering från VM tipset");
            message.setText("Hej " + user.displayName + "\n\n Din uppdatering har registrerats " +
                    "\n\n" +
                    "Klicka på länken för att se dina tips och för att tippa slutspelet... \n" +
                    "http://54.76.168.51:8080/authenticate/" + user.token +
                    "\n \n  " +
                    "Mvh Admin \n\n bit.ly/vm_tips");

            //Transport.send(message);

            System.out.println("Mail sent to " + user.email + " : " + user.token);
        } catch (Exception ex) {
            System.out.println("Failed to sed message to " + user.displayName);
            throw new IllegalStateException("failed to send message ", ex);
        }

    }


}
