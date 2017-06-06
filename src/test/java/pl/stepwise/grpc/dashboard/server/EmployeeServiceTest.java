package pl.stepwise.grpc.dashboard.server;

import java.util.ArrayList;
import java.util.Arrays;
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
import pl.stepwise.grpc.dashboard.messages.EmployeeServiceGrpc;
import pl.stepwise.grpc.dashboard.messages.Messages;
import pl.stepwise.grpc.dashboard.messages.Messages.UploadPhotoRequest;
import pl.stepwise.grpc.dashboard.messages.Messages.UploadPhotoResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import com.google.common.io.ByteStreams;
import com.google.protobuf.ByteString;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnit4.class)
public class EmployeeServiceTest {

    private static final String UNIQUE_SERVER_NAME = "in-process server for " + EmployeeServiceTest.class;

    private final Server inProcessServer = InProcessServerBuilder
            .forName(UNIQUE_SERVER_NAME).addService(
                    ServerInterceptors.interceptForward(new EmployeeService(), new MetadataServerInterceptor())
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
    public void shouldGetEmployeeByLogin() throws Exception {
        //given
        EmployeeServiceGrpc.EmployeeServiceBlockingStub blockingStub = EmployeeServiceGrpc
                .newBlockingStub(inProcessChannel);

        //when
        Messages.EmployeeResponse response = blockingStub
                .getByLogin(Messages.GetByLoginRequest.newBuilder().setLogin("rgorzkowski").build());

        Messages.Employee employee = response.getEmployee();

        //then
        assertThat(employee.getFirstName()).isEqualTo("Rafal");
        assertThat(employee.getLastName()).isEqualTo("Gorzkowski");
    }

    /**
     * To test the server, make calls with a real stub using the in-process channel, and verify
     * behaviors or state changes from the client side.
     */
    @Test
    public void shouldGetAllEmplyees() throws Exception {
        //given
        EmployeeServiceGrpc.EmployeeServiceBlockingStub blockingStub = EmployeeServiceGrpc
                .newBlockingStub(inProcessChannel);

        //when
        Iterator<Messages.EmployeeResponse> all = blockingStub.getAll(Messages.GetAllRequest.newBuilder().build());

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

        EmployeeServiceGrpc.EmployeeServiceStub stub = EmployeeServiceGrpc.newStub(inProcessChannel);

        //when
        StreamRecorder<UploadPhotoResponse> responseObserver = StreamRecorder.create();
        StreamObserver<UploadPhotoRequest> requestObserver = stub.uploadPhoto(responseObserver);

        for (UploadPhotoRequest request : requests) {
            requestObserver.onNext(request);
        }
        requestObserver.onCompleted();

        //then
        assertThat(responseObserver.firstValue().get().getStatus()).isEqualTo("OK");

    }

    private List<byte[]> split(byte[] photo, int blockSize) {
        List<byte[]> result = new ArrayList<>();
        int fullBlockCount = photo.length / blockSize;

        for (int i = 0; i < fullBlockCount; i++) {
            int startIdx = i * blockSize;
            result.add(Arrays.copyOfRange(photo, startIdx, startIdx + blockSize));
        }
        if (photo.length % blockSize > 0) {
            int startIdx = fullBlockCount * blockSize;
            result.add(Arrays.copyOfRange(photo, startIdx, startIdx + photo.length % blockSize));
        }
        return result;
    }
}