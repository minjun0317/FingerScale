package fingerscale.scale;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * 압력 값을 무게(g)로 바꾼다.
 * 손가락만 올렸을 때의 압력을 영점으로 저장해 두고, 거기서 늘어난 만큼만 무게로 계산한다.
 */
public class ScaleCalculator {
    private static final int SMOOTHING = 50;           // 최근 50개(약 0.5초) 평균
    private static final double STABLE_MIN_RANGE = 5;  // 흔들림 허용 범위(압력 단위)
    private static final double STABLE_RATIO = 0.03;   // 또는 평균의 3%
    private static final double DEAD_ZONE_GRAMS = 5;   // 손가락 힘이 조금씩 변해서 생기는 0~5g은 0g으로 본다

    private final Deque<Double> recent = new ArrayDeque<>();
    private double gramsPerUnit;
    private double zeroPressure;
    private boolean zeroSet;

    public ScaleCalculator(double gramsPerUnit) {
        this.gramsPerUnit = gramsPerUnit;
    }

    public void addPressure(double pressure) {
        recent.addLast(pressure);
        if (recent.size() > SMOOTHING) recent.removeFirst();
    }

    /** 손을 떼면 이전 값과 영점을 지운다. 손가락 힘이 매번 달라서 영점도 다시 잡아야 한다. */
    public void reset() {
        recent.clear();
        zeroSet = false;
    }

    public double getAveragePressure() {
        double sum = 0;
        for (double p : recent) sum += p;
        return recent.isEmpty() ? 0 : sum / recent.size();
    }

    /** 최근 값들이 거의 안 흔들리면 true */
    public boolean isStable() {
        if (recent.size() < SMOOTHING) return false;
        double min = Double.MAX_VALUE, max = -Double.MAX_VALUE;
        for (double p : recent) {
            min = Math.min(min, p);
            max = Math.max(max, p);
        }
        double allowed = Math.max(STABLE_MIN_RANGE, getAveragePressure() * STABLE_RATIO);
        return max - min < allowed;
    }

    public void setZero() {
        zeroPressure = getAveragePressure();
        zeroSet = true;
    }

    public boolean isZeroSet() {
        return zeroSet;
    }

    /** 영점에서 늘어난 압력 */
    public double getPressureAboveZero() {
        return zeroSet ? getAveragePressure() - zeroPressure : 0;
    }

    /** 영점 기준 무게 그대로 (음수일 수도 있음) */
    public double getRawGrams() {
        return getPressureAboveZero() * gramsPerUnit;
    }

    /** 화면에 보여줄 무게 (5g 미만은 0) */
    public double getWeightGrams() {
        double grams = getRawGrams();
        return grams < DEAD_ZONE_GRAMS ? 0 : grams;
    }

    public double getGramsPerUnit() {
        return gramsPerUnit;
    }

    public void setGramsPerUnit(double gramsPerUnit) {
        this.gramsPerUnit = gramsPerUnit;
    }
}
