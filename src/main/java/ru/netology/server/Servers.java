package ru.netology.server;

import ru.netology.server.handlers.Handler;
import ru.netology.server.httphandlers.Request;
import ru.netology.server.httphandlers.Response;
import ru.netology.server.threads.Multithreading;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

public class Servers {

    private final static ExecutorService executorService = Multithreading.getExecutor();

    private Servers(){}

    public static Server getServer(){

        return new Server() {

            private final Map<String, Map<String, Handler>> handlers = new HashMap<>();


            @Override
            public void listen(int port) {
                try (final var serverSocket = new ServerSocket(port)) {
                    while(true){

                        final Socket socket = serverSocket.accept();

                        executorService.submit(new Runnable() {
                            @Override
                            public void run() {
                                Map<String, Handler> handlerMap;
                                final Request request = new Request(socket);

                                //////////////////////////////////////////////////////////
                                System.out.println(request.getQueryParam("name"));
                                System.out.println(request.getQueryParams());
                                //////////////////////////////////////////////////////////

                                try {
                                    final var out = new BufferedOutputStream(socket.getOutputStream());
                                    final Response response = new Response();

                                    if (handlers.containsKey(request.getPath())){
                                        handlerMap = handlers.get(request.getPath());
                                        Handler handler = handlerMap.get(request.getMethod());
                                        if (handler != null){
                                            handler.handle(request, out);
                                        }else{
                                            out.write(response.getHeaderNotFound().getBytes());
                                            out.flush();
                                        }
                                    }else{
                                        handlerMap = handlers.get("default");
                                        Handler handler = handlerMap.get(request.getMethod());

                                        if (handler != null){
                                            handler.handle(request, out);
                                        }else{
                                            out.write(response.getHeaderNotFound().getBytes());
                                            out.flush();
                                        }
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }finally {

                                    if (!socket.isClosed()) {
                                        try {
                                            socket.close();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }


                            }
                        });

                    }

                } catch (IOException e){
                    e.printStackTrace();

                }
            }

                @Override
            public void addHandler(String method, String path, Handler handler) {
                final Map<String, Handler> inner = Map.of(method, handler);
                this.handlers.put(path, inner);
            }
        };
    }
}
