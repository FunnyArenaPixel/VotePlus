package fap.voteplus;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerChatEvent;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.utils.TextFormat;
import fap.voteplus.ecoAPI.EcoPlayer;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class VotePlusEvents implements Listener {

    private final VotePlusMain main;

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        var player = event.getPlayer();
        EcoPlayer.set(player.getName(), main.getPlayerConfig(player));
        // 如果当前服务器正在投票中，加入投票
        if (main.getVoteData() != null) {
            main.vote(player, main.getConfig().getInt("未投票玩家默认结果"));
            player.sendMessage(TextFormat.colorize(
                    "&a你已加入了投票！请在聊天栏输入数字！0=反对 1=同意"
            ));
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        var player = event.getPlayer();
        var ecoPlayer = EcoPlayer.get(player.getName());
        if (ecoPlayer != null) {
            ecoPlayer.save();
        }
        EcoPlayer.remove(player.getName());
        main.getVotingPlayers().removeIf(vp -> player.getName().equalsIgnoreCase(vp.getName()));
    }

    @EventHandler
    public void onChat(PlayerChatEvent event) {
        var player = event.getPlayer();
        var message = event.getMessage();
        if (main.getVoteData() != null) {
            var sub = message.charAt(0);
            try {
                var i = Integer.parseInt(String.valueOf(sub));
                if (i == 0 || i == 1) {
                    main.vote(player, i);
                }
            } catch (NumberFormatException ignored) {

            }
        }
    }

}
