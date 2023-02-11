package dev.grits.database;


import co.aikar.idb.*;
import ptl.ajneb97.PlayerTimeLimit;
import ptl.ajneb97.model.TimeLimitPlayer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class DatabaseAPI {

    PlayerTimeLimit plugin;

    public DatabaseAPI(PlayerTimeLimit plugin) {
        this.plugin = plugin;
    }

    String STATEMENT_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS player_time(uuid VARCHAR(255),name VARCHAR(255),currentTime INT,totalTime INT, messages BOOLEAN,PRIMARY KEY (uuid));";
    String STATEMENT_INSERT_ONE_PLAYER = "INSERT INTO player_time VALUES (?,?,?,?,?)";

    String INSERT_WILDCARD = "(?,?,?,?,?)";
    String STATEMENT_INSERT_PLAYERS = "INSERT INTO player_time(uuid,name,currentTime,totalTime,messages) VALUES %s ON DUPLICATE KEY UPDATE name = VALUES(name), currentTime = VALUES(currentTime), totalTime = VALUES(totalTime), messages = VALUES(messages)";

    String STATEMENT_SELECT_PLAYERS = "SELECT * FROM player_time;";

    public void createTables() {
        try {
            DB.executeUpdate(STATEMENT_CREATE_TABLE);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void insertPlayer(UUID uuid, String name, Integer currentTime, Integer totalTime) {
        try {
            DB.executeInsert(STATEMENT_INSERT_ONE_PLAYER, uuid.toString(), name, currentTime);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void insertPlayers(List<TimeLimitPlayer> playersTime) {
        String STATEMENT = String.format(STATEMENT_INSERT_PLAYERS, playersTime.stream().map(ptl -> INSERT_WILDCARD).collect(Collectors.joining(",")));
        if(playersTime.size() < 1) return;
        List<Object> values = new ArrayList<>();
        for (TimeLimitPlayer ptl : playersTime) {
            values.addAll(Arrays.asList(ptl.getUuid().toString(), ptl.getName(), ptl.getCurrentTime(), ptl.getTotalTime(),ptl.isMessageEnabled()));
        }
        try {
            plugin.getLogger().info(STATEMENT);
            DB.executeUpdate(STATEMENT, values.toArray());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void initDB() {
        DatabaseConfig dbCfg = plugin.getConfigsManager().getMainConfigManager().getDatabaseConfig();
        DatabaseOptions options = DatabaseOptions.builder()
                .mysql(dbCfg.getUsername(), dbCfg.getPassword(), dbCfg.getDatabase(), dbCfg.getHostAndPort())
                .useOptimizations(true)
                .logger(plugin.getLogger())
                .build();
        final Database db = PooledDatabaseOptions.builder().options(options).createHikariDatabase();
        DB.setGlobalDatabase(db);
        createTables();
    }

    public void savePlayersTime() {
        insertPlayers(plugin.getPlayerManager().getPlayers());
    }

    public void loadPlayers() {
        try {
            List<DbRow> playersResults = DB.getResults(STATEMENT_SELECT_PLAYERS);
            ArrayList<TimeLimitPlayer> players = new ArrayList<TimeLimitPlayer>();
            for (DbRow pRow : playersResults) {
                String name = pRow.getString("name");
                String uuid = pRow.getString("uuid");

                TimeLimitPlayer p = new TimeLimitPlayer(uuid, name);

                p.setCurrentTime(pRow.getInt("currentTime"));
                p.setTotalTime(pRow.getInt("totalTime"));
                p.setMessageEnabled(pRow.get("messages") == null ? true : pRow.get("messages"));
                players.add(p);
            }
            plugin.getPlayerManager().setPlayers(players);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


    }

    public void reloadDB() {
        DB.close();
        initDB();
    }

}
