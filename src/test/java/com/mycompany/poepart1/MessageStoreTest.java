package com.mycompany.poepart1;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/*
 * Unit tests for Part 3 (arrays, Stored Messages menu features and the JSON file).
 * @author Student
 */
public class MessageStoreTest {

    // the sender is the user who is logged in
    private static final String SENDER = "+27718693002";

    // the message texts from the test data in the task
    private static final String TEXT1 = "Did you get the cake?";
    private static final String TEXT2 = "Where are you? You are late! I have asked you to be on time.";
    private static final String TEXT3 = "Yohoooo, I am at your gate.";
    private static final String TEXT4 = "It is dinner time!";
    private static final String TEXT5 = "Ok, I am leaving without you.";

    /*
     Runs before every test: starts with empty arrays and no JSON file, then
     adds the test data (the developer enters it here, it is not hard-coded
     in the application). sentMessage(1) = Sent, (2) = Disregard, (3) = Stored.
     Message 4's value "0838884567" is used as its message ID and recipient.
     */
    @BeforeEach
    public void setUp() {
        Message.clearAll();
        new File(MessageStore.JSON_FILE).delete();

        new Message("1234567890", 0, SENDER, "+27834557896", TEXT1).sentMessage(1);
        new Message("2345678901", 1, SENDER, "+27838884567", TEXT2).sentMessage(3);
        new Message("3456789012", 2, SENDER, "+27834484567", TEXT3).sentMessage(2);
        new Message("0838884567", 3, SENDER, "0838884567", TEXT4).sentMessage(1);
        new Message("5678901234", 4, SENDER, "+27838884567", TEXT5).sentMessage(3);
    }

    // ---------- the six tests from the task ----------

    @Test
    public void testSentMessagesArrayCorrectlyPopulated() {
        List<String> expected = List.of("Did you get the cake?", "It is dinner time!");
        assertEquals(expected, MessageStore.getSentMessages());
    }

    @Test
    public void testDisplayLongestMessage() {
        assertEquals("Where are you? You are late! I have asked you to be on time.",
                MessageStore.getLongestStoredMessage());
    }

    @Test
    public void testSearchForMessageID() {
        assertEquals("It is dinner time!", MessageStore.getMessageByID("0838884567"));
        assertEquals("0838884567", MessageStore.getRecipientByID("0838884567"));
    }

    @Test
    public void testSearchMessagesForRecipient() {
        List<String> expected = List.of(
                "Where are you? You are late! I have asked you to be on time.",
                "Ok, I am leaving without you.");
        assertEquals(expected, MessageStore.getMessagesForRecipient("+27838884567"));
    }

    @Test
    public void testDeleteMessageUsingHash() {
        String hash = new Message("2345678901", 1, SENDER, "+27838884567", TEXT2).getMessageHash();
        assertEquals("Message: \"Where are you? You are late! I have asked you to be on time.\""
                + " successfully deleted.", MessageStore.deleteMessageByHash(hash));
        // the message must be gone from the arrays
        assertEquals(List.of(TEXT5), MessageStore.getStoredMessages());
        assertEquals("Message ID not found.", MessageStore.getMessageByID("2345678901"));
    }

    @Test
    public void testDisplayReportOfSentMessages() {
        String expected = "Message ID: 1234567890\n"
                + "Message Hash: 12:0:DIDCAKE\n"
                + "Recipient: +27834557896\n"
                + "Message: Did you get the cake?\n\n"
                + "Message ID: 0838884567\n"
                + "Message Hash: 08:3:ITTIME\n"
                + "Recipient: 0838884567\n"
                + "Message: It is dinner time!\n\n";
        assertEquals(expected, Message.printMessages());
    }

    // ---------- the other arrays ----------

    @Test
    public void testDisregardedMessagesArrayCorrectlyPopulated() {
        assertEquals(List.of(TEXT3), MessageStore.getDisregardedMessages());
    }

    @Test
    public void testStoredMessagesArrayCorrectlyPopulated() {
        assertEquals(List.of(TEXT2, TEXT5), MessageStore.getStoredMessages());
    }

    @Test
    public void testMessageIDArrayCorrectlyPopulated() {
        List<String> expected = List.of("1234567890", "2345678901", "3456789012",
                "0838884567", "5678901234");
        assertEquals(expected, MessageStore.getMessageIDs());
    }

    @Test
    public void testMessageHashArrayCorrectlyPopulated() {
        List<String> expected = List.of("12:0:DIDCAKE", "23:1:WHERETIME", "34:2:YOHOOOOGATE",
                "08:3:ITTIME", "56:4:OKYOU");
        assertEquals(expected, MessageStore.getMessageHashes());
    }

    // ---------- other Stored Messages menu features ----------

    @Test
    public void testSendersAndRecipientsOfStoredMessages() {
        String expected = "Sender: +27718693002 | Recipient: +27838884567\n"
                + "Sender: +27718693002 | Recipient: +27838884567\n";
        assertEquals(expected, MessageStore.getSendersAndRecipients());
    }

    @Test
    public void testStoredMessagesReportShowsFullDetails() {
        String expected = "Message ID: 2345678901\n"
                + "Message Hash: 23:1:WHERETIME\n"
                + "Sender: +27718693002\n"
                + "Recipient: +27838884567\n"
                + "Message: " + TEXT2 + "\n\n"
                + "Message ID: 5678901234\n"
                + "Message Hash: 56:4:OKYOU\n"
                + "Sender: +27718693002\n"
                + "Recipient: +27838884567\n"
                + "Message: " + TEXT5 + "\n\n";
        assertEquals(expected, MessageStore.getReport(MessageStore.FLAG_STORED, true));
    }

    // ---------- failure cases ----------

    @Test
    public void testSearchForMessageIDNotFound() {
        assertEquals("Message ID not found.", MessageStore.getMessageByID("0000000000"));
    }

    @Test
    public void testSearchRecipientWithNoMessages() {
        assertEquals(new ArrayList<String>(), MessageStore.getMessagesForRecipient("+27000000000"));
    }

    @Test
    public void testDisregardedMessageIsNotFoundInRecipientSearch() {
        assertEquals(new ArrayList<String>(), MessageStore.getMessagesForRecipient("+27834484567"));
    }

    @Test
    public void testDeleteMessageHashNotFound() {
        assertEquals("Message hash not found.", MessageStore.deleteMessageByHash("99:9:NOHASH"));
        assertEquals(5, MessageStore.getMessageIDs().size());
    }

    @Test
    public void testLongestMessageWhenNothingIsStored() {
        Message.clearAll();
        assertEquals("There are no stored messages.", MessageStore.getLongestStoredMessage());
    }

    // ---------- JSON file ----------

    // the stored messages are saved to messages.json, so reading it into empty arrays gives them back
    @Test
    public void testReadJsonFileIntoArrays() {
        Message.clearAll();
        assertEquals(2, MessageStore.readStoredMessagesFromJson());
        assertEquals(List.of(TEXT2, TEXT5), MessageStore.getStoredMessages());
        assertEquals(List.of("2345678901", "5678901234"), MessageStore.getMessageIDs());
        assertEquals(List.of("23:1:WHERETIME", "56:4:OKYOU"), MessageStore.getMessageHashes());
        assertEquals("+27838884567", MessageStore.getRecipientByID("2345678901"));
    }

    // messages that are already in the arrays must not be added a second time
    @Test
    public void testReadJsonFileDoesNotAddDuplicates() {
        assertEquals(0, MessageStore.readStoredMessagesFromJson());
        assertEquals(2, MessageStore.getStoredMessages().size());
    }

    // a deleted message must also be removed from the JSON file
    @Test
    public void testDeletedMessageIsRemovedFromJsonFile() {
        MessageStore.deleteMessageByHash("23:1:WHERETIME");
        Message.clearAll();
        assertEquals(1, MessageStore.readStoredMessagesFromJson());
        assertEquals(List.of(TEXT5), MessageStore.getStoredMessages());
    }

    // quotes and special characters must survive being saved and read back
    @Test
    public void testJsonFileKeepsQuotesInMessage() {
        Message.clearAll();
        new Message("1111111111", 0, SENDER, "+27834557896", "He said \"hi\" to me").sentMessage(3);
        Message.clearAll();
        MessageStore.readStoredMessagesFromJson();
        assertEquals(List.of("He said \"hi\" to me"), MessageStore.getStoredMessages());
    }

    @Test
    public void testReadJsonFileWhenFileDoesNotExist() {
        Message.clearAll();
        new File(MessageStore.JSON_FILE).delete();
        assertEquals(0, MessageStore.readStoredMessagesFromJson());
    }
}
