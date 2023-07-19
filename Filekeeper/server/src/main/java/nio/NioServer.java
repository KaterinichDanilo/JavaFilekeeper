package nio;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

public class NioServer {
    ServerSocketChannel server;
    Selector selector;
    SocketChannel channel;

    final String NEWLINE = "-> ";
    private final String HOMEDIR = "./server/UserFiles";
    private String currentDir = "./server/UserFiles";

    public NioServer() throws IOException {
        this.server = ServerSocketChannel.open();
        this.selector = Selector.open();

        server.bind(new InetSocketAddress(300));
        server.configureBlocking(false);
        server.register(selector, SelectionKey.OP_ACCEPT);

    }

    public void start() throws IOException {
        while (server.isOpen()) {
            selector.select();
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = keys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                if (key.isAcceptable()) {
                    handleAccept();
                }
                if (key.isReadable()) {
                    handleRead(key);
                }
                iterator.remove();
            }

        }
    }

    private void handleRead(SelectionKey key) throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(1024);
        channel = (SocketChannel) key.channel();
        StringBuilder s = new StringBuilder();
        while (channel.isOpen()) {
            int read = channel.read(buf);
            if (read < 0) {
                channel.close();
                return;
            }
            if (read == 0) {
                break;
            }
            buf.flip();
            while (buf.hasRemaining()) {
                s.append((char) buf.get());
            }
            buf.clear();
        }

        byte[] message = s.toString().getBytes(StandardCharsets.UTF_8);
        checkCommand(message, channel);
        channel.write(ByteBuffer.wrap(NEWLINE.getBytes(StandardCharsets.UTF_8)));

//        for (SelectionKey selectedKey : selector.keys()) {
//            if (selectedKey.isValid() && selectedKey.channel() instanceof SocketChannel sc) {
//                sc.write(ByteBuffer.wrap(NEWLINE.getBytes(StandardCharsets.UTF_8)));
//            }
//        }
    }

    private void checkCommand(byte[] message, SocketChannel channel) throws IOException {
        String m = getStringMessage(message);

        if (m.equals("ls")) {
            ls();
            return;
        }
        if (m.split(" ")[0].equals("cat")) {
            cat(new File(m.split(" ")[1]));
            return;
        }
        if (m.split(" ")[0].equals("cd")) {
            cd(m.split(" ")[1]);
            return;
        }
    }

    private void ls() throws IOException {
        File[] files = new File(currentDir).listFiles();
        StringBuilder sb = new StringBuilder("Files: ");

        for (File file : files) {
            sb.append(file.getName() + " ");
        }
        sb.append("\n");
        channel.write(ByteBuffer.wrap(sb.toString().getBytes(StandardCharsets.UTF_8)));
    }

    private void handleAccept() throws IOException {
        SocketChannel channel = server.accept();
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ);
        channel.write(ByteBuffer.wrap("Hello, client!".getBytes(StandardCharsets.UTF_8)));
    }

    private String getStringMessage(byte[] message) {
        StringBuilder strMessage = new StringBuilder(new String(message, StandardCharsets.UTF_8));
        strMessage.delete(strMessage.length() - 2, strMessage.length() - 1);
        strMessage.deleteCharAt(strMessage.length() - 1);
        return strMessage.toString();
    }

    private void cat(File file) throws IOException {
        try {
            StringBuilder sb = new StringBuilder();
            FileReader fr = new FileReader(currentDir + "/" + file);
            BufferedReader reader = new BufferedReader(fr);
            String str = reader.readLine();
            while (str != null) {
                sb.append(str);
                sb.append("\n");
                str = reader.readLine();
            }
            sb.append("\n");
            System.out.println(sb.toString());
            channel.write(ByteBuffer.wrap(sb.toString().getBytes(StandardCharsets.UTF_8)));
        } catch (FileNotFoundException e) {
            channel.write(ByteBuffer.wrap(("I can't find the file" + file.getAbsolutePath()).getBytes(StandardCharsets.UTF_8)));
        } catch (IOException e) {
            channel.write(ByteBuffer.wrap(("Oops, something went wrong...").getBytes(StandardCharsets.UTF_8)));
        }

    }

    private void cd(String newDir) throws IOException {
        Path path = Paths.get(newDir);

        if (Files.exists(path)) {
            currentDir = newDir;
            channel.write(ByteBuffer.wrap(("The folder was successfully changed!").getBytes(StandardCharsets.UTF_8)));
            return;
        }
        channel.write(ByteBuffer.wrap(("Еhe folder you entered does not exist").getBytes(StandardCharsets.UTF_8)));
    }
}
