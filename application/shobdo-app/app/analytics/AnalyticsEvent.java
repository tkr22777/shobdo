package analytics;

import lombok.Builder;
import lombok.Getter;

import java.util.Date;

@Getter
@Builder
public class AnalyticsEvent {

    // Event types
    public static final String WORD_LOOKUP = "word_lookup";
    public static final String SEARCH      = "search";
    public static final String RANDOM      = "random";
    public static final String WOTD        = "wotd";

    private final String event;     // one of the constants above
    private final Date   ts;        // timestamp
    private final String ip;        // real client IP (X-Forwarded-For preferred)
    private final String word;      // word looked up or search string (nullable)
    private final String referrer;  // Referer header (nullable)
    private final int    status;    // HTTP response status code
}
