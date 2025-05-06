package mailserver.Filter;

import mailserver.Model.Message;

import java.util.List;

public class CompositeSpamFilter implements SpamFilter {
    private final List<SpamFilter> filters;

    public CompositeSpamFilter(List<SpamFilter> filters) {
        this.filters = filters;
    }

    @Override
    public boolean isSpam(Message message) {
        for (SpamFilter filter : filters) {
            if (filter.isSpam(message)) {
                return true;
            }
        }
        return false;
    }
}
