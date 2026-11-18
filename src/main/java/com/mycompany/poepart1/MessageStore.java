package com.mycompany.poepart1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * MessageStore (Part 3) - keeps all the arrays for QuickChat.
 *
 * PARALLEL ARRAYS: messageIDs, messageHashes, senders, recipients,
 * messageTexts and messageFlags always have the same length. Position 0 in
 * every one of them belongs to the same message, position 1 to the next one,
 * and so on. So once a message is found in one array (for example by its hash),
 * its other details are at the same position in the other arrays.
 *
 * The three arrays the task asks for (sentMessages, disregardedMessages and
 * storedMessages) hold the text of the messages in each group.
 *
 * @author Student
 */
public class MessageStore {

    // name of the JSON file that stored messages are saved in and read from
    public static final String JSON_FILE = "messages.json";

    // the three flags a message can have
    public static final String FLAG_SENT = "Sent";
    public static final String FLAG_DISREGARD = "Disregard";
    public static final String FLAG_STORED = "Stored";

    // the arrays asked for in the task
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

    // ---------- populating the arrays (nothing is hard-coded) ----------

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

        if (flag.equals(FLAG_SENT)) {
            sentMessages.add(text);
        } else if (flag.equals(FLAG_DISREGARD)) {
            disregardedMessages.add(text);
        } else if (flag.equals(FLAG_STORED)) {
            storedMessages.add(text);
        }
    }

    // ---------- features for the Stored Messages menu ----------

    // (a) Sender and recipient of every stored message
    public static String getSendersAndRecipients() {
        String result = "";
        for (int i = 0; i < messageFlags.size(); i++) {
            if (messageFlags.get(i).equals(FLAG_STORED)) {
                result = result + "Sender: " + senders.get(i)
                        + " | Recipient: " + recipients.get(i) + "\n";
            }
        }
        return result;
    }

    // (b) The longest stored message
    public static String getLongestStoredMessage() {
        if (storedMessages.isEmpty()) {
            return "There are no stored messages.";
        }
        String longest = storedMessages.get(0);
        for (int i = 1; i < storedMessages.size(); i++) {
            if (storedMessages.get(i).length() > longest.length()) {
                longest = storedMessages.get(i);
            }
        }
        return longest;
    }

    // (c) Search for a message ID - the message at the same position is returned
    public static String getMessageByID(String id) {
        int position = messageIDs.indexOf(id);
        if (position == -1) {
            return "Message ID not found.";
        }
        return messageTexts.get(position);
    }

    // (c) Search for a message ID - the recipient at the same position is returned
    public static String getRecipientByID(String id) {
        int position = messageIDs.indexOf(id);
        if (position == -1) {
            return "Message ID not found.";
        }
        return recipients.get(position);
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

    // (e) Delete a message using its hash. The message is removed from every array.
    public static String deleteMessageByHash(String hash) {
        int position = messageHashes.indexOf(hash);
        if (position == -1) {
            return "Message hash not found.";
        }

        String text = messageTexts.get(position);
        String flag = messageFlags.get(position);

        messageIDs.remove(position);
        messageHashes.remove(position);
        senders.remove(position);
        recipients.remove(position);
        messageTexts.remove(position);
        messageFlags.remove(position);

        if (flag.equals(FLAG_SENT)) {
            sentMessages.remove(text);
        } else if (flag.equals(FLAG_DISREGARD)) {
            disregardedMessages.remove(text);
        } else if (flag.equals(FLAG_STORED)) {
            storedMessages.remove(text);
            saveStoredMessagesToJson(); // keep the file the same as the array
        }
        return "Message: \"" + text + "\" successfully deleted.";
    }

    // (f) Report with the full details of all messages that have the given flag
    public static String getReport(String flag, boolean showSender) {
        String report = "";
        for (int i = 0; i < messageFlags.size(); i++) {
            if (messageFlags.get(i).equals(flag)) {
                report = report + "Message ID: " + messageIDs.get(i) + "\n"
                        + "Message Hash: " + messageHashes.get(i) + "\n";
                if (showSender) {
                    report = report + "Sender: " + senders.get(i) + "\n";
                }
                report = report + "Recipient: " + recipients.get(i) + "\n"
                        + "Message: " + messageTexts.get(i) + "\n\n";
            }
        }
        return report;
    }

    // ---------- JSON file: saving and reading ----------

    /*
     Saves every stored message to messages.json. Returns true if it worked.
     The JSON layout follows the rules on json.org and FileWriter is used as
     shown in the Oracle docs (see references at the bottom of this file).
     */
    public static boolean saveStoredMessagesToJson() {
        String json = "[\n";
        boolean firstMessage = true;
        for (int i = 0; i < messageFlags.size(); i++) {
            if (messageFlags.get(i).equals(FLAG_STORED)) {
                if (!firstMessage) {
                    json = json + ",\n";
                }
                firstMessage = false;
                json = json + "  {\n"
                        + "    \"messageID\": \"" + clean(messageIDs.get(i)) + "\",\n"
                        + "    \"messageHash\": \"" + clean(messageHashes.get(i)) + "\",\n"
                        + "    \"sender\": \"" + clean(senders.get(i)) + "\",\n"
                        + "    \"recipient\": \"" + clean(recipients.get(i)) + "\",\n"
                        + "    \"message\": \"" + clean(messageTexts.get(i)) + "\"\n"
                        + "  }";
            }
        }
        json = json + "\n]";

        try (FileWriter writer = new FileWriter(JSON_FILE)) {
            writer.write(json);
            return true;
        } catch (IOException e) {
            System.out.println("Error saving file: " + e.getMessage());
            return false;
        }
    }

    /*
     Reads messages.json into the arrays. Every message in the file is added
     as a stored message, unless its ID is already in the arrays (so nothing
     is added twice). Returns how many messages were added.
     The file is read line by line with a BufferedReader (see the Oracle
     reference at the bottom of this file). "message" is the last field of each
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
                    // clear the details ready for the next entry in the file
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

    // Gets the text between the quotes of a line like:  "recipient": "+27834557896",
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

    // Turns the escaped JSON text back into normal text (the opposite of clean)
    private static String unclean(String text) {
        String result = "";
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\\' && i + 1 < text.length()) {
                i++;
                char next = text.charAt(i);
                if (next == 'n') {
                    result = result + "\n";
                } else {
                    result = result + next; // \" becomes " and \\ becomes \
                }
            } else {
                result = result + c;
            }
        }
        return result;
    }

    // ---------- getters and clearing ----------

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

    // Clears every array (used by the unit tests so every test starts fresh)
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

Anthropic (2026) Claude (AI assistant) was used to help plan, structure and explain this code. Available at:
https://claude.ai (Accessed: 22 October 2026).
*/
