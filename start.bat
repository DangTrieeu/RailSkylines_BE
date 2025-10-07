@echo off
cd /d "d:\Work\Maintain RailSKyLines\Maintain\RailSkylines_BE"

REM Load environment variables from .env file
for /f "tokens=1,2 delims==" %%a in (.env) do (
    set %%a=%%b
)

REM Display the API key (first 20 chars for security)
echo Starting application with OPENAI_API_KEY: %OPENAI_API_KEY:~0,20%...

REM Start the Spring Boot application
gradlew.bat bootRun