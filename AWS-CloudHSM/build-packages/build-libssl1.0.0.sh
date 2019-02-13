#!/bin/sh
docker run --rm -iv "$(pwd):/usr/src/libssl1.0.0/target" debian:stretch sh -e << EOF
  cd /usr/src/libssl1.0.0
  echo "deb-src http://security.debian.org/debian-security jessie/updates main" >>/etc/apt/sources.list
  apt-get update
  apt-get dist-upgrade -y
  apt-get build-dep -y libssl1.0.0
  apt-get source -b libssl1.0.0
  install -o $(id -u) -g $(id -g) libssl1.0.0_*.deb target/
EOF
