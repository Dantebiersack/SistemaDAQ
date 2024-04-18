package systemadaq;

import com.fazecast.jSerialComm.SerialPort;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ConexionArduino {

    SerialPort puertoSerial;
    OutputStream salida;
    InputStream entrada;

    public void conexion(String puerto, int vel) {
        try {
            puertoSerial = SerialPort.getCommPort(puerto);
            puertoSerial.setBaudRate(vel);
            puertoSerial.setNumDataBits(8);
            puertoSerial.setNumStopBits(1);
            puertoSerial.setParity(0);//normalmente va 1, pero para dht11 ect es 0
            puertoSerial.openPort();

        } catch (Exception e) {
            System.out.println("Revisa tu conexión con puerto serial");
        }
    }

    public void busDatos() {
        salida = puertoSerial.getOutputStream();
        entrada = puertoSerial.getInputStream();
    }

    public void enviarDatos(String c) throws IOException {
        salida.write(c.getBytes());
        salida.flush();
    }

    public String recibeDeArduino() throws IOException {
        String datos = "";
        while (entrada.available() > 0) {
            datos += (char) entrada.read();
        }
        return datos;
    }

    public boolean cerrarPuerto() {
        boolean cierra;
        cierra = puertoSerial.closePort();
        return cierra;
    }
}
