import java.io.*;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;

public class ClientHandler {
    private final String userDir = "server/UserFiles";

    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private InputStream is;
    private OutputStream os;
    private FileInputStream fis;
    private BufferedInputStream bis;
    private FileOutputStream fos;
    private BufferedOutputStream bos;

    private String message;
    private BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

    public ClientHandler(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            is = socket.getInputStream();
            os = socket.getOutputStream();

            new Thread(() -> {
                try {
                    String toServer;
                    while (true) {
                        toServer = in.readUTF();
                        System.out.println("Server got message: " + toServer);
                        if (toServer.startsWith("RECEIVEFILE@")) {
                            String fileName = toServer.split("@")[1];
                            sendFile(fileName);
                            String message = "CLIENTCOMMANDLINE@File " + fileName.split("/")[fileName.split("/").length - 1] +
                                    "was successfully copied to your computer!";
                            sendMsg(message);
                        }
                        if (toServer.startsWith("SENDFILE@")) {
                            String fileName = toServer.split("@")[1].split("%%")[0];
                            long fileSize = Long.valueOf(toServer.split("@")[1].split("%%")[1]).longValue();
                            getFile(fileName, fileSize);
                            String message = "CLIENTCOMMANDLINE@File " + fileName.split("/")[fileName.split("/").length - 1] +
                                    "was successfully copied from your computer to the server!";
                            sendMsg(message);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }).start();

            sendFilesInfo(userDir);
            System.out.println("send userDir");
//            sendFile("server\\UserFiles\\34.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMsg(String message) {
        try {
            out.writeUTF(message);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<File> getFiles(String dir) {
        File path = new File(dir);
        File[] arrFiles = path.listFiles();
        List<File> lst = Arrays.asList(arrFiles);
        return lst;
    }

    private void sendFilesInfo(String dir) {
        List<File> fileList = getFiles(dir);
        StringBuilder sb = new StringBuilder("FILEINFO@");
        ArrayList<Fileinfo> fileinfo = new ArrayList<>();
        for (File file : fileList) {
            fileinfo.add(new Fileinfo(file.toPath()));
        }
        sb.append(dir + "&");
        for (Fileinfo file : fileinfo) {
            sb.append(file.getFilename() + "%%" + file.getType().toString() + "%%" + Long.toString(file.getSize()) + "%%" + file.getLastModified() + "&");
        }
        sendMsg(sb.toString());
    }

    private void sendFile(String fileToSend) throws IOException {
        // send file
        File myFile = new File (fileToSend);
        byte [] mybytearray  = new byte [(int)myFile.length()];
        fis = new FileInputStream(myFile);
        bis = new BufferedInputStream(fis);
        bis.read(mybytearray,0,mybytearray.length);
        System.out.println("Sending " + fileToSend + "(" + mybytearray.length + " bytes)");
        os.write(mybytearray,0,mybytearray.length);
        os.flush();
        System.out.println("Done.");

    }


    public void getFile(String fileReceive, long fileSize) throws IOException {
        byte [] mybytearray  = new byte [Math.toIntExact(fileSize)];
        int bytesRead;
        int current = 0;
        fos = new FileOutputStream(fileReceive);
        bos = new BufferedOutputStream(fos);
        bytesRead = is.read(mybytearray,0,mybytearray.length);
        current = bytesRead;

        do {
            bytesRead =
                    is.read(mybytearray, current, (mybytearray.length-current));
            if(bytesRead >= 0) current += bytesRead;
        } while(bytesRead > -1);

        bos.write(mybytearray, 0 , current);
        bos.flush();
        System.out.println("File " + fileReceive
                + " downloaded (" + current + " bytes read)");
    }

}
