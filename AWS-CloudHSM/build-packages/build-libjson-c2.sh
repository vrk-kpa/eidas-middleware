#!/bin/sh
docker run --rm -iv "$(pwd):/usr/src/libjson-c2/target" debian:stretch sh -e << EOF
  cd /usr/src/libjson-c2
  echo "deb-src http://deb.debian.org/debian jessie main" >>/etc/apt/sources.list
  apt-get update
  apt-get dist-upgrade -y
  apt-get build-dep -y libjson-c2
  apt-get source -b libjson-c2
  install -o $(id -u) -g $(id -g) libjson-c2_0.11-4_amd64.deb target/
EOF
