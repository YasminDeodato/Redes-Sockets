import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {

    private ArrayList<ConnectionHandler> connections;
    private ServerSocket server;
    private boolean status;
    private ExecutorService pool;
    private static FileWriter myWriter;

    public Server() {
        connections = new ArrayList<>();
        status = false;
    }

    @Override
    public void run() {
        try {
            server = new ServerSocket(9999);

            pool = Executors.newCachedThreadPool();
            iniciarLog();
            BufferedReader inReader = new BufferedReader(new InputStreamReader(System.in));
            while (!status) {
                Socket client = server.accept();

                ConnectionHandler handler = new ConnectionHandler(client);
                connections.add(handler);

                pool.execute(handler);
            }

            //fecharLog();
        } catch (IOException e) {
             shutdown();
        }
    }

    public void broadcast(String message) {
        for (ConnectionHandler ch: connections) {
            if (ch != null) {
                ch.sendMessage(message);

            }
        }
    }

    public void shutdown() {
        try {
            status = true;
            if (!server.isClosed()) {
                server.close();
                //fecharLog();
            }
            for (ConnectionHandler ch: connections) {
                ch.shutdown();
            }
        } catch (Exception e) {
            // IGNORE
        }
    }

    class ConnectionHandler implements Runnable {

        private Socket client;
        private BufferedReader in;
        private PrintWriter out;

        private String nickname;
        
        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                out = new PrintWriter(client.getOutputStream(), true);

                out.println("Please, set your nickname: ");
                nickname = in.readLine();

                System.out.println(nickname + " connected!");
                broadcast(nickname + " joined the chat!");

                String message;
                while ((message = in.readLine()) != null) {
                    if (message.startsWith("/nickname")) {
                        String[] messageSplit = message.split(" ", 2);

                        if (messageSplit.length == 2) {
                            broadcast(nickname + " renamed to " + messageSplit[1]);
                            System.out.println(nickname + " renamed to " + messageSplit[1]);

                            nickname = messageSplit[1];
                            out.println("Successfully changed nickname to " + nickname + "!");
                        } else {
                            out.println("No nickname provided!");
                        }
                    } else if (message.startsWith("/quit")) {
                        System.out.println(nickname + " disconnected!");
                        broadcast(nickname + " left the chat!");
                        shutdown();
                    } else {
                        gravarMensagem(nickname, message);
                        broadcast(nickname + ": " + message);
                    }
                }
            } catch (Exception e) {
                shutdown();
            }
        }
        
        public ConnectionHandler(Socket client) {
            this.client = client;
        }

        public void sendMessage(String message) {
            out.println(message);
        }

        public void shutdown() {
            try {
                in.close();
                out.close();

                if (!client.isClosed()) {
                    client.close();
                }
            } catch (Exception e) {
                // IGNORE
            }
        }
        
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.run();
    }

    public static void iniciarLog() {
        try {
            myWriter = new FileWriter("teste.txt");
            myWriter.write("----------------Chat " + Instant.now().toString() + "----------------");
            myWriter.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    private static void gravarMensagem(String cliente, String mensagem) {
        String log = Instant.now().toString() + " - " + cliente + " : '" + mensagem + "'";
        try {
            myWriter = new FileWriter("teste.txt");
            myWriter.write(log);
            myWriter.close();
            System.out.println("Log escrito com sucesso");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    private static void fecharLog() {
        try {
            myWriter.close();
            System.out.println("arquivo fechado com sucesso");}
        catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
}
