# Packer template for German eIDAS Middleware

## Description

This directory contains a solution for converting the [official German eIDAS
Middleware release](https://github.com/Governikus/eidas-middleware/releases)
into an AWS-compatible AMI image by using
[Packer](https://www.packer.io/intro/).

The Packer templates utilizes the [Amazon EBS Surrogate
builder](https://www.packer.io/docs/builders/amazon-ebssurrogate.html) which
provisions a new temporary EC2 instance for the purpose of creating an AMI
image. An EBS volume is attached to the instance to which the contents of the
original middleware image are copied. A bootstrap script runs inside the target
environment and prepares the AMI image for example by installing the GRUB
bootloader on the EBS volume. Finally a snapshot of the EBS volume is created
and from that snapshot an AMI image is created.

## Files

* packer.json: Packer template for configuring the builder EC2 instance and
  running the provisioner scripts
* bootstrap.sh: Bootstrap script which runs on the builder EC2 instance
* chroot-bootstrap.sh: Bootstrap script which runs inside the target environment
  on the AMI image being prepared

## How to use

### Downloading packer

Get packer sofware from https://www.packer.io/downloads.html
This script has been tested with packer version 1.2.5. However, newer versions might work too.

### Updating to a new version

Check the latest release version from the
[eidas-middleware](https://github.com/Governikus/eidas-middleware/releases)
repository on GitHub.

Calculate the SHA256 digest for the released .ova file for example by
downloading the file and then running `sha256sum eidas-middleware-*.ova`.
Another option is to use the [Hash Archive](https://hash-archive.org/) service
to calculate the SHA256 digest from the file URL.

Edit `bootstrap.sh` and change the value of `EIDASMW_VERSION` to the latest
release (for example `1.0.7`). Change the value of `EIDASMW_SHA256` to the
SHA256 digest from the previous step (for example
`7550411bcb6c18466f7e243dc5c3c210d11492a26e807e65a1f0dd13ad12cc41`).

Edit `packer.json` and change the version number in `ami_name` to the latest
release.

### Converting

To run the conversion, make sure you have valid AWS credentials
 
Using [AWS CLI named profiles](https://docs.aws.amazon.com/cli/latest/userguide/cli-multiple-profiles.html):

    AWS_PROFILE=[profile] packer build packer.json

Using [aws-vault](https://github.com/99designs/aws-vault):

    aws-vault exec [profile] -- packer build packer.json

If the conversion finished successfully, you should be able to see the produced
AMI ID in the Packer output.
