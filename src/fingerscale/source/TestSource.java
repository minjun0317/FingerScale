package fingerscale.source;

import java.util.Random;

/**
 * 맥북 없이 돌려볼 때 쓰는 가짜 값.
 * 손가락만 올린 상태 → 동전(7.7g) → 지우개(12.3g) → 손 뗌 을 3초씩 반복한다.
 */
public class TestSource implements PressureSource {
    private static final double FINGER_PRESSURE = 300.0;
    private static final double NOISE = 1.0;
    private static final double[] OBJECTS = {0, 7.7, 12.3};

    private final double gramsPerUnit;
    private final Random random = new Random();
    private volatile boolean running;

    public TestSource(double gramsPerUnit) {
        this.gramsPerUnit = gramsPerUnit;
    }

    @Override
    public void start(PressureListener listener) {
        running = true;
        Thread thread = new Thread(() -> {
            long startTime = System.currentTimeMillis();
            while (running) {
                long step = (System.currentTimeMillis() - startTime) / 3000 % (OBJECTS.length + 1);
                PressureReading reading;
                if (step == OBJECTS.length) {
                    reading = new PressureReading(0, 0, 0);
                } else {
                    double pressure = FINGER_PRESSURE + OBJECTS[(int) step] / gramsPerUnit
                            + random.nextGaussian() * NOISE;
                    reading = new PressureReading(1, pressure, 1.0);
                }
                listener.onReading(reading);
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }, "test-source");
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void stop() {
        running = false;
    }

    @Override
    public String getName() {
        return "테스트";
    }
}
