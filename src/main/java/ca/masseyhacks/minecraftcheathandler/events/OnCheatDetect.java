package ca.masseyhacks.minecraftcheathandler.events;

import ca.masseyhacks.minecraftcheathandler.MinecraftCheatHandler;
import com.gmail.olexorus.witherac.api.CheckType;
import com.gmail.olexorus.witherac.api.ViolationEvent;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.FirebaseApp;
import com.google.firebase.cloud.FirestoreClient;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import redis.clients.jedis.Jedis;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class OnCheatDetect implements Listener {
    private final MinecraftCheatHandler plugin;

    public OnCheatDetect(MinecraftCheatHandler plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCheatDetect(ViolationEvent violationEvent) {
        Player player = violationEvent.getPlayer();
        CheckType cheatType = violationEvent.getType();

        try {
            recordCheatIncident(player, cheatType);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void recordCheatIncident(Player cheatedPlayer, CheckType checkType) {
        UUID playerUUID = cheatedPlayer.getUniqueId();
        UUID cheatUUID = UUID.randomUUID();
        String cheat = checkType.getCheckDescription();
        Firestore db = FirestoreClient.getFirestore();
        long time = Instant.now().getEpochSecond();
        DocumentReference docRef = db.collection("cheat-incidents").document(cheatUUID.toString());
        Map<String, Object> data = new HashMap<>();
        data.put("playerUUID", playerUUID.toString());
        data.put("playerName", cheatedPlayer.getDisplayName());
        data.put("cheat", cheat);
        data.put("timestamp", time);
        //asynchronously write data
        ApiFuture<WriteResult> result = docRef.set(data);
        //Redis
        Jedis jedis = plugin.jedis;
        jedis.zadd("cheat-player:" + playerUUID.toString(), time + 30 * 60, cheatUUID.toString());

        Map<String, String> hash = new HashMap<>();
        hash.put("cheat", cheat);
        hash.put("timestamp", (Long.toString(time)));
        jedis.hset("cheat-incident:" + cheatUUID.toString(), hash);
    }

}
