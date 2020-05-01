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

import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
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
        String cheat = checkType.getCheckName();
        Firestore db = FirestoreClient.getFirestore();
        DocumentReference docRef = db.collection("cheat-incidents").document(cheatUUID.toString());
        Map<String, Object> data = new HashMap<>();
        data.put("playerUUID", playerUUID.toString());
        data.put("cheat", cheat);
        data.put("timestamp", Instant.now().getEpochSecond());
        //asynchronously write data
        ApiFuture<WriteResult> result = docRef.set(data);
    }
}
