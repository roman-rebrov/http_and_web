package ru.netology.server.handlers;

import ru.netology.server.httphandlers.Request;

import java.io.BufferedOutputStream;

public interface Handler {
    void handle(Request request, BufferedOutputStream responseStream);
}
