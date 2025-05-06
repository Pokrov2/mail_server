package mailserver.Model;

import mailserver.Filter.SpamFilter;

import java.util.ArrayList;
import java.util.List;

public class User {
    private final String username;
    private final List<Message> inbox = new ArrayList<>();
    private final List<Message> outbox = new ArrayList<>();
    private final List<Message> spam = new ArrayList<>();
    private SpamFilter spamFilter;

    public User(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public List<Message> getInbox() {
        return new ArrayList<>(inbox);
    }

    public List<Message> getOutbox() {
        return new ArrayList<>(outbox);
    }

    public List<Message> getSpam() {
        return new ArrayList<>(spam);
    }

    public void setSpamFilter(SpamFilter spamFilter) {
        this.spamFilter = spamFilter;
    }

    public void sendMessage(User receiver, String caption, String text) {
        Message message = new Message(caption, text, this.username, receiver.getUsername());
        outbox.add(message);
        if (receiver.spamFilter != null && receiver.spamFilter.isSpam(message)) {
            receiver.spam.add(message);
        } else {
            receiver.inbox.add(message);
        }
    }
}
