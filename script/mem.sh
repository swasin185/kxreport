#!/bin/bash
ps -o pid,user,rss,command -e | awk '
  BEGIN { OFS="\t" }
  # Print the header
  NR==1 { print "PID", "USER", "RAM_MB", "COMMAND" }
  
  # Filter for tomcat OR mysql users
  $2 == "tomcat" || $2 == "mysql" { 
    print $1, $2, $3/1024, $4 
  }'
  
echo
echo -e "JAVA\tUSER\tRAM_MB\tSURV\tNURS\tLONG\tJVM\tGC\tCLEAN"
echo "-----------------------------------------------------------------------------------"
for pid in $(pgrep -u tomcat); do
    # 1. Get System Info (User and RSS)
    SYS_INFO=$(ps -p $pid -o user,rss --no-headers)
    USER=$(echo $SYS_INFO | awk '{print $1}')
    RSS=$(echo $SYS_INFO | awk '{print $2/1024}')

    # 2. Get JVM Info
    # We use 'tail -n 1' to skip the jstat header and handle the awk math
    JVM_INFO=$(sudo jstat -gc $pid | awk 'NR>1 {printf "%.2f\t%.2f\t%.2f\t%.2f\t%.2f\t%d", $1/1024, $2/1024, $6/1024, $8/1024, $10/1024, $15}')

    # 3. Print combined row
    echo -e "$pid\t$USER\t${RSS}\t$JVM_INFO"
done
echo "-----------------------------------------------------------------------------------"
echo 
echo -e "Database Connections"
sudo mysql -e \
    "SELECT user, db, count(*) as count, round(avg(time), 2) as avg_time,\
    round(avg(MEMORY_USED) / 1024, 2) as 'used_MB', round(max(MAX_MEMORY_USED) / 1024, 2) as 'max_MB' \
    FROM information_schema.processlist \
    WHERE user!= 'root' \
    GROUP BY user, host, db"