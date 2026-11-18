package com.mycompany.poepart1;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;

/** Part 3: arrays and search/report features. */
public class MessageManager {
    private final ArrayList<Message> discardedMessages = new ArrayList<>();
    private final ArrayList<Message> savedFromFile = new ArrayList<>();

    // The five required arrays. They are refreshed when messages change.
    private Message[] sentMessages = new Message[0];
    private Message[] disregardedMessages = new Message[0];
    private Message[] storedMessages = new Message[0];
    private String[] messageHashes = new String[0];
    private String[] messageIDs = new String[0];

    public void addDisregarded(Message message) {
        discardedMessages.add(message);
        refreshArrays();
    }

    public void refreshArrays() {
        sentMessages = Message.getSentMessages().toArray(new Message[0]);
        disregardedMessages = discardedMessages.toArray(new Message[0]);
        // Both newly stored messages and messages loaded from JSON are searchable.
        ArrayList<Message> allStored = new ArrayList<>(Message.getStoredMessages());
        for (Message saved : savedFromFile) {
            boolean exists = false;
            for (Message current : allStored) {
                if (current.getMessageID().equals(saved.getMessageID())) {
                    exists = true;
                    break;
                }
            }
            if (!exists) allStored.add(saved);
        }
        storedMessages = allStored.toArray(new Message[0]);
        ArrayList<String> hashes = new ArrayList<>();
        ArrayList<String> ids = new ArrayList<>();
        for (Message m : sentMessages) {
            hashes.add(m.getMessageHash());
            ids.add(m.getMessageID());
        }
        for (Message m : storedMessages) {
            hashes.add(m.getMessageHash());
            ids.add(m.getMessageID());
        }
        for (Message m : disregardedMessages) {
            hashes.add(m.getMessageHash());
            ids.add(m.getMessageID());
        }
        messageHashes = hashes.toArray(new String[0]);
        messageIDs = ids.toArray(new String[0]);
    }

    public boolean readStoredMessages() {
        savedFromFile.clear();
        File file = new File(Message.JSON_FILE);
        if (!file.exists()) {
            refreshArrays();
            return false;
        }
        try (FileReader reader = new FileReader(file)) {
            Type type = new TypeToken<ArrayList<Message>>() {}.getType();
            ArrayList<Message> loaded = new Gson().fromJson(reader, type);
            if (loaded != null) savedFromFile.addAll(loaded);
            refreshArrays();
            return true;
        } catch (IOException | com.google.gson.JsonParseException e) {
            refreshArrays();
            return false;
        }
    }

    public Message[] getSentMessages() { refreshArrays(); return sentMessages; }
    public Message[] getDisregardedMessages() { refreshArrays(); return disregardedMessages; }
    public Message[] getStoredMessages() { refreshArrays(); return storedMessages; }
    public String[] getMessageHashes() { refreshArrays(); return messageHashes; }
    public String[] getMessageIDs() { refreshArrays(); return messageIDs; }

    public String senderAndRecipient() {
        refreshArrays();
        if (sentMessages.length == 0 && storedMessages.length == 0) return "No messages found.";
        String result = "";
        for (Message m : sentMessages) result += "Sender: Current user\nRecipient: " + m.getRecipient() + "\n";
        for (Message m : storedMessages) result += "Sender: Current user\nRecipient: " + m.getRecipient() + "\n";
        return result;
    }

    public String longestSentMessage() {
        refreshArrays();
        Message longest = null;
        for (Message m : sentMessages) {
            if (longest == null || m.getMessageText().length() > longest.getMessageText().length()) longest = m;
        }
        for (Message m : storedMessages) {
            if (longest == null || m.getMessageText().length() > longest.getMessageText().length()) longest = m;
        }
        return longest == null ? "No messages found." : longest.getMessageText();
    }

    public String searchMessageID(String id) {
        refreshArrays();
        for (Message m : sentMessages) {
            if (m.getMessageID().equals(id)) return "Recipient: " + m.getRecipient() + "\nMessage: " + m.getMessageText();
        }
        for (Message m : storedMessages) {
            if (m.getMessageID().equals(id)) return "Recipient: " + m.getRecipient() + "\nMessage: " + m.getMessageText();
        }
        return "Message ID not found.";
    }

    public String searchRecipient(String recipient) {
        refreshArrays();
        String result = "";
        for (Message m : sentMessages) {
            if (m.getRecipient().equals(recipient)) result += "Sent: " + m.getMessageText() + "\n";
        }
        for (Message m : storedMessages) {
            if (m.getRecipient().equals(recipient)) result += "Stored: " + m.getMessageText() + "\n";
        }
        return result.isEmpty() ? "No messages found for this recipient." : result;
    }

    public String deleteByHash(String hash) {
        refreshArrays();
        for (Message m : sentMessages) {
            if (m.getMessageHash().equalsIgnoreCase(hash)) {
                Message.getSentMessages().remove(m);
                refreshArrays();
                return "Message successfully deleted.";
            }
        }
        for (Message m : storedMessages) {
            if (m.getMessageHash().equalsIgnoreCase(hash)) {
                Message.getStoredMessages().removeIf(x -> x.getMessageHash().equalsIgnoreCase(hash));
                savedFromFile.removeIf(x -> x.getMessageHash().equalsIgnoreCase(hash));
                saveStoredMessages();
                refreshArrays();
                return "Message successfully deleted.";
            }
        }
        return "Message hash not found.";
    }

    // Rewrites the JSON so a deleted stored message stays deleted after restart.
    private void saveStoredMessages() {
        refreshArrays();
        try (java.io.FileWriter writer = new java.io.FileWriter(Message.JSON_FILE)) {
            new Gson().toJson(storedMessages, writer);
        } catch (IOException e) {
            System.out.println("Unable to update stored messages: " + e.getMessage());
        }
    }

    public String report() {
        refreshArrays();
        if (sentMessages.length == 0) return "No sent messages to report.";
        String result = "SENT MESSAGES REPORT\n";
        for (Message m : sentMessages) {
            result += "Message Hash: " + m.getMessageHash() + "\n"
                    + "Recipient: " + m.getRecipient() + "\n"
                    + "Message: " + m.getMessageText() + "\n\n";
        }
        return result;
    }
}
