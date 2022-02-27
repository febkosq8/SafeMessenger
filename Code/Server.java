import java.net.*;
import java.io.*;
import java.io.Serializable;

import java.security.*;

import java.security.Signature;

class Message implements Serializable{
    String msg;
    String userid;
    String timestamp;
}

class Server extends Message
{
    
    public static void main(String[] args) throws IOException
    {
        int MsgCount = 0;
        Message[] MsgArr = new Message[1000];
        int port = Integer.parseInt(args[0]);
        System.out.println("Hello User, This is Server!");
        System.out.println("Running on Port:"+port);  

        Socket socket = null;
        ServerSocket serverSocket = null;

        DataInputStream dis = null;
        DataOutputStream dos = null;

        serverSocket = new ServerSocket (port);

        while (true)
        {
            try
            {
                socket = serverSocket.accept();
                System.out.println("Client Connected!\n");

                dos = new DataOutputStream(socket.getOutputStream());
                dis = new DataInputStream(socket.getInputStream());

                String Counter=Integer.toString(MsgCount);
                dos.writeUTF(Counter);
                dos.flush();

                for(int i=0;i<MsgCount;i++)
                {
                    String out1=MsgArr[i].userid;
                    String out2=MsgArr[i].timestamp;
                    String out3=MsgArr[i].msg;

                    dos.writeUTF(out1);
                    dos.flush();

                    dos.writeUTF(out2);
                    dos.flush();

                    dos.writeUTF(out3);
                    dos.flush();
                }


                while (true)
                {
                    

                    String loggedUser=dis.readUTF();

                    String UserChoice=dis.readUTF();

                    if (UserChoice.equals("y"))
                    {
                        System.out.println("User entered y, Waiting for userID & Msg\n");
                        boolean all=dis.readBoolean();
                        String msgFromClient = dis.readUTF();
                        String msgTimestamp=dis.readUTF();
                        if(all==false)
                        {
                            try 
                            {

                                String SignFileName=loggedUser+".pub";
                                Signature sign = Signature.getInstance("SHA1withRSA");
                                ObjectInputStream UnSignIn = new ObjectInputStream(new FileInputStream(SignFileName));
                                PublicKey UnSignPubKey = (PublicKey)UnSignIn.readObject();
                                UnSignIn.close();
                                                             
                                sign.initVerify(UnSignPubKey);
                                sign.update(loggedUser.getBytes());
                                sign.update(msgTimestamp.getBytes());
                                sign.update(msgFromClient.getBytes());
                                byte[] signature=new byte[256];
                                dis.readFully(signature);

                                boolean boolSign = sign.verify(signature);

                                if(boolSign) 
                                {
                                    MsgArr[MsgCount] = new Message();
                                    MsgArr[MsgCount].userid=loggedUser;
                                    MsgArr[MsgCount].timestamp=msgTimestamp;
                                    MsgArr[MsgCount].msg=msgFromClient;
                                    
                                    System.out.println("New Entry from Client");
                                    System.out.println("Sender : "+MsgArr[MsgCount].userid);
                                    System.out.println("Date : "+MsgArr[MsgCount].timestamp);
                                    System.out.println("Message : "+MsgArr[MsgCount].msg);
                                    System.out.println();

                                    MsgCount++;

                                    System.out.println("Signature verified and POST has been saved to Server\n");

                                    System.out.println("Successfully Added ! MsgCount:"+MsgCount+"\n");
                                } 
                                else 
                                {
                                    System.out.println("Signature failed to verify. POST Discarded !\n");
                                }
                                
                            }
                            catch (NoSuchAlgorithmException | ClassNotFoundException | InvalidKeyException | SignatureException e)
                            {
                                e.printStackTrace();
                            }
                        }
                        else
                        {
                            MsgArr[MsgCount] = new Message();
                            MsgArr[MsgCount].userid=loggedUser;
                            MsgArr[MsgCount].timestamp=msgTimestamp;
                            MsgArr[MsgCount].msg=msgFromClient;
                            
                            System.out.println("New Entry from Client");
                            System.out.println("Sender : "+MsgArr[MsgCount].userid);
                            System.out.println("Date : "+MsgArr[MsgCount].timestamp);
                            System.out.println("Message : "+MsgArr[MsgCount].msg);
                            System.out.println();

                            MsgCount++;

                            System.out.println("Successfully Added ! MsgCount:"+MsgCount+"\n");

                        }
                        
                        dos.writeUTF("POST Recieved!");
                        dos.flush();
                        
                    }
                    else
                    {
                        System.out.println("Client Disconnected ! Waiting for new connection !\n");
                        break;
                    }
                    
                }                                
            }
            catch (IOException e)
            {
                e.printStackTrace();
                if(serverSocket != null)
                {
                    serverSocket.close();
                }
            }
        }
        
    }
}