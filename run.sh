#!/bin/zsh
# 실행: ./run.sh   (맥북 없이 테스트: ./run.sh --test)
cd "$(dirname "$0")"
java -cp out fingerscale.Main "$@"
