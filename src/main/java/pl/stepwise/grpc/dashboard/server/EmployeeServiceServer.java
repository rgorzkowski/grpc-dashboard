package pl.stepwise.grpc.dashboard.server;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Paths;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptors;
import io.grpc.ServerServiceDefinition;

/**
 * Created by rafal on 6/2/17.
 */
public class EmployeeServiceServer {

    private Server server;

    public static void main(String[] args) {
        try {
            EmployeeServiceServer serviceServer = new EmployeeServiceServer();
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

        EmployeeService employeeService = new EmployeeService();
        ServerServiceDefinition serviceDefinition =
                ServerInterceptors.interceptForward(employeeService, new MetadataServerInterceptor());

        server = ServerBuilder.forPort(port)
                .useTransportSecurity(cert, key)
                .addService(serviceDefinition)
                .build()
                .start();
        System.out.println("Listening on port " + port);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down server");
            EmployeeServiceServer.this.stop();
        }));

        server.awaitTermination();
    }

    private void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    private File getFileFromClassPath(String path) throws URISyntaxException {
        return Paths.get(getClass().getClassLoader().getResource(path).toURI())
                .toFile();
    }

}
