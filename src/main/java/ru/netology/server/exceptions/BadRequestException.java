package ru.netology.server.exceptions;

import java.io.IOException;

public class BadRequestException extends IOException {
    public BadRequestException(String mess){
        super(mess);
    }

}
