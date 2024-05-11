#!/bin/bash

awk -F/ '/rtt/{print $5}' serverToDatabaseNetLatency.log

awk -F/ '/rtt/{i++;if(i<3){sum+=$5}} END { print sum*5}' serverToDatabaseNetLatency.log
awk -F/ '/rtt/{i++;if(i<5){sum+=$5}} END { print sum*5}' serverToDatabaseNetLatency.log
awk -F/ '/rtt/{i++;if(i<10){sum+=$5}} END { print sum*5}' serverToDatabaseNetLatency.log

