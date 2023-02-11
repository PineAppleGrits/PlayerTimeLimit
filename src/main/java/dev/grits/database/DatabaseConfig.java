package dev.grits.database;

public class DatabaseConfig {
    public DatabaseConfig(Boolean enabled, String username, String password, String host, String port, String database) {
        super();
        this.username = username;
        this.password = password;
        this.host = host;
        this.port = port;
        this.database = database;
        this.enabled = enabled;
    }

    private Boolean enabled;
    private String username;
    private String password;
    private String host;
    private String port;
    private String database;

    public Boolean getEnabled() {
        return this.enabled;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public String getHost() {
        return this.host;
    }

    public String getPort() {
        return this.port;
    }

    public String getDatabase() {
        return this.database;
    }

    public String getHostAndPort() {
        return this.host + ":" + this.port;
    }
}
