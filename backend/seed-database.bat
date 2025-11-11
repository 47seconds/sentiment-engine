@echo off
echo Seeding database with test data...

docker exec -i sentiment-postgres psql -U sentiment_user -d sentiment_db < "%~dp0src\main\resources\db\seed-data.sql"

if %ERRORLEVEL% EQU 0 (
    echo Database seeded successfully!
) else (
    echo Failed to seed database. Error code: %ERRORLEVEL%
)

pause
