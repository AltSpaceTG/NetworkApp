package ru.altspace.chat.server;

import ru.altspace.network.TCPConnection;
import ru.altspace.network.TCPConnectionListener;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.Buffer;
import java.util.ArrayList;

public class ChatServer implements TCPConnectionListener {

    public static void main(String[] args) {
        new ChatServer();
    }

    private final ArrayList<TCPConnection> connections = new ArrayList<>();
    private String msgHistory = "";
    private final Thread saveThread;

    private ChatServer() {
        saveThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        saveThread.sleep(60000); // Пауза в 1 минуту
                        saveStringToFile(msgHistory); // раз в минуту сохраняем лог
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        saveThread.start();

        System.out.println("Server started");
        try (ServerSocket serverSocket = new ServerSocket(25565);) {
            while (true) {
                try {
                    new TCPConnection(this, serverSocket.accept());
                } catch (IOException e) {
                    System.out.println("TCPConnection exception: " + e);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized void onConnectionReady(TCPConnection tcpConnection) {
        connections.add(tcpConnection);
        tcpConnection.sendString(msgHistory);
        sendToAllConnections("Client connected: " + tcpConnection);
    }

    @Override
    public synchronized void onReceiveString(TCPConnection tcpConnection, String value) {
        sendToAllConnections(value);
    }

    @Override
    public synchronized void onDisconnect(TCPConnection tcpConnection) {
        connections.remove(tcpConnection);
        sendToAllConnections("Client disconnected: " + tcpConnection);
    }

    @Override
    public synchronized void onException(TCPConnection tcpConnection, Exception e) {
        System.out.println("TCPConnection exception: " + e);
    }

    private void sendToAllConnections(String value) {
        System.out.println(value);
        msgHistory += value + "\n";
        for(TCPConnection con : connections) con.sendString(value);
    }

    private void saveStringToFile(String str) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("output.txt", false))) {
            writer.write(str);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
