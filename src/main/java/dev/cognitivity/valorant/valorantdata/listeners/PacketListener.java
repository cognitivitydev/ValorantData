package dev.cognitivity.valorant.valorantdata.listeners;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import dev.cognitivity.valorant.valorantdata.ValorantData;
import org.bukkit.entity.Player;

public class PacketListener extends PacketListenerAbstract {
    
    public PacketListener() {
        super(PacketListenerPriority.HIGHEST);
    }
    
    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        Player player = (Player) event.getPlayer();
        if (!ValorantData.getInstance().isLoading() && player != null && ValorantData.getInstance().getData(player) == null
                && event.getPacketType() != PacketType.Login.Server.DISCONNECT) {
            ValorantData.getInstance().createData(player);
        }
    }
    
    @Override
    public void onPacketSend(PacketSendEvent event) {
        Player player = (Player) event.getPlayer();
        if (!ValorantData.getInstance().isLoading() && player != null && ValorantData.getInstance().getData(player) == null
                && event.getPacketType() != PacketType.Login.Server.DISCONNECT) {
            ValorantData.getInstance().createData(player);
        }
    }
}
