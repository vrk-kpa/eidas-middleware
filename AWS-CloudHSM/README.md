# Working with eidas-middleware on AWS and configuring CloudHSM

## AMI-conversion
AMI-conversion folder contais packer script which converts middleware ova image into AWS-ami image.
With this guide you can deploy the middleware ova image into AWS as ec2-instance.

## build-packages
AWS doesn't have cloudhsm-client tools for Debian Strecth, thus it is necessary to build 
and install libjson and libssl manually. Scripts for building the necessary packages are
in build-packages folder.

## Generating keys
Guide for generating keys to CloudHSM can be found in generate_keys.txt

## Configuring middleware to use CloudHSM
First, it is necessary to install the packages built in build-packages guide on the middleware ec2-instance.

Second, install the AWS CloudHSM client Ubuntu 16.04 LTS version 
(https://docs.aws.amazon.com/cloudhsm/latest/userguide/install-and-configure-client-linux.html).

Third, Guide for installing CloudHSM Java libraries, which the middleware can use, can be found here:
https://docs.aws.amazon.com/cloudhsm/latest/userguide/java-library-install.html. Use the Ubuntu 16.04 LTS guide. 
The credentials can be provided for eidas-middleware service as environment variables.

After these modify the hsmExternalCfgLocations property in pkcs11.properties file in the middleware config 
folder to point to a file with the following content:

library = /opt/cloudhsm/lib/libcloudhsm_pkcs11.so
name = cloudhsm
slot = 1

The rest of the configurations should be the same as when deploying with the Swedish implementation of softHSM.

