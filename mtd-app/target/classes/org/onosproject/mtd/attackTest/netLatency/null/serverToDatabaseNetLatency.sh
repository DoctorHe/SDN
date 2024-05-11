#!/bin/bash
#Statistical network latency   server to database
generateHostNum(){
  echo $(($RANDOM%2+1))
}
for i in {1..10}
do
  temp=`generateHostNum`
  ip=$[126]".0.0."$[temp%2+1]
  echo ${ip}
  ping -c 5 ${ip} >> serverToDatabaseNetLatency.log
done








