package me.replydev.db;

import com.zaxxer.hikari.HikariDataSource;
import me.replydev.mcping.data.FinalResponse;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class ServerRepository {

    private HikariDataSource dataSource;

    private Logger logger = LogManager.getLogManager().getLogger("DB");

    public ServerRepository() {
        dataSource = SqlitePool.getHikarDatasource();
    }

    public void createTable() {
        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement("CREATE TABLE IF NOT EXISTS Server(\n" + "host TEXT NOT NULL,\n" + "port INTEGER NOT NULL,\n" + "motd TEXT NOT NULL,\n" + "favicon TEXT,\n" + "version TEXT,\n" + "PRIMARY KEY (host, port)\n" + ");")) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public void createBackupTable() {
        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement("CREATE TABLE IF NOT EXISTS Backup" + "(" + "host TEXT," + "time TEXT DEFAULT CURRENT_TIMESTAMP" + ")")) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void createBackup(String host) {
        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement("INSERT INTO Backup (host) VALUES (?)")) {
            stmt.setString(1, host);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<String> loadLatestBackup() {
        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement("SELECT host FROM Backup ORDER BY time DESC LIMIT 1")) {
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                return Optional.of(resultSet.getString("host"));
            }
        } catch (SQLException e) {
            return Optional.empty();
        }
        return Optional.empty();
    }

    public void createServer(String ip, int port, String desc, FinalResponse response) {
        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement("INSERT INTO Server VALUES (?,?,?,?,?)")) {
            stmt.setString(1, ip);
            stmt.setInt(2, port);
            stmt.setString(3, desc);
            stmt.setString(4, response.getFavIcon());
            stmt.setString(5, response.getVersion().getName());
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.warning(e.getMessage());
        }
    }

}
