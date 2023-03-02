package ru.netology.server.httphandlers;

import ru.netology.server.Part;
import ru.netology.server.exceptions.BadRequestException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Request {

    private final Socket socket;
    private BufferedInputStream in;
    private String path;
    private String method;
    private List<String> headers;
    private byte[] body;
    private Map<String, Part> parts = new HashMap<>();
    private String multipart = "multipart/form-data";


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


        } catch (IOException e) {
            e.printStackTrace();
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


                if (type.startsWith(multipart)) {

                    final byte[] boundaryPart = contentTypeParts[2].split("=")[1].getBytes(StandardCharsets.UTF_8);
                    final byte[] boundary = new byte[boundaryPart.length + 2];
                    boundary[0] = '-';
                    boundary[1] = '-';
                    for (int i = 0; i < boundaryPart.length; i++) {
                        boundary[i + 2] = boundaryPart[i];
                    }


                    int start = 0, end = 0;
                    outer:
                    for (int i = 0; i < body.length; i++) {
                        if (body[i] == boundary[0]) {
                            end = (i - 1 < 0) ? i : i - 1;
                            if (end < 1) {
                                continue;
                            }
                            for (int j = 0; j < boundary.length; j++) {
                                if (body[i + j] != boundary[j]) {
                                    i = i + j;
                                    continue outer;
                                }
                            }
                            final byte[] rawPart = Arrays.copyOfRange(body, start, end);
                            start = end + boundary.length;
                            i = start;

                            final int indexOfSepar = this.indexOf(rawPart, delimiter, 0, rawPart.length);
                            if (indexOfSepar != 0) {
                                final byte[] partHeaders = Arrays.copyOfRange(rawPart, 0, indexOfSepar);
                                final int indexOfBodySepar = indexOfSepar + delimiter.length;
                                final byte[] partValue = Arrays.copyOfRange(rawPart, indexOfBodySepar, rawPart.length);
                                final Part part = new Part(partHeaders, partValue);
                                this.parts.put(part.getName(), part);
                            }
                        }
                    }
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


    public Part getPart(String name) {
        return this.parts.get(name);
    }

    public List<Part> getParts() {
        return new ArrayList<>(this.parts.values());
    }

    public String getPath() {
        return this.path;
    }

    public String getMethod() {
        return this.method;
    }

}
