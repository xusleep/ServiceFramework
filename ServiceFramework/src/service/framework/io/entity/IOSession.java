package service.framework.io.entity;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * <p>
 * Title: 客户端请求信息类
 * </p>
 * 
 * @author starboy
 * @version 1.0
 */

public class IOSession {
	private SocketChannel sc;
	private byte[] dataInput = null;;
	Object obj;
	private int registerInterestOps = SelectionKey.OP_READ;

	public IOSession() {
	}

	public IOSession(SocketChannel sc) {
		this.sc = sc;
	}

	public SocketChannel getSc() {
		return sc;
	}

	public void setSc(SocketChannel sc) {
		this.sc = sc;
	}

	public void attach(Object obj) {
		this.obj = obj;
	}

	public Object attachment() {
		return obj;
	}

	public byte[] getDataInput() {
		return dataInput;
	}

	public void setDataInput(byte[] dataInput) {
		this.dataInput = dataInput;
	}

	public int getRegisterInterestOps() {
		return registerInterestOps;
	}

	public void setRegisterInterestOps(int registerInterestOps) {
		this.registerInterestOps = registerInterestOps;
	}

}
