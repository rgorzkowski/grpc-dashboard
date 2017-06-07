package pl.stepwise.grpc.dashboard.client;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import pl.stepwise.grpc.dashboard.messages.Messages;
import pl.stepwise.grpc.dashboard.messages.UserServiceGrpc;

import static pl.stepwise.grpc.dashboard.common.FileSupport.getFileFromClassPath;

/**
 * Created by rafal on 6/7/17.
 */
public class UserServiceClient extends Application {

    UserServiceGrpc.UserServiceBlockingStub blockingClient;

    UserServiceGrpc.UserServiceStub asyncClient;

    public UserServiceClient() throws Exception {
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

        blockingClient = UserServiceGrpc.newBlockingStub(channel);

        asyncClient = UserServiceGrpc.newStub(channel);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                System.out.println("Closing channel");
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

        Button btn2 = createButton("Send unary request");
        btn2.setOnAction(event -> {
            Messages.UserResponse response = sendUnaryRequest();
            StackPane secondaryLayout = new StackPane();
            secondaryLayout.getChildren().add(new Label(response.getUser().toString()));

            Stage secondStage = new Stage();
            secondStage.setTitle("Unary response");
            secondStage.setScene(new Scene(secondaryLayout, 200, 100));
            secondStage.show();
        });

        Button btn3 = createButton("Server side streaming");
        btn3.setOnAction(event -> {
            Iterator<Messages.UserResponse> allUsers = getAllUsers();
            String allUsersAsString = StreamSupport
                    .stream(Spliterators.spliteratorUnknownSize(allUsers, Spliterator.ORDERED), false)
                    .map(Messages.UserResponse::getUser)
                    .map(Object::toString)
                    .collect(Collectors.joining(System.lineSeparator()));

            StackPane secondaryLayout = new StackPane();
            secondaryLayout.getChildren().add(new Label(allUsersAsString));

            Stage secondStage = new Stage();
            secondStage.setTitle("Unary response");
            secondStage.setScene(new Scene(secondaryLayout, 400, 600));
            secondStage.show();
        });

        grid.add(btn, 0, 1);
        grid.add(btn2, 0, 2);
        grid.add(btn3, 0, 3);

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
            blockingClient
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

    private Messages.UserResponse sendUnaryRequest() {
        Messages.UserResponse response = blockingClient
                .getByLogin(Messages.GetByLoginRequest.newBuilder().setLogin("rgorzkowski").build());

        System.out.println(response.getUser());
        return response;
    }

    private Iterator<Messages.UserResponse> getAllUsers() {
        return blockingClient.getAll(Messages.GetAllRequest.newBuilder().build());
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
