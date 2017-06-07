package pl.stepwise.grpc.dashboard.client;

import java.util.concurrent.TimeUnit;

import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.StatusRuntimeException;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.MetadataUtils;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import pl.stepwise.grpc.dashboard.messages.EmployeeServiceGrpc;
import pl.stepwise.grpc.dashboard.messages.Messages;

import static pl.stepwise.grpc.dashboard.common.FileSupport.getFileFromClassPath;

/**
 * Created by rafal on 6/7/17.
 */
public class EmployeeServiceClient extends Application {

    EmployeeServiceGrpc.EmployeeServiceBlockingStub blockingClient;

    EmployeeServiceGrpc.EmployeeServiceStub asyncClient;

    public EmployeeServiceClient() throws Exception {
        initializeConnections();
    }

    public static void main(String[] args) throws Exception {
        launch(args);
    }

    private void initializeConnections() throws Exception {
        ManagedChannel channel = NettyChannelBuilder
                .forAddress("localhost", 9000)
                .sslContext(
                        GrpcSslContexts.forClient()
                                .trustManager(getFileFromClassPath("certs/cert.pem"))
                                .build()
                ).build();

        blockingClient = EmployeeServiceGrpc.newBlockingStub(channel);

        asyncClient = EmployeeServiceGrpc.newStub(channel);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                System.out.println("Closing channel");
                TimeUnit.MICROSECONDS.sleep(500);
                channel.shutdown();
                channel.awaitTermination(2, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }));
    }

    @Override
    public void start(Stage primaryStage) {
        GridPane grid = createGrid();

        Button btn = createButton("Send metadata");
        btn.setOnAction(event -> sendMetadata());

        Button btn2 = createButton("222");
        btn2.setOnAction(event -> System.out.println("Hello World!"));

        grid.add(btn, 0, 1);
        grid.add(btn2, 0, 2);

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        root.setCenter(grid);

        Scene scene = new Scene(root, 365, 300);

        primaryStage.setTitle("grpc Client");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void sendMetadata() {
        Metadata metadata = new Metadata();
        metadata.put(Metadata.Key.of("username", Metadata.ASCII_STRING_MARSHALLER), "rgorzkowski");
        metadata.put(Metadata.Key.of("password", Metadata.ASCII_STRING_MARSHALLER), "password");
        try {
            Messages.EmployeeResponse response = blockingClient
                    .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata))
                    .getByLogin(Messages.GetByLoginRequest.newBuilder().setLogin("testTest1234").build());
        } catch (StatusRuntimeException e) {
            System.out.println(e.getStatus());
            Metadata trailers = e.getTrailers();
            trailers.keys().forEach(key ->
                    System.out.println(
                            key + " -> " + trailers.get(Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER))));
        }
    }

    private GridPane createGrid() {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(5);
        grid.setVgap(5);
        grid.setPadding(new Insets(10));
        return grid;
    }

    private Button createButton(String text) {
        Button button = new Button(text);
        button.setMaxSize(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        GridPane.setFillHeight(button, true);
        GridPane.setFillWidth(button, true);
        GridPane.setHgrow(button, Priority.ALWAYS);
        GridPane.setVgrow(button, Priority.ALWAYS);
        return button;
    }
}
