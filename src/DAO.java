import java.sql.*;
import java.util.*;

public class DAO {
    static Connection c() { return DBConnection.get(); }

    // ── SERVICES ──
    public static boolean addService(String name, String owner, String desc) {
        try { PreparedStatement ps = c().prepareStatement("INSERT INTO services(name,owner,description) VALUES(?,?,?)");
            ps.setString(1,name); ps.setString(2,owner); ps.setString(3,desc); return ps.executeUpdate()>0;
        } catch(Exception e) { e.printStackTrace(); return false; }
    }

    public static boolean updateService(int id, String name, String owner, String desc) {
        try { PreparedStatement ps = c().prepareStatement("UPDATE services SET name=?,owner=?,description=? WHERE id=?");
            ps.setString(1,name); ps.setString(2,owner); ps.setString(3,desc); ps.setInt(4,id); return ps.executeUpdate()>0;
        } catch(Exception e) { e.printStackTrace(); return false; }
    }

    public static boolean deleteService(int id) {
        try { PreparedStatement ps = c().prepareStatement("DELETE FROM services WHERE id=?");
            ps.setInt(1,id); return ps.executeUpdate()>0;
        } catch(Exception e) { e.printStackTrace(); return false; }
    }

    public static List<Models.Service> getServices(String search) {
        List<Models.Service> list = new ArrayList<>();
        try { String sql = search.isEmpty() ? "SELECT * FROM services ORDER BY created_at DESC"
                : "SELECT * FROM services WHERE name LIKE ? OR owner LIKE ? ORDER BY created_at DESC";
            PreparedStatement ps = c().prepareStatement(sql);
            if (!search.isEmpty()) { ps.setString(1,"%"+search+"%"); ps.setString(2,"%"+search+"%"); }
            ResultSet rs = ps.executeQuery();
            while(rs.next()) list.add(new Models.Service(rs.getInt("id"),rs.getString("name"),rs.getString("owner"),rs.getString("description"),rs.getString("status"),rs.getString("created_at")));
        } catch(Exception e) { e.printStackTrace(); }
        return list;
    }

    public static void updateServiceStatus(int id, String status) {
        try { PreparedStatement ps = c().prepareStatement("UPDATE services SET status=? WHERE id=?");
            ps.setString(1,status); ps.setInt(2,id); ps.executeUpdate();
        } catch(Exception e) { e.printStackTrace(); }
    }

    // ── ENDPOINTS ──
    public static boolean addEndpoint(int serviceId, String url, String method) {
        try { PreparedStatement ps = c().prepareStatement("INSERT INTO endpoints(service_id,url,method) VALUES(?,?,?)");
            ps.setInt(1,serviceId); ps.setString(2,url); ps.setString(3,method); return ps.executeUpdate()>0;
        } catch(Exception e) { e.printStackTrace(); return false; }
    }

    public static boolean deleteEndpoint(int id) {
        try { PreparedStatement ps = c().prepareStatement("DELETE FROM endpoints WHERE id=?");
            ps.setInt(1,id); return ps.executeUpdate()>0;
        } catch(Exception e) { e.printStackTrace(); return false; }
    }

    public static List<Models.Endpoint> getEndpoints(int serviceId) {
        List<Models.Endpoint> list = new ArrayList<>();
        try { PreparedStatement ps = c().prepareStatement("SELECT * FROM endpoints WHERE service_id=?");
            ps.setInt(1,serviceId); ResultSet rs = ps.executeQuery();
            while(rs.next()) list.add(new Models.Endpoint(rs.getInt("id"),rs.getInt("service_id"),rs.getString("url"),rs.getString("method")));
        } catch(Exception e) { e.printStackTrace(); }
        return list;
    }

    // ── HEALTH LOGS ──
    public static void saveLog(int endpointId, String status, int code, long ms) {
        try { PreparedStatement ps = c().prepareStatement("INSERT INTO health_logs(endpoint_id,status,status_code,response_time) VALUES(?,?,?,?)");
            ps.setInt(1,endpointId); ps.setString(2,status); ps.setInt(3,code); ps.setLong(4,ms); ps.executeUpdate();
        } catch(Exception e) { e.printStackTrace(); }
    }

    public static List<Models.HealthLog> getLogs(int endpointId) {
        List<Models.HealthLog> list = new ArrayList<>();
        try { PreparedStatement ps = c().prepareStatement("SELECT * FROM health_logs WHERE endpoint_id=? ORDER BY checked_at DESC LIMIT 20");
            ps.setInt(1,endpointId); ResultSet rs = ps.executeQuery();
            while(rs.next()) list.add(new Models.HealthLog(rs.getInt("id"),rs.getInt("endpoint_id"),rs.getString("status"),rs.getInt("status_code"),rs.getLong("response_time"),rs.getString("checked_at")));
        } catch(Exception e) { e.printStackTrace(); }
        return list;
    }

    // ── DASHBOARD ANALYTICS ──
    public static int countServices()  { return queryInt("SELECT COUNT(*) FROM services"); }
    public static int countEndpoints() { return queryInt("SELECT COUNT(*) FROM endpoints"); }
    public static int countUp()        { return queryInt("SELECT COUNT(*) FROM services WHERE status='UP'"); }
    public static int countDown()      { return queryInt("SELECT COUNT(*) FROM services WHERE status='DOWN'"); }
    public static long avgResponseTime(){ return queryLong("SELECT AVG(response_time) FROM health_logs"); }

    public static List<Object[]> slowestAPIs() {
        List<Object[]> list = new ArrayList<>();
        try { String sql = "SELECT s.name, e.url, AVG(h.response_time) AS avg_ms FROM health_logs h " +
                "JOIN endpoints e ON h.endpoint_id=e.id JOIN services s ON e.service_id=s.id " +
                "GROUP BY e.id ORDER BY avg_ms DESC LIMIT 5";
            ResultSet rs = c().createStatement().executeQuery(sql);
            while(rs.next()) list.add(new Object[]{rs.getString("name"),rs.getString("url"),String.format("%.0f ms",rs.getDouble("avg_ms"))});
        } catch(Exception e) { e.printStackTrace(); }
        return list;
    }

    private static int  queryInt (String sql) { try { ResultSet rs=c().createStatement().executeQuery(sql); return rs.next()?rs.getInt(1):0; } catch(Exception e){return 0;} }
    private static long queryLong(String sql) { try { ResultSet rs=c().createStatement().executeQuery(sql); return rs.next()?rs.getLong(1):0; } catch(Exception e){return 0;} }
}