#!/bin/bash
sudo pip3 install ec2_metadata
aws s3 cp s3://pms-bucket-test/staticip/static_private_ip.py ./
python2 static_private_ip.py $1