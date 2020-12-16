#!/bin/bash

####################################################################
# backup pip3 library in EMR master node
####################################################################

pip3 freeze > /home/hadoop/requirements.txt
aws s3 cp /home/hadoop/requirements.txt s3://pms-bucket-test/dev_emr_backup/

####################################################################
# backup jupyterhub user info
####################################################################

echo "c.LocalAuthenticator.create_system_users = True" | sudo tee -a /etc/jupyter/conf/jupyterhub_config.py
sudo docker restart jupyterhub

token=$(sudo docker exec jupyterhub /opt/conda/bin/jupyterhub token jovyan | tail -1)
sleep 1s
user_list=$(curl -XGET -s -k https://$(hostname):9443/hub/api/users -H "Authorization: token $token" | jq '.[].name' | sed 's/"//g')
echo $user_list > /home/hadoop/jupyterhub_user_list.txt

aws s3 cp /home/hadoop/jupyterhub_user_list.txt s3://pms-bucket-test/dev_emr_backup/jupyterhub_user_list.txt

####################################################################
# backup jupyterhub pip library
####################################################################

sudo docker exec jupyterhub bash -c "pip freeze > jupyterhub_requirements.txt"
sudo docker cp jupyterhub:/home/jovyan/jupyterhub_requirements.txt /home/hadoop/
aws s3 cp /home/hadoop/jupyterhub_requirements.txt s3://pms-bucket-test/dev_emr_backup/

####################################################################
# backup yum library in EMR master node
####################################################################

rpm -qa > /home/hadoop/yum_list_backup.log
aws s3 cp /home/hadoop/yum_list_backup.log s3://pms-bucket-test/dev_emr_backup/

####################################################################
# backup jupyterhub files
####################################################################

aws s3 sync /mnt/var/lib/jupyter/home/ s3://pms-bucket-test/dev_jupyterhub_backup/ --exclude "*/jupyterhub.sqlite" --exclude "*/jupyterhub-proxy.pid" --exclude "*/.autovizwidget/*" --exclude "*/.ipynb_checkpoints/*" --exclude "*/.ipython/*" --exclude "*/.local/*"  --exclude "*/.sparkmagic/*" --exclude "*/.bash_logout" --exclude "*/.bashrc" --exclude "*/.profile"
