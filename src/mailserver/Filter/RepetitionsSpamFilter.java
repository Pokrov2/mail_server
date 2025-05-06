package mailserver.Filter;

import mailserver.Model.Message;

import java.util.HashMap;
import java.util.Map;

public class RepetitionsSpamFilter implements SpamFilter {
    private final int maxRepetitions;

    public RepetitionsSpamFilter(int maxRepetitions) {
        this.maxRepetitions = maxRepetitions;
    }

    @Override
    public boolean isSpam(Message message) {
        String[] words = message.GetText().toLowerCase().split("[^\\p{L}]+");
        Map<String, Integer> wordCount = new HashMap<>();
        for (String word : words) {
            wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
            if (wordCount.get(word) > maxRepetitions) {
                return true;
            }
        }
        return false;
    }
}
