package fingerscale.source;

/** 트랙패드에서 한 번 읽은 값. 만들어진 뒤에는 바뀌지 않는다. */
public class PressureReading {
    private final int fingerCount;
    private final double pressure;
    private final double size;

    public PressureReading(int fingerCount, double pressure, double size) {
        this.fingerCount = fingerCount;
        this.pressure = pressure;
        this.size = size;
    }

    /** helper가 출력한 "손가락수 압력 면적" 한 줄을 읽는다. 형식이 틀리면 null. */
    public static PressureReading parse(String line) {
        String[] parts = line.trim().split("\\s+");
        if (parts.length < 3) return null;
        try {
            return new PressureReading(
                    Integer.parseInt(parts[0]),
                    Double.parseDouble(parts[1]),
                    Double.parseDouble(parts[2]));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public int getFingerCount() { return fingerCount; }
    public double getPressure() { return pressure; }
    public double getSize() { return size; }
    public boolean isTouching() { return fingerCount > 0; }
}
