package fingerscale.record;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** 측정 기록 하나. 저장된 기록은 바뀌면 안 되니까 setter가 없다. */
public class Measurement {
    public static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final String name;
    private final double grams;
    private final LocalDateTime time;

    public Measurement(String name, double grams, LocalDateTime time) {
        this.name = name;
        this.grams = grams;
        this.time = time;
    }

    public String getName() { return name; }
    public double getGrams() { return grams; }
    public LocalDateTime getTime() { return time; }

    public String getTimeText() {
        return time.format(TIME_FORMAT);
    }
}
