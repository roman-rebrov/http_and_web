package ru.netology.server.httphandlers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class Request {

    private final Socket socket;
    private  BufferedReader in;
    private  String path;
    private  String method;

    public Request(Socket socket) {
        this.socket = socket;

        try {
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            final String requestLine = in.readLine();
            final String [] parts = requestLine.split(" ");

            if (parts.length != 3) {
                System.out.println("No format");
                return;
            }

            this.path = parts[1];
            this.method = parts[0];
        } catch (NullPointerException | IOException e) {
            System.out.println(e);
        }
    }


    public String getPath() {
        return this.path;
    }

    public String getMethod() {
        return this.method;
    }

}
