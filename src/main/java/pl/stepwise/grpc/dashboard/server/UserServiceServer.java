package pl.stepwise.grpc.dashboard.server;

import java.io.File;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptors;
import io.grpc.ServerServiceDefinition;

import static pl.stepwise.grpc.dashboard.common.FileSupport.getFileFromClassPath;

/**
 * Created by rafal on 6/2/17.
 */
public class UserServiceServer {

    private Server server;

    public static void main(String[] args) {
        try {
            UserServiceServer serviceServer = new UserServiceServer();
            serviceServer.start();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e);
        }
    }

    private void start() throws Exception {
        final int port = 9000;

        File cert = getFileFromClassPath("certs/cert.pem");
        File key = getFileFromClassPath("certs/key.pem");

        UserService UserService = new UserService();
        ServerServiceDefinition serviceDefinition =
                ServerInterceptors.interceptForward(UserService, new MetadataServerInterceptor());

        server = ServerBuilder
                .forPort(port)
                .useTransportSecurity(cert, key)
                .addService(serviceDefinition)
                .build()
                .start();
        System.out.println("Listening on port " + port);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down server");
            UserServiceServer.this.stop();
        }));

        server.awaitTermination();
    }

    private void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

}
