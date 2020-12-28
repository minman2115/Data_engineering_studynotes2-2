.

Data_Engineering_TIL(20201228)

#### [학습시 참고한 자료]

- default jupyterhub 사용자 계정관리 문제점

URL : https://github.com/jupyterhub/jupyterhub/issues/1210

- Native Authenticator 관련내용

참고자료 1 - 블로그글 "Simpler authentication for small scale JupyterHubs with NativeAuthenticator"

URL : https://blog.jupyter.org/simpler-authentication-for-small-scale-jupyterhubs-with-nativeauthenticator-999534c77a09

참고자료 2 - Native Authenticator 설명서

URL : https://native-authenticator.readthedocs.io/en/latest/quickstart.html#installation

- 'Error in Authenticator.pre_spawn_start: KeyError "getpwnam(): name not found:' 관련내용

URL : https://discourse.jupyter.org/t/setup-jupyterhub-setup-with-nativeauthenticator/3652/8

#### [학습내용]

- default jupyterhub 사용자 계정관리 문제점

the default authenticator is PAMAuthenticator. 
PAMAuthenticator means it uses the account in the linux system.

suposed you have an account in your linux named "Robert", and the password is "123456"
just use the "Robert" for username, and "123456" for password.

in short words, use the account you login in you linux system.

JupyterHub doesn't itself allow creation of user accounts. That's generally handled by the outside system that the Hub uses for authentication, such as PAM (system users) or GitHub, etc.

--> 간편하게 주피터허브 WebUI에서 사용자를 추가/제거/관리 하고 싶다.

- 해결방안 : Native Authenticator 를 사용해보자.

Native Authenticator란 Native Authenticator was created to supply smaller JupyterHub installations with a more convenient authentication system instead of maintaining user accounts by hand, without the overhead of needing an external third party service.

- Native Authenticator 주요기능

1) A signup page that creates new users

2) Username sanitation (avoiding spaces, commas and backslashes)

3) "Administrator" privileges that can be given to specific users

4) The ability for users to change their passwords

5) A panel for controlling access to the system on a user-by-user basis

- Native Authenticator 설치법

EMR 마스터 노드 터미널에 ssh 접속해서 아래와 같은 명령어로 설치가능 


```bash
sudo docker exec jupyterhub bash -c "pip install jupyterhub-nativeauthenticator"
echo "c.LocalAuthenticator.create_system_users = True" | sudo tee -a /etc/jupyter/conf/jupyterhub_config.py
echo "c.JupyterHub.authenticator_class = 'nativeauthenticator.NativeAuthenticator'" | sudo tee -a /etc/jupyter/conf/jupyterhub_config.py
echo "c.NativeAuthenticator.open_signup = True" | sudo tee -a /etc/jupyter/conf/jupyterhub_config.py
sudo docker restart jupyterhub
```

- Native Authenticator을 설치하고 jupyterhub에서 사용자계정 생성후 로그인 시 아래와 같은 'Error' 대처법

`Error in Authenticator.pre_spawn_start: KeyError "getpwnam(): name not found:`

Error 원인 : if you' re using the default JupyterHub spawner (LocalProcessSpawner), then you have to make sure that for each authenticated user, there is also a matching local UNIX user. (The useradd command can be used to create the admin user on your system.)

예를들어서 `minman` 이라는 아이디를 생성했다고, 위와 같은 Error가 났다고 한다면 EMR 마스터 노드에 접속해서 아래와 같이 명령어를 실행해서 계정을 활성화 시켜야함

`sudo docker exec jupyterhub useradd -m -s /bin/bash -N minman`
