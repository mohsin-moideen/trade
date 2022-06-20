#!/bin/bash
mvn compile assembly:single
sudo nohup  java -jar ./target/trade-0.1-jar-with-dependencies.jar &
export BOT_PID=$!
echo Bot running on process ${BOT_PID}
exit 0