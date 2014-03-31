package edu.buffalo.cse.cse486586.groupmessenger;

import java.io.Serializable;
import java.util.Arrays;

public class Message implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String msgType;
	String msgFrom;
	String message;
	int v_id;
	int vecClock[];
	int msgId;
	int newId;
	boolean first;

	Message(int mId, int vClock[], String mType, String msg, String portStr,
			boolean f) {
		msgId = mId;
		vecClock = Arrays.copyOf(vClock, vClock.length);
		msgType = mType;
		message = msg;
		first = f;
		msgFrom = portStr;
		if (portStr.equals("5554"))
			v_id = 0;
		else if (portStr.equals("5556"))
			v_id = 1;
		else if (portStr.equals("5558"))
			v_id = 2;
	}

	Message(int mId, int nId, String mType, String portStr) {
		msgId = mId;
		msgType = mType;
		newId = nId;
		msgFrom = portStr;
		if (portStr.equals("5554"))
			v_id = 0;
		else if (portStr.equals("5556"))
			v_id = 1;
		else if (portStr.equals("5555"))
			v_id = 2;
	}
}