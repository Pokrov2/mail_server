package mailserver.main;

import mailserver.Filter.*;
import mailserver.Model.User;
import mailserver.Storage.UserStorage;
import mailserver.Model.Message;

import java.util.*;

public class MailServer {
    private final Scanner scanner = new Scanner(System.in);
    private final UserStorage userStorage = new UserStorage();
    private boolean isSettingFilter = false;
    private String filteringUser = null;
    private final List<SpamFilter> currentFilters = new ArrayList<>();

    public void run() {
        System.out.println("An impudent copy mail.ru launched:");
        while (true) {
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("exit")) break;

            if (isSettingFilter) {
                handleFilterInput(input);
            } else {
                handleCommand(input);
            }
        }
    }

    public void handleCommand(String input) {
        String[] tokens = input.split(" ");
        if (tokens.length == 0) return;
        String command = tokens[0].toLowerCase();
        switch (command) {
            case "add": handleAdd(tokens); break;
            case "list": handleList(); break;
            case "send": handleSend(tokens); break;
            case "inbox": handleInbox(tokens, false); break;
            case "spam": handleInbox(tokens, true); break;
            case "outbox": handleOutbox(tokens); break;
            case "setfilter": handleSetFilter(tokens); break;
            default: System.out.println("Неизвестная команда.");
        }
    }

    private void handleAdd(String[] tokens) {
        if (tokens.length != 2) {
            System.out.println("Правильно: add <Пользователь>");
            return;
        }
        String username = tokens[1];
        if (userStorage.UserExists(username)) {
            System.out.println("Такой пользователь уже существует");
        } else {
            userStorage.AddUser(new User(username));
            System.out.println("Пользователь добавлен: " + username);
        }
    }

    private void handleList() {
        System.out.println("Пользователи:");
        for (User user : userStorage.GetAllUsers()) {
            System.out.println("- " + user.GetUsername());
        }
    }

    private void handleSend(String[] tokens) {
        if (tokens.length < 5) {
            System.out.println("Правильно: send <Отправитель> <Получатель> <заголовок> <текст>");
            return;
        }
        String senderName = tokens[1];
        String receiverName = tokens[2];
        if (!userStorage.UserExists(senderName) || !userStorage.UserExists(receiverName)) {
            System.out.println("Отправитель или получатель отсутствует");
            return;
        }
        String caption = tokens[3];
        String text = String.join(" ", Arrays.copyOfRange(tokens, 4, tokens.length));
        User sender = userStorage.GetUser(senderName);
        User receiver = userStorage.GetUser(receiverName);
        sender.SendMessage(receiver, caption, text);
        System.out.println("Сообщение отправлено!.");
    }

    private void handleInbox(String[] tokens, boolean spam) {
        if (tokens.length != 2) {
            System.out.println("Правильно: " + (spam ? "spam" : "inbox") + " <Пользователь>");
            return;
        }
        String username = tokens[1];
        if (!userStorage.UserExists(username)) {
            System.out.println("Пользователь отсутствует");
            return;
        }
        User user = userStorage.GetUser(username);
        List<Message> messages = spam ? user.GetSpam() : user.GetInbox();
        if (messages.isEmpty()) {
            System.out.println("Сообщения: пусто.");
            return;
        }
        for (Message msg : messages) {
            System.out.println("-----");
            System.out.println(msg);
        }
    }

    private void handleOutbox(String[] tokens) {
        if (tokens.length != 2) {
            System.out.println("Правильно: outbox <Пользователь>");
            return;
        }
        String username = tokens[1];
        if (!userStorage.UserExists(username)) {
            System.out.println("Пользователь отсутствует");
            return;
        }
        User user = userStorage.GetUser(username);
        List<Message> outbox = user.GetOutbox();
        if (outbox.isEmpty()) {
            System.out.println("Отправленные сообщения: пусто.");
            return;
        }
        for (Message msg : outbox) {
            System.out.println("-----");
            System.out.println(msg);
        }
    }

    private void handleSetFilter(String[] tokens) {
        if (tokens.length != 2) {
            System.out.println("Правильно: setfilter <Имя пользователя>");
            return;
        }
        String username = tokens[1];
        if (!userStorage.UserExists(username)) {
            System.out.println("Пользователь отсутствует");
            return;
        }
        isSettingFilter = true;
        filteringUser = username;
        currentFilters.clear();
        System.out.println("Примеры фильтров: simple, keywords, repetition, sender. Напишите 'done' чтобы закончить вводить фильтры:");
    }

    private void handleFilterInput(String input) {
        if (input.equalsIgnoreCase("done")) {
            User user = userStorage.GetUser(filteringUser);
            user.SetSpamFilter(new CompositeSpamFilter(currentFilters));
            System.out.println("Спам фильтр установлен для " + filteringUser);
            isSettingFilter = false;
            filteringUser = null;
            currentFilters.clear();
            return;
        }

        String[] tokens = input.split(" ");
        switch (tokens[0]) {
            case "simple":
                currentFilters.add(new SimpleSpamFilter());
                System.out.println("Добавлен простой фильтр");
                break;
            case "keywords":
                if (tokens.length < 2) {
                    System.out.println("Правильно: keywords <слово1> <слово2> ...");
                    break;
                }
                List<String> keywords = Arrays.asList(Arrays.copyOfRange(tokens, 1, tokens.length));
                currentFilters.add(new KeywordsSpamFilter(keywords));
                System.out.println("Добавлен фильтр ключевых слов: " + keywords);
                break;
            case "repetition":
                if (tokens.length != 2) {
                    System.out.println("Правильно: repetition <число>");
                    break;
                }
                try {
                    int limit = Integer.parseInt(tokens[1]);
                    currentFilters.add(new RepetitionsSpamFilter(limit));
                    System.out.println("Добавлен фильтр повторений с лимитом " + limit);
                } catch (NumberFormatException e) {
                    System.out.println("Ошибка: лимит должен быть числом");
                }
                break;
            case "sender":
                if (tokens.length < 2) {
                    System.out.println("Правильно: sender <имя1> <имя2> ...");
                    break;
                }
                Set<String> blocked = new HashSet<>(Arrays.asList(Arrays.copyOfRange(tokens, 1, tokens.length)));
                currentFilters.add(new SenderSpamFilter(blocked));
                System.out.println("Добавлен фильтр отправителей: " + blocked);
                break;
            default:
                System.out.println("Неизвестный фильтр: " + tokens[0]);
        }
    }
}
