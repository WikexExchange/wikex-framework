@echo off
REM =============================================================================
REM Wikex Platform - Single Build Command (Windows)
REM Usage: build.bat
REM =============================================================================

echo 🚀 Building Complete Wikex Platform...

REM Create network if not exists
docker network create wikex-network 2>nul || echo Network already exists

REM Build and start all services
docker-compose up -d --build

echo.
echo ✅ Wikex Platform deployed successfully!
echo.
echo 📊 Service Status:
docker-compose ps

echo.
echo 🌐 Access Points:
echo   - Gateway: http://localhost:8080
echo   - Nacos:   http://localhost:8848/nacos
echo   - MySQL:   localhost:3306 ^(root/wikex123^)
echo   - Redis:   localhost:6379 ^(wikex123^)

echo.
echo 💡 Useful Commands:
echo   - Stop all:     docker-compose down
echo   - View logs:    docker-compose logs -f [service-name]
echo   - Restart:      docker-compose restart [service-name]