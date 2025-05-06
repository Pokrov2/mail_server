package mailserver.Filter;

import mailserver.Model.Message;

import java.util.HashSet;
import java.util.Set;

public class SenderSpamFilter implements SpamFilter {
    private final Set<String> blockedSenders = new HashSet<>();

    public SenderSpamFilter(Set<String> blockedSenders) {
        this.blockedSenders.addAll(blockedSenders);
    }

    @Override
    public boolean isSpam(Message message) {
        return blockedSenders.contains(message.GetSender());
    }
}
