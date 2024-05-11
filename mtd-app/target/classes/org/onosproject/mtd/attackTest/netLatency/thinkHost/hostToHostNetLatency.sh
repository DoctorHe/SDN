#!/bin/bash
#Statistical network latency host to host
generateHostNum(){
  echo $(($RANDOM%16+1))
}
for i in {1..90}
do
  temp=`generateHostNum`
  ip=$[121+temp/4]".0.0."$[temp%4+1]
  echo ${ip}
  ping -c 5 ${ip} >> hostToHostNetLatency.log
done








