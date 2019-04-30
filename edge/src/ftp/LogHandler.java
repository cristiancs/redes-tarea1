package ftpserver;

import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.io.FileWriter;
import java.io.File;
import java.io.PrintWriter;
import java.io.IOException;

public class LogHandler {
    public void StartLog() {
        try {
            File file = new File("log.txt");
            file.delete();
            file.createNewFile();
            FileWriter fr = new FileWriter(file, true);
            fr.write("DATETIME  EVENT   DESCRIPTION\n");
            fr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void writeLog(String event, String message) {
        try {
            DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            Date date = new Date();

            File file = new File("log.txt");
            FileWriter fr = new FileWriter(file, true);
            fr.write(dateFormat.format(date) + "  " + event + "   " + message + "\n");
            fr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}