docker stop elasticsearch logstash
docker rm elasticsearch logstash
docker volume rm buy-and-sell_elasticvolume buy-and-sell_logstashvolume
docker image rm buy-and-sell-logstash docker.elastic.co/elasticsearch/elasticsearch:8.5.3
docker-compose up -d logstash