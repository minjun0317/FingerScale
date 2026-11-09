package fingerscale.scale;

/** 저울이 지금 어떤 상태인지. 화면은 이 상태를 보고 안내 문구를 고른다. */
public enum ScaleState {
    NO_TOUCH("손가락 하나를 트랙패드에 올려주세요"),
    SETTLING("물건 없이 손가락만 가만히 두세요…"),
    READY("준비 완료 · 손가락은 그대로 두고 물건을 올려보세요"),
    MEASURING("재는 중…"),
    STABLE("측정 완료"),
    MULTI_TOUCH("손가락은 하나만 올려주세요"),
    DRIFT("손가락 힘이 바뀌었어요 · 스페이스바로 영점을 다시 맞춰주세요");

    private final String message;

    ScaleState(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    /** 이 상태에서 무게 숫자를 믿을 수 있는지 */
    public boolean showsWeight() {
        return this == READY || this == MEASURING || this == STABLE;
    }
}
