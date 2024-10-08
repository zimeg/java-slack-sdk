package test_locally.jakarta_socket_mode;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.slack.api.Slack;
import com.slack.api.SlackConfig;
import com.slack.api.jakarta_socket_mode.JakartaSocketModeClientFactory;
import com.slack.api.socket_mode.SocketModeClient;
import com.slack.api.socket_mode.listener.WebSocketMessageListener;
import com.slack.api.socket_mode.response.AckResponse;
import com.slack.api.socket_mode.response.MessagePayload;
import com.slack.api.socket_mode.response.MessageResponse;
import com.slack.api.util.json.GsonFactory;
import com.slack.api.util.thread.DaemonThreadExecutorServiceProvider;
import com.slack.api.util.thread.ExecutorServiceProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import util.mock_server.MockWebApiServer;
import util.mock_server.MockWebSocketServer;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SocketModeClientTest {

    static final Gson GSON = GsonFactory.createSnakeCase();
    static final String VALID_APP_TOKEN = "xapp-valid-123123123123123123123123123123123123";

    MockWebApiServer webApiServer = new MockWebApiServer();
    MockWebSocketServer wsServer = new MockWebSocketServer();
    SlackConfig config = new SlackConfig();
    Slack slack = Slack.getInstance(config);

    @Before
    public void setup() throws Exception {
        webApiServer.start();
        wsServer.start();
        config = new SlackConfig();
        config.setMethodsEndpointUrlPrefix(webApiServer.getMethodsEndpointPrefix());
        slack = Slack.getInstance(config);
    }

    @After
    public void tearDown() throws Exception {
        webApiServer.stop();
        wsServer.stop();
    }

    // -------------------------------------------------
    // Default implementation
    // -------------------------------------------------

    @Test
    public void attributes() throws Exception {
        try (SocketModeClient client = JakartaSocketModeClientFactory.create(slack, VALID_APP_TOKEN)) {
            assertNotNull(client.getAppToken());
            assertNotNull(client.getMessageQueue());
            assertNotNull(client.getGson());
            assertNotNull(client.getLogger());
            assertNotNull(client.getSlack());
            assertNotNull(client.getWssUri());
        }
    }

    @Test
    public void connect() throws Exception {
        try (SocketModeClient client = JakartaSocketModeClientFactory.create(slack, VALID_APP_TOKEN)) {
            AtomicBoolean received = new AtomicBoolean(false);
            client.addWebSocketMessageListener(helloListener(received));
            client.addWebSocketErrorListener(error -> {
            });
            client.addWebSocketCloseListener((code, reason) -> {
            });
            client.addEventsApiEnvelopeListener(envelope -> {
            });
            client.addInteractiveEnvelopeListener(envelope -> {
            });
            client.addSlashCommandsEnvelopeListener(envelope -> {
            });

            client.connect();
            int counter = 0;
            while (!received.get() && counter < 20) {
                Thread.sleep(100L);
                counter++;
            }
            assertTrue(received.get());

            client.disconnect();
            client.runCloseListenersAndAutoReconnectAsNecessary(1000, null);

            client.removeWebSocketMessageListener(client.getWebSocketMessageListeners().get(0));
            client.removeWebSocketErrorListener(client.getWebSocketErrorListeners().get(0));
            client.removeWebSocketCloseListener(client.getWebSocketCloseListeners().get(0));

            client.removeEventsApiEnvelopeListener(client.getEventsApiEnvelopeListeners().get(0));
            client.removeInteractiveEnvelopeListener(client.getInteractiveEnvelopeListeners().get(0));
            client.removeSlashCommandsEnvelopeListener(client.getSlashCommandsEnvelopeListeners().get(0));
        }
    }

    @Test
    public void connect_with_custom_ExecutorService() throws Exception {
        final AtomicBoolean called = new AtomicBoolean(false);
        final AtomicBoolean called2 = new AtomicBoolean(false);
        config.setExecutorServiceProvider(new ExecutorServiceProvider() {
            @Override
            public ExecutorService createThreadPoolExecutor(String threadGroupName, int poolSize) {
                called.set(true);
                return DaemonThreadExecutorServiceProvider.getInstance()
                        .createThreadPoolExecutor(threadGroupName, poolSize);
            }

            @Override
            public ScheduledExecutorService createThreadScheduledExecutor(String threadGroupName) {
                called2.set(true);
                return DaemonThreadExecutorServiceProvider.getInstance()
                        .createThreadScheduledExecutor(threadGroupName);
            }
        });
        Slack slack = Slack.getInstance(config);
        try (SocketModeClient client = JakartaSocketModeClientFactory.create(slack, VALID_APP_TOKEN)) {
            AtomicBoolean received = new AtomicBoolean(false);
            client.addWebSocketMessageListener(helloListener(received));
            client.addWebSocketErrorListener(error -> {
            });
            client.addWebSocketCloseListener((code, reason) -> {
            });
            client.addEventsApiEnvelopeListener(envelope -> {
            });
            client.addInteractiveEnvelopeListener(envelope -> {
            });
            client.addSlashCommandsEnvelopeListener(envelope -> {
            });

            client.connect();
            int counter = 0;
            while (!received.get() && counter < 20) {
                Thread.sleep(100L);
                counter++;
            }
            assertTrue(received.get());

            client.disconnect();
            client.runCloseListenersAndAutoReconnectAsNecessary(1000, null);

            client.removeWebSocketMessageListener(client.getWebSocketMessageListeners().get(0));
            client.removeWebSocketErrorListener(client.getWebSocketErrorListeners().get(0));
            client.removeWebSocketCloseListener(client.getWebSocketCloseListeners().get(0));

            client.removeEventsApiEnvelopeListener(client.getEventsApiEnvelopeListeners().get(0));
            client.removeInteractiveEnvelopeListener(client.getInteractiveEnvelopeListeners().get(0));
            client.removeSlashCommandsEnvelopeListener(client.getSlashCommandsEnvelopeListeners().get(0));
        }
        assertTrue(called.get());
        assertTrue(called2.get());
    }

    @Test
    public void maintainCurrentSession() throws Exception {
        try (SocketModeClient client = JakartaSocketModeClientFactory.create(slack, VALID_APP_TOKEN)) {
            client.connect();
            client.maintainCurrentSession();
        }
    }

    @Test
    public void connectToNewEndpoint() throws Exception {
        try (SocketModeClient client = JakartaSocketModeClientFactory.create(slack, VALID_APP_TOKEN)) {
            AtomicBoolean received = new AtomicBoolean(false);
            client.addWebSocketMessageListener(helloListener(received));
            client.connect();
            client.disconnect();
            client.connectToNewEndpoint();
            int counter = 0;
            while (!received.get() && counter < 20) {
                Thread.sleep(100L);
                counter++;
            }
            assertTrue(received.get());
        }
    }

    @Test
    public void sendSocketModeResponse() throws Exception {
        try (SocketModeClient client = JakartaSocketModeClientFactory.create(slack, VALID_APP_TOKEN)) {
            AtomicBoolean received = new AtomicBoolean(false);
            client.addWebSocketMessageListener(helloListener(received));
            client.connect();
            int counter = 0;
            while (!received.get() && counter < 20) {
                Thread.sleep(100L);
                counter++;
            }
            assertTrue(received.get());
            client.sendSocketModeResponse(AckResponse.builder().envelopeId("xxx").build());
            client.sendSocketModeResponse(MessageResponse.builder()
                    .envelopeId("xxx")
                    .payload(MessagePayload.builder().text("Hi there!").build())
                    .build());
        }
    }

    @Test
    public void messageReceiver() throws Exception {
        try (SocketModeClient client = JakartaSocketModeClientFactory.create(slack, VALID_APP_TOKEN)) {
            AtomicBoolean helloReceived = new AtomicBoolean(false);
            AtomicBoolean received = new AtomicBoolean(false);
            client.addWebSocketMessageListener(helloListener(helloReceived));
            client.addWebSocketMessageListener(envelopeListener(received));
            client.connect();
            int counter = 0;
            while (!received.get() && counter < 50) {
                Thread.sleep(100L);
                counter++;
            }
            assertTrue(helloReceived.get());
            assertTrue(received.get());
        }
    }

    // -------------------------------------------------

    private static Optional<String> getEnvelopeType(String message) {
        JsonElement msg = GSON.fromJson(message, JsonElement.class);
        if (msg != null && msg.isJsonObject()) {
            JsonElement typeElem = msg.getAsJsonObject().get("type");
            if (typeElem != null && typeElem.isJsonPrimitive()) {
                return Optional.of(typeElem.getAsString());
            }
        }
        return Optional.empty();
    }

    private static WebSocketMessageListener helloListener(AtomicBoolean received) {
        return message -> {
            Optional<String> type = getEnvelopeType(message);
            if (type.isPresent()) {
                if (type.get().equals("hello")) {
                    received.set(true);
                }
            }
        };
    }

    private static List<String> MESSAGE_TYPES = Arrays.asList(
            "events",
            "interactive",
            "slash_commands"
    );

    private static WebSocketMessageListener envelopeListener(AtomicBoolean received) {
        return message -> {
            Optional<String> type = getEnvelopeType(message);
            if (type.isPresent()) {
                if (MESSAGE_TYPES.contains(type.get())) {
                    received.set(true);
                }
            }
        };
    }
}
