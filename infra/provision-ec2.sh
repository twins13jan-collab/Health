#!/usr/bin/env bash
# Egypt National Health System v6 — EC2 provisioning + environment audit.
# Idempotent: safe to re-run. Run as a sudo-capable user on Ubuntu 22.04/24.04.
#   ./provision-ec2.sh audit   -> only report what is present/missing (no changes)
#   ./provision-ec2.sh install -> audit, then install everything missing
set -euo pipefail
MODE="${1:-audit}"

have() { command -v "$1" >/dev/null 2>&1; }
ver() { "$1" --version 2>/dev/null | head -1 || echo "n/a"; }

audit() {
  echo "=== environment audit ==="
  echo "os:        $(. /etc/os-release 2>/dev/null; echo "${PRETTY_NAME:-unknown}")"
  echo "cpu:       $(nproc) vCPU | ram: $(free -h | awk '/^Mem:/{print $2}') | disk: $(df -h / | awk 'NR==2{print $4" free"}')"
  for t in docker java mvn node npm python3 flutter dart nginx psql; do
    if have "$t"; then echo "OK   $t: $(ver "$t")"; else echo "MISS $t"; fi
  done
  if have docker; then docker compose version >/dev/null 2>&1 && echo "OK   docker compose v2" || echo "MISS docker compose v2"; fi
  if have nvidia-smi; then echo "GPU: $(nvidia-smi --query-gpu=name,memory.total --format=csv,noheader 2>/dev/null | head -1)"; else echo "GPU: none detected (fine unless hosting local AI models)"; fi
}

install_missing() {
  sudo apt-get update -y
  # Docker (official convenience path via apt repositories is preferred in
  # locked-down environments; the compose plugin is the hard requirement)
  if ! have docker; then
    curl -fsSL https://get.docker.com | sudo sh
    sudo usermod -aG docker "$USER" || true
  fi
  # Java 21 + Maven (platform-api build on the server: fresh clones have no jar)
  have java || sudo apt-get install -y openjdk-21-jdk
  have mvn  || sudo apt-get install -y maven
  # Node 20 (web build)
  if ! have node; then
    curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash -
    sudo apt-get install -y nodejs
  fi
  # Python (analytics-ai) + PostgreSQL client + Nginx + TLS tooling
  have python3 || sudo apt-get install -y python3 python3-venv python3-pip
  have psql    || sudo apt-get install -y postgresql-client
  have nginx   || sudo apt-get install -y nginx
  sudo apt-get install -y certbot python3-certbot-nginx ufw fail2ban git unzip jq || true
  # Flutter SDK (mobile build — the piece that could never be verified off-server)
  if ! have flutter; then
    sudo mkdir -p /opt/flutter
    curl -fsSL https://storage.googleapis.com/flutter_infra_release/releases/stable/linux/flutter_linux_3.24.5-stable.tar.xz \
      | sudo tar -xJ -C /opt/flutter --strip-components=1
    sudo ln -sf /opt/flutter/bin/flutter /usr/local/bin/flutter
    sudo ln -sf /opt/flutter/bin/dart /usr/local/bin/dart
  fi
}

audit
if [ "$MODE" = "install" ]; then
  install_missing
  echo "=== audit after install ==="
  audit
fi
