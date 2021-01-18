package io.swapastack.gomoku;

import com.google.gson.Gson;
import io.swapastack.gomoku.shared.TestMessage;

import java.net.URI;
import java.nio.ByteBuffer;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

/**
 * The SimpleClient extends the WebSocketClient class.
 * The SimpleClient class could be used to establish a WebSocket (ws, wss) connection
 * a WebSocketServer.
 *
 * The send(String message) method could be used to send a String message to the
 * WebSocketServer.
 *
 * note: this client could be used to implement the network standard document.
 *
 * @author Dennis Jehle
 */
public class SimpleClient extends WebSocketClient {

    // 'Google Gson is a java library that can be used to convert Java Object
    // into their JSON representation.'
    // see: https://github.com/google/gson
    // see: https://github.com/google/gson/blob/master/UserGuide.md#TOC-Serializing-and-Deserializing-Generic-Types
    private final Gson gson_;

    /**
     * This is the constructor of the SimpleClient class.
     *
     * @param server_uri {@link URI}
     * @author Dennis Jehle
     */
    public SimpleClient(URI server_uri) {
        super(server_uri);
        // create Gson converter
        gson_ = new Gson();
    }

    /**
     * This method is called if the connection to the WebSocketServer is open.
     *
     * @param handshake_data {@link ServerHandshake}
     * @author Dennis Jehle
     */
    @Override
    public void onOpen(ServerHandshake handshake_data) {
        // create new TestMassage Java object
        TestMessage message = new TestMessage("Mario", "Hello, it is me.");
        // create JSON String from TestMessage Java object
        String test_message = gson_.toJson(message);
        // send JSON encoded test message as String to the connected WebSocket server
        send(test_message);
        // 'debug' output
        System.out.println("new connection opened");
    }

    /**
     * This method is called if the connection to the WebSocketServer was closed.
     *
     * @param code status code
     * @param reason the reason for closing the connection
     * @param remote was the close initiated by the remote host
     * @author Dennis Jehle
     */
    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("closed with exit code " + code + " additional info: " + reason);
    }

    /**
     * This message is called if the WebSocketServer sends a String message to the client.
     *
     * @param message a String message from the WebSocketServer e.g. JSON message
     * @author Dennis Jehle
     */
    @Override
    public void onMessage(String message) {
        System.out.println("received message: " + message);
    }

    /**
     * This method is called if the WebSocketServer send a binary message to the client.
     * note: This method is not necessary for this project, because the network standard
     * document specifies a JSON String message protocol.
     *
     * @param message a binary message
     * @author Dennis Jehle
     */
    @Override
    public void onMessage(ByteBuffer message) {
        // do nothing, because binary messages are not supported
    }

    /**
     * This method is called if an exception was thrown.
     *
     * @param exception {@link Exception}
     * @author Dennis Jehle
     */
    @Override
    public void onError(Exception exception) {
        System.err.println("an error occurred:" + exception);
    }
}