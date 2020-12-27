Data_Engineering_TIL_(20201227)

### [학습시 참고자료]

'Things you need to know about Hadoop and YARN being a Spark developer' 블로그글을 읽고 공부한 내용을 정리한 노트입니다.

URL : https://luminousmen.com/post/hadoop-yarn-spark

### [필기노트]

![0](https://user-images.githubusercontent.com/41605276/103164228-e2380000-484b-11eb-9108-5bca268ecfae.png)

![1](https://user-images.githubusercontent.com/41605276/103164200-6dfd5c80-484b-11eb-86df-4a02be9b10f5.png)

In addition, the OS has a processor component: a kernel, a scheduler, and some threads and a process that allows programs to run on data.

![2](https://user-images.githubusercontent.com/41605276/103164264-638f9280-484c-11eb-8c25-55587eddbed3.png)

![25](https://user-images.githubusercontent.com/41605276/103164303-dc8eea00-484c-11eb-8954-43c3b50701da.png)

### * YARN Architecture

![3](https://user-images.githubusercontent.com/41605276/103164521-bfa7e600-484f-11eb-8e89-9a086eff07f2.png)

1) Client

- can submit any type of application supported by YARN


2) ResourceManager(RM)

- keeps track of live Nodemanagers and avaliable resources


- allocates avaliable resources to appropriate applications and tasks


- monitors application master(MapReduce master, spark master)

3) ApplicationMaster(AM)

- coordinates the execution of all tasks within its application


- asks for appropriate resource containers to run tasks


4) NodeManager(NM)

- privides computational resources in form of containers


- managers processes running in containers


5) containers

- can run different types of task(also application master)


- has different sizes. ex) CPU, RAM

![4](https://user-images.githubusercontent.com/41605276/103165069-99d20f80-4856-11eb-96c8-1ea9201e4499.png)

![5](https://user-images.githubusercontent.com/41605276/103165830-da825680-485f-11eb-864c-ff5fe06da764.jpg)

![6](https://user-images.githubusercontent.com/41605276/103165832-e3732800-485f-11eb-9e13-74386e5908eb.jpg)

![7](https://user-images.githubusercontent.com/41605276/103165833-e8d07280-485f-11eb-80e1-1a290be418fd.jpg)


- Interesting facts and features


YARN offers a number of other great features. It is beyond the scope of this post to describe them all, but I have included some noteworthy features:


1) `Uberization` is the ability to run all MapReduce tasks in ApplicationMaster's JVM if the tasks are small enough. This way, you avoid the overhead associated with requesting containers from ResourceManager and asking NodeManagers to run (presumably small) tasks.


2) Simplified management and access to application log files. Application-generated logs do not remain on individual slave nodes (as in MRv1) but are moved to a central repository, such as HDFS. They can later be used for debugging or for historical analysis to detect performance problems.
