package fingerscale.source;

/** 압력 값을 보내주는 곳. 실제 트랙패드와 테스트용 가짜 값이 이걸 구현한다. */
public interface PressureSource {
    void start(PressureListener listener) throws Exception;
    void stop();
    String getName();
}
