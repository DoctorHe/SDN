#!/bin/bash
#Statistical network latency  host to server
generateHostNum(){
  echo $(($RANDOM%16+1))
}
for i in {1..20}
do
  temp=`generateHostNum`
  ip=$[121+temp/4]".0.0."$[temp%4+1]
  echo ${ip}
  ping -c 5 ${ip} >> hostToServertNetLatency.log
done








