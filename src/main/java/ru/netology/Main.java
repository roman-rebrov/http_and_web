package ru.netology;

import ru.netology.server.Server;
import ru.netology.server.Servers;
import ru.netology.server.handlers.Handler;
import ru.netology.server.httphandlers.Request;
import ru.netology.server.httphandlers.Response;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) {

        final Server server = Servers.getServer();


        server.addHandler("GET", "default", new Handler() {
            @Override
            public void handle(Request request, BufferedOutputStream responseStream) {
                final var filePath = Path.of(".", "public", request.getPath());
                final Response resp = new Response();

                if (filePath.toFile().isFile()) {

                    try {

                        final String mimeType = Files.probeContentType(filePath);
                        final long length = Files.size(filePath);

                        resp.setLength(length);
                        resp.setType(mimeType);

                        responseStream.write(resp.getHeaderOk().getBytes());

                        Files.copy(filePath, responseStream);
                        responseStream.flush();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                } else {

                }

            }
        });


        server.addHandler("GET", "/classic.html", new Handler() {
            @Override
            public void handle(Request request, BufferedOutputStream responseStream) {
                try {

                    final Response resp = new Response();
                    final var filePath = Path.of(".", "public", request.getPath());

                    final var mimeType = Files.probeContentType(filePath);
                    final String template = Files.readString(filePath);


                    final var content = template.replace(
                            "{time}",
                            LocalDateTime.now().toString()
                    ).getBytes();

                    resp.setLength(content.length);
                    resp.setType(mimeType);

                    responseStream.write(resp.getHeaderOk().getBytes());
                    responseStream.write(content);
                    responseStream.flush();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        });


        server.listen(9999);
    }
}
