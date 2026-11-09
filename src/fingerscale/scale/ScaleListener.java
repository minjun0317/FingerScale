package fingerscale.scale;

import fingerscale.source.PressureReading;

public interface ScaleListener {
    void onUpdate(ScaleState state, double grams, PressureReading reading);
}
