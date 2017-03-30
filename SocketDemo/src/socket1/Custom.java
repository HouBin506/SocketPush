package socket1;

/**
 * Created by HouBin on 2017/3/14.
 */

public class Custom {
	// 消息类型
	public static final int MESSAGE_ACTIVE = 0;// 心跳包
	public static final int MESSAGE_EVENT = 1;// 事件包
	public static final int MESSAGE_CLOSE = 3;// 断开连接

	// 定义客户端和服务器端的称呼
	public static final String NAME_SERVER = "服务器";
	public static final String NAME_CLIENT = "客户端";

	// 定义服务器的ip和端口号
	public static final String SERVER_HOST = "10.10.117.24";
	public static final int SERVER_PORT = 9001;

	public static final int SOCKET_CONNECT_TIMEOUT = 6;// 设置Socket连接超时为6秒
	public static final int SOCKET_ACTIVE_TIME = 60;// 发送心跳包的时间间隔为60秒需要与客户端一致

	public static final String ACTION_SOCKET_MESSSAGE = "com.herenit.socketmessage";
}
