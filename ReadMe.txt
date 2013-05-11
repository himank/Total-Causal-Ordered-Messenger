   Totally and Causally Ordered Group Messenger with a Local Persistent Key-Value Table

This project support both total and causal ordering. Any application(avd) will multicast message using B-mulicast and that message should be totally & causally ordered. Moreover, the messages are stored as <key-value> in the local provider of each avd.

In this particular implementation, One of the avd(process) is acting as a sequencer responsible for global ordering. In this algorithm avd-5556 is acting as a sequencer. Each avd is having a vector clock (in this case of size 3) and a process sequence number, if it is sequencer then in addition to this one vector clock for sequencer and a variable depicting global sequence number is also used.

Vector clock will help us maintaing the causal ordering of the messages and sequencer will help us in maintaing the sequence of the message same in all the avds.

Two test buttons are used with this app:
a) Test Case 1: Multicasts 5 messages in sequence. Multicasting of one message should be followed by 3 seconds sleep of the thread.

b)Test Case 2: Clicking on this button, app should multicast one message. Receiving of this first message should trigger all app instances to multicast exactly two more messages. Unlike the test case 1, no delay is introduced.
Thus, in total with 3 AVDs, there should be 1 + 3 + 3 = 7 multicast messages.

Message format used:  AVD-Name:sequence number

Note: This is particluarly tested and tried on 3 avds(processes) but can be extended based on the requirement. 