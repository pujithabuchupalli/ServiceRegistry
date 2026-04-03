import java.sql.*;

public class DBConnection {
    private static final String URL  = "jdbc:mysql://localhost:3306/dev_portal";
    private static final String USER = "root";
    private static final String PASS = "BangBang@1234";
    private static Connection conn;

    public static Connection get() {
        try {
            if (conn == null || conn.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                conn = DriverManager.getConnection(URL, USER, PASS);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return conn;
    }

    public static void initDB() {
        try {
            Statement st = get().createStatement();
            st.execute("CREATE TABLE IF NOT EXISTS services(" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "name VARCHAR(100) NOT NULL," +
                "owner VARCHAR(100)," +
                "description TEXT," +
                "status VARCHAR(20) DEFAULT 'UNKNOWN'," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            st.execute("CREATE TABLE IF NOT EXISTS endpoints(" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "service_id INT," +
                "url VARCHAR(300)," +
                "method VARCHAR(10) DEFAULT 'GET'," +
                "FOREIGN KEY(service_id) REFERENCES services(id) ON DELETE CASCADE)");

            st.execute("CREATE TABLE IF NOT EXISTS health_logs(" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "endpoint_id INT," +
                "status VARCHAR(10)," +
                "status_code INT," +
                "response_time BIGINT," +
                "checked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY(endpoint_id) REFERENCES endpoints(id) ON DELETE CASCADE)");

            st.execute("CREATE TABLE IF NOT EXISTS dependencies(" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "service_id INT," +
                "depends_on INT," +
                "FOREIGN KEY(service_id) REFERENCES services(id) ON DELETE CASCADE," +
                "FOREIGN KEY(depends_on)  REFERENCES services(id) ON DELETE CASCADE)");

        } catch (Exception e) { e.printStackTrace(); }
    }
}