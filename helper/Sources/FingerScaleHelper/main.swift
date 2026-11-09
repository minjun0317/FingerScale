import CPrivate
import Foundation

// 트랙패드 값을 한 줄씩 출력한다: "손가락수 압력 면적"
// Java 프로그램이 이 출력을 읽어서 무게로 바꾼다.

setvbuf(stdout, nil, _IOLBF, 0)

let callback: MTContactCallback = { _, touches, count, _, _ in
    var pressure: Float = 0
    var size: Float = 0
    var fingers = 0
    if let touches {
        for i in 0..<Int(count) {
            let t = touches[i]
            guard t.state == 4 || t.state == 5 else { continue }
            fingers += 1
            pressure += t.pressure
            size += t.size
        }
    }
    print(fingers, pressure, size)
    return 0
}

let devices = TPMultitouchStart(callback)
if devices <= 0 {
    FileHandle.standardError.write("트랙패드를 찾지 못했습니다 (\(devices))\n".data(using: .utf8)!)
    exit(1)
}
dispatchMain()
