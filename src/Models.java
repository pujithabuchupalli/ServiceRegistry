public class Models {

    public static class Service {
        public int id; public String name, owner, description, status, createdAt;
        public Service(int id, String name, String owner, String description, String status, String createdAt) {
            this.id=id; this.name=name; this.owner=owner; this.description=description; this.status=status; this.createdAt=createdAt;
        }
        public String toString() { return name; }
    }

    public static class Endpoint {
        public int id, serviceId; public String url, method;
        public Endpoint(int id, int serviceId, String url, String method) {
            this.id=id; this.serviceId=serviceId; this.url=url; this.method=method;
        }
        public String toString() { return "[" + method + "] " + url; }
    }

    public static class HealthLog {
        public int id, endpointId, statusCode; public String status, checkedAt; public long responseTime;
        public HealthLog(int id, int endpointId, String status, int statusCode, long responseTime, String checkedAt) {
            this.id=id; this.endpointId=endpointId; this.status=status; this.statusCode=statusCode; this.responseTime=responseTime; this.checkedAt=checkedAt;
        }
    }
}