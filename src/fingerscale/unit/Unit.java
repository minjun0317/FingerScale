package fingerscale.unit;

/** 무게 단위의 부모 클래스. 그램 값을 받아서 각 단위로 바꾼다. */
public abstract class Unit {
    public abstract String getSymbol();

    public abstract double fromGrams(double grams);

    public String format(double grams) {
        // 압력 1칸이 약 1.4g이라 소수점은 의미가 없어서 정수로 보여준다
        return String.format("%.0f%s", fromGrams(grams), getSymbol());
    }
}
