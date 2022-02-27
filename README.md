# **Java Cryptographic Messenger**
We have a Java Server and Client, that utilizes various algorithms like with Base64, RSA/ECB/PKCS1Padding, SHA1withRSA to enable sending of data from one user to another (or everyone).

## **How the System works ?**

### **General Architecture**

* The server program is always running once started, and listens for incoming connections at the port specified.
* When a client is connected, the server handles the request, then waits for the next request .
* We assumme that only one client will connect to the server at any one time.
* Each user has a unique userid (simple string).
  * Each user is associated with a pair of RSA public and private keys, with filenames that have .pub or .prv after the userid, respectively
  * Thus the key files are named alice.pub, alice.prv if the userid is alice.
  * These keys are generated separately by a program [RSAKeyGen.java](/Code/RSAKeyGen.java) before execution of the Client and Server.
* It is assumed that the server already has the public keys of all legitimate users, and each client program user already has their own private key as well as the public keys of anyone to whom they want to send secret messages.
* The client and server programs never create any new keys.
* A "post" consists of three pieces of information: the userid of the sender, the message which may or may not be encrypted , and a timestamp.

### **Server Side**

* The server keeps a collection of all the posts sent by all active users. 
* The server keeps all the posts, in the order they are received. Note that there are no persistent storage of these posts (so when the server program quits, all posts are lost). The posts are otherwise never removed.
* The system allows both unencrypted posts that are intended for everyone, and encrypted posts that can only be decrypted by the intended recipient. 
  * If a post is encrypted, the message part would have been encrypted with RSA and the appropriate key of the intended recipient, then converted to a Base64 string.
  * The sender userid and timestamp parts are not encrypted.
* Since this encrypted-and-converted message is also a string, the server treats it the same way as an unencrypted message. Note that neither the server nor anyone other than the intended recipient knows how to decrypt it or even who this encrypted message is for.
* Upon the connection of a new client :
  * The server first sends all the posts it currently has to the client.
  * Then, it checks if the client wants to post a message. If the client does not want to post a message, then the connection ends.
  * Otherwise, the server receives the post (the sender userid, the possibly encrypted message, and the timestamp).
  * It also receives from the client a signature, that is computed based on all three fields of the post and signed by the client (with the appropriate key) to prove their identity. Note that the signature itself is not part of the post and is not supposed to be stored.
 * After receiving the post and the signature, the server verifies the signature with the appropriate key. 
  * If the signature checks out, it accepts the post and adds it to its collection of posts. 
  * If the signature does not verify, the post is discarded.
  * The server print's the contents of the post and the accept/reject decision to the screen.
* The connection then ends and the server should wait for the next client. The server should not quit or terminate (even if the signature check fails). 

### **Client Side**

When the client program starts, it connects to the server (which should be already running on the same port number as the Client) to retrieve all the posts. 

* For each post, it displays the sender userid and the timestamp, and handles the possibly encrypted message as follows. 
  * Since it does not know whether each post is encrypted for this user or not, it attempts to decrypt every message as if it is intended for this user; i.e., it convert the message as if it is Base64-encoded, then decrypt it with the appropriate key (as if it is encrypted for this user).
  * If the Base64 conversion does not result in an IllegalArgumentException and the decryption does not result in a BadPaddingException, it is then assumed to be a correct decryption, and it displays this decrypted message.
  * Otherwise (if one of the above exceptions happen), the message is either readable plaintext intended for everyone, or some Base64-encoded string of a message encrypted and intended for someone else; in both cases it displays the original message.
  * Note that for the intended recipient, the decryption happened "transparently" and they would not know that the message was encrypted and intended only for them; while for all other users they will see the presence of an encrypted message from the sender.
* After displaying all posts within the Server, the client program then asks the user whether they want to post a message.
  * If the user wants to, then it prompts the user to enter the userid of the recipient, and the message. 
  * If the user enters "all" as the recipient userid, then the message is not encrypted.
  * Otherwise, it is encrypted with RSA/ECB/PKCS1Padding and with the appropriate key to ensure only the intended recipient can read it.
  * The encryption result is then converted to a Base64 string, and this becomes the "message" part of the post.
* The client program then generate's a signature based on the whole post (the three fields, where the message part is to be treated just like a string whether it was encrypted or not), using the SHA1withRSA algorithm with the appropriate key to prove the identity of the sender.
* The post and the signature are then sent to the server.

## **Instructions to Run**

Use these instructions to run the Server and Client locally on your system [Link](/RunInstructions.md)

## **Release**

_v1.0 - initial version which has been tested and confirmed working for both encrypted transfer between the Client and Server [Link](github link)_
