package ca.masseyhacks.minecraftcheathandler;

import ca.masseyhacks.minecraftcheathandler.events.OnCheatDetect;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.bukkit.block.data.type.Fire;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Paths;
import java.util.logging.Level;

public class MinecraftCheatHandler extends JavaPlugin {

    @Override
    public void onEnable(){
        //Fired when the server enables the plugin
        FileConfiguration config = this.getConfig();
        try {
            FileInputStream serviceAccount = new FileInputStream(Paths.get(getDataFolder().getPath(), "minecraft-cheat-handler-firebase-adminsdk-v1xqd-8559bb0f17.json").toString());

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://minecraft-cheat-handler.firebaseio.com")
                    .build();

            FirebaseApp.initializeApp(options);
            getLogger().info("Firebase Initialized");

        } catch (Exception e) {
            getLogger().log(Level.SEVERE,"Failed to initialize Firebase");

            e.printStackTrace();
            return;
        }

        getLogger().info("Connection to database established.");

        getLogger().info("Registering event handlers.");
        getServer().getPluginManager().registerEvents(new OnCheatDetect(this), this);


    }
}
