package org.pds.client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.*;

public class DnsClient {
    private static final int DNS_PORT = 5354;
    private static final int HTTP_PORT = 8080;
    private static String domainName;
    private static String dnsServerAddress = "255.255.255.255";

    public static void main(String[] args) {
        System.out.println("DNS Client start");
        if (args.length < 2) {
            System.out.println("Usage: java DnsClient <domain> <address>");
            return;
        }
        domainName = args[0];
        String ipAddress = args[1];

        discoverDnsServer();
        registerWithDns(domainName, ipAddress);
        startHttpServer();
    }

    private static void discoverDnsServer() {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setBroadcast(true);
            String message = "DISCOVER_DNS";
            byte[] buffer = message.getBytes();
            DatagramPacket packet = new DatagramPacket(
                    buffer,
                    buffer.length,
                    InetAddress.getByName(dnsServerAddress),
                    DNS_PORT
            );
            socket.send(packet);

            byte[] responseBuffer = new byte[256];
            DatagramPacket responsePacket = new DatagramPacket(responseBuffer, responseBuffer.length);
            socket.receive(responsePacket);
            dnsServerAddress = new String(responsePacket.getData(), 0, responsePacket.getLength());
            System.out.println("DNS Server found at: " + dnsServerAddress);
        } catch (Exception e) {
            System.out.println("Could not discover DNS server, using default.");
        }
    }

    private static void registerWithDns(String domain, String ip) {
        try (DatagramSocket socket = new DatagramSocket()) {
            String message = "REGISTER " + domain + " " + ip;
            byte[] buffer = message.getBytes();
            DatagramPacket packet = new DatagramPacket(
                    buffer,
                    buffer.length,
                    InetAddress.getByName(dnsServerAddress),
                    DNS_PORT
            );
            socket.send(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void startHttpServer() {
        try (ServerSocket serverSocket = new ServerSocket(HTTP_PORT)) {
            System.out.println("HTTP Server running on port " + HTTP_PORT);
            while (true) {
                Socket client = serverSocket.accept();
                PrintWriter out = new PrintWriter(client.getOutputStream(), true);
                out.println("HTTP/1.1 200 OK\r\nContent-Type: text/html\r\n\r\n");
                out.println("<html><body><h1>Welcome to " + domainName + "</h1></body></html>");
                out.close();
                client.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
