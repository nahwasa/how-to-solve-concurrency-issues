* MYSQL 설치 및 실행
docker pull mysql
docker run -d -p 3306:3306 -e MYSQL_ROOT_PASSWORD=1234 --name mysql mysql

* 데이터베이스 생성
docker exec -it mysql bash
mysql -u root -p
create database stock_example;