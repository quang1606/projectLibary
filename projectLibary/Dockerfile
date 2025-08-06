# Bắt đầu từ image Elasticsearch chính thức
FROM docker.elastic.co/elasticsearch/elasticsearch:8.11.1

# Cài đặt plugin analysis-icu
RUN /usr/share/elasticsearch/bin/elasticsearch-plugin install analysis-icu

# Không cần USER root/USER elasticsearch nữa vì lệnh RUN mặc định đã đủ quyền