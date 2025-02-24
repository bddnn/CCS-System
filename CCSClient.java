import java.io.*;
import java.net.*;
import java.util.Scanner;

public class CCSClient {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java CCSClient <port>");
            return;
        }

        int port;
        try {
            port = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            System.out.println("Invalid port number.");
            return;
        }

        try {
            InetAddress broadcastAddress = InetAddress.getByName("255.255.255.255");
            DatagramSocket udpSocket = new DatagramSocket();
            udpSocket.setBroadcast(true);

            String discoverMessage = "CCS DISCOVER";
            byte[] sendData = discoverMessage.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcastAddress, port);
            udpSocket.send(sendPacket);

            byte[] receiveBuffer = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            udpSocket.setSoTimeout(5000);

            String serverAddress;
            try {
                udpSocket.receive(receivePacket);
                String response = new String(receivePacket.getData(), 0, receivePacket.getLength());
                if ("CCS FOUND".equals(response)) {
                    serverAddress = receivePacket.getAddress().getHostAddress();
                    System.out.println("Server found at: " + serverAddress);
                } else {
                    System.out.println("Unexpected response from server.");
                    udpSocket.close();
                    return;
                }
            } catch (SocketTimeoutException e) {
                System.out.println("No response from server. Exiting.");
                udpSocket.close();
                return;
            }
            udpSocket.close();

            Socket tcpSocket = new Socket(serverAddress, port);
            BufferedReader reader = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream()));
            PrintWriter writer = new PrintWriter(tcpSocket.getOutputStream(), true);

            System.out.println("Connected to the server. Type commands in the format: <OPER> <ARG1> <ARG2>");
            System.out.println("Example: ADD 10 20, SUB 30 15. Type EXIT to quit.");

            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.print("> ");
                String command = scanner.nextLine().trim();

                if ("EXIT".equalsIgnoreCase(command)) {
                    System.out.println("Exiting client.");
                    break;
                }

                writer.println(command);

                String response = reader.readLine();
                if (response != null) {
                    System.out.println("Server response: " + response);
                } else {
                    System.out.println("Server disconnected.");
                    break;
                }
            }

            tcpSocket.close();
            scanner.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
