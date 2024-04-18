package systemadaq;

import java.sql.Connection;
import java.sql.DriverManager;

public class ConexionMysql {
    Connection conn;
    public Connection open() {
        String user = "root";
        String password = "root";
        String url = "jdbc:mysql://127.0.0.1:3306/sistema_daq";
        String parametros = "?useSSL=false&useUnicode=true&characterEncoding=utf-8";
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(url + parametros, user, password);
            return conn;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }
    public void close() {
        if (conn != null) {
            try {
                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException();
            }
        }
    }
}
