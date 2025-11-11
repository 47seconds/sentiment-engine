@echo off
cd /d %~dp0
echo Starting Spring Boot Backend...
mvn spring-boot:run
pause
