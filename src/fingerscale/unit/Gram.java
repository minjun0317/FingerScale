package fingerscale.unit;

public class Gram extends Unit {
    @Override
    public String getSymbol() {
        return "g";
    }

    @Override
    public double fromGrams(double grams) {
        return grams;
    }
}
