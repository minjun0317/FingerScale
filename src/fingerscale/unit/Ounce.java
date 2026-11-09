package fingerscale.unit;

public class Ounce extends Unit {
    private static final double GRAMS_PER_OUNCE = 28.3495;

    @Override
    public String getSymbol() {
        return "oz";
    }

    @Override
    public double fromGrams(double grams) {
        return grams / GRAMS_PER_OUNCE;
    }

    @Override
    public String format(double grams) {
        return String.format("%.2f%s", fromGrams(grams), getSymbol());
    }
}
