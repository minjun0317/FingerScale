package fingerscale;

import fingerscale.debug.DebugLog;
import fingerscale.record.RecordStore;
import fingerscale.scale.ScaleCalculator;
import fingerscale.scale.ScaleController;
import fingerscale.settings.Settings;
import fingerscale.source.PressureSource;
import fingerscale.source.TestSource;
import fingerscale.source.TrackpadSource;
import fingerscale.ui.ScaleWindow;

import javax.swing.*;
import java.nio.file.Path;
import java.util.List;

/**
 * 실행:  ./run.sh            트랙패드로 재기
 *        ./run.sh --test     가짜 값으로 화면만 확인
 *        ./run.sh --debug    data/debug.log 에 값 기록
 */
public class Main {
    private static final double TEST_GRAMS_PER_UNIT = 0.1;

    public static void main(String[] args) {
        List<String> options = List.of(args);
        boolean testMode = options.contains("--test");
        boolean debugMode = options.contains("--debug");

        Path dataDir = Path.of("data");
        Settings settings = new Settings(dataDir.resolve(testMode ? "settings-test.properties" : "settings.properties"));
        RecordStore store = new RecordStore(dataDir.resolve("records.csv"));

        double gramsPerUnit = testMode ? TEST_GRAMS_PER_UNIT : settings.getGramsPerUnit();
        ScaleCalculator calculator = new ScaleCalculator(gramsPerUnit);
        ScaleController controller = new ScaleController(calculator);
        PressureSource source = testMode ? new TestSource(gramsPerUnit) : new TrackpadSource();

        if (debugMode) controller.addListener(new DebugLog(dataDir.resolve("debug.log"), calculator));

        SwingUtilities.invokeLater(() -> {
            ScaleWindow window = new ScaleWindow(source.getName(), controller, settings, store);
            controller.addListener(window);
            window.setVisible(true);
            Runtime.getRuntime().addShutdownHook(new Thread(source::stop));
            try {
                // 값은 다른 스레드에서 오니까 Swing 스레드로 넘겨서 처리한다
                source.start(reading -> SwingUtilities.invokeLater(() -> controller.onReading(reading)));
            } catch (Exception e) {
                JOptionPane.showMessageDialog(window, e.getMessage(), "시작 실패", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}
