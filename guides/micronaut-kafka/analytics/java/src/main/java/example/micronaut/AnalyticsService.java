package example.micronaut;

import javax.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Singleton
public class AnalyticsService {

    private final Map<Book, Long> bookAnalytics = new ConcurrentHashMap<>(); // <1>

    public void updateBookAnalytics(Book book) { // <2>
        bookAnalytics.compute(book, (k, v) -> {
            if (v == null) {
                return 1L;
            } else {
                return v + 1;
            }
        });
    }

    public List<BookAnalytics> listAnalytics() { // <3>
        return bookAnalytics
                .entrySet()
                .stream()
                .map(e -> new BookAnalytics(e.getKey().getIsbn(), e.getValue()))
                .collect(Collectors.toList());
    }
}
