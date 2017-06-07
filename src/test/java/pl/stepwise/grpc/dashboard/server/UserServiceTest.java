package pl.stepwise.grpc.dashboard.server;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.ServerInterceptors;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.StreamRecorder;
import pl.stepwise.grpc.dashboard.messages.Messages;
import pl.stepwise.grpc.dashboard.messages.Messages.UploadPhotoRequest;
import pl.stepwise.grpc.dashboard.messages.Messages.UploadPhotoResponse;
import pl.stepwise.grpc.dashboard.messages.Messages.User;
import pl.stepwise.grpc.dashboard.messages.Messages.UserRequest;
import pl.stepwise.grpc.dashboard.messages.Messages.UserResponse;
import pl.stepwise.grpc.dashboard.messages.UserServiceGrpc;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import com.google.common.io.ByteStreams;
import com.google.protobuf.ByteString;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.newArrayList;
import static pl.stepwise.grpc.dashboard.common.FileSupport.split;

@RunWith(JUnit4.class)
public class UserServiceTest {

    private static final String UNIQUE_SERVER_NAME = "in-process server for " + UserServiceTest.class;

    private final Server inProcessServer = InProcessServerBuilder
            .forName(UNIQUE_SERVER_NAME).addService(
                    ServerInterceptors.interceptForward(new UserService(), new MetadataServerInterceptor())
            ).directExecutor().build();

    private final ManagedChannel inProcessChannel =
            InProcessChannelBuilder.forName(UNIQUE_SERVER_NAME).directExecutor().build();

    /**
     * Creates and starts the server with the {@link InProcessServerBuilder},
     * and creates an in-process channel with the {@link InProcessChannelBuilder}.
     */
    @Before
    public void setUp() throws Exception {
        inProcessServer.start();
    }

    /**
     * Shuts down the in-process channel and server.
     */
    @After
    public void tearDown() {
        inProcessChannel.shutdownNow();
        inProcessServer.shutdownNow();
    }

    /**
     * To test the server, make calls with a real stub using the in-process channel, and verify
     * behaviors or state changes from the client side.
     */
    @Test
    public void shouldGetUserByLogin() throws Exception {
        //given
        UserServiceGrpc.UserServiceBlockingStub blockingStub = UserServiceGrpc
                .newBlockingStub(inProcessChannel);

        //when
        UserResponse response = blockingStub
                .getByLogin(Messages.GetByLoginRequest.newBuilder().setLogin("rgorzkowski").build());

        User User = response.getUser();

        //then
        assertThat(User.getFirstName()).isEqualTo("Rafal");
        assertThat(User.getLastName()).isEqualTo("Gorzkowski");
    }

    /**
     * To test the server, make calls with a real stub using the in-process channel, and verify
     * behaviors or state changes from the client side.
     */
    @Test
    public void shouldGetAllEmplyees() throws Exception {
        //given
        UserServiceGrpc.UserServiceBlockingStub blockingStub = UserServiceGrpc
                .newBlockingStub(inProcessChannel);

        //when
        Iterator<UserResponse> all = blockingStub.getAll(Messages.GetAllRequest.newBuilder().build());

        //then
        assertThat(all).hasSize(3);
    }

    @Test
    public void shouldUploadPhoto() throws Exception {
        //given
        byte[] photoByteArray = ByteStreams.toByteArray(
                this.getClass().getClassLoader().getResourceAsStream("kuoka.jpg")
        );

        final List<UploadPhotoRequest> requests = split(photoByteArray, 10_000)
                .stream()
                .map(bytes ->
                        UploadPhotoRequest.newBuilder()
                                .setData(ByteString.copyFrom(bytes))
                                .build()
                ).collect(Collectors.toList());

        UserServiceGrpc.UserServiceStub stub = UserServiceGrpc.newStub(inProcessChannel);

        //when
        StreamRecorder<UploadPhotoResponse> responseObserver = StreamRecorder.create();
        StreamObserver<UploadPhotoRequest> requestObserver = stub.uploadPhoto(responseObserver);

        for (UploadPhotoRequest request : requests) {
            requestObserver.onNext(request);
        }
        requestObserver.onCompleted();

        //then
        UploadPhotoResponse response = responseObserver.firstValue().get();
        assertThat(response.getStatus()).isEqualTo("OK");
        assertThat(response.getBody().size()).isEqualTo(photoByteArray.length);
    }

    @Test
    public void shouldSaveAll() throws Exception {
        //given
        UserServiceGrpc.UserServiceStub stub = UserServiceGrpc.newStub(inProcessChannel);

        //when
        StreamRecorder<UserResponse> responseObserver = StreamRecorder.create();
        StreamObserver<UserRequest> requestObserver = stub.saveAll(responseObserver);

        List<UserRequest> requests = newArrayList(
                User.newBuilder()
                        .setFirstName("Jan")
                        .setLastName("Kowalik")
                        .build(),
                User.newBuilder()
                        .setFirstName("Marcin")
                        .setLastName("Opania")
                        .build()
        )
                .stream()
                .map(User -> UserRequest.newBuilder()
                        .setUser(User)
                        .build()
                )
                .collect(Collectors.toList());

        for (UserRequest request : requests) {
            requestObserver.onNext(request);
        }
        requestObserver.onCompleted();

        //then
        assertThat(responseObserver.getValues()).hasSize(2);
    }
}