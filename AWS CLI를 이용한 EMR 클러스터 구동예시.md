.

Data_Engineering_TIL(20200521)


#### STEP 1) `aws configure` 명령어로 먼저 권한이 있는 IAM credential을 설정해준다.


#### STEP 2) 아래 코드와 같은 양식으로 명령어를 실행하여 EMR 클러스터를 실행한다.


```bash
$ pwd
/home/minman

$ mkdir create_emr_cluster

$ cd create_emr_cluster/

$ pwd
/home/minman/create_emr_cluster

$ vim create_cluster.sh
#!/bin/bash

aws s3 cp s3://my_bucket/$1/emr_config/emr_config.json .
    
aws emr create-cluster \
--applications Name=Hadoop Name=JupyterHub Name=Ganglia Name=Spark Name=JupyterEnterpriseGateway Name=Livy \
--tags "env=$1" "Name=my-emr-$1" \
--ec2-attributes '{"KeyName":"my-emr-key","InstanceProfile":"EMR_EC2_DefaultRole","ServiceAccessSecurityGroup":"sg-xxxxxxxxxxxxx","SubnetId":"subnet-xxxxxxxxxx","EmrManagedSlaveSecurityGroup":"sg-xxxxxxxxxxxxx","EmrManagedMasterSecurityGroup":"sg-xxxxxxxxxxxxx"}' \
--release-label emr-6.2.0 \
--log-uri "s3n://my-logs/$1/emr/cluster/" \
--steps '[{"Args":["s3://my-bucket/$1/emr/config/emr_application_settings.sh"],"Type":"CUSTOM_JAR","ActionOnFailure":"CONTINUE","Jar":"s3://us-west-2.elasticmapreduce/libs/script-runner/script-runner.jar","Properties":"","Name":"emr_application_settings"}]' \
--step-concurrency-level 25 \
--instance-groups '[{"InstanceCount":1,"BidPrice":"OnDemandPrice","EbsConfiguration":{"EbsBlockDeviceConfigs":[{"VolumeSpecification":{"SizeInGB":128,"VolumeType":"gp2"},"VolumesPerInstance":4}]},"InstanceGroupType":"TASK","InstanceType":"r5a.8xlarge","Name":"Task - 14"},{"InstanceCount":1,"BidPrice":"OnDemandPrice","EbsConfiguration":{"EbsBlockDeviceConfigs":[{"VolumeSpecification":{"SizeInGB":256,"VolumeType":"gp2"},"VolumesPerInstance":4}]},"InstanceGroupType":"TASK","InstanceType":"m4.16xlarge","Name":"Task - 4"},{"InstanceCount":1,"BidPrice":"OnDemandPrice","EbsConfiguration":{"EbsBlockDeviceConfigs":[{"VolumeSpecification":{"SizeInGB":192,"VolumeType":"gp2"},"VolumesPerInstance":4}]},"InstanceGroupType":"TASK","InstanceType":"m5.12xlarge","Name":"Task - 6"},{"InstanceCount":1,"BidPrice":"OnDemandPrice","EbsConfiguration":{"EbsBlockDeviceConfigs":[{"VolumeSpecification":{"SizeInGB":256,"VolumeType":"gp2"},"VolumesPerInstance":4}]},"InstanceGroupType":"TASK","InstanceType":"m5.16xlarge","Name":"Task - 3"},{"InstanceCount":1,"BidPrice":"OnDemandPrice","EbsConfiguration":{"EbsBlockDeviceConfigs":[{"VolumeSpecification":{"SizeInGB":128,"VolumeType":"gp2"},"VolumesPerInstance":4}]},"InstanceGroupType":"TASK","InstanceType":"r4.8xlarge","Name":"Task - 5"},{"InstanceCount":1,"BidPrice":"OnDemandPrice","EbsConfiguration":{"EbsBlockDeviceConfigs":[{"VolumeSpecification":{"SizeInGB":64,"VolumeType":"gp2"},"VolumesPerInstance":4}]},"InstanceGroupType":"TASK","InstanceType":"r5.4xlarge","Name":"Task - 10"},{"InstanceCount":1,"BidPrice":"OnDemandPrice","EbsConfiguration":{"EbsBlockDeviceConfigs":[{"VolumeSpecification":{"SizeInGB":64,"VolumeType":"gp2"},"VolumesPerInstance":4}]},"InstanceGroupType":"TASK","InstanceType":"r4.4xlarge","Name":"Task - 9"},{"InstanceCount":1,"BidPrice":"OnDemandPrice","EbsConfiguration":{"EbsBlockDeviceConfigs":[{"VolumeSpecification":{"SizeInGB":160,"VolumeType":"gp2"},"VolumesPerInstance":4}]},"InstanceGroupType":"TASK","InstanceType":"m4.10xlarge","Name":"Task - 8"},{"InstanceCount":1,"BidPrice":"OnDemandPrice","EbsConfiguration":{"EbsBlockDeviceConfigs":[{"VolumeSpecification":{"SizeInGB":192,"VolumeType":"gp2"},"VolumesPerInstance":4}]},"InstanceGroupType":"TASK","InstanceType":"r5.12xlarge","Name":"Task - 11"},{"InstanceCount":1,"BidPrice":"OnDemandPrice","EbsConfiguration":{"EbsBlockDeviceConfigs":[{"VolumeSpecification":{"SizeInGB":256,"VolumeType":"gp2"},"VolumesPerInstance":4}]},"InstanceGroupType":"TASK","InstanceType":"m5a.16xlarge","Name":"Task - 12"},{"InstanceCount":3,"EbsConfiguration":{"EbsBlockDeviceConfigs":[{"VolumeSpecification":{"SizeInGB":128,"VolumeType":"gp2"},"VolumesPerInstance":4}]},"InstanceGroupType":"CORE","InstanceType":"r5.8xlarge","Name":"Core - 2"},{"InstanceCount":1,"BidPrice":"OnDemandPrice","EbsConfiguration":{"EbsBlockDeviceConfigs":[{"VolumeSpecification":{"SizeInGB":128,"VolumeType":"gp2"},"VolumesPerInstance":4}]},"InstanceGroupType":"TASK","InstanceType":"m5.8xlarge","Name":"Task - 7"},{"InstanceCount":1,"EbsConfiguration":{"EbsBlockDeviceConfigs":[{"VolumeSpecification":{"SizeInGB":32,"VolumeType":"gp2"},"VolumesPerInstance":2}]},"InstanceGroupType":"MASTER","InstanceType":"r5.8xlarge","Name":"Master - 1"},{"InstanceCount":1,"BidPrice":"OnDemandPrice","EbsConfiguration":{"EbsBlockDeviceConfigs":[{"VolumeSpecification":{"SizeInGB":192,"VolumeType":"gp2"},"VolumesPerInstance":4}]},"InstanceGroupType":"TASK","InstanceType":"m5a.12xlarge","Name":"Task - 13"}]' \
--configurations file:///home/minman/create_emr_cluster/emr_config.json \
--auto-scaling-role EMR_AutoScaling_DefaultRole \
--bootstrap-actions '[{"Path":"s3://my-bucket/$1/emr/config/emr_bootstrap.sh","Name":"EMR_settings"}]' \
--ebs-root-volume-size 50 \
--service-role EMR_DefaultRole \
--enable-debugging \
--managed-scaling-policy '{"ComputeLimits":{"MaximumOnDemandCapacityUnits":3,"UnitType":"Instances","MaximumCapacityUnits":40,"MinimumCapacityUnits":15,"MaximumCoreCapacityUnits":3}}' \
--name "my-emr-$1" \
--scale-down-behavior TERMINATE_AT_TASK_COMPLETION \
--termination-protected \
--region us-west-2
    
$ sh create_cluster.sh dev
{
    "ClusterId":"j-xxxxxxxxxxxxx",
    "ClusterArn":"arn:aws:elasticmapreduce:us-west-2:1234557843:cluster/j-xxxxxxxxxxxxxxx"
}

$ cat emr_config.json
[
	{
		"Classification":"yarn-site",
		"Properties":{
			"yarn.resourcemanager.scheduler.class":"org.apache.hadoop.yarn.server.resourcemanager.scheduler.fair.FairScheduler",
			"yarn.nodemanager.pmem-check-enabled":"false",
			"yarn.scheduler.fair.preemption":"true",
			"yarn.nodemanager.vmem-check-enabled":"false"
		}
	},
	{
		"Classification":"spark",
		"Properties":{
			"maximizeResourceAllocation":"false"
		}
	},
	{
		"Classification":"spark-defaults",
		"Properties":{
			"spark.driver.maxResultSize":"16g",
			"spark.memory.offHeap.size":"2g",
			"spark.rdd.compress":"true",
			"spark.network.timeout":"600s",
			"spark.sql.shuffle.partitions":"40",
			"spark.memory.offHeap.enabled":"true",
			"spark.shuffle.spill.compress":"true",
			"spark.shuffle.compress":"true",
			"spark.default.parallelism":"30",
			"spark.serializer":"org.apache.spark.serializer.KryoSerializer",
			"spark.dynamicAllocation.enabled":"true",
			"spark.dynamicAllocation.minExecutors":"1",
			"spark.dynamicAllocation.initialExecutors":"1",
			"spark.dynamicAllocation.maxExecutors":"10",
			"spark.dynamicAllocation.executorAllocationRatio":"0.5",
			"spark.executor.memoryOverhead":"4g",
			"spark.executor.memory":"32g",
			"spark.sql.execution.arrow.pyspark.enabled":"true",
			"spark.driver.memory":"32g",
			"spark.kryoserializer.buffer.max":"1024m",
			"spark.rpc.message.maxSize":"256",
			"spark.yarn.scheduler.reporterThread.maxFailures":"5",
			"spark.driver.cores":"5",
			"spark.executor.heartbeatInterval":"60s",
			"spark.executor.cores":"4",
			"spark.memory.storageFraction":"0.30",
			"spark.checkpoint.compress":"true",
			"spark.sql.sources.partitionOverwriteMode":"dynamic",
			"spark.storage.level":"MEMORY_AND_DISK_SER",
			"spark.driver.memoryOverhead":"4g",
			"spark.minman.lib":"s3://my_bucket/dev/emr/lib/my_lib.py",
			"spark.scheduler.mode":"FAIR",
			"spark.sql.crossJoin.enabled":"true",
			"spark.memory.fraction":"0.80",
			"spark.executor.extraJavaOptions":"-XX:+UseG1GC -XX:+UnlockDiagnosticVMOptions -XX:+G1SummarizeConcMark -XX:InitiatingHeapOccupancyPercent=35 -Dfile.encoding=utf-8",
			"spark.driver.extraJavaOptions":"-XX:+UseG1GC -XX:+UnlockDiagnosticVMOptions -XX:+G1SummarizeConcMark -XX:InitiatingHeapOccupancyPercent=35 -Dfile.encoding=utf-8",
			"spark.yarn.executor.memoryOverheadFactor":"0.1875",
			"spark.executor.defaultJavaOptions":"-verbose:gc -XX:+PrintGCDetails -XX:PrintGCDateStamps -XX:OnOutOfMemoryError='kill -9 %p'",
			"spark.driver.defaultJavaOptions":"-XX:OnOutOfMemoryError='kill -9 %p'",
			"spark.sql.autoBroadcastJoinThreshold":"-1",
			"spark.port.maxRetries":"30"
		}
	},
	{
		"Classification":"livy-conf",
		"Properties":{
			"livy.server.session.timeout-check":"true",
			"livy.server.session.timeout":"9h",
			"livy.server.yarn.app-lookup-timeout":"120s"
		}
	},
	{
		"Classification":"spark-hive-site",
		"Properties":{
			"hive.metastore.client.factory.class":"com.amazonaws.glue.catalog.metastore.AWSGlueDataCatalogHiveClientFactory"
		}
	}
]
```
