@echo off

rem startup jar
java -jar ../boot/@project.build.finalName@.jar --spring.config.location=../config/

pause