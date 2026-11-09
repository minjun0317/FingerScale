#!/bin/zsh
# Swift helper와 Java 프로그램을 빌드한다
set -e
cd "$(dirname "$0")"
(cd helper && swift build -c release)
rm -rf out
javac -encoding UTF-8 -d out $(find src -name '*.java')
echo "빌드 완료"
