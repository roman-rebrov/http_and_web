package ru.netology.server.httphandlers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.List;
import org.apache.http.NameValuePair;

public class Request {

    private final Socket socket;
    private  BufferedReader in;
    private  String path;
    private  String method;
    private List<NameValuePair> bodyQuery;


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

    private int indexOf(byte[] array, byte[] target, int start, int end) {
        outer:
        for (int i = start; i < end; i++) {
            if (target[0] == array[i]) {
                for (int j = 0; j < target.length; j++) {
                    if (target[j] != array[i + j]) {
                        continue outer;
                    }
                }
                return i;
            }
        }
        return -1;
    }


    public List<NameValuePair> getPostParams() {
        return this.bodyQuery;
    }

    public NameValuePair getPostParam(String name) {
        if (this.bodyQuery != null) {
            for (NameValuePair el : this.bodyQuery) {
                if (name.equals(el.getName())) {
                    return el;
                }
            }
        }
        return null;
    }

    public String getPath() {
        return this.path;
    }

    public String getMethod() {
        return this.method;
    }

}
