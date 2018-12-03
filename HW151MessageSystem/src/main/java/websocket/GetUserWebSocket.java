package websocket;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import datasets.UserDataSet;
import gsonconverters.UserDataSetConverter;
import messagesystem.Address;
import messagesystem.MessageSystem;
import messagesystem.message.Message;
import messagesystem.message.MessageSystemContext;
import messagesystem.message.messageimpl.MsgGetUserId;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

@WebSocket
public class GetUserWebSocket implements FrontendService {
    private Session session;
    private Address address;
    private MessageSystemContext context;

    public GetUserWebSocket(MessageSystemContext context, Address address) {
        this.context = context;
        this.address = address;
        init();
    }

    public GetUserWebSocket() {
    }

    @OnWebSocketConnect
    public void onConnect(Session session) throws IOException {
        System.out.println(session.getRemoteAddress().getHostString() + " connected!");
        setSession(session);
    }

    @OnWebSocketClose
    public void onClose(Session session, int status, String reason) {
        System.out.println(session.getRemoteAddress().getHostString() + " closed!");
    }

    @OnWebSocketMessage
    @Override
    public void handleRequest(String data) {
        System.out.println("server get from site: " + data);

        JsonObject jsonObject = new JsonParser().parse(data).getAsJsonObject();
        String value= jsonObject.get("id").getAsString();
        Message message = new MsgGetUserId(getAddress(), context.getDbAddress(), value);
        context.getMessageSystem().sendMessage(message);
        System.out.println("server send to MS: " + value);
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    @Override
    public void init() {
        context.getMessageSystem().addAddressee(this);
        Thread thread = new Thread(() -> {
            LinkedBlockingQueue<Message> queue = context.getMessageSystem().getMessagesMap().get(this.getAddress());
            while (true) {
                try {
                    Message message = queue.take();
                    System.out.println("queue --" + message.getFrom().toString() + "-" +message.getTo().toString());
                    message.exec(this);
                } catch (InterruptedException e) {
                   // logger.log(Level.INFO, "Thread interrupted. Finishing: " + name);
                    return;
                }
            }
        });
        thread.start();

    }

    @Override
    public <T> void sendResult(T user) {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(UserDataSet.class, new UserDataSetConverter());
        Gson gson = builder.create();

        String value = user!=null?gson.toJson(user):gson.toJson("not found");
        try {
            session.getRemote().sendString(value);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("server send to site: "+value);
    }

    @Override
    public Address getAddress() {
        return address;
    }

    @Override
    public MessageSystem getMS() {
        return context.getMessageSystem();
    }
}
