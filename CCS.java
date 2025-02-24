import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class CCS {
    private static final AtomicInteger connectedClients = new AtomicInteger(0);
    private static final AtomicInteger totalOperations = new AtomicInteger(0);
    private static final AtomicInteger addOperations = new AtomicInteger(0);
    private static final AtomicInteger subOperations = new AtomicInteger(0);
    private static final AtomicInteger mulOperations = new AtomicInteger(0);
    private static final AtomicInteger divOperations = new AtomicInteger(0);
    private static final AtomicInteger errorCount = new AtomicInteger(0);
    private static final AtomicInteger resultSum = new AtomicInteger(0);

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java -jar CCS.jar <port>");
            return;
        }

        int port;
        try {
            port = Integer.parseInt(args[0]);
            if (port <= 1024 || port > 65535) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            System.out.println("Invalid port number. Please specify a port between 1025 and 65535.");
            return;
        }

        ExecutorService clientHandlerPool = Executors.newCachedThreadPool();

        try {
            DatagramSocket udpSocket = new DatagramSocket(port);
            ServerSocket tcpSocket = new ServerSocket(port);

            System.out.println("CCS Server running on port " + port);

            Thread udpThread = new Thread(() -> startUDPServer(udpSocket));
            udpThread.start();

            Thread statsThread = new Thread(() -> reportStats());
            statsThread.start();

            while (true) {
                Socket clientSocket = tcpSocket.accept();
                connectedClients.incrementAndGet();
                clientHandlerPool.submit(() -> handleClient(clientSocket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void startUDPServer(DatagramSocket udpSocket) {
        byte[] buffer = new byte[1024];
        while (true) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                udpSocket.receive(packet);

                String message = new String(packet.getData(), 0, packet.getLength());
                if (message.startsWith("CCS DISCOVER")) {
                    byte[] response = "CCS FOUND".getBytes();
                    DatagramPacket responsePacket = new DatagramPacket(
                            response, response.length, packet.getAddress(), packet.getPort());
                    udpSocket.send(responsePacket);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void handleClient(Socket clientSocket) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String command;
            while ((command = reader.readLine()) != null) {
                String[] parts = command.split(" ");
                if (parts.length != 3) {
                    writer.println("ERROR");
                    errorCount.incrementAndGet();
                    continue;
                }

                String operation = parts[0];
                int arg1, arg2;
                try {
                    arg1 = Integer.parseInt(parts[1]);
                    arg2 = Integer.parseInt(parts[2]);
                } catch (NumberFormatException e) {
                    writer.println("ERROR");
                    errorCount.incrementAndGet();
                    continue;
                }

                int result;
                try {
                    switch (operation) {
                        case "ADD":
                            result = arg1 + arg2;
                            addOperations.incrementAndGet();
                            break;
                        case "SUB":
                            result = arg1 - arg2;
                            subOperations.incrementAndGet();
                            break;
                        case "MUL":
                            result = arg1 * arg2;
                            mulOperations.incrementAndGet();
                            break;
                        case "DIV":
                            if (arg2 == 0) throw new ArithmeticException();
                            result = arg1 / arg2;
                            divOperations.incrementAndGet();
                            break;
                        default:
                            writer.println("ERROR");
                            errorCount.incrementAndGet();
                            continue;
                    }
                    writer.println(result);
                    resultSum.addAndGet(result);
                } catch (ArithmeticException e) {
                    writer.println("ERROR");
                    errorCount.incrementAndGet();
                }

                totalOperations.incrementAndGet();
                System.out.println("Processed: " + command);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            connectedClients.decrementAndGet();
        }
    }

    private static void reportStats() {
        Map<String, Integer> last10SecondsStats = new ConcurrentHashMap<>();
        while (true) {
            try {
                Thread.sleep(10000);

                System.out.println("\n--- Global Statistics ---");
                System.out.println("Connected Clients: " + connectedClients.get());
                System.out.println("Total Operations: " + totalOperations.get());
                System.out.println("ADD Operations: " + addOperations.get());
                System.out.println("SUB Operations: " + subOperations.get());
                System.out.println("MUL Operations: " + mulOperations.get());
                System.out.println("DIV Operations: " + divOperations.get());
                System.out.println("Errors: " + errorCount.get());
                System.out.println("Result Sum: " + resultSum.get());
                System.out.println("-------------------------\n");

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
