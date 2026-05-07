#!/bin/bash
# EC2 Setup Script for USTA Query App
# Target: Amazon Linux 2023 or Ubuntu 24.04 (ARM64)
set -euo pipefail

echo "=== USTA Query - EC2 Setup ==="

# Detect OS
if [ -f /etc/os-release ]; then
    . /etc/os-release
    OS=$ID
else
    echo "Cannot detect OS"
    exit 1
fi

# Install Docker
echo ">>> Installing Docker..."
if [ "$OS" = "amzn" ]; then
    dnf update -y
    dnf install -y docker git
    systemctl enable docker
    systemctl start docker
    usermod -aG docker ec2-user

    # Install Docker Compose plugin
    mkdir -p /usr/local/lib/docker/cli-plugins
    COMPOSE_VERSION=$(curl -s https://api.github.com/repos/docker/compose/releases/latest | grep tag_name | cut -d'"' -f4)
    curl -SL "https://github.com/docker/compose/releases/download/${COMPOSE_VERSION}/docker-compose-linux-aarch64" \
        -o /usr/local/lib/docker/cli-plugins/docker-compose
    chmod +x /usr/local/lib/docker/cli-plugins/docker-compose
elif [ "$OS" = "ubuntu" ]; then
    apt-get update
    apt-get install -y ca-certificates curl gnupg
    install -m 0755 -d /etc/apt/keyrings
    curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /etc/apt/keyrings/docker.gpg
    echo "deb [arch=arm64 signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" \
        > /etc/apt/sources.list.d/docker.list
    apt-get update
    apt-get install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin git
    usermod -aG docker ubuntu
fi

# Add swap (1GB safety net for 2GB instance)
echo ">>> Adding 1GB swap..."
if [ ! -f /swapfile ]; then
    dd if=/dev/zero of=/swapfile bs=1M count=1024
    chmod 600 /swapfile
    mkswap /swapfile
    swapon /swapfile
    echo '/swapfile none swap sw 0 0' >> /etc/fstab
fi

# Configure Docker log rotation
mkdir -p /etc/docker
cat > /etc/docker/daemon.json <<EOF
{
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "10m",
    "max-file": "3"
  }
}
EOF
systemctl restart docker

echo ""
echo "=== Setup Complete ==="
echo ""
echo "Next steps:"
echo "  1. Clone your repo or scp the project to this instance"
echo "  2. cd into the project directory"
echo "  3. Edit deploy/.env.production with your credentials"
echo "  4. Run: cd deploy && docker compose build"
echo "  5. Run: docker compose up -d"
echo "  6. Verify: docker compose ps"
echo "  7. Test: curl http://localhost/api/v1/players/search?q=test"
echo ""
echo "Public URL: http://$(curl -s http://169.254.169.254/latest/meta-data/public-ipv4 2>/dev/null || echo '<EC2_PUBLIC_IP>')"
