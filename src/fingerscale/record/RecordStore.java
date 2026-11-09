package fingerscale.record;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** 측정 기록을 CSV 파일(이름,무게,시간)로 저장하고 읽어온다. */
public class RecordStore {
    private final Path file;

    public RecordStore(Path file) {
        this.file = file;
    }

    public void save(Measurement m) throws IOException {
        Files.createDirectories(file.getParent());
        String name = m.getName().replace(",", " ").replace("\n", " ");
        String line = name + "," + m.getGrams() + "," + m.getTime() + System.lineSeparator();
        Files.writeString(file, line, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    public List<Measurement> loadAll() throws IOException {
        List<Measurement> list = new ArrayList<>();
        if (!Files.exists(file)) return list;
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            String[] parts = line.split(",");
            if (parts.length != 3) continue;
            try {
                list.add(new Measurement(parts[0], Double.parseDouble(parts[1]), LocalDateTime.parse(parts[2])));
            } catch (RuntimeException e) {
                // 깨진 줄은 건너뛴다
            }
        }
        return list;
    }

    public void clear() throws IOException {
        Files.deleteIfExists(file);
    }
}
