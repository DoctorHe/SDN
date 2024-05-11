#!/bin/bash

# shellcheck disable=SC2037
all=`wc -l < /home/leibnizrz/work_space/mtd_ws/mtd-app/src/main/java/org/onosproject/mtd/attackTest/interceptedHostIp.log`

echo "从报文中获取到的主机ip地址数量为:":${all}

hostNumber=1;
for ipAdress in `cat /home/leibnizrz/work_space/mtd_ws/mtd-app/src/main/java/org/onosproject/mtd/attackTest/interceptedHostIp.log`
do
    nmap $ipAdress;
    echo ${hostNumber}".扫描的ip地址为:" ${ipAdress}
    hostNumber=`expr $hostNumber + 1`
done
