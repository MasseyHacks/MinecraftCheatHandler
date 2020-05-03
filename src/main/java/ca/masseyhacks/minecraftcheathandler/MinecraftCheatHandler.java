package ca.masseyhacks.minecraftcheathandler;

import ca.masseyhacks.minecraftcheathandler.events.OnCheatDetect;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.bukkit.block.data.type.Fire;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

import java.io.FileInputStream;
import java.nio.file.Paths;
import java.util.logging.Level;

public class MinecraftCheatHandler extends JavaPlugin {

    public JedisPool jedisPool;

    private void loadConfiguration() {
        getConfig().addDefault("Services.Redis.Host", "localhost");
        getConfig().addDefault("Services.Redis.Port", 6379);
        getConfig().addDefault("Services.Redis.Password", "");
        getConfig().addDefault("Services.Firebase.KeyFileName", "minecraft-cheat-events-firebase-adminsdk-12tn7-aab6408a82.json");
        getConfig().addDefault("redisExpireTime", 2700); // REDIS record expire in seconds
        getConfig().options().copyDefaults(true);
        saveConfig();
    }

    @Override
    public void onEnable() {
        loadConfiguration();
        //Fired when the server enables the plugin
        FileConfiguration config = this.getConfig();

        connectToRedis(config);
        getLogger().info("Redis Connection Established");

        try {
            FileInputStream serviceAccount = new FileInputStream(Paths.get(getDataFolder().getPath(), config.getString("Services.Firebase.KeyFileName")).toString());

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://minecraft-cheat-handler.firebaseio.com")
                    .build();

            FirebaseApp.initializeApp(options);
            getLogger().info("Firebase Initialized");

        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to initialize Firebase");

            e.printStackTrace();
            return;
        }

        getLogger().info("Connection to database established.");

        getLogger().info("Registering event handlers.");
        getServer().getPluginManager().registerEvents(new OnCheatDetect(this), this);
    }

    public void connectToRedis(FileConfiguration config) {
        String password = getConfig().getString("Services.Redis.Password");
        String host = config.getString("Services.Redis.Host");
        int port = config.getInt("Services.Redis.Port");
        //noinspection ConstantConditions
        if (password.length() == 0) {
            jedisPool = new JedisPool(new JedisPoolConfig(), host , port);
            return;
        }
        jedisPool = new JedisPool(new JedisPoolConfig(), host, port, Protocol.DEFAULT_TIMEOUT, password);
    }

    @Override
    public void onDisable() {
       jedisPool.close();
    }
}
