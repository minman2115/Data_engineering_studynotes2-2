#!/bin/bash

####################################################################
# restore yum library in EMR master node
####################################################################

aws s3 cp s3://pms-bucket-test/dev_emr_backup/yum_list_backup.log /home/hadoop/yum_list_backup.log
sudo yum -y install $(cat /home/hadoop/yum_list_backup.log)

####################################################################
# restore pip3 library in EMR master node
####################################################################

aws s3 cp s3://pms-bucket-test/dev_emr_backup/requirements.txt /home/hadoop/requirements.txt
sudo pip3 install $(grep -ivE "beautifulsoup4|boto|click|jmespath|joblib|lxml|mysqlclient|nltk|nose|numpy|py-dateutil|python37-sagemaker-pyspark|pytz|PyYAML|regex|six|tqdm|windmill" /home/hadoop/requirements.txt)


####################################################################
# restore jupyterhub pip library
####################################################################

aws s3 cp s3://pms-bucket-test/dev_emr_backup/jupyterhub_requirements.txt /home/hadoop/jupyterhub_requirements.txt
sudo docker cp /home/hadoop/jupyterhub_requirements.txt jupyterhub:/home/jovyan/jupyterhub_requirements.txt
sudo docker exec jupyterhub bash -c "pip install -r jupyterhub_requirements.txt"


####################################################################
# restore jupyterhub user info
####################################################################

echo "c.LocalAuthenticator.create_system_users = True" | sudo tee -a /etc/jupyter/conf/jupyterhub_config.py
echo "c.Authenticator.admin_users = {'jovyan'}" | sudo tee -a /etc/jupyter/conf/jupyterhub_config.py

aws s3 cp s3://pms-bucket-test/dev_emr_backup/jupyterhub_user_list.txt /home/hadoop/jupyterhub_user_list.txt
sed -i 's/jovyan //g' /home/hadoop/jupyterhub_user_list.txt

set -x
USERS=($( cat /home/hadoop/jupyterhub_user_list.txt ))
sleep 1s
TOKEN=$(sudo docker exec jupyterhub /opt/conda/bin/jupyterhub token jovyan | tail -1)
sleep 1s
password=$(echo "bXlwYXNzd2Q=" | base64 -d)
# bXlwYXNzd2Q= : mypasswd

for i in "${USERS[@]}";
do 
   sudo docker exec jupyterhub useradd -m -s /bin/bash -N $i
   sudo docker exec jupyterhub bash -c "echo $i:$password | chpasswd"
done

echo $(sed -e s/' '/"','"/g /home/hadoop/jupyterhub_user_list.txt) > /home/hadoop/jupyterhub_user_list.txt
echo $(sed "s/^/'/" /home/hadoop/jupyterhub_user_list.txt) > /home/hadoop/jupyterhub_user_list.txt
sed -i "s/$/'/g" /home/hadoop/jupyterhub_user_list.txt

users=$(cat /home/hadoop/jupyterhub_user_list.txt)
echo "c.Authenticator.whitelist = {$users}" | sudo tee -a /etc/jupyter/conf/jupyterhub_config.py
sudo docker restart jupyterhub


####################################################################
# restore jupyterhub files
####################################################################

sudo aws s3 cp s3://pms-bucket-test/dev_jupyterhub_backup/ /mnt/var/lib/jupyter/home/ --recursive
sudo docker restart jupyterhub
