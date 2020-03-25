# 使用说明
##打包
mvn package -Dmaven.test.skip=true
## 本地服务
cd ..\target
java -jar  -Dspring.profiles.active=peer1 raft-0.0.1-SNAPSHOT.jar
java -jar -Dspring.profiles.active=peer2 raft-0.0.1-SNAPSHOT.jar 
java -jar  -Dspring.profiles.active=peer3 raft-0.0.1-SNAPSHOT.jar


## 打开获取leader对应的节点，发起请求
peer1:7101
peer2:7102
peer3:7103
leader为peer1，打开接口页面http://localhost:7101/doc.html，添加或查询数据

raft.heartbeatPeriodMilliseconds 表示心跳间隔时间，一般至少需要2*raft.heartbeatPeriodMilliseconds才能完成一次共识，因此缩短这个时间会加快共识速度，但是由于间隔此时间就会发生一次广播请求，所以设置太短会导致cpu过高，建议20-100之间
