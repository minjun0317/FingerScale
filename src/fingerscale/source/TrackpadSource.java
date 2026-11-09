package fingerscale.source;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/** Swift helper 프로그램을 실행하고, 출력되는 값을 한 줄씩 읽어온다. */
public class TrackpadSource implements PressureSource {
    private static final String DEFAULT_HELPER = "helper/.build/release/FingerScaleHelper";

    private final File helper;
    private Process process;
    private Thread reader;

    public TrackpadSource() {
        this(new File(System.getProperty("fingerscale.helper", DEFAULT_HELPER)));
    }

    public TrackpadSource(File helper) {
        this.helper = helper;
    }

    @Override
    public void start(PressureListener listener) throws IOException {
        if (!helper.canExecute()) {
            throw new IOException("helper 프로그램이 없습니다: " + helper.getPath() + " (./build.sh 먼저 실행)");
        }
        process = new ProcessBuilder(helper.getAbsolutePath())
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .start();

        reader = new Thread(() -> {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = in.readLine()) != null) {
                    PressureReading reading = PressureReading.parse(line);
                    if (reading != null) listener.onReading(reading);
                }
            } catch (IOException e) {
                // stop()으로 프로세스를 끄면 여기로 온다
            }
        }, "trackpad-reader");
        reader.setDaemon(true);
        reader.start();
    }

    @Override
    public void stop() {
        if (process != null) process.destroy();
    }

    @Override
    public String getName() {
        return "트랙패드";
    }
}
