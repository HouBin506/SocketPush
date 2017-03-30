package socket1;

import java.io.Serializable;

/**
 * Created by HouBin on 2017/3/14.
 * 模拟数据模型，用于客户端与服务端的传输
 */
public class SocketMessage implements Serializable {
    private static final long serialVersionUID = 6420255745200652880L;
    private int type;
    private String message;
    private String from;
    private String to;
    private String userId;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "SocketMessage{" +
                "type=" + type +
                ", message='" + message + '\'' +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", userId='" + userId + '\'' +
                '}';
    }
}
