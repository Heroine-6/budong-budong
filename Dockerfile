FROM docker.elastic.co/elasticsearch/elasticsearch:8.18.8

# 한국어 분석기(nori) 설치
RUN elasticsearch-plugin install analysis-nori