#!/bin/bash

# shellcheck disable=SC2037
all=`wc -l <interceptedHostIp.log`;

echo "从报文中获取到的主机ip地址数量为:":${all}
hostNumber=1
while read ipAdress;
  do
    if(($ipAdress !=" "));then
      nmap $ipAdress
      echo ${hostNumber}".扫描的ip地址为:" ${ipAdress}
      hostNumber++
    else
      break
    fi
done < interceptedHostIp.log


