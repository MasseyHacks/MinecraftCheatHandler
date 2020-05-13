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
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

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
        long time = Instant.now().getEpochSecond();

        recordToFirestore(playerUUID, cheatedPlayer.getDisplayName(), cheatUUID, cheat, time);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                Jedis jedis = plugin.jedisPool.getResource();
                recordToRedis(jedis, playerUUID, cheatUUID, cheat, time);
            } catch (JedisConnectionException e) {
                plugin.connectToRedis(plugin.getConfig());
                Jedis jedis = plugin.jedisPool.getResource();
                recordToRedis(jedis, playerUUID, cheatUUID, cheat, time);
            }
        });

    }

    private void recordToFirestore(UUID playerUUID, String playerName, UUID cheatUUID, String cheat, long time) {
        Firestore db = FirestoreClient.getFirestore();
        DocumentReference docRef = db.collection("cheat-incidents").document(cheatUUID.toString());
        Map<String, Object> data = new HashMap<>();
        data.put("playerUUID", playerUUID.toString());
        data.put("playerName", playerName);
        data.put("cheat", cheat);
        data.put("timestamp", time);
        //asynchronously write data
        ApiFuture<WriteResult> result = docRef.set(data);

    }

    private void recordToRedis(Jedis jedis, UUID playerUUID, UUID cheatUUID, String cheat, long time) {

        jedis.zadd("cheat-player:" + playerUUID.toString(), time, "cheat-incident:" + cheatUUID.toString());

        Map<String, String> hash = new HashMap<>();
        hash.put("cheat", cheat);
        hash.put("timestamp", (Long.toString(time)));

        jedis.hset("cheat-incident:" + cheatUUID.toString(), hash);
        jedis.expire("cheat-incident:" + cheatUUID.toString(), plugin.getConfig().getInt("redisExpireTime"));
        jedis.close();
    }

}
