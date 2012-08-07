package server;

import java.nio.channels.SocketChannel;
/**
 * This code is taken from http://rox-xmlrpc.sourceforge.net/niotut/
 * which is without a copyright notice.
 */
class ServerDataEvent {
	public NetworkSelector server;
	public SocketChannel socket;
	public byte[] data;
	
	public ServerDataEvent(NetworkSelector server, SocketChannel socket, byte[] data) {
		this.server = server;
		this.socket = socket;
		this.data = data;
	}
}