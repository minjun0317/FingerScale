package fingerscale.debug;

import fingerscale.scale.ScaleCalculator;
import fingerscale.scale.ScaleListener;
import fingerscale.scale.ScaleState;
import fingerscale.source.PressureReading;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 문제를 찾을 때 쓰는 기록. ./run.sh --debug 로 켜면
 * 매 순간의 값(압력, 영점, 무게, 상태)을 파일에 남긴다.
 */
public class DebugLog implements ScaleListener {
    private final ScaleCalculator calculator;
    private final long startTime = System.currentTimeMillis();
    private BufferedWriter out;

    public DebugLog(Path file, ScaleCalculator calculator) {
        this.calculator = calculator;
        try {
            Files.createDirectories(file.getParent());
            out = Files.newBufferedWriter(file);
            out.write("시간(초)\t손가락\t압력\t평균\t영점여부\t영점위압력\t무게g\t상태\n");
        } catch (IOException e) {
            out = null;
        }
    }

    @Override
    public void onUpdate(ScaleState state, double grams, PressureReading r) {
        if (out == null) return;
        try {
            out.write(String.format("%.2f\t%d\t%.0f\t%.1f\t%b\t%.1f\t%.1f\t%s%n",
                    (System.currentTimeMillis() - startTime) / 1000.0,
                    r.getFingerCount(), r.getPressure(), calculator.getAveragePressure(),
                    calculator.isZeroSet(), calculator.getPressureAboveZero(), grams, state));
            out.flush();
        } catch (IOException e) {
            out = null;
        }
    }
}
