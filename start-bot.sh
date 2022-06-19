#!/bin/bash
mvn compile assembly:single
nohup  java -jar ./target/trade-0.1-jar-with-dependencies.jar &
export BOT_PID=$!