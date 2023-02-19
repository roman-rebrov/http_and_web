package ru.netology.server;

public class Part {

    private String name;
    private byte [] value;
    private String [] headers;

    public Part(byte [] headers, byte [] val){

        this.headers = new String(headers).split("; ");

        for (String h : this.headers){
            if (h.startsWith("name")){
                final String [] elements = h.split("=");
                if (elements.length > 1) {
                    this.name = elements[1].replaceAll("^\"|\"$", "");
                }
            }
        }
        this.value = val;
    }

    public String getName() {
        return this.name;
    }

    public byte[] getValue() {
        return this.value;
    }
}
