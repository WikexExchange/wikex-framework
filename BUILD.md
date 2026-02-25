# Wikex Platform - Docker Image Building

This directory contains files and scripts for building Docker images of all Wikex services.

## Files Overview

| File | Description |
|------|-------------|
| `docker-compose.{env}.yml` | Docker Compose file for building images only |
| `build-images.sh` | Linux/macOS build script (Bash) |
| `build-images.ps1` | Windows build script (PowerShell) |
| `build-images.bat` | Windows build script (Batch) |

## Quick Start

### Option 1: Using Docker Compose directly

```bash
# Build all services
docker-compose -f docker-compose.{env}.yml build

# Build specific service
docker-compose -f docker-compose.{env}.yml build gateway

# Build without cache
docker-compose -f docker-compose.{env}.yml build --no-cache

# Build with parallel processing
docker-compose -f docker-compose.{env}.yml build --parallel
```

### Option 2: Using build scripts

#### Linux/macOS (Bash)
```bash
# Make script executable
chmod +x build-images.sh

# Build all services
./build-images.sh

# Build with specific version
./build-images.sh -v v1.0.0

# Build specific service
./build-images.sh -s gateway

# Build with cleanup
./build-images.sh --clean

# Show help
./build-images.sh --help
```

#### Windows (PowerShell)
```powershell
# Build all services
.\build-images.ps1

# Build with specific version
.\build-images.ps1 -Version "v1.0.0"

# Build specific service
.\build-images.ps1 -Service "gateway"

# Build with cleanup
.\build-images.ps1 -Clean

# Show help
.\build-images.ps1 -Help
```

#### Windows (Batch)
```batch
REM Build all services
build-images.bat

REM Build with specific version
build-images.bat --version v1.0.0

REM Show help
build-images.bat --help
```

## Available Services

The following services can be built:

### Core Services
- `gateway` - API Gateway service
- `user-service` - User management service
- `admin-service` - Administration service

### Business Services
- `exchange-service` - Exchange trading service
- `market-service` - Market data service
- `match-service` - Order matching service
- `p2p-service` - Peer-to-peer trading service
- `active-service` - Activity management service
- `agent-service` - Agent management service
- `earn-service` - Earning/staking service
- `chat-service` - Chat/messaging service
- `open-service` - Open API service

### Supporting Services
- `job-admin` - Job scheduling service
- `robot-market-service` - Market making robot service
- `robot-normal-service` - Trading robot service
- `kline-tools` - K-line data tools
- `swap-service` - Token swap service
- `coinswap-service` - Coin swap service
- `second-service` - Secondary market service
- `option-service` - Options trading service
- `netty-service` - Network communication service
- `udun-service` - Wallet integration service
- `permission-service` - Permission management service
- `delivery-service` - Delivery/settlement service

## Build Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `VERSION` | Image version tag | `latest` |
| `BUILD_DATE` | Build timestamp | Auto-generated |
| `VCS_REF` | Git commit hash | Auto-detected |
| `PARALLEL_BUILDS` | Number of parallel builds | `4` |

### Build Arguments

The Docker builds accept the following arguments:
- `BUILD_DATE` - Timestamp when the image was built
- `VCS_REF` - Git commit reference
- `VERSION` - Version tag for the image

### Example with custom environment

```bash
# Linux/macOS
export VERSION=v1.0.0
export BUILD_DATE=$(date -u +'%Y-%m-%dT%H:%M:%SZ')
export VCS_REF=$(git rev-parse --short HEAD)
./build-images.sh

# Windows PowerShell
$env:VERSION = "v1.0.0"
.\build-images.ps1
```

## Build Process

The build process follows this order:

1. **Core Services**: Gateway, User Service, Admin Service
2. **Business Services**: Exchange, Market, Match services, etc.
3. **Supporting Services**: Jobs, Tools, Robots, etc.

This order ensures that dependencies are built before dependent services.

## Troubleshooting

### Common Issues

1. **Docker not running**
   ```
   Error: Docker is not running
   Solution: Start Docker Desktop or Docker daemon
   ```

2. **Dockerfile not found**
   ```
   Error: Cannot find Dockerfile
   Solution: Ensure you're running from the project root directory
   ```

3. **Build failures due to memory**
   ```
   Solution: Increase Docker memory limits or reduce parallel builds:
   ./build-images.sh -p 2
   ```

4. **Permission denied (Linux/macOS)**
   ```
   Solution: Make script executable:
   chmod +x build-images.sh
   ```

### Build Performance

- Use `--parallel` flag for faster builds
- Adjust `PARALLEL_BUILDS` based on system resources
- Use `--no-cache` only when necessary (slower builds)
- Consider using Docker BuildKit for better performance:
  ```bash
  export DOCKER_BUILDKIT=1
  docker-compose -f docker-compose.build.yml build
  ```

### Cleaning Up

After building, you may want to clean up:

```bash
# Remove dangling images
docker image prune -f

# Remove unused images
docker image prune -a -f

# Remove build cache
docker builder prune -f
```

## Integration with CI/CD

These scripts can be integrated into CI/CD pipelines:

### GitHub Actions Example
```yaml
- name: Build Docker Images
  run: |
    chmod +x build-images.sh
    ./build-images.sh -v ${{ github.sha }}
```

### Jenkins Example
```groovy
stage('Build Images') {
    steps {
        script {
            sh './build-images.sh -v ${BUILD_NUMBER}'
        }
    }
}
```

## Next Steps

After building images:

1. **Test images**: Use `docker run` to test individual services
2. **Push to registry**: Tag and push images to your Docker registry
3. **Deploy**: Use the main `docker-compose.yml` to deploy services
4. **Monitor**: Check logs and metrics after deployment

For deployment, refer to the main `docker-compose.yml` file and deployment documentation.