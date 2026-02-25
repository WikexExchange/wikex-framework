# 🚀 Wikex Platform

Complete cryptocurrency trading platform với 20+ microservices.

## 🚀 Quick Start

Deploy the complete Wikex microservices platform with a single command:

```bash
# Build and start all services
docker-compose up -d --build

# View service logs  
docker-compose logs -f [service-name]

# Stop all services
docker-compose down

# Remove all data (including logs)
docker-compose down -v
```

### 📊 **Log Management**
All services are configured with log rotation to prevent disk space issues:
- **Max file size**: 10MB per log file
- **Max files**: 3 files per service (30MB total per service)
- **Compression**: Enabled for rotated logs
- **Driver**: JSON file format for structured logging

### 🌐 Service Access Points
- **API Gateway**: http://localhost:8888
- **Nacos Console**: http://localhost:8848/nacos (nacos/GIdQOvzeFEhjmYkB)
- **MySQL Database**: localhost:3306 (root/wikex123)
- **Redis Cache**: localhost:6379 (wikex123)
- **MongoDB**: localhost:27017 (admin/wikex123)
- **RocketMQ Console**: http://localhost:28080
- **Sentinel Dashboard**: http://localhost:8858
- **Seata Server**: localhost:8091

### 🏗️ Infrastructure Services
The platform includes complete infrastructure stack:

#### **Service Discovery & Configuration**
- **Nacos**: Service registry, configuration center, and service mesh
- **Seata**: Distributed transaction coordinator with MySQL backend
  - Database: `wikex_seata` (auto-created)
  - Configuration: Nacos-integrated registry

#### **Message Queue**
- **RocketMQ**: High-performance distributed messaging
  - NameServer: port 9876
  - Broker: ports 10909, 10911
  - Console: port 28080 (web management)

#### **Data Storage**  
- **MySQL 5.7**: Primary relational database with `wikex` and `wikex_seata` schemas
- **MongoDB 6.0**: NoSQL database for logs, analytics, and flexible data structures
- **Redis 7**: Cache layer with password authentication

#### **Monitoring & Flow Control**
- **Sentinel**: Flow control, circuit breaker, and system adaptive protection
  - Dashboard: Real-time monitoring and rule management
  - Integration: Nacos-based rule configuration

### 📁 Configuration Files
All infrastructure configurations are in the `config/` directory:
- `config/rocketmq/broker.conf` - RocketMQ broker settings
- `config/seata/registry.conf` - Seata server configuration  
- `config/seata/seata_database.sql` - Seata database schema
- `config/mongodb/init-mongo.js` - MongoDB collections and indexes
- `config/sentinel/application.properties` - Sentinel dashboard configuration
- `config/sentinel/flowrule.json` - Flow control rules for services
- `config/bootstrap.yml` - Nacos bootstrap configuration

### 🔧 **Production Features**
- **Log Rotation**: All services limited to 30MB logs (10MB × 3 files)
- **Auto Restart**: Services restart automatically unless stopped manually
- **Health Monitoring**: Built-in health checks for infrastructure services
- **Resource Limits**: Optimized JVM settings for containerized deployment
- **Persistent Data**: Volumes ensure data survives container restarts

---

## 📋 Manual Installation (Legacy Reference)
### RocketMQ Installation
#### RocketMQ NameService Installation
```properties
docker run -d --restart=always --name rmqnamesrv --privileged=true -p 9876:9876 -v /root/docker/rocketmq/data/namesrv/logs:/root/logs -v /root/docker/rocketmq/data/namesrv/store:/root/store -e "MAX_POSSIBLE_HEAP=100000000" rocketmqinc/rocketmq sh mqnamesrv
```

#### RocketMQ Broker Installation
##### Broker Configuration File (broker.conf)
```editorconfig
# Cluster name; multiple clusters can be configured for multiple nodes
brokerClusterName = DefaultCluster
# Broker name; master and slave use the same name to indicate their relationship
brokerName = broker-a
# 0 indicates Master, >0 indicates different slaves
brokerId = 0
# Time to delete messages, default is 4 AM
deleteWhen = 04
# Message retention time on disk, in hours
fileReservedTime = 48
# Options: SYNC_MASTER, ASYNC_MASTER, SLAVE; indicates data sync mechanism between Master and Slave
brokerRole = ASYNC_MASTER
# Flush strategy: ASYNC_FLUSH or SYNC_FLUSH; SYNC_FLUSH writes to disk before returning success, ASYNC_FLUSH does not
flushDiskType = ASYNC_FLUSH
# Set the IP address of the server hosting the broker
brokerIP1 = 127.0.0.1
# Maximum disk usage ratio
diskMaxUsedSpaceRatio=99
```

##### Installation Command
```properties
docker run -d --restart=always --name rmqbroker --link rmqnamesrv:namesrv -p 10911:10911 -p 10909:10909 --privileged=true -v /root/docker/rocketmq/data/broker/logs:/root/logs -v /root/docker/rocketmq/data/broker/store:/root/store -v /root/docker/rocketmq/config/broker.conf:/opt/rocketmq-4.4.0/conf/broker.conf:ro -e "NAMESRV_ADDR=namesrv:9876" -e "MAX_POSSIBLE_HEAP=200000000" rocketmqinc/rocketmq sh mqbroker -c /opt/rocketmq-4.4.0/conf/broker.conf
```

#### RocketMQ Console Installation
```properties
docker run -d --restart=always --name rmqadmin -e "JAVA_OPTS=-Drocketmq.namesrv.addr=127.0.0.1:9876 -Dcom.rocketmq.sendMessageWithVIPChannel=false" -p 28080:8080 pangliang/rocketmq-console-ng
```

### Nacos Installation
```properties
docker run -d -p 8848:8848 -e MODE=standalone --restart always --name nacos nacos/nacos-server:v2.1.0
```

### Sentinel Dashboard Installation
```properties
docker run --name sentinel -p 8858:8858 -d bladex/sentinel-dashboard
```

### Seata Installation
#### Seata Database Creation
[Script Link](https://github.com/seata/seata/blob/1.4.1/script/server/db/mysql.sql)

#### Start Temporary Container
```properties
docker run --name seata-server -p 8091:8091 -e SEATA_IP=127.0.0.1 -d seataio/seata-server:1.4.1
```

#### Retrieve registry.conf from Temporary Container
```properties
mkdir /opt/seata
docker cp seata-server:/seata-server/resources/registry.conf /opt/seata
```

#### Modify registry.conf Configuration
```properties
vim /opt/seata/registry.conf
```
```properties
registry {
    type = "nacos"
    loadBalance = "RandomLoadBalance"
    loadBalanceVirtualNodes = 10

    nacos {
        application = "seata-server"
        serverAddr = "127.0.0.1:8848"
        group = "SEATA_GROUP"
        namespace = ""
        cluster = "default"
        username = ""
        password = ""
    }
}
config {
    type = "nacos"

    nacos {
        serverAddr = "127.0.0.1:8848"
        namespace = "seata"
        group = "SEATA_GROUP"
        username = "nacos"
        password = "GIdQOvzeFEhjmYkB"
    }
}
```

#### Remove Temporary Container After Configuring registry.conf
```properties
docker stop seata-server
docker rm seata-server
```

#### Push Seata Dependency Configurations to Nacos
[Configuration Files](https://github.com/seata/seata/tree/1.4.1/script/config-center)
Directory structure (downloaded locally):
```properties
/opt/seata
├── config.txt
└── nacos
    └── nacos-config.sh
```

#### Modify config.txt Configuration
```properties
vim /opt/seata/config.txt

service.vgroupMapping.wikex_tx_group=default

store.mode=db

store.db.driverClassName=com.mysql.jdbc.Driver
store.db.url=jdbc:mysql://127.0.0.1:3306/na_seata?useUnicode=true
store.db.user=na_seata
store.db.password=PBcjrbSYDGxXKN35
```

#### Create Namespace "seata" in Nacos

#### Execute Push Command
```properties
cd /opt/seata/nacos

bash nacos-config.sh -h 127.0.0.1 -p 8848 -g SEATA_GROUP -t seata -u nacos -w nacos
```

#### Start Seata Service
```properties
docker run --name seata-server --restart=always -p 8091:8091 -e SEATA_IP=127.0.0.1 -e SEATA_CONFIG_NAME=file:/seata-server/resources/registry.conf -v /opt/seata/registry.conf:/seata-server/resources/registry.conf -v /opt/seata/logs:/root/logs -d seataio/seata-server:1.4.1
```

#### Add undo_log Table to Each Database
[undo_log Table Script](https://github.com/seata/seata/blob/1.4.1/script/client/at/db/mysql.sql)

# ⚡ Docker Architecture

## Simplified Build Process
- **Single centralized Dockerfile** - Tất cả services build từ 1 Dockerfile  
- **Multi-stage builds** - Build toàn bộ project 1 lần, extract từng service
 Ví dụ build user-service : docker build --target user-service -t wikex/user-service .
- **Optimized caching** - Docker layer caching tối ưu

## Project Structure
```
wikex-framework/
├── 📄 Dockerfile              # Single multi-stage build
├── 📄 docker-compose.yml      # Complete platform orchestration  
├── 📄 build.sh/.bat           # One-command deployment
├── 📄 .dockerignore           # Optimized ignore rules
└── 📁 Services (No individual Dockerfiles)
```

Developed by Wikex Team 🌟