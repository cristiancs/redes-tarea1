package ftpclient;

import java.io.*;
import java.net.*;

import java.util.Base64;
import java.nio.file.Paths;
import java.nio.file.Files;

import java.util.Scanner;

class FTPClient {
    private String encodeFileToBase64Binary(String fileName) throws IOException {
        byte[] encoded = Base64.getEncoder().encode(Files.readAllBytes(Paths.get(fileName)));
        return new String(encoded);
    }

    private byte[] DecodeBase64ToString(String Text) {
        byte[] decoded = Base64.getDecoder().decode(Text);
        return decoded;
    }

    public static void main(String argv[]) throws Exception {
        FTPClient programm = new FTPClient();
        programm.start();

    }

    public void start() {

        try {
            Socket clientSocket = new Socket("127.0.0.1", 59898);
            PrintWriter outToServer = new PrintWriter(clientSocket.getOutputStream(), true);
            Scanner inFromServer = new Scanner(clientSocket.getInputStream());

            Scanner userInput = new Scanner(System.in);
            String inText = inFromServer.nextLine();
            outToServer.println("HELLO");

            while (true) {
                System.out.println("Waiting for commands");
                String inConsole = System.console().readLine();
                if (inConsole.length() == 0) {
                    break;
                }
                if (inConsole.equals("ls")) {

                    outToServer.println(inConsole);

                    inText = "";

                    while (!inText.equals("END")) {
                        inText = inFromServer.nextLine();

                        if (!inText.equals("END")) {
                            System.out.println(inText);
                        }

                    }
                } else if (inConsole.startsWith("delete")) {
                    outToServer.println(inConsole);
                    inText = inFromServer.nextLine();
                } else if (inConsole.startsWith("get")) {
                    outToServer.println(inConsole);
                    inText = inFromServer.nextLine();
                    if (inText.equals("NOFILE")) {
                        System.out.println("El archivo no existe en el servidor remoto");
                    } else {
                        String parts[] = inConsole.split(" ");

                        File file = new File("files/" + parts[1]);
                        file.createNewFile();
                        OutputStream fr = new FileOutputStream(file);
                        fr.write(DecodeBase64ToString(inText));
                        fr.close();
                        inText = inFromServer.nextLine();
                        System.out.println("OK");
                    }

                } else if (inConsole.startsWith("put")) {

                    String parts[] = inConsole.split(" ");
                    File tempFile = new File("files/" + parts[1]);

                    boolean exists = tempFile.exists();
                    if (exists) {
                        outToServer.println(inConsole);
                        outToServer.println(encodeFileToBase64Binary("files/" + parts[1]));
                        inText = inFromServer.nextLine();
                        System.out.println(inText);
                    } else {
                        System.out.println("Archivo no existe");
                    }

                } else {
                    // outToServer.println(inConsole);
                    System.out.println("Comando no reconocido");
                }

            }
            inFromServer.close();
            userInput.close();
            clientSocket.close();

        } catch (Exception e) {

            System.out.println(e);
        }

    }
}
