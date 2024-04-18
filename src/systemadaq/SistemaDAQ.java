package systemadaq;

import DWEET.DweetIO;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SistemaDAQ {

    ConexionArduino miConexion;
    DHT11 dht;
    private boolean continuarLeyendo = true;

    public SistemaDAQ() {
        miConexion = new ConexionArduino();
        miConexion.conexion("COM3", 9600);
        miConexion.busDatos();
        dht = new DHT11();
        dht.start();
    }

    public void detenerLectura() {
        continuarLeyendo = false;
    }

    public static void main(String args[]) {
        SistemaDAQ monitoreo = new SistemaDAQ();
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            try {
                System.out.println("inserta 0 para detener el programa:");
                String input = br.readLine();
                if (input.equals("0")) {
                    monitoreo.detenerLectura();
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class DHT11 extends Thread {

        @Override
        public void run() {
            while (true && continuarLeyendo) {
                try {
                    if(miConexion.recibeDeArduino()!=null){
                    
                    }
                    String datos = miConexion.recibeDeArduino().trim();
                    String partes[] = datos.split(",");
                    if (partes.length == 3) {
                        String temperaturaC = partes[0];
                        String temperaturaF = partes[1];
                        String distancia = partes[2];
                        System.out.println(distancia);
                        if (distancia.equals("0")) {
                            System.out.println("Temperatura en °C: " + partes[0] + ", Temperatura en °F: " + partes[1]);
                            //envio de datos a la nube de DWEET
                            String thingName = "nube1";
                            JsonObject json = new JsonObject();
                            json.addProperty("temperaturaC", partes[0]);
                            json.addProperty("temperaturaF", partes[1]);
                            DweetIO.publish(thingName, json);
                            //insersion en la bd
                            insertarEnBD(temperaturaC, temperaturaF);
                        }else if(!distancia.equals("0")){
                            System.out.println("Temperatura en °C: " + partes[0] + ", Temperatura en °F: " + partes[1]
                             + ", Se detecto movimiento a " + partes[2]+" cm");
                            //envio de datos a la nube de DWEET
                            String thingName = "nube2";
                            JsonObject json = new JsonObject();
                            json.addProperty("distancia", partes[2]);
                            DweetIO.publish(thingName, json);
                            //insersion en la bd
                            insertarEnBD(temperaturaC, temperaturaF);
                            insertarEnBD2(distancia);
                        }
                    }
                    Thread.sleep(3000);
                } catch (IOException | InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }

        //insersion en bd
        private void insertarEnBD(String temperaturaC, String temperaturaF) {
            Connection connection = null;
            PreparedStatement preparedStatement = null;

            try {
                ConexionMysql conexionMysql = new ConexionMysql();
                connection = conexionMysql.open();

                // Consulta SQL para insertar en la tabla
                String sql = "INSERT INTO temperatura VALUES (?, ?,now())";

                preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, temperaturaC);
                preparedStatement.setString(2, temperaturaF);

                // Ejecutar la consulta
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                // Cerrar recursos
                try {
                    if (preparedStatement != null) {
                        preparedStatement.close();
                    }
                    if (connection != null) {
                        connection.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }//cierre de insertarEnBD
        
        //insersion en bd 2
        private void insertarEnBD2(String distancia) {
            Connection connection = null;
            PreparedStatement preparedStatement = null;

            try {
                ConexionMysql conexionMysql = new ConexionMysql();
                connection = conexionMysql.open();

                // Consulta SQL para insertar en la tabla
                String sql = "INSERT INTO movimiento VALUES (?,now())";

                preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, distancia);

                // Ejecutar la consulta
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                // Cerrar recursos
                try {
                    if (preparedStatement != null) {
                        preparedStatement.close();
                    }
                    if (connection != null) {
                        connection.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
