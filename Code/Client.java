import java.net.*;
import java.io.*;
import java.io.Serializable;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.*;

import java.util.Base64;
import java.util.Date;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;

import java.util.Scanner;

class PostSignature implements Serializable
{
    byte[] PostSign;
}

class Client extends PostSignature
{
    public static void main(String[] args) throws Exception, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException
    {
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String userid = args[2];

        System.out.println("Hello User, This is Client!\n");
        System.out.println("Running on Port:"+port+", Logged In User:"+userid); 

        Socket socket = null;
        
        DataInputStream dis = null;
        DataOutputStream dos = null;

        socket = new Socket (host, port);

        dos = new DataOutputStream(socket.getOutputStream());
        dis = new DataInputStream(socket.getInputStream());

        Scanner scanner = new Scanner (System.in);

        String Counter=dis.readUTF();
        int MsgCount = Integer.parseInt(Counter);

        System.out.println("There are : "+MsgCount+" post(s).");
        System.out.println();
        
        for(int i=0;i<MsgCount;i++)
        {
            String out1=dis.readUTF();
            String out2=dis.readUTF();;
            String out3=dis.readUTF();
            byte[] out4 = Base64.getDecoder().decode(out3);
            String decryptFileName=userid+".prv";
            String decryptedMsg = new String();
            try
            {
                ObjectInputStream out = new ObjectInputStream(new FileInputStream(decryptFileName));
                PrivateKey PriKey = (PrivateKey)out.readObject();
                out.close();
                Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                cipher.init(Cipher.DECRYPT_MODE, PriKey);
                byte[] stringBytes = cipher.doFinal(out4);
                decryptedMsg = new String(stringBytes, "UTF8");
                System.out.println();
            }
            catch(IllegalBlockSizeException e)
            {
                e.printStackTrace();
            }
            catch (BadPaddingException e)
            {
                decryptedMsg=out3;
            }
            catch (IllegalArgumentException e)
            {
                decryptedMsg=out3;
            }
            finally
            {
                System.out.println("Sender : "+out1);
                System.out.println("Date : "+ out2);
                System.out.println("Message : "+decryptedMsg);
                System.out.println();
            }
        }

        try
        {
            
            while (true)
            {
                String add;

                dos.writeUTF(userid);
                dos.flush();

                System.out.println("Do you want to add a post ? [y/n]");
                add=scanner.nextLine();
                dos.writeUTF(add);
                dos.flush();

                if (add.equals("y"))
                {
                    boolean all=false;
                    String sendID;

                    System.out.println("Enter the recipient userid (type \"all\" for posting without encryption): ");
                    sendID=scanner.nextLine();


                    if(sendID.equals("all"))
                    {
                        all=true;
                    }
                    dos.writeBoolean(all);
                    dos.flush();
                    System.out.println("Enter your message: ");
                    String PreMsg = scanner.nextLine();
                    Date tempDDate = new Date();
                    String date = ""+tempDDate;
                    if(all==false)
                    {
                        String pubfilename=sendID+".pub";

                        ObjectInputStream EncryptIn = new ObjectInputStream(new FileInputStream(pubfilename));
                        PublicKey PubKey = (PublicKey)EncryptIn.readObject();
                        EncryptIn.close();

                        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                        cipher.init(Cipher.ENCRYPT_MODE, PubKey);
                        byte[] EncryptMsg = cipher.doFinal(PreMsg.getBytes("UTF8"));
                        String encodedMsg = Base64.getEncoder().encodeToString(EncryptMsg);
                        
                        String SignFileName=userid+".prv";
                        ObjectInputStream SignIn = new ObjectInputStream(new FileInputStream(SignFileName));
                        PrivateKey SignPriKey = (PrivateKey)SignIn.readObject();
                        SignIn.close();

                        Signature sign = Signature.getInstance("SHA1withRSA");
                        
                        sign.initSign(SignPriKey);
                        sign.update(userid.getBytes());
                        sign.update(date.getBytes());
                        sign.update(encodedMsg.getBytes());
                        byte[] signature = sign.sign();

                        dos.writeUTF(encodedMsg);
                        dos.flush();

                        dos.writeUTF(date);
                        dos.flush();

                        dos.write(signature);


                    }
                    else
                    {
                        dos.writeUTF(PreMsg);
                        dos.flush();

                        dos.writeUTF(date);
                        dos.flush();
                    }

                    System.out.println("Server Response: " + dis.readUTF()+"\n");
                }                
                else
                {
                    System.out.println("User Entered n, Exiting !\n");
                    break;
                }
                
            }
        }catch (IOException e)
        {
            e.printStackTrace();
        }
        
        finally
        {
            try
            {
                if(socket != null)
                {
                    socket.close();
                }
                if(dos !=null)
                {
                    dos.close();
                }
                if(dis != null)
                {
                    dis.close();
                }
                if(scanner != null)
                {
                    scanner.close();
                } 
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
              
    }
}