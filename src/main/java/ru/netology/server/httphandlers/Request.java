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
    private BufferedInputStream in;
    private String path;
    private String method;
    private List<NameValuePair> bodyQuery;
    private byte[] body;
    private List<String> headers;
    private String urlencodedType = "application/x-www-form-urlencoded";


    public Request(Socket socket) {
        this.socket = socket;

        final int limit = 4096;
        final byte[] doubleLineBreakDelimiter = new byte[]{'\r', '\n', '\r', '\n'};
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
            }

            this.in.reset();

            // ------------HEADERS--------------------------------------------------------------
            final int headersStart = requestLineEnd + ordinaryLineBreakDelimiter.length;
            final int headersEnd = this.indexOf(buffer, doubleLineBreakDelimiter, headersStart, maxLength);

            if (headersEnd == -1) {
                throw new BadRequestException("Too long line");
            }

            in.skip(headersStart);

            final byte[] headersArr = in.readNBytes(headersEnd - headersStart);
            this.headers = Arrays.asList(new String(headersArr).split("\r\n"));

            // -------------BODY-----------------------------------------------------------------
            if (!(requestLine[0].equals("GET"))) {
                this.body = this.bodyHandle(in, doubleLineBreakDelimiter.length, doubleLineBreakDelimiter);
            }



        } catch (NullPointerException | IOException e) {
            System.out.println(e);
        }
    }


    private byte[] bodyHandle(BufferedInputStream in, int skip, byte[] delimiter) throws IOException {
        byte[] body = null;

        in.skip(skip);

        int contentLength = 0;
        for (String el : this.headers) {
            if (el.startsWith("Content-Length")) {
                final String[] contentLengthHeader = el.split(" ");
                contentLength = Integer.parseInt(contentLengthHeader[1]);
            }
        }

        if (contentLength != 0) {
            body = in.readNBytes(contentLength);

        }

        for (String el : this.headers) {
            if (el.startsWith("Content-Type")) {

                final String[] contentTypeParts = el.split(" ");
                String type = contentTypeParts[1];
                if (type.startsWith(urlencodedType)) {

                    this.bodyQuery = URLEncodedUtils.parse(new String(body), Charset.defaultCharset());
                    break;

                }
            }
        }
        return body;
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
