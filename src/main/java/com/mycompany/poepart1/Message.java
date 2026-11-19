package com.mycompany.poepart1;

import java.util.Random;

/**
 * Message class for QuickChat (Part 2 and Part 3).
 * Each Message object holds the details of ONE message:
 * message ID, message number, sender, recipient, message text and message hash.
 * In Part 3 the arrays that keep track of all the messages are in the
 * MessageStore class.
 *
 * @author Student
 */
public class Message {

    // name of the JSON file that stored messages are saved in
    public static final String JSON_FILE = MessageStore.JSON_FILE;

    private String messageID;
    private int messageNumber;
    private String sender;
    private String recipient;
    private String messageText;
    private String messageHash;

    // Used when the sender is not needed (Part 2)
    public Message(String messageID, int messageNumber, String recipient, String messageText) {
        this(messageID, messageNumber, "", recipient, messageText);
    }

    // The hash is created automatically as soon as the message is created
    public Message(String messageID, int messageNumber, String sender, String recipient, String messageText) {
        this.messageID = messageID;
        this.messageNumber = messageNumber;
        this.sender = sender;
        this.recipient = recipient;
        this.messageText = messageText;
        this.messageHash = createMessageHash();
    }

    // Creates a random 10 digit message ID (built one digit at a time in a loop)
    public static String generateMessageID() {
        Random random = new Random();
        String id = "";
        for (int i = 0; i < 10; i++) {
            id = id + random.nextInt(10);
        }
        return id;
    }

    // Makes sure the message ID is not more than 10 characters
    public boolean checkMessageID() {
        return messageID != null && messageID.length() <= 10;
    }

    // Returns the text that is shown when a message ID has been created
    public String getMessageIDStatus() {
        return "Message ID generated: " + messageID;
    }

    /*
     Checks the recipient's cell number. This reuses checkCellPhoneNumber()
     from the Login class in my  Part 1.
     */
    public String checkRecipientCell() {
        Login loginHelper = new Login(recipient, "", "");
        if (loginHelper.checkCellPhoneNumber()) {
            return "Cell phone number successfully captured.";
        }
        return "Cell phone number is incorrectly formatted or does not contain an "
                + "international code. Please correct the number and try again.";
    }

    // Checks that the message is not more than 250 characters
    public String checkMessageLength() {
        int length = messageText.length();
        if (length <= 250) {
            return "Message ready to send.";
        }
        return "Message exceeds 250 characters by " + (length - 250)
                + "; please reduce the size.";
    }

    /*
     Creates the message hash: first two numbers of the message ID, a colon,
     the message number, a colon, then the first and last word of the message
     in capital letters. Example: 00:0:HITONIGHT
     */
    public String createMessageHash() {
        String[] words = messageText.trim().split("\\s+");

        // remove punctuation such as "?" or "," so only letters and numbers are left
        String firstWord = words[0].replaceAll("[^A-Za-z0-9]", "");
        String lastWord = words[words.length - 1].replaceAll("[^A-Za-z0-9]", "");

        String firstTwoNumbers = messageID.substring(0, 2);
        return (firstTwoNumbers + ":" + messageNumber + ":" + firstWord + lastWord).toUpperCase();
    }

    /*
     Does what the user chose: 1 = send, 2 = disregard, 3 = store for later.
     The message is added to the arrays of its group (Part 3).
     Returns the message that must be shown to the user.
     */
    public String sentMessage(int choice) {
        if (choice == 1) {
            MessageStore.addMessage(this, MessageStore.FLAG_SENT);
            return "Message successfully sent.";
        } else if (choice == 2) {
            MessageStore.addMessage(this, MessageStore.FLAG_DISREGARD);
            return "Press 0 to delete the message.";
        } else if (choice == 3) {
            if (storeMessage()) {
                return "Message successfully stored.";
            }
            return "Message could not be stored.";
        }
        return "Invalid option.";
    }

    // Returns the full details of this message in the required order
    public String getDetails() {
        return "Message ID: " + messageID + "\n"
                + "Message Hash: " + messageHash + "\n"
                + "Recipient: " + recipient + "\n"
                + "Message: " + messageText + "\n";
    }

    // Returns the details of ALL messages sent while the program is running
    public static String printMessages() {
        return MessageStore.getReport(MessageStore.FLAG_SENT, false);
    }

    // Returns how many messages have been sent
    public static int returnTotalMessages() {
        return MessageStore.getSentMessages().size();
    }

    // Adds this message to the stored arrays and saves them to messages.json
    public boolean storeMessage() {
        MessageStore.addMessage(this, MessageStore.FLAG_STORED);
        return MessageStore.saveStoredMessagesToJson();
    }

    // Clears all the arrays (used by the unit tests so every test starts fresh)
    public static void clearAll() {
        MessageStore.clearAll();
    }

    public String getMessageID() {
        return messageID;
    }

    public String getMessageHash() {
        return messageHash;
    }

    public String getSender() {
        return sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getMessageText() {
        return messageText;
    }
}
/* References
Anthropic (2026) Claude (AI assistant) was used to help structure and explain the steps in creating this code. Available at:
https://claude.ai (Accessed: 22 October 2026).

freeCodeCamp.org (2022) Java Programming for Beginners – Full Course. Available at: https://www.youtube.com/watch?v=A74TOX803D0 (Accessed: 9 September 2026).

Saini, M. (2018) Java NetBeans Tutorial | chat application in java. Available at: https://www.youtube.com/watch?v=dlacc6831zw (Accessed: 9 September 2026).
*/
