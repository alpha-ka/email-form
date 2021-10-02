package com.alpha.SpringBootEmailTutorial.AttachmentReceiver;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
 
import javax.mail.Address;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Part;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeBodyPart;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
 
@Service
public class EmailAttachmentReceiver {
   
	@Value("${savepath}")
	private String saveDirectory;
	 
  
    public void downloadEmailAttachments( String protocol, String host, String port, String userName, String password) {
        Properties properties = new Properties();
 
        // server setting
        properties.put("mail.smpt.auth", "true"); 
        properties.put("mail.smpt.host", host);
        properties.put("mail.smpt.port", port);
         

        // SSL setting
        properties.setProperty("mail.smpt.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.setProperty("mail.smpt.socketFactory.fallback", "false");
        properties.setProperty("mail.smpt.socketFactory.port", port);
 
     // Get the Session object.
        Session session = Session.getInstance(properties,
        new javax.mail.Authenticator() {
        protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(userName, password);
        }
        });
        session.setDebug(true);
 
        try {
            // connects to the message store
            Store store = session.getStore(protocol);
            store.connect(userName, password);
 
            // opens the inbox folder
            Folder folderInbox = store.getFolder("INBOX");
            folderInbox.open(Folder.READ_ONLY);
 
            // fetches new messages from server
            Message[] arrayMessages = folderInbox.getMessages();
 
            for (int i = 0; i < arrayMessages.length; i++) {
                Message message = arrayMessages[i];
                Address[] fromAddress = message.getFrom();
                String from = fromAddress[0].toString();
                String subject = message.getSubject();
                String sentDate = message.getSentDate().toString();
 
                String contentType = message.getContentType();
                String messageContent = "";
 
                // store attachment file name, separated by comma
                String attachFiles = "";
 
                if (contentType.contains("multipart")) {
                    // content may contain attachments
                    Multipart multiPart = (Multipart) message.getContent();
                    int numberOfParts = multiPart.getCount();
                    for (int partCount = 0; partCount < numberOfParts; partCount++) {
                        MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(partCount);
                        if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                            // this part is attachment
                            String fileName = part.getFileName();
                            attachFiles += fileName + ", ";
                            part.saveFile(saveDirectory + File.separator + fileName);
                        } else {
                            // this part may be the message content
                            messageContent = part.getContent().toString();
                        }
                    }
 
                    if (attachFiles.length() > 1) {
                        attachFiles = attachFiles.substring(0, attachFiles.length() - 2);
                    }
                } else if (contentType.contains("text/plain")
                        || contentType.contains("text/html")) {
                    Object content = message.getContent();
                    if (content != null) {
                        messageContent = content.toString();
                    }
                }
 
                // print out details of each message
                System.out.println("Message #" + (i + 1) + ":");
                System.out.println("\t From: " + from);
                System.out.println("\t Subject: " + subject);
                System.out.println("\t Sent Date: " + sentDate);
                System.out.println("\t Message: " + messageContent);
                System.out.println("\t Attachments: " + attachFiles);
            }
 
            // disconnect
            folderInbox.close(false);
            store.close();
        } catch (NoSuchProviderException ex) {
            System.out.println("No provider for "+protocol);
            ex.printStackTrace();
        } catch (MessagingException ex) {
            System.out.println("Could not connect to the message store");
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
 
    /**
     * Runs this program with Gmail POP3 server
     */
//    public static void main(String[] args) {
//        String host = "pop.gmail.com";
//        String port = "995";
//        String userName = "your_email";
//        String password = "your_password";
// 
//        String saveDirectory = "E:/Attachment";
// 
//        EmailAttachmentReceiver receiver = new EmailAttachmentReceiver();
//        receiver.setSaveDirectory(saveDirectory);
//        receiver.downloadEmailAttachments(host, port, userName, password);
// 
//    }
}