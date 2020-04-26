package ca.masseyhacks.minecraftcheathandler.events;

import ca.masseyhacks.minecraftcheathandler.MinecraftCheatHandler;
import com.gmail.olexorus.witherac.api.CheckType;
import com.gmail.olexorus.witherac.api.ViolationEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void recordCheatIncident(Player cheatedPlayer, CheckType checkType) throws SQLException {
        UUID playerUUID = cheatedPlayer.getUniqueId();
        UUID cheatUUID = UUID.randomUUID();
        String cheat = checkType.getCheckName();
        Timestamp cheatTime = new Timestamp(Instant.now().getEpochSecond());
        String sql = "INSERT INTO CheatIncidents VALUES (" + cheatUUID + ", " + playerUUID + ", " + cheat + ", " + cheatTime + ") ";
        Statement stmt = plugin.connection.createStatement();
        stmt.executeQuery(sql);
    }
}
