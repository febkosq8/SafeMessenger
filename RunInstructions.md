## **Information to host Server and Client**

# ***Prerequisites***

*1. Java installed locally*

*2. Terminal*

***Tested working on Windows 11 v21H2 with Java v17.0.2***

# **Steps to compile the Server and Client**

* Download the latest source code from GitHub

```
javac Server.java
javac Client.java
```

# **Steps to run the Server and Client**

### #1 

* Download the latest release from GitHub
* Open Windows Terminal / Command Prompt on the root folder of the release

### #2 Create public and private keys for user's

* Run the below code to generate both public (.pub) and private (.prv) keys for the userid.
```
java RSAKeyGen userid
```
Example :
```
java RSAKeyGen alice
java RSAKeyGen bob
```

### #3 Run Server

* Run the below code to start the server with the arguments: port
```
java Server port
```
Example :
```
java Server 5555
java Server 8888
```

### #4 Run Client

* Run the below code to start the client with the arguments: host, port, userid
```
java Client host port userid
```
Example :
```
java Client localhost 5555 alice
java Client localhost 8888 bob
```

### #5 Screenshot's

Server and Client running with User: alice send message to bob and all
![ScreenShot1](https://user-images.githubusercontent.com/33223665/155872813-b313796d-37f9-4e42-91b6-371d50be457f.png)

