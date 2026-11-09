// swift-tools-version:5.10
import PackageDescription

let package = Package(
    name: "FingerScaleHelper",
    platforms: [.macOS("26.0")],
    targets: [
        .target(name: "CPrivate"),
        .executableTarget(name: "FingerScaleHelper", dependencies: ["CPrivate"]),
    ]
)
