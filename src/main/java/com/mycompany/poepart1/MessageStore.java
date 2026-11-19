package com.mycompany.poepart1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @author Student
 */
public class MessageStore {

    // name of the JSON file that stored messages are saved in and read from
    public static final String JSON_FILE = "messages.json";

    // 3 flags a message can have
    public static final String FLAG_SENT = "Sent";
    public static final String FLAG_DISREGARD = "Disregard";
    public static final String FLAG_STORED = "Stored";

    // shows when a message ID or message hash is not in the arrays
    public static final String ID_NOT_FOUND = "Message ID not found.";
    public static final String HASH_NOT_FOUND = "Message hash not found.";

    // the arrays 
    private static ArrayList<String> sentMessages = new ArrayList<>();
    private static ArrayList<String> disregardedMessages = new ArrayList<>();
    private static ArrayList<String> storedMessages = new ArrayList<>();
    private static ArrayList<String> messageHashes = new ArrayList<>();
    private static ArrayList<String> messageIDs = new ArrayList<>();

    // extra parallel arrays so every message keeps all its details
    private static ArrayList<String> senders = new ArrayList<>();
    private static ArrayList<String> recipients = new ArrayList<>();
    private static ArrayList<String> messageTexts = new ArrayList<>();
    private static ArrayList<String> messageFlags = new ArrayList<>();

    // Adds a message to the parallel arrays and to the array of its group
    public static void addMessage(Message message, String flag) {
        addToArrays(message.getMessageID(), message.getMessageHash(), message.getSender(),
                message.getRecipient(), message.getMessageText(), flag);
    }

    // The one place where every array is filled, so they always stay parallel
    private static void addToArrays(String id, String hash, String sender,
            String recipient, String text, String flag) {
        messageIDs.add(id);
        messageHashes.add(hash);
        senders.add(sender);
        recipients.add(recipient);
        messageTexts.add(text);
        messageFlags.add(flag);

        getGroup(flag).add(text);
    }

    // Returns the array that holds the messages with this flag
    private static ArrayList<String> getGroup(String flag) {
        if (flag.equals(FLAG_SENT)) {
            return sentMessages;
        } else if (flag.equals(FLAG_DISREGARD)) {
            return disregardedMessages;
        }
        return storedMessages;
    }

    // (a) Sender and recipient of every stored message
    public static String getSendersAndRecipients() {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < messageFlags.size(); i++) {
            if (messageFlags.get(i).equals(FLAG_STORED)) {
                result.append("Sender: ").append(senders.get(i))
                        .append(" | Recipient: ").append(recipients.get(i)).append("\n");
            }
        }
        return result.toString();
    }

    // (b) The longest stored message, found by searching the parallel arrays
    public static String getLongestStoredMessage() {
        int longestPosition = -1;
        for (int i = 0; i < messageTexts.size(); i++) {
            if (messageFlags.get(i).equals(FLAG_STORED)) {
                if (longestPosition == -1
                        || messageTexts.get(i).length() > messageTexts.get(longestPosition).length()) {
                    longestPosition = i;
                }
            }
        }
        if (longestPosition == -1) {
            return "There are no stored messages.";
        }
        return messageTexts.get(longestPosition);
    }

    // (c) Search for a message ID - the message at the same position is returned
    public static String getMessageByID(String id) {
        return lookUpByID(id, messageTexts);
    }

    // (c) Search for a message ID - the recipient at the same position is returned
    public static String getRecipientByID(String id) {
        return lookUpByID(id, recipients);
    }

    // Finds the ID in messageIDs and returns the value at that position in the given parallel array
    private static String lookUpByID(String id, ArrayList<String> parallelArray) {
        int position = messageIDs.indexOf(id);
        if (position == -1) {
            return ID_NOT_FOUND;
        }
        return parallelArray.get(position);
    }

    // (d) All messages (sent or stored) for one recipient. Disregarded ones are left out.
    public static ArrayList<String> getMessagesForRecipient(String recipient) {
        ArrayList<String> found = new ArrayList<>();
        for (int i = 0; i < recipients.size(); i++) {
            boolean sameRecipient = recipients.get(i).equals(recipient);
            boolean sentOrStored = !messageFlags.get(i).equals(FLAG_DISREGARD);
            if (sameRecipient && sentOrStored) {
                found.add(messageTexts.get(i));
            }
        }
        return found;
    }

    // (e) Delete a message using its hash. The message is removed in every array.
    public static String deleteMessageByHash(String hash) {
        int position = messageHashes.indexOf(hash);
        if (position == -1) {
            return HASH_NOT_FOUND;
        }

        String text = messageTexts.get(position);
        String flag = messageFlags.get(position);

        messageIDs.remove(position);
        messageHashes.remove(position);
        senders.remove(position);
        recipients.remove(position);
        messageTexts.remove(position);
        messageFlags.remove(position);

        getGroup(flag).remove(text);
        if (flag.equals(FLAG_STORED)) {
            saveStoredMessagesToJson();
        }
        return "Message: \"" + text + "\" successfully deleted.";
    }

    // (f) Report with the full details of all messages that have the given flag
    public static String getReport(String flag, boolean showSender) {
        StringBuilder report = new StringBuilder();
        for (int i = 0; i < messageFlags.size(); i++) {
            if (messageFlags.get(i).equals(flag)) {
                report.append("Message ID: ").append(messageIDs.get(i)).append("\n");
                report.append("Message Hash: ").append(messageHashes.get(i)).append("\n");
                if (showSender) {
                    report.append("Sender: ").append(senders.get(i)).append("\n");
                }
                report.append("Recipient: ").append(recipients.get(i)).append("\n");
                report.append("Message: ").append(messageTexts.get(i)).append("\n\n");
            }
        }
        return report.toString();
    }

    // JSON file saving and reading 

    public static boolean saveStoredMessagesToJson() {
        StringBuilder json = new StringBuilder("[\n");
        boolean firstMessage = true;
        for (int i = 0; i < messageFlags.size(); i++) {
            if (messageFlags.get(i).equals(FLAG_STORED)) {
                if (!firstMessage) {
                    json.append(",\n");
                }
                firstMessage = false;
                json.append("  {\n");
                json.append("    \"messageID\": \"").append(clean(messageIDs.get(i))).append("\",\n");
                json.append("    \"messageHash\": \"").append(clean(messageHashes.get(i))).append("\",\n");
                json.append("    \"sender\": \"").append(clean(senders.get(i))).append("\",\n");
                json.append("    \"recipient\": \"").append(clean(recipients.get(i))).append("\",\n");
                json.append("    \"message\": \"").append(clean(messageTexts.get(i))).append("\"\n");
                json.append("  }");
            }
        }
        json.append("\n]");

        try (FileWriter writer = new FileWriter(JSON_FILE)) {
            writer.write(json.toString());
            return true;
        } catch (IOException e) {
            System.out.println("Error saving file: " + e.getMessage());
            return false;
        }
    }

    /*
     Reads messages.json into the arrays. Every message in the file is added
     as a stored message, unless its ID is already in the arrays. Returns how many messages were added.
     The file is read line by line with a BufferedReader. "message" is the last field of each
     entry in the file, so once it is read the whole entry has been collected.
     */
    public static int readStoredMessagesFromJson() {
        File file = new File(JSON_FILE);
        if (!file.exists()) {
            return 0;
        }

        int added = 0;
        String id = "";
        String hash = "";
        String sender = "";
        String recipient = "";

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            while (line != null) {
                line = line.trim();
                if (line.startsWith("\"messageID\":")) {
                    id = getJsonValue(line);
                } else if (line.startsWith("\"messageHash\":")) {
                    hash = getJsonValue(line);
                } else if (line.startsWith("\"sender\":")) {
                    sender = getJsonValue(line);
                } else if (line.startsWith("\"recipient\":")) {
                    recipient = getJsonValue(line);
                } else if (line.startsWith("\"message\":")) {
                    String text = getJsonValue(line);
                    if (!messageIDs.contains(id)) {
                        addToArrays(id, hash, sender, recipient, text, FLAG_STORED);
                        added++;
                    }
                    // clear the details ready for the next entry in this file
                    id = "";
                    hash = "";
                    sender = "";
                    recipient = "";
                }
                line = reader.readLine();
            }
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
        return added;
    }

    // Gets the text between the quotes of a line
    private static String getJsonValue(String line) {
        String value = line.substring(line.indexOf(":") + 1).trim();
        if (value.endsWith(",")) {
            value = value.substring(0, value.length() - 1);
        }
        value = value.substring(1, value.length() - 1); // take off the two quotes
        return unclean(value);
    }

    // Escapes quotes, backslashes and new lines so the text is valid inside JSON
    private static String clean(String text) {
        return text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }

    // Turns the escaped JSON text back into normal text
    private static String unclean(String text) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\\' && i + 1 < text.length()) {
                i++;
                char next = text.charAt(i);
                if (next == 'n') {
                    result.append("\n");
                } else {
                    result.append(next);
                }
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    // getters and clearing section

    public static ArrayList<String> getSentMessages() {
        return sentMessages;
    }

    public static ArrayList<String> getDisregardedMessages() {
        return disregardedMessages;
    }

    public static ArrayList<String> getStoredMessages() {
        return storedMessages;
    }

    public static ArrayList<String> getMessageHashes() {
        return messageHashes;
    }

    public static ArrayList<String> getMessageIDs() {
        return messageIDs;
    }

    // Clears every array
    public static void clearAll() {
        sentMessages.clear();
        disregardedMessages.clear();
        storedMessages.clear();
        messageHashes.clear();
        messageIDs.clear();
        senders.clear();
        recipients.clear();
        messageTexts.clear();
        messageFlags.clear();
    }
}
/* References
Oracle (2026) BufferedReader (Java SE 21 API). Available at:
https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/io/BufferedReader.html (Accessed: 22 October 2026).

Oracle (2026) FileWriter (Java SE 21 API). Available at:
https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/io/FileWriter.html (Accessed: 22 October 2026).

Oracle (2026) ArrayList (Java SE 21 API). Available at:
https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/ArrayList.html (Accessed: 22 October 2026).

Ecma International (2017) The JSON Data Interchange Syntax (ECMA-404). Available at:
https://www.json.org/json-en.html (Accessed: 22 October 2026).

freeCodeCamp.org (2022) Java Programming for Beginners – Full Course. Available at: https://www.youtube.com/watch?v=A74TOX803D0 (Accessed: 9 September 2026).

Saini, M. (2018) Java NetBeans Tutorial | chat application in java. Available at: https://www.youtube.com/watch?v=dlacc6831zw (Accessed: 9 September 2026).
*/
