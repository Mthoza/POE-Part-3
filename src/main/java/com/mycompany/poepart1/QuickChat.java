package com.mycompany.poepart1;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * QuickChat (Parts 2 & 3) - main menu, message entry, and Stored Messages menu.
 * Runs once the user has logged in successfully.
 *
 * @author Student
 */
public class QuickChat {

    public static void start(Scanner input, String sender) {
        // Load any previously stored messages from messages.json into the arrays
        MessageStore.readStoredMessagesFromJson();

        System.out.println("Welcome to QuickChat.");

        int numMessages = 0;
        while (numMessages < 1) {
            System.out.println("How many messages do you wish to enter?");
            numMessages = readNumber(input);
            if (numMessages < 1) {
                System.out.println("Please enter a number of 1 or more.");
            }
        }

        boolean messagesEntered = false;
        int choice = 0;

        // Main menu loop: runs until the user selects 3 (Quit)
        while (choice != 3) {
            System.out.println("\nPlease choose an option:");
            System.out.println("1) Send Messages");
            System.out.println("2) Show recently sent messages");
            System.out.println("3) Quit");
            System.out.println("4) Stored Messages");
            choice = readNumber(input);

            if (choice == 1) {
                if (messagesEntered) {
                    System.out.println("You have already entered all " + numMessages + " messages.");
                } else {
                    enterMessages(input, numMessages, sender);
                    messagesEntered = true;
                }
            } else if (choice == 2) {
                String sent = Message.printMessages();
                if (sent.isEmpty()) {
                    System.out.println("No messages have been sent yet.");
                } else {
                    System.out.println("\nRecently Sent Messages:\n" + sent);
                }
            } else if (choice == 3) {
                System.out.println("Goodbye!");
            } else if (choice == 4) {
                storedMessagesMenu(input);
            } else {
                System.out.println("Invalid option, please enter 1, 2, 3 or 4.");
            }
        }
    }

    // Submenu for Option 4: Stored Messages (Requirements 2a - 2f)
    private static void storedMessagesMenu(Scanner input) {
        String subChoice = "";
        while (!subChoice.equals("g")) {
            System.out.println("\n--- Stored Messages Menu ---");
            System.out.println("a) Display the sender and recipient of all stored messages");
            System.out.println("b) Display the longest stored message");
            System.out.println("c) Search for a message ID");
            System.out.println("d) Search for all messages for a particular recipient");
            System.out.println("e) Delete a message using the message hash");
            System.out.println("f) Display a report of all stored messages");
            System.out.println("g) Return to main menu");
            subChoice = input.nextLine().trim().toLowerCase();

            if (subChoice.equals("a")) {
                String result = MessageStore.getSendersAndRecipients();
                System.out.println(result.isEmpty() ? "No stored messages found." : result);

            } else if (subChoice.equals("b")) {
                System.out.println("Longest stored message:\n" + MessageStore.getLongestStoredMessage());

            } else if (subChoice.equals("c")) {
                System.out.println("Enter the Message ID to search:");
                String id = input.nextLine().trim();
                String msg = MessageStore.getMessageByID(id);
                if (msg.equals("Message ID not found.")) {
                    System.out.println(msg);
                } else {
                    System.out.println("Recipient: " + MessageStore.getRecipientByID(id));
                    System.out.println("Message: " + msg);
                }

            } else if (subChoice.equals("d")) {
                System.out.println("Enter the recipient cell number (e.g. +27838884567):");
                String recipient = input.nextLine().trim();
                ArrayList<String> matches = MessageStore.getMessagesForRecipient(recipient);
                if (matches.isEmpty()) {
                    System.out.println("No messages found for recipient " + recipient + ".");
                } else {
                    System.out.println("Messages for " + recipient + ":");
                    for (String m : matches) {
                        System.out.println("- " + m);
                    }
                }

            } else if (subChoice.equals("e")) {
                System.out.println("Enter the Message Hash to delete (e.g. 23:1:WHERETIME):");
                String hash = input.nextLine().trim();
                System.out.println(MessageStore.deleteMessageByHash(hash));

            } else if (subChoice.equals("f")) {
                String report = MessageStore.getReport(MessageStore.FLAG_STORED, true);
                System.out.println(report.isEmpty() ? "No stored messages to report." : "\n" + report);

            } else if (!subChoice.equals("g")) {
                System.out.println("Invalid option, please choose a, b, c, d, e, f, or g.");
            }
        }
    }

    // Lets the user enter the set number of messages
    private static void enterMessages(Scanner input, int numMessages, String sender) {
        for (int i = 0; i < numMessages; i++) {
            System.out.println("\nMessage " + (i + 1) + " of " + numMessages);

            String recipient = "";
            boolean recipientOk = false;
            while (!recipientOk) {
                System.out.println("Please enter the recipient cell number (e.g. +27718693002):");
                recipient = input.nextLine();
                Message check = new Message("0000000000", i, sender, recipient, "check");
                String result = check.checkRecipientCell();
                System.out.println(result);
                recipientOk = result.equals("Cell phone number successfully captured.");
            }

            String text = "";
            boolean textOk = false;
            while (!textOk) {
                System.out.println("Please enter your message:");
                text = input.nextLine();
                Message check = new Message("0000000000", i, sender, recipient, text);
                String result = check.checkMessageLength();
                if (result.equals("Message ready to send.")) {
                    textOk = true;
                } else {
                    System.out.println("Please enter a message of less than 250 characters.");
                }
                System.out.println(result);
            }

            Message message = new Message(Message.generateMessageID(), i, sender, recipient, text);
            System.out.println(message.getMessageIDStatus());

            int sendChoice = 0;
            while (sendChoice < 1 || sendChoice > 3) {
                System.out.println("Please choose an option:");
                System.out.println("1) Send Message");
                System.out.println("2) Disregard Message");
                System.out.println("3) Store Message to send later");
                sendChoice = readNumber(input);
            }

            System.out.println(message.sentMessage(sendChoice));

            if (sendChoice == 1) {
                System.out.println("\n" + message.getDetails());
            } else if (sendChoice == 2) {
                String answer = input.nextLine();
                if (answer.equals("0")) {
                    System.out.println("Message deleted.");
                }
            }
        }

        System.out.println("All messages have been entered.");
        System.out.println("Messages sent:\n" + Message.printMessages());
        System.out.println("Total messages sent: " + Message.returnTotalMessages());
    }

    private static int readNumber(Scanner input) {
        int number = 0;
        boolean valid = false;
        while (!valid) {
            try {
                number = Integer.parseInt(input.nextLine().trim());
                valid = true;
            } catch (NumberFormatException e) {
                System.out.println("That is not a number, please try again.");
            }
        }
        return number;
    }
}
/* References
freeCodeCamp.org (2022) Java Programming for Beginners – Full Course. Available at: https://www.youtube.com/watch?v=A74TOX803D0 (Accessed: 9 September 2026).

Anthropic (2026) Claude (AI assistant) was used to help plan and structure this code. Available at: https://claude.ai (Accessed: 15 October 2026).
*/