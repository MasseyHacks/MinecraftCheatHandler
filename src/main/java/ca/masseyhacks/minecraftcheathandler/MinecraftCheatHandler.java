package ca.masseyhacks.minecraftcheathandler;

import ca.masseyhacks.minecraftcheathandler.events.OnCheatDetect;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class MinecraftCheatHandler extends JavaPlugin {
    public Connection connection;
    private void initDb() throws SQLException {
        String sqlCheatCreate = "CREATE TABLE IF NOT EXISTS " + "CheatIncidents"
                + "  (cheatUUID          VARCHAR(36),"
                + "   mcUUID             VARCHAR(36),"
                + "   cheatType          VARCHAR(40),"
                + "   cheatTime          TIMESTAMP , PRIMARY KEY(`cheatUUID`))";
        Statement stmt = connection.createStatement();
        stmt.execute(sqlCheatCreate);
    }
    private boolean initMySQL(String url, String username, String password){
        try { //We use a try catch to avoid errors, hopefully we don't get any.
            Class.forName("com.mysql.jdbc.Driver"); //this accesses Driver in jdbc.
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            getLogger().severe("jdbc driver unavailable!");
            return false;
        }
        try { //Another try catch to get any SQL errors (for example connections errors)
            connection = DriverManager.getConnection(url, username, password);
            //with the method getConnection() from DriverManager, we're trying to set
            //the connection's url, username, password to the variables we made earlier and
            //trying to get a connection at the same time. JDBC allows us to do this.
            initDb();
        } catch (SQLException e) { //catching errors)
            e.printStackTrace(); //prints out SQLException errors to the console (if any)
            return false;
        }
        return true;
    }

    private String buildDBURL(String host, String database){

        return "jdbc:mysql://" +
                host +
                ":3306/" +
                database;
    }
    @Override
    public void onEnable(){
        //Fired when the server enables the plugin
        FileConfiguration config = this.getConfig();

        // Default config values
        config.addDefault("mysqlHost", "127.0.0.1");
        config.addDefault("mysqlUser", "minecraft");
        config.addDefault("mysqlPassword", "password");
        config.addDefault("mysqlDb", "minecraftDB");
        config.addDefault("placeholderCacheRefreshTime", 600);

        config.options().copyDefaults(true);
        saveConfig();

        if(!initMySQL(
                buildDBURL(config.getString("mysqlHost"), config.getString("mysqlDb")),
                config.getString("mysqlUser"),
                config.getString("mysqlPassword"))
        ){
            // halt setup if MySQL cannot connect
            return;
        }

        getLogger().info("Connection to database established.");

        getLogger().info("Registering event handlers.");
        getServer().getPluginManager().registerEvents(new OnCheatDetect(this), this);


    }
}
