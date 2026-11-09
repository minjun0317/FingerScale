# 손가락저울 (FingerScale)

맥북 트랙패드의 압력 값으로 물건 무게를 재는 프로그램.
압력 읽기만 Swift(helper/)가 하고, 나머지는 전부 Java(src/)로 만들었다.

## 실행

```
./build.sh          # 처음 한 번
./run.sh            # 트랙패드로 재기
./run.sh --test     # 맥북 없이 가짜 값으로 화면 확인
./run.sh --debug    # data/debug.log 에 값 기록
```

## 사용법

1. 손가락 하나를 트랙패드에 가볍게 올린다 → 1초 뒤 "준비 완료"
2. 손가락은 그대로 두고 물건을 올린다 → "측정 완료"가 뜨면 그 값이 무게
3. 이름을 적고 저장 (비워두면 "측정 1"로 저장)

- 스페이스바: 영점 다시 맞추기
- 설정 → 보정하기: 무게를 아는 물건으로 다시 맞추기 (예: 에어팟 프로3 55.1g)

## 구조

```
src/fingerscale/
├── Main.java               실행, 객체 연결
├── source/                 압력 값 가져오기
│   ├── PressureSource      인터페이스
│   ├── TrackpadSource      Swift helper 실행해서 값 읽기
│   ├── TestSource          가짜 값
│   └── PressureReading     값 한 개 (손가락 수, 압력)
├── scale/                  계산과 판단
│   ├── ScaleCalculator     평균, 영점, 압력→g
│   ├── ScaleController     상태 판단 (영점 잡는 중, 측정 중 …)
│   ├── ScaleState          상태 목록과 안내 문구
│   └── ScaleListener
├── unit/                   Unit ← Gram, Ounce
├── record/                 Measurement, RecordStore (CSV 저장)
├── settings/Settings       보정 값 저장
├── ui/                     ScaleWindow, RecordsDialog
└── debug/DebugLog          --debug 기록
```
