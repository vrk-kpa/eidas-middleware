#!/bin/sh

set -o errexit
set -o nounset
set -o xtrace

EIDASMW_VERSION=1.0.7
EIDASMW_SHA256=7550411bcb6c18466f7e243dc5c3c210d11492a26e807e65a1f0dd13ad12cc41
DEST_DEV=/dev/xvdf

# Install qemu-img for disk image conversion
yum install -y qemu-img

# Download eIDAS Middleware and verify SHA256 digest
curl -LO https://github.com/Governikus/eidas-middleware/releases/download/${EIDASMW_VERSION}/eidas-middleware-${EIDASMW_VERSION}.ova
echo "${EIDASMW_SHA256}  eidas-middleware-${EIDASMW_VERSION}.ova" | sha256sum -c

# Extract VMDK and convert into raw disk image
tar xf eidas-middleware-${EIDASMW_VERSION}.ova eidas-middleware-${EIDASMW_VERSION}-disk001.vmdk
qemu-img convert eidas-middleware-${EIDASMW_VERSION}-disk001.vmdk eidas-middleware-${EIDASMW_VERSION}-disk001.raw
rm eidas-middleware-${EIDASMW_VERSION}-disk001.vmdk

# Set up loopback device from disk image
losetup -P -f eidas-middleware-${EIDASMW_VERSION}-disk001.raw
SRC_DEV=$(losetup -j eidas-middleware-${EIDASMW_VERSION}-disk001.raw | cut -d : -f 1)

# Mount partitions from disk image
mkdir /media/eidas-middleware
mount -o ro ${SRC_DEV}p1 /media/eidas-middleware
mount -o ro ${SRC_DEV}p2 /media/eidas-middleware/var
mount -o ro ${SRC_DEV}p3 /media/eidas-middleware/opt

# Create and mount a single ext4 partition on target disk
echo 'start=2048, type=83' | sfdisk ${DEST_DEV}
mkfs.ext4 ${DEST_DEV}1
mkdir /media/eidas-middleware-custom
mount ${DEST_DEV}1 /media/eidas-middleware-custom

# Clone contents from original disk image to target disk
rsync -aAHSX --numeric-ids /media/eidas-middleware/ /media/eidas-middleware-custom/

# Configure DNS on target disk
cp /etc/resolv.conf /media/eidas-middleware-custom/etc/resolv.conf

# Bind mount directories from host system to chroot
for DIR in dev dev/pts proc sys ; do
	mount --bind /${DIR} /media/eidas-middleware-custom/${DIR}
done

# Copy and run chroot bootstrap script
install -m 0755 /tmp/chroot-bootstrap.sh /media/eidas-middleware-custom/tmp/
chroot /media/eidas-middleware-custom /tmp/chroot-bootstrap.sh
rm /media/eidas-middleware-custom/tmp/chroot-bootstrap.sh

# Reinstall GRUB on target disk
chroot /media/eidas-middleware-custom grub-install ${DEST_DEV}

# Unmount directories from chroot
for DIR in sys proc dev/pts dev ; do
	umount /media/eidas-middleware-custom/${DIR}
done

# Unmount target disk
umount /media/eidas-middleware-custom
