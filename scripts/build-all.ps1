$ErrorActionPreference = "Stop"
Push-Location services/payment-service; mvn clean package -DskipTests; Pop-Location
Push-Location services/order-service; mvn clean package -DskipTests; Pop-Location
Push-Location services/reliability-controller; mvn clean package -DskipTests; Pop-Location
Write-Host "All backend services built."
