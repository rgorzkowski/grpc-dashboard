package pl.stepwise.grpc.dashboard.server;

import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.ServerInterceptors;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import pl.stepwise.grpc.dashboard.messages.EmployeeServiceGrpc;
import pl.stepwise.grpc.dashboard.messages.Messages;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

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
    public void shouldGetUserByLogin() throws Exception {
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
}