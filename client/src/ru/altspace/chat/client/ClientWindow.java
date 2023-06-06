package ru.altspace.chat.client;

import ru.altspace.network.TCPConnection;
import ru.altspace.network.TCPConnectionListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class ClientWindow extends JFrame implements ActionListener, TCPConnectionListener {

    private static final String IP_ADRESS = "127.0.0.1";
    private static final int PORT = 25565;
    private static final int WIDTH = 600;
    private static final int HEIGHT = 400;

    private static ClientWindow window;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                window = new ClientWindow();
            }
        });
    }

    private final JTextArea log = new JTextArea();
    JScrollPane scrollPane = new JScrollPane(log);
    private final JTextField fieldNickName = new JTextField("your name");
    private final JTextField fieldInput = new JTextField("your message");

    private TCPConnection connection;

    private ClientWindow () {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(WIDTH,HEIGHT);

        setLocationRelativeTo(null);
        setAlwaysOnTop(true);

        log.setEditable(false);
        log.setLineWrap(true);

        fieldInput.addActionListener(this);
        add(scrollPane, BorderLayout.CENTER);
        add(fieldInput, BorderLayout.SOUTH);
        add(fieldNickName, BorderLayout.NORTH);

        setVisible(true);
        try {
            connection = new TCPConnection(this, IP_ADRESS, PORT);
        } catch (IOException e) {
            onException(connection, e);
        }
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        String msg = fieldInput.getText();
        if(msg.equals("")) return;
        fieldInput.setText(null);
        connection.sendString(fieldNickName.getText() + ": " + msg);
    }

    @Override
    public void onConnectionReady(TCPConnection tcpConnection) {
        printMessage("Connection ready...");
    }

    @Override
    public void onReceiveString(TCPConnection tcpConnection, String value) {
        printMessage(value);
    }

    @Override
    public void onDisconnect(TCPConnection tcpConnection) {
        printMessage("Connection closed...");
    }

    @Override
    public void onException(TCPConnection tcpConnection, Exception e) {
        printMessage("Connection exception: " + e);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                    window.dispose();
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        thread.start();
    }

    private synchronized void printMessage(String msg) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                log.append(msg + '\n');
                log.setCaretPosition(log.getDocument().getLength());
            }
        });

    }
}
