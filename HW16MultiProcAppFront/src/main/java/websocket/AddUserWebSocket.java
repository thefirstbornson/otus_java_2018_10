package websocket;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import messagesystem.MsgJsonDBMethodWrapper;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import serversocket.ClientSocketMsgWorker;
import serversocket.SocketMsgWorker;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebSocket
public class AddUserWebSocket    {
    private static final Logger logger = Logger.getLogger(GetUserWebSocket.class.getName());
    private Session session;
    SocketMsgWorker socketAddUser;
    public static final String HOST = "localhost";
    public static final int SOCKET_PORT = 5050;

    public AddUserWebSocket() {
        try {
            startAddUserSocket();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void startAddUserSocket () throws IOException {
        socketAddUser = new ClientSocketMsgWorker(HOST, SOCKET_PORT);
        socketAddUser.init();
        logger.log(Level.INFO, "Front socket " +socketAddUser);

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> {
            try {
                while (true) {
                    final String result = socketAddUser.take();
                    JsonObject object = new JsonParser().parse(result).getAsJsonObject();
                    String userJson = object.get("message").getAsString();
                    sendResult(userJson);
                    logger.log(Level.INFO, "Message handled: " + userJson);
                }
            } catch (InterruptedException e) {
                logger.log(Level.SEVERE, e.getMessage());
            }
        });
    }


    @OnWebSocketMessage
    public void handleRequest( String user) {
        System.out.println("server get from front: " + user);
        Gson gson = new Gson();
        MsgJsonDBMethodWrapper msgJsnWrp = new MsgJsonDBMethodWrapper("save"
                                                                        ,new String[]{user}
                                                                        ,new String[]{"datasets.UserDataSet"});
        String jsonmsg = gson.toJson(msgJsnWrp);
        socketAddUser.send(jsonmsg);
    }


    public <T> void sendResult(T user) {
        String resultMsg;
        try {
            resultMsg = user.equals("null")?"user saved":"user has not saved";
            session.getRemote().sendString(resultMsg);
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.log(Level.INFO, "server send to front: "+user);
    }



    @OnWebSocketConnect
    public void onConnect(Session session) throws IOException {
        logger.log(Level.INFO, session.getRemoteAddress().getHostString() + " connected!");
        setSession(session);
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    @OnWebSocketClose
    public void onClose(Session session, int status, String reason) {
        logger.log(Level.INFO, session.getRemoteAddress().getHostString() + " closed!");
    }



}
