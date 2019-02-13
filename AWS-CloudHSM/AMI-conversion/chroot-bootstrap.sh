#!/bin/sh

set -o errexit
set -o nounset
set -o xtrace

export DEBIAN_FRONTEND=noninteractive
export GRUB_DISABLE_OS_PROBER=true

# Set debconf selections
debconf-set-selections << "EOF"
grub-pc grub-pc/install_devices multiselect /dev/xvdf
grub-pc grub-pc/install_devices_disks_changed multiselect /dev/xvdf
EOF

# Upgrade packages and install cloud utilities
apt-get update
apt-get dist-upgrade -y
apt-get purge --auto-remove -y
apt-get install -y cloud-guest-utils cloud-init
apt-get clean

# Disable password authentication
sed -i "s/^#PasswordAuthentication yes/PasswordAuthentication no/" /etc/ssh/sshd_config
chpasswd -e << EOF
root:*
eidasmw:*
EOF

# Fix system files owned by eidasmw
chown root:root \
  /etc/apt/sources.list \
  /etc/network/if-pre-up.d/iptables \
  /etc/systemd/system/eidas-middleware.service

# Remove sudo rights and supplemental groups from eidasmw
sed -i /^eidasmw/d /etc/sudoers
usermod -G "" eidasmw

# Change timezone to UTC
echo Etc/UTC > /etc/timezone
ln -sf ../usr/share/zoneinfo/Etc/UTC /etc/localtime

# Generate fstab
cat << EOF > /etc/fstab
UUID=$(findmnt / -n -o UUID) / ext4 errors=remount-ro 0 1
EOF

# Regenerate GRUB configuration and update initramfs
grub-mkconfig -o /boot/grub/grub.cfg
update-initramfs -u
