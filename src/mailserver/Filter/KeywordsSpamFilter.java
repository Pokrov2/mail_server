package mailserver.Filter;

import mailserver.Model.Message;

import java.util.List;

public class KeywordsSpamFilter implements SpamFilter {
    private final List<String> keywords;

    public KeywordsSpamFilter(List<String> keywords) {
        this.keywords = keywords.stream().map(String::toLowerCase).toList();
    }

    @Override
    public boolean isSpam(Message message) {
        String text = message.getText().toLowerCase();
        String caption = message.getCaption().toLowerCase();
        return keywords.stream().anyMatch(kw -> text.contains(kw) || caption.contains(kw));

    }
}