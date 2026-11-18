package com.mycompany.poepart1;

import java.io.File;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MessageManagerTest {
    private MessageManager manager;
    private Message first, second, third, fourth, fifth;

    @BeforeEach
    public void setUp() {
        Message.clearAll();
        new File(Message.JSON_FILE).delete();
        manager = new MessageManager();
        first = new Message("0012345678", 1, "+27834557896", "Did you get the cake?");
        second = new Message("1112345678", 2, "+27838884567", "Where are you? You are late! I have asked you to be on time.");
        third = new Message("2212345678", 3, "+27834484567", "Yohoooo, I am at your gate.");
        fourth = new Message("3312345678", 4, "+27838884567", "It is dinner time !");
        fifth = new Message("4412345678", 5, "+27838884567", "Ok, I am leaving without you.");
        first.sentMessage(1);
        second.sentMessage(3);
        manager.addDisregarded(third);
        fourth.sentMessage(1);
        fifth.sentMessage(3);
        manager.readStoredMessages();
    }

    @Test
    public void testArraysPopulated() {
        assertEquals(2, manager.getSentMessages().length);
        assertEquals(1, manager.getDisregardedMessages().length);
        assertEquals(2, manager.getStoredMessages().length);
        assertEquals(5, manager.getMessageHashes().length);
        assertEquals(5, manager.getMessageIDs().length);
    }

    @Test
    public void testSenderAndRecipient() {
        assertTrue(manager.senderAndRecipient().contains("+27834557896"));
    }

    @Test
    public void testLongestSentMessage() {
        assertEquals("Where are you? You are late! I have asked you to be on time.", manager.longestSentMessage());
    }

    @Test
    public void testSearchMessageID() {
        assertTrue(manager.searchMessageID(fourth.getMessageID()).contains("It is dinner time !"));
    }

    @Test
    public void testSearchRecipient() {
        String result = manager.searchRecipient("+27838884567");
        assertTrue(result.contains("Where are you?"));
        assertTrue(result.contains("It is dinner time !"));
        assertTrue(result.contains("Ok, I am leaving without you."));
    }

    @Test
    public void testDeleteUsingHash() {
        assertEquals("Message successfully deleted.", manager.deleteByHash(second.getMessageHash()));
        assertFalse(manager.searchRecipient("+27838884567").contains("Where are you?"));
    }

    @Test
    public void testReadJSON() {
        assertTrue(manager.readStoredMessages());
        assertEquals(2, manager.getStoredMessages().length);
    }

    @Test
    public void testReport() {
        String result = manager.report();
        assertTrue(result.contains("Message Hash:"));
        assertTrue(result.contains("Recipient:"));
        assertTrue(result.contains("Message:"));
    }

    @Test
    public void testUnknownSearchAndDelete() {
        assertEquals("Message ID not found.", manager.searchMessageID("unknown"));
        assertEquals("Message hash not found.", manager.deleteByHash("unknown"));
    }
}
