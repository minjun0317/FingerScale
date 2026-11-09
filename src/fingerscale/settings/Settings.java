package fingerscale.settings;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** 보정 값(압력 1당 몇 g인지)을 파일에 저장하고 불러온다. */
public class Settings {
    private static final String KEY = "gramsPerUnit";
    // 기본값: 에어팟 프로3(55.1g)을 6번 올렸을 때 압력이 매번 59~62 늘어났다 → 55.1 / 60 ≈ 0.92
    private static final double DEFAULT_GRAMS_PER_UNIT = 0.92;

    private final Path file;
    private double gramsPerUnit = DEFAULT_GRAMS_PER_UNIT;

    public Settings(Path file) {
        this.file = file;
        load();
    }

    private void load() {
        if (!Files.exists(file)) return;
        Properties props = new Properties();
        try (Reader in = Files.newBufferedReader(file)) {
            props.load(in);
            gramsPerUnit = Double.parseDouble(props.getProperty(KEY, String.valueOf(DEFAULT_GRAMS_PER_UNIT)));
        } catch (IOException | NumberFormatException e) {
            gramsPerUnit = DEFAULT_GRAMS_PER_UNIT;
        }
    }

    public double getGramsPerUnit() {
        return gramsPerUnit;
    }

    public void setGramsPerUnit(double value) throws IOException {
        if (value <= 0) throw new IllegalArgumentException("보정 값은 0보다 커야 합니다");
        gramsPerUnit = value;
        Files.createDirectories(file.getParent());
        Properties props = new Properties();
        props.setProperty(KEY, String.valueOf(value));
        try (Writer out = Files.newBufferedWriter(file)) {
            props.store(out, "FingerScale settings");
        }
    }
}
