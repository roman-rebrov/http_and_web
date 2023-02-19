package ru.netology.server.httphandlers;

public class Response {


    public static final String OK = "200 OK";
    public static final String NOT_Found = "404 Not Found";
    public static final String Bad_Request = "400 Bad Request";


    private String status = OK;
    private String type = "";
    private long length = 0L;


    private String getHeader(){
        return  "HTTP/1.1 " + this.status + "\r\n" +
                "Content-Type: " + this.type + "\r\n" +
                "Content-Length: " + this.length + "\r\n" +
                "Connection: close\r\n" +
                "\r\n";
    }

    public void setLength(long length){
        this.length = length;
    }

    public void setType(String type){
        this.type = type;
    }

    public String getHeaderOk(){
        this.status = OK;
        return this.getHeader();
    }
    public String getBadRequest(){
        this.status = Bad_Request;
        return this.getHeader();
    }
    public String getHeaderNotFound(){
        this.status = NOT_Found;
        return this.getHeader();
    }

}
