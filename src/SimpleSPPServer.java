import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.*;
import java.nio.ByteBuffer;


public class SimpleSPPServer {
    private Robot robot;
    private InputStream inputStream = null;
    private Point mouseInfo;
    private Float x;
    private Float y;

    public static float fromByteArray(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getFloat();
    }

    //Démarrer le serveur
    private void startServer() throws IOException {

        //On créée un UUID
        UUID uuid = new UUID("1101", true);

        //On créée l'URL du service
        String connectionString = "btspp://localhost:" + uuid + ";name=Sample SPP Server";

        while (true) {
            //On ouvre l'url du serveur
            StreamConnectionNotifier streamConnectionNotifier = (StreamConnectionNotifier) Connector.open(connectionString);

            //On attend la connection du client
            System.out.println("Server started. Waiting for client to connect...");
            StreamConnection connection = streamConnectionNotifier.acceptAndOpen();

            RemoteDevice device = RemoteDevice.getRemoteDevice(connection);
            System.out.println("Remote device address : " + device.getBluetoothAddress());
            System.out.println("Remote device name : " + device.getFriendlyName(true));

            //On lit les messages du client
            inputStream = connection.openInputStream();

            try {
                robot = new Robot();
            } catch (AWTException e) {
                e.printStackTrace();
            }

            while (true) {
                int entree = inputStream.read();
                if (entree == 1) {
                    System.out.println("Contrôle Souris");
                    System.out.println(entree);

                    int etape = 0;

                    while (true) {

                        byte[] byteRecu = new byte[4];
                        inputStream.read(byteRecu);
                        Float valeurRecue = fromByteArray(byteRecu);
                        System.out.println("Valeur reçue = " + valeurRecue);
                        if (valeurRecue == 10000) {
                            robot.mousePress(InputEvent.BUTTON1_MASK);
                            robot.mouseRelease(InputEvent.BUTTON1_MASK);
                        } else if (valeurRecue == 20000) {
                            robot.mousePress(InputEvent.BUTTON3_MASK);
                            robot.mouseRelease(InputEvent.BUTTON3_MASK);
                        } else if (valeurRecue == 30000) {
                            System.out.println("Sortie...");
                            break;
                        } else {
                            mouseInfo = MouseInfo.getPointerInfo().getLocation();
                            if (etape == 0) {
                                System.out.println("x = " + valeurRecue);
                                x = valeurRecue;
                                etape = etape + 1;
                            } else if (etape == 1) {
                                System.out.println("y = " + valeurRecue);
                                y = valeurRecue;
                                robot.mouseMove(mouseInfo.x + x.intValue(), mouseInfo.y + y.intValue());
                                etape = 0;
                            }
                        }
                    }
                } else if (entree == 2) {
                    System.out.println("Diapo");
                    System.out.println(entree);
                    robot.keyPress(KeyEvent.VK_F5);
                    robot.keyRelease(KeyEvent.VK_F5);

                    while (true) {
                        int value = inputStream.read();

                        if (value == 1) {
                            robot.keyPress(KeyEvent.VK_RIGHT);
                            System.out.println("Right");
                            robot.keyRelease(KeyEvent.VK_RIGHT);
                        } else if (value == 2) {
                            robot.keyPress(KeyEvent.VK_LEFT);
                            System.out.println("Left");
                            robot.keyRelease(KeyEvent.VK_LEFT);

                        } else if (value == 3) {
                            System.out.println("Sortie...");
                            break;
                        }
                    }

                } else if (entree == 3 || entree == 0 ) {
                    System.out.println(entree);
                    break;
                }
            }
            streamConnectionNotifier.close();
            inputStream = null;
            streamConnectionNotifier = null;
            connection = null;

        }
    }


        public static void main(String[] args) throws IOException {
        //On affiche le nom et l'adresse locale
        LocalDevice localDevice = LocalDevice.getLocalDevice();
        System.out.println("Adress : " + localDevice.getBluetoothAddress());
        System.out.println("Name : " + localDevice.getFriendlyName());

        SimpleSPPServer simpleSPPServer = new SimpleSPPServer();
        simpleSPPServer.startServer();
    }
}
