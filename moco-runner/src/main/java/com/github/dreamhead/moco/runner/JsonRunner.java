package com.github.dreamhead.moco.runner;

import com.github.dreamhead.moco.HttpServer;
import com.github.dreamhead.moco.internal.ActualHttpServer;
import com.github.dreamhead.moco.parser.HttpServerParser;

import java.io.InputStream;
import java.util.List;

import static com.github.dreamhead.moco.Moco.*;
import static com.google.common.collect.ImmutableList.of;

public class JsonRunner implements Runner {

    private final HttpServerParser httpServerParser = new HttpServerParser();
    private final StandaloneRunner runner = new StandaloneRunner();
    private final List<? extends InputStream> streams;
    private int port;

    public JsonRunner(List<? extends InputStream> streams, int port) {
        this.streams = streams;
        this.port = port;
    }

    public void run() {
        runner.run(createHttpServer(streams, port));
    }

    public void stop() {
        runner.stop();
    }

    public void restart(InputStream is, int port) {
        HttpServer httpServer = createHttpServer(of(is), port);
        stop();
        run(httpServer);
    }

    private void run(HttpServer httpServer) {
        runner.run(httpServer);
    }

    private HttpServer createHttpServer(List<? extends InputStream> streams, int port) {
        HttpServer server = createBaseHttpServer(streams, port);
        server.request(by(uri("/favicon.ico"))).response(content(pathResource("favicon.png")), header("Content-Type", "image/png"));
        return server;
    }

    private HttpServer createBaseHttpServer(List<? extends InputStream> streams, int port) {
        HttpServer server = new ActualHttpServer(port);

        for (InputStream stream : streams) {
            HttpServer parsedServer = httpServerParser.parseServer(stream, port);
            server = mergeServer(server, parsedServer);
        }

        return server;
    }

    private HttpServer mergeServer(HttpServer server, HttpServer parsedServer) {
        ActualHttpServer thisServer = (ActualHttpServer) server;
        return thisServer.mergeHttpServer((ActualHttpServer)parsedServer);
    }
}
