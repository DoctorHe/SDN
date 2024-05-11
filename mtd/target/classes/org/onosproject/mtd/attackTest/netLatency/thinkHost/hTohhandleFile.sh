#!/bin/bash

awk -F/ '/rtt/{print $5}' hostToHostNetLatency.log

awk -F/ '/rtt/{i++;if(i<10){sum+=$5}} END { print sum*5}' hostToHostNetLatency.log
awk -F/ '/rtt/{i++;if(i<30){sum+=$5}} END { print sum*5}' hostToHostNetLatency.log
awk -F/ '/rtt/{i++;if(i<50){sum+=$5}} END { print sum*5}' hostToHostNetLatency.log
awk -F/ '/rtt/{i++;if(i<70){sum+=$5}} END { print sum*5}' hostToHostNetLatency.log
awk -F/ '/rtt/{i++;if(i<90){sum+=$5}} END { print sum*5}' hostToHostNetLatency.log

