package fingerscale.scale;

import fingerscale.source.PressureReading;

import java.util.ArrayList;
import java.util.List;

/**
 * 압력 값이 들어올 때마다 지금 상태(손가락 없음, 영점 잡는 중, 측정 중…)를 정하고 무게를 계산한다.
 * 화면과 상관없는 판단은 전부 여기서 한다.
 */
public class ScaleController {
    private static final long AUTO_ZERO_DELAY_MS = 1000;   // 손가락 올리고 1초 뒤 자동 영점
    private static final long LIFT_DELAY_MS = 300;         // 0.3초 넘게 안 닿아야 손 뗀 걸로 본다
    private static final double MIN_FINGER_PRESSURE = 10;  // 손가락이 막 닿는 중(압력 1~9)엔 영점을 안 잡는다
    private static final double DRIFT_GRAMS = -5;          // 영점보다 이만큼 가벼워지면 손가락 힘이 약해진 것

    private final ScaleCalculator calculator;
    private final List<ScaleListener> listeners = new ArrayList<>();

    private ScaleState state = ScaleState.NO_TOUCH;
    private long touchStartTime;  // 이번에 손가락을 올린 시각 (0이면 안 닿아 있음)
    private long lastTouchTime;   // 마지막으로 닿아 있던 시각
    private boolean autoZeroDone; // 이번 터치에서 자동 영점을 이미 했는지

    public ScaleController(ScaleCalculator calculator) {
        this.calculator = calculator;
    }

    public void addListener(ScaleListener listener) {
        listeners.add(listener);
    }

    public void onReading(PressureReading reading) {
        long now = System.currentTimeMillis();

        if (!reading.isTouching()) {
            // 잠깐 떨어진 건 무시하고, 0.3초 넘게 떨어져 있어야 손 뗀 걸로 본다
            if (touchStartTime != 0 && now - lastTouchTime > LIFT_DELAY_MS) {
                calculator.reset();
                touchStartTime = 0;
                autoZeroDone = false;
                state = ScaleState.NO_TOUCH;
            }
            notifyListener(reading);
            return;
        }

        lastTouchTime = now;
        if (touchStartTime == 0) touchStartTime = now;

        if (reading.getFingerCount() > 1) {
            // 손가락이 두 개 이상이면 압력이 더해져서 무게가 틀리게 나온다
            state = ScaleState.MULTI_TOUCH;
            notifyListener(reading);
            return;
        }

        calculator.addPressure(reading.getPressure());
        tryAutoZero(now);
        state = decideState();
        notifyListener(reading);
    }

    private void tryAutoZero(long now) {
        if (autoZeroDone) return;
        boolean waited = now - touchStartTime > AUTO_ZERO_DELAY_MS;
        boolean fingerSettled = calculator.getAveragePressure() >= MIN_FINGER_PRESSURE;
        if (waited && fingerSettled && calculator.isStable()) {
            calculator.setZero();
            autoZeroDone = true;
        }
    }

    private ScaleState decideState() {
        if (!calculator.isZeroSet()) return ScaleState.SETTLING;
        if (calculator.getRawGrams() < DRIFT_GRAMS) return ScaleState.DRIFT;
        if (calculator.getWeightGrams() == 0) return ScaleState.READY;
        return calculator.isStable() ? ScaleState.STABLE : ScaleState.MEASURING;
    }

    /** 영점을 지금 값으로 다시 맞춘다. 손가락이 안 닿아 있으면 false */
    public boolean zero() {
        if (touchStartTime == 0 || state == ScaleState.MULTI_TOUCH) return false;
        calculator.setZero();
        autoZeroDone = true;
        state = decideState();
        return true;
    }

    /** 지금 올린 물건의 실제 무게를 받아서 압력→g 비율을 다시 계산한다. 새 비율을 돌려준다. */
    public double calibrate(double realGrams) {
        double delta = calculator.getPressureAboveZero();
        if (!calculator.isZeroSet() || delta <= 0) {
            throw new IllegalStateException("손가락만 올려 영점을 맞춘 뒤, 물건을 올린 상태에서 해주세요");
        }
        if (realGrams <= 0) throw new IllegalArgumentException("무게는 0보다 커야 합니다");
        double gramsPerUnit = realGrams / delta;
        calculator.setGramsPerUnit(gramsPerUnit);
        return gramsPerUnit;
    }

    public ScaleState getState() {
        return state;
    }

    public double getWeightGrams() {
        return state.showsWeight() ? calculator.getWeightGrams() : 0;
    }

    public ScaleCalculator getCalculator() {
        return calculator;
    }

    private void notifyListener(PressureReading reading) {
        for (ScaleListener l : listeners) l.onUpdate(state, getWeightGrams(), reading);
    }
}
