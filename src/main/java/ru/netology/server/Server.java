package ru.netology.server;

import ru.netology.server.handlers.Handler;


public interface Server {
    void listen(int port);
    void addHandler(String method, String path, Handler handler);
}
