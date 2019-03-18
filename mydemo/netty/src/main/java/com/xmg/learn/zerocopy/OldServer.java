package com.xmg.learn.zerocopy;


import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class OldServer {

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8888);

        while (true){
            Socket accept = serverSocket.accept();

            DataInputStream dataInputStream = new DataInputStream(accept.getInputStream());

            byte[] byteArr = new byte[4096];
            while (true){
                int read = dataInputStream.read(byteArr);
                if (read == -1){
                    break;
                }
            }
        }


    }
}
