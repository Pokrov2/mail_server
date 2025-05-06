package mailserver.Filter;

import mailserver.Model.Message;

public class SimpleSpamFilter implements SpamFilter {
    @Override
    public boolean isSpam(Message message) {
        return message.getText().toLowerCase().contains("spam");
    }
}
