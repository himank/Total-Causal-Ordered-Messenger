package edu.buffalo.cse.cse486586.groupmessenger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Vector;
import java.util.concurrent.Executors;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class GroupMessengerActivity extends Activity {

	//private static final String TAG = MyContentProvider.class.getName();
	private ServerSocket serverSocket;
	private final int serverPort = 10000;
	private String ip = "10.0.2.2";
	private String portStr="";
	private Hashtable<Integer,String> msgWaitingQueue = new Hashtable<Integer,String>();        //Initially when messages are coming put into this hash table
	//private Queue<Message> sequencerWaitingQueue = new LinkedList<Message>();; //
	private Vector<Message> globalEvent = new Vector<Message>();
	//private Queue<Message> processQueue = new LinkedList<Message>();
	private PriorityQueue<OrderQueue> orderQueue;			 // When displaying message check on this priority Queue which is prioritised (ascending) based on new Sequence number
	private int groupSequenceNumber = 0;					 // At Each process message received	
	private int overallSequenceNumber = 0;                   // Used by Sequencer to allocated new sequence
	private int counter = 0;								 // At each process concatenate with the port number & increment this
	//private int[] sequencerArray = {55540,55560,55580};	// Initial value of vector that I will use in Sequencer Class to maintain fifo ordering
	Handler handler = new Handler();
	private int conn [] = {11108,11112, 11116};
	private int vectorClock [] = {0, 0, 0};
	private int vectorClockProcess [] = {0, 0, 0};
	private int currIndex;
	private String currAvd;
	private int testcase1_num = 0;
	private int testcase2_num = 0;
	private ContentResolver contentResolver;
	private static final String KEY_FIELD = "key";
	private static final String VALUE_FIELD = "value";

	Comparator<OrderQueue> comparator = new NewIdComparator();
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messenger);
        TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());
        initialise();
        Thread conn = new Thread(new ServerThread());
		conn.start();
		contentResolver = getContentResolver();
		findViewById(R.id.button1).setOnClickListener(new OnPTestClickListener(tv, getContentResolver()));
	}
	
	// Initialise the variables.
	public void initialise() {
		TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
		portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
		orderQueue =  new PriorityQueue<OrderQueue>(10,comparator);
		if (portStr.equals("5554")) {
			currIndex = 0;
			currAvd = "avd0";
		} else if (portStr.equals("5556")) {
			currIndex = 1;
			currAvd = "avd1";
		} else if (portStr.equals("5558")) {
			currIndex = 2;
			currAvd = "avd2";
		}
	}
    
	// This thread responsible for receiving messages.
	
    public class ServerThread implements Runnable {
		public void run(){
			try {
				serverSocket = new ServerSocket(serverPort);
				while(true) {
					Socket incoming = serverSocket.accept();
					Executors.newSingleThreadExecutor().execute(new ServingClient(incoming));
					
				}
			
			} catch(IOException e) {
					e.printStackTrace();
			}
		}
		
	}
	
	// This thread responsible for creating the object and sending to the socket.
	
    public class ServingClient implements Runnable {
    	Socket incoming = null;
    	ServingClient(Socket s) {
    		incoming = s;
    	}
		public void run(){
			
			try {
				ObjectInputStream in = new ObjectInputStream(incoming.getInputStream()); 
				Message receive = (Message)in.readObject();  
				Executors.newSingleThreadExecutor().execute(new ProcessMessageQueue(receive));
				Log.e("Message id", Integer.toString(receive.msgId));
				in.close();  
				
			} catch(Exception e) {
				e.printStackTrace();
			} finally {
				try {
					incoming.close();
				} catch(IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_group_messenger, menu);
        return true;
    }
    
    public void testCase1(View view) {
    	
    	Thread testcase1 = new Thread(new TestCase1());
    	testcase1.start();
    	
    }
    
    public void testCase2(View view) {
    	
    	String id = (portStr) + Integer.toString(counter);
		++vectorClock[currIndex];
    	Message m = new Message(Integer.parseInt(id), vectorClock, "message", currAvd + ":" + testcase2_num, portStr, true);
		++counter;
		++testcase2_num;
    	for (int i = 0; i < 3; ++i)
			new Thread(new ClientThread(m, conn[i])).start();
		
    }
    
    public void sendMessage(View view) {
		
		EditText editText = (EditText)findViewById(R.id.editText1);
		String msg = editText.getText().toString();
		editText.setText("");
		String id = (portStr) + Integer.toString(counter);
		++vectorClock[currIndex];
		Message m = new Message(Integer.parseInt(id),vectorClock,"message",msg,portStr,false);
		++counter;
		// Create 3 client thread for each process & send message in parallel
		for (int i = 0; i < 3; ++i)
			new Thread(new ClientThread(m, conn[i])).start();
		
		
		
	}
		public class ClientThread implements Runnable {
			Message message;
			Socket outgoing = null;
			int portno;
					
			ClientThread(Message msg, int port) {
				message = msg;
				portno = port;
				
			}
			public void run() {
				try {
					outgoing = new Socket(ip,portno);
					ObjectOutputStream out = new ObjectOutputStream(outgoing.getOutputStream());  
					out.writeObject(message);  
					out.close();
				} catch(IOException e) {
					e.printStackTrace();
				} finally {
					try {
						
						outgoing.close();
					} catch(IOException e) {
						e.printStackTrace();
					}
				}
				
			}
		}
	
	
	/*
	 * This class will process the message & check the message
	 * If the message is from sequencer then match it with process group id
	 * else put the message into waiting queue
	*/
	public class ProcessMessageQueue implements Runnable {
		Message msgRecv;
		ProcessMessageQueue(Message m) {
			msgRecv = m;
		}
		public void run(){
			processMessage(msgRecv);
			processTestCase2(msgRecv);
			
		}
		public void processMessage(Message msgRecv) {
			if((msgRecv.msgType).equals("Order")) {							// First put the order message in queue & then check to avoid simultaneous action of process thread & Sequencer thread
				OrderQueue o = new OrderQueue(msgRecv.msgId, msgRecv.newId);
				orderQueue.add(o);
				
			} else {
				msgWaitingQueue.put(msgRecv.msgId,msgRecv.message);
				Log.e("vector No", Integer.toString(msgRecv.vecClock[0]) + ":" + Integer.toString(msgRecv.vecClock[1]) + ":" + Integer.toString(msgRecv.vecClock[2]));
				if (portStr.equalsIgnoreCase("5556")) {
					boolean flag = false;
					if ((msgRecv.vecClock[msgRecv.v_id] == vectorClockProcess[msgRecv.v_id] + 1)) // && (msgRecv.vecClock[i] <= vectorClockProcess[i])) {
							flag = true;
					else
							flag = false;
					if (flag) {
						vectorClockProcess[msgRecv.v_id] = Math.max(vectorClockProcess[msgRecv.v_id], msgRecv.vecClock[msgRecv.v_id]);
						Executors.newSingleThreadExecutor().execute(new Sequencer(msgRecv));
					} else {
						globalEvent.add(msgRecv);
					}
					if (globalEvent.size() > 0) {
						Iterator<Message> it = globalEvent.iterator();
						while (it.hasNext()) {
							Message first = it.next();
							boolean f = false;
							if (first.vecClock[first.v_id] == vectorClockProcess[first.v_id] + 1) //&& (first.vecClock[i] <= vectorClockProcess[i])) {
								f = true;
							else 
							 	f = false;
							if (f) {
								vectorClockProcess[first.v_id] = Math.max(vectorClockProcess[first.v_id], first.vecClock[first.v_id]);
								Executors.newSingleThreadExecutor().execute(new Sequencer(first));
								it.remove();
							} 
						}
					}
				} else {
					for (int i = 0; i <=2; ++i) {
						vectorClock[i] = Math.max(vectorClock[i], msgRecv.vecClock[i]);
					}
					
				}
				
								
			}
			while (orderQueue.size() != 0) {
				OrderQueue front = orderQueue.peek();
				if (front.newId == groupSequenceNumber && msgWaitingQueue.containsKey(front.oldId)) {
						Log.e("Inside msg waitig group",Integer.toString(groupSequenceNumber));
						final String msg = msgWaitingQueue.get(front.oldId);		// split[1] here contains msg key
						msgWaitingQueue.remove(front.oldId);						// Remove this and insert same msg with new key sent by sequencer
						groupSequenceNumber += 1;									// Increment group Sequencer Number because msg is accepted and displayed
						orderQueue.poll();
						ContentValues cv = new ContentValues();
						cv.put(KEY_FIELD, Integer.toString(front.newId));
			            cv.put(VALUE_FIELD, msg);
			            contentResolver.insert(MyContentProvider.contentURI, cv);
			            handler.post(new Runnable() {
							public void run() {
								
								TextView textview = (TextView)findViewById(R.id.textView1);
								String mydate = java.text.DateFormat.getTimeInstance().format(Calendar.getInstance().getTime());
								textview.append("\n"+msg+"\n\t\t\t\t\t\t\t"+mydate);
							}
						});
										
				} else {
					break;
				}
				
			}
		}
		/*
		 * Below Implementation responsible for Test Case 2
		 */
		public void processTestCase2(Message msgRecv) {
			if (msgRecv.first) {
				int j = 1;
				while (j < 3) {
					String id = (portStr) + Integer.toString(counter);
					++vectorClock[currIndex];
					Message m = new Message(Integer.parseInt(id), vectorClock, "message", currAvd + ":" + Integer.toString(testcase2_num), portStr, false);
					++counter;
					++testcase2_num;
					for (int i = 0; i < 3; ++i)
						new Thread(new ClientThread(m, conn[i])).start();
						
					++j;
				}
			}

		}
		
	}
	
	/*
	 * This class work as a Sequencer & follow FIFO ordering(array is used for each index denotes a process) 
 	 * when messages are coming for sequences
	*/
	public class Sequencer implements Runnable {
		Message msgRecv;
		Sequencer(Message m) {
			msgRecv = m;
		}
		public void run() {
			processSequencer(msgRecv);
			
		}
		public void processSequencer(Message msgRecv) {
			Message m = new Message(msgRecv.msgId,overallSequenceNumber, "Order",portStr);
			overallSequenceNumber += 1;
			for (int i = 0; i < 3; ++i)
				new Thread(new ClientThread(m, conn[i])).start();
						
		}
	}
	/*
	 * Test Case 1 class: Responsible for Multicasting 5 message with a sleep time of 3 seconds
	 */
	public class TestCase1 implements Runnable {
		
		public void run() {
			
			for (int j = 0; j < 5; ++j) {
	    		String msg = currAvd + ":" + Integer.toString(testcase1_num);
	    		String id = (portStr) + Integer.toString(counter);
	    		++vectorClock[currIndex];
	    		Message m = new Message(Integer.parseInt(id),vectorClock,"message",msg,portStr,false);
				++counter;
				++testcase1_num;
	    		for (int i = 0; i < 3; ++i)
					new Thread(new ClientThread(m, conn[i])).start();
				try {
	    		    Thread.sleep(3000);
	    		} catch(InterruptedException ex) {
	    		    Thread.currentThread().interrupt();
	    		}
	    	}
		}
	}
	/*
	 * OrderQueue Class is used to create a priority queue
	 * used to store order message coming from sequencer
	 * Below this class is comparator defined for the queue
	 */
	public class OrderQueue {			
	   int oldId, newId;
	   OrderQueue(int old, int n) {
		   oldId = old;
		   newId = n;
	   }
	}
	public class NewIdComparator implements Comparator<OrderQueue> {
	    @Override
	    public int compare(OrderQueue x, OrderQueue y) {
	        // Assume neither string is null. Real code should
	        // probably be more robust
	        if (x.newId > y.newId)
	            return 1;
	        if (x.newId < y.newId)
	            return -1;
	       return 0;
	    }
	}
	
	
	
}

	
