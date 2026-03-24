#!/bin/sh

if [ $DEBUG = "true" ]; then
    DEBUG_PARAM="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=0.0.0.0:5005"
fi

java -cp "bin/*" -DlogPath=/var/log/connector-app $DEBUG_PARAM cz.senslog.connector.app.Main -cf /app/config.yaml
