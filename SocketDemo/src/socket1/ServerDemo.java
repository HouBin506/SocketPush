package socket1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.plaf.TextUI;
import javax.xml.soap.Text;

import org.json.JSONObject;
import org.omg.CORBA.portable.CustomValue;

public class ServerDemo {
	private int count = 0;
	private boolean isStartServer = false;
	private ArrayList<SocketThread> mThreadList = new ArrayList<SocketThread>();

	public static void main(String[] args) throws IOException {

		ServerDemo server = new ServerDemo();
		server.start();
	}

	/**
	 * 开启服务端的Socket
	 * @throws IOException
	 */
	public void start() throws IOException {
		// 启动服务ServerSocket，设置端口号
		ServerSocket ss = new ServerSocket(9001);
		System.out.println("服务端已开启，等待客户端连接:");
		isStartServer = true;
		int socketID = 0;
		Socket socket = null;
		startMessageThread();
		while (isStartServer) {
			// 此处是一个阻塞方法，当有客户端连接时，就会调用此方法
			socket = ss.accept();
			System.out.println("客户端连接成功" + socket.getInetAddress());
			// 4. 为这个客户端的Socket数据连接
			SocketThread thread = new SocketThread(socket, socketID++);
			thread.start();
			mThreadList.add(thread);
		}
	}

	private void startMessageThread() {
		new Timer().schedule(new TimerTask() {

			@Override
			public void run() {
				try {
					for (SocketThread st : mThreadList) {// 分别向每个客户端发送消息
						if (st.socket == null || st.socket.isClosed())
							continue;
						System.out.println("客户端的userId：" + st.userId + "  消息编号：" + count);
						if (st.userId == null || "".equals(st.userId))// 如果暂时没有确定Socket对应的用户Id先不发
							continue;
						String content = "我是从服务器发来的消息：" + count;
						
						// 根据userId模拟服务端向不同的客户端推送消息
						if (count % 2 == 0) {
							if (st.userId.equals("002"))
								content = "我是从服务器发发送给用户002的消息：" + count;
							else
								continue;
						} else {
							if (st.userId.equals("001"))
								content = "我是从服务器发发送给用户001的消息：" + count;
							else
								continue;
						}
						SocketMessage message = new SocketMessage();
						message.setFrom(Custom.NAME_SERVER);
						message.setTo(Custom.NAME_CLIENT);
						message.setMessage(content);
						message.setType(Custom.MESSAGE_EVENT);
						message.setUserId(st.userId);
						BufferedWriter writer = st.writer;
						String jMessage = Util.initJsonObject(message).toString() + "\n";
						writer.write(jMessage);
						writer.flush();
						System.out.println("向客户端" + st.socket.getInetAddress() + "发送了消息：" + content);
					}
					count++;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}, 0, 1000 * 30);//此处设置定时器目的是模仿服务端向客户端推送消息，假定每隔30秒推送一条消息

	}

	/**
	 * 关闭与SocketThread所代表的客户端的连接
	 * @param socketThread要关闭的客户端
	 * @throws IOException
	 */
	private void closeSocketClient(SocketThread socketThread) throws IOException {
		if (socketThread.socket != null && !socketThread.socket.isClosed()) {
			if (socketThread.reader != null)
				socketThread.reader.close();
			if (socketThread.writer != null)
				socketThread.writer.close();
			socketThread.socket.close();
		}
		mThreadList.remove(socketThread);
		socketThread = null;
	}

	/**
	 * 客户端Socket线程，
	 * @author 华硕
	 *
	 */
	public class SocketThread extends Thread {

		public int socketID;
		public Socket socket;//客户端的Socket
		public BufferedWriter writer;
		public BufferedReader reader;
		public String userId;//客户端的UserId
		private long lastTime;

		public SocketThread(Socket socket, int count) {
			socketID = count;
			this.socket = socket;
			lastTime = System.currentTimeMillis();
		}

		@Override
		public void run() {
			super.run();

			try {
				reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
				//循环监控读取客户端发来的消息
				while (isStartServer) {
					// 超出了发送心跳包规定时间，说明客户端已经断开连接了这时候要断开与该客户端的连接
					long interval = System.currentTimeMillis() - lastTime;
					if (interval >= (Custom.SOCKET_ACTIVE_TIME * 1000 * 4)) {
						System.out.println("客户端发包间隔时间严重延迟，可能已经断开了interval：" + interval);
						System.out.println("Custom.SOCKET_ACTIVE_TIME * 1000:" + Custom.SOCKET_ACTIVE_TIME * 1000);
						closeSocketClient(this);
						break;
					}
					if (reader.ready()) {
						lastTime = System.currentTimeMillis();
						System.out.println("收到消息，准备解析:");
						String data = reader.readLine();
						System.out.println("解析成功:" + data);
						SocketMessage from = Util.parseJson(data);
						//给UserID赋值，此处是我们项目的需求，根据客户端不同的UserId来分别进行推送
						if (userId == null || "".equals(userId))
							userId = from.getUserId();
						SocketMessage to = new SocketMessage();
						if (from.getType() == Custom.MESSAGE_ACTIVE) {//心跳包
							System.out.println("收到心跳包：" + socket.getInetAddress());
							to.setType(Custom.MESSAGE_ACTIVE);
							to.setFrom(Custom.NAME_SERVER);
							to.setTo(Custom.NAME_CLIENT);
							to.setMessage("");
							to.setUserId(userId);
							writer.write(Util.initJsonObject(to).toString() + "\n");
							writer.flush();
						} else if (from.getType() == Custom.MESSAGE_CLOSE) {//关闭包
							System.out.println("收到断开连接的包：" + socket.getInetAddress());
							to.setType(Custom.MESSAGE_CLOSE);
							to.setFrom(Custom.NAME_SERVER);
							to.setTo(Custom.NAME_CLIENT);
							to.setMessage("");
							to.setUserId(userId);
							writer.write(Util.initJsonObject(to).toString() + "\n");
							writer.flush();
							closeSocketClient(this);
							break;
						} else if (from.getType() == Custom.MESSAGE_EVENT) {//事件包，客户端可以向服务端发送自定义消息
							System.out.println("收到普通消息包：" + from.getMessage());
						}
					}
					Thread.sleep(100);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}
}
