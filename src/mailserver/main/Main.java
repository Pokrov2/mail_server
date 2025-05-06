package mailserver.main;

import mailserver.Filter.KeywordsSpamFilter;
import mailserver.Filter.*;
import mailserver.Model.User;
import mailserver.Storage.UserStorage;

import java.util.*;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final UserStorage userStorage = new UserStorage();

    public static void main(String[] args) {
        System.out.println("Mail Server Started. Enter commands:");
        while (true) {
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("exit")) break;
            handleCommand(input);
        }
    }

    private static void handleCommand(String input) {
        String[] tokens = input.split(" ");
        if (tokens.length == 0) return;

        String command = tokens[0];
        switch (command) {
            case "add":
                handleAdd(tokens);
                break;
            case "list":
                handleList(tokens);
                break;
            case "send":
                handleSend(tokens);
                break;
            case "inbox":
                handleInbox(tokens, false);
                break;
            case "spam":
                handleInbox(tokens, true);
                break;
            case "outbox":
                handleOutbox(tokens);
                break;
            case "setfilter":
                handleSetFilter(tokens);
                break;
            default:
                System.out.println("Unknown command.");
        }
    }

    private static void handleAdd(String[] tokens) {
        if (tokens.length != 2) {
            System.out.println("Usage: add <username>");
            return;
        }
        String username = tokens[1];
        if (userStorage.userExists(username)) {
            System.out.println("User already exists.");
        } else {
            userStorage.addUser(new User(username));
            System.out.println("User added: " + username);
        }
    }

    private static void handleList(String[] tokens) {
        if (tokens.length != 1) {
            System.out.println("Usage: list");
            return;
        }
        System.out.println("Users:");
        for (User user : userStorage.getAllUsers()) {
            System.out.println("- " + user.getUsername());
        }
    }

    private static void handleSend(String[] tokens) {
        if (tokens.length < 5) {
            System.out.println("Usage: send <sender> <receiver> <caption> <text>");
            return;
        }
        String senderName = tokens[1];
        String receiverName = tokens[2];
        if (!userStorage.userExists(senderName) || !userStorage.userExists(receiverName)) {
            System.out.println("Sender or receiver does not exist.");
            return;
        }
        String caption = tokens[3];
        String text = String.join(" ", Arrays.copyOfRange(tokens, 4, tokens.length));
        User sender = userStorage.getUser(senderName);
        User receiver = userStorage.getUser(receiverName);
        sender.sendMessage(receiver, caption, text);
        System.out.println("Message sent.");
    }

    private static void handleInbox(String[] tokens, boolean spam) {
        if (tokens.length != 2) {
            System.out.println("Usage: " + (spam ? "spam" : "inbox") + " <username>");
            return;
        }
        String username = tokens[1];
        if (!userStorage.userExists(username)) {
            System.out.println("User does not exist.");
            return;
        }
        User user = userStorage.getUser(username);
        List<mailserver.Model.Message> messages = spam ? user.getSpam() : user.getInbox();
        if (messages.isEmpty()) {
            System.out.println("No messages.");
            return;
        }
        for (mailserver.Model.Message msg : messages) {
            System.out.println("-----");
            System.out.println(msg);
        }
    }

    private static void handleOutbox(String[] tokens) {
        if (tokens.length != 2) {
            System.out.println("Usage: outbox <username>");
            return;
        }
        String username = tokens[1];
        if (!userStorage.userExists(username)) {
            System.out.println("User does not exist.");
            return;
        }
        User user = userStorage.getUser(username);
        List<mailserver.Model.Message> outbox = user.getOutbox();
        if (outbox.isEmpty()) {
            System.out.println("No sent messages.");
            return;
        }
        for (mailserver.Model.Message msg : outbox) {
            System.out.println("-----");
            System.out.println(msg);
        }
    }

    private static void handleSetFilter(String[] tokens) {
        if (tokens.length < 3) {
            System.out.println("Usage: setfilter <username> <filter1> <filter2> ... done");
            return;
        }
        String username = tokens[1];
        if (!userStorage.userExists(username)) {
            System.out.println("User does not exist.");
            return;
        }
        List<SpamFilter> filters = new ArrayList<>();
        int i = 2;
        while (i < tokens.length) {
            String filter = tokens[i];
            switch (filter) {
                case "simple":
                    filters.add(new SimpleSpamFilter());
                    break;
                case "keywords":
                    i++;
                    List<String> keywords = new ArrayList<>();
                    while (i < tokens.length && !tokens[i].equals("done")) {
                        keywords.add(tokens[i++]);
                    }
                    filters.add(new KeywordsSpamFilter(keywords));
                    continue;
                case "repetitions":
                    i++;
                    if (i < tokens.length) {
                        int limit = Integer.parseInt(tokens[i]);
                        filters.add(new RepetitionsSpamFilter(limit));
                    } else {
                        System.out.println("Missing repetition limit.");
                        return;
                    }
                    break;
                case "sender":
                    i++;
                    Set<String> blocked = new HashSet<>();
                    while (i < tokens.length && !tokens[i].equals("done")) {
                        blocked.add(tokens[i++]);
                    }
                    filters.add(new SenderSpamFilter(blocked));
                    continue;
                case "done":
                    i = tokens.length;
                    break;
                default:
                    System.out.println("Unknown filter: " + filter);
                    return;
            }
            i++;
        }

        User user = userStorage.getUser(username);
        user.setSpamFilter(new CompositeSpamFilter(filters));
        System.out.println("Spam filter set for " + username);
    }
}
