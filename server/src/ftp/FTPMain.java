package ftpserver;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.io.*;
import java.util.Base64;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.Arrays;

public class FTPMain {

    public void start() throws Exception {
        try (var listener = new ServerSocket(59898)) {
            System.out.println("Server Started ...");
            LogHandler log = new LogHandler();
            log.StartLog();

            ThreadPool threadPool = new ThreadPool(3, 20);

            while (true) {
                threadPool.submitTask(new RequestHandler(listener.accept()));
            }
        }
    }

    public static void main(String[] args) throws Exception {

        FTPMain programm = new FTPMain();
        programm.start();

    }

    private class RequestHandler implements Runnable {
        private Socket socket;

        RequestHandler(Socket socket) {
            this.socket = socket;
        }

        private String encodeFileToBase64Binary(String fileName) throws IOException {
            byte[] encoded = Base64.getEncoder().encode(Files.readAllBytes(Paths.get(fileName)));
            return new String(encoded);
        }

        private byte[] DecodeBase64ToString(String Text) {
            byte[] decoded = Base64.getDecoder().decode(Text);
            return decoded;
        }

        @Override
        public void run() {
            LogHandler log = new LogHandler();
            String ip = socket.getInetAddress().toString();
            ip = ip.replace("/", "");
            log.writeLog("connection", ip + " conexión entrante");
            int mensajes = 0;
            Boolean waitForFile = false;
            String fileName = "";
            try {
                var in = new Scanner(socket.getInputStream());
                var out = new PrintWriter(socket.getOutputStream(), true);

                out.println("HELLO");
                while (in.hasNextLine()) {
                    String mensaje = in.nextLine();
                    if (waitForFile) {
                        File file = new File("files/" + fileName);
                        file.createNewFile();
                        OutputStream fr = new FileOutputStream(file);
                        fr.write(DecodeBase64ToString(mensaje));
                        fr.close();
                        out.println("OK");
                        log.writeLog("command", "servidor envía respuesta a " + ip);
                        waitForFile = false;
                    } else if (!mensaje.equals("HELLO") && mensajes == 0) {
                        System.out.println("mensajes:" + mensajes);
                        log.writeLog("error", "conexión rechazada por" + ip);
                        out.println("HANDSHAKEERROR");
                        throw new IllegalArgumentException("Error en handshake");
                    } else if (mensajes > 0) {
                        log.writeLog("command", ip + " " + mensaje);
                        if (mensaje.equals("ls")) {

                            File curDir = new File("./files");
                            File[] filesList = curDir.listFiles();
                            for (File f : filesList) {
                                out.println(f.getName());
                            }
                            out.println("END");
                            log.writeLog("response", "servidor envía respuesta a " + ip);
                        } else if (mensaje.startsWith("get")) {
                            String parts[] = mensaje.split(" ");

                            File tempFile = new File("files/" + parts[1]);
                            boolean exists = tempFile.exists();
                            if (exists) {
                                out.println(encodeFileToBase64Binary("files/" + parts[1]));
                                out.println("END");
                            } else {
                                out.println("NOFILE");
                            }
                            log.writeLog("response", "servidor envía respuesta a " + ip);
                        } else if (mensaje.startsWith("put")) {
                            String parts[] = mensaje.split(" ");
                            waitForFile = true;
                            fileName = parts[1];

                        } else if (mensaje.startsWith("delete")) {
                            String parts[] = mensaje.split(" ");
                            File file = new File("files/" + parts[1]);
                            if (file.delete()) {
                                out.println("Archivo " + parts[1] + " eliminado");
                            } else {
                                out.println("Error al eliminar " + parts[1]);
                            }
                            log.writeLog("response", "servidor envía respuesta a " + ip);

                        } else {
                            out.println("Comando no reconocido");
                        }
                    }

                    mensajes += 1;
                    // out.println(in.nextLine().toUpperCase());
                }
            } catch (Exception e) {
                System.out.println("Error:" + socket);
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                }
                System.out.println("Closed: " + socket);
            }
        }
    }
}
