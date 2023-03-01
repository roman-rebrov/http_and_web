package ru.netology.server.httphandlers;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import ru.netology.server.exceptions.BadRequestException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

public class Request {

    private final Socket socket;
    private  BufferedInputStream in;
    private  String path;
    private  String method;
    private List<NameValuePair> query;

    public Request(Socket socket) {
        this.socket = socket;
        final int limit = 4096;
        final byte[] ordinaryLineBreakDelimiter = new byte[]{'\r', '\n'};

        try {

            this.in = new BufferedInputStream(this.socket.getInputStream());
            this.in.mark(limit);

            final byte[] buffer = new byte[limit];
            final int maxLength = in.read(buffer);
            final int requestLineEnd = this.indexOf(buffer, ordinaryLineBreakDelimiter, 0, maxLength);

            if (requestLineEnd == -1) {
                throw new BadRequestException("Too long line");
            }

            final String[] requestLine = new String(Arrays.copyOf(buffer, requestLineEnd)).split(" ");


            if (requestLine.length != 3) {
                throw new BadRequestException("Not correct request line");
            }

            this.method = requestLine[0];
            this.path = requestLine[1];

            if (path.indexOf("?") != -1) {
                final String[] separated = this.path.split("\\?");
                this.path = separated[0];

                if (!separated[1].isEmpty()) {
                    this.query = URLEncodedUtils.parse(separated[1], Charset.defaultCharset());
                }
            }

            this.in.reset();
        } catch (NullPointerException | IOException e) {
            System.out.println("--" + e);
        }
    }

    public List<NameValuePair> getQueryParams() {
        return this.query;
    }

    public NameValuePair getQueryParam(String name) {
        if (this.query != null) {
            for (NameValuePair el : this.query) {
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

}
