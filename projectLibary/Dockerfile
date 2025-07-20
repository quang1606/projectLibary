FROM ubuntu:latest
LABEL authors="XuanQuang"
FROM docker.elastic.co/elasticsearch/elasticsearch:8.11.1

# Cài đặt plugin analysis-icu để hỗ trợ tìm kiếm tiếng Việt
RUN /usr/share/elasticsearch/bin/elasticsearch-plugin install analysis-icu