package com.jelmstrom.tips.user;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailNotification {

    private final Properties props;
    private Session session;

    private final InternetAddress internetAddress;
    private InternetAddress[] toAddress;


    public EmailNotification(User adminUser)  {

        props = new Properties();
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
            local = new InternetAddress(adminUser.email);
        } catch (AddressException e) {
            System.out.println("Failed to create user for FROM address");
        }
        internetAddress = local;
    }

    public void sendMail(User user) {

        try {

            Message message = new MimeMessage(session);
            message.setFrom(internetAddress);
            //toAddress = InternetAddress.parse(user.email);
            toAddress = InternetAddress.parse("c.elmstrom@gmail.com");
            InternetAddress[] bccAddress = InternetAddress.parse("johan.elmstrom@gmail.com");
            message.setRecipients(Message.RecipientType.TO,toAddress);
            message.setRecipients(Message.RecipientType.BCC,bccAddress);
            message.setSubject("Uppdatering från VM tipsed");
            message.setText("Hej " + user.displayName + "\n\n Din uppdatering har registrerats " +
                    "\n\n\n Mvh Admin \n\n" +
                    "http://bit.ly/vm_tips") ;

            Transport.send(message);

            System.out.println("Mail sent");
        } catch (Exception ex){
            System.out.println("Failed to sed message to " + user.displayName);
        }

    }


}
