package fap.voteplus;

import cn.nukkit.Player;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class VotePlayer {

    public static List<VotePlayer> players = new ArrayList<>();

    public static VotePlayer get(String n) {
        return players.stream()
                .filter(mtp -> n.equals(mtp.getName()))
                .findFirst()
                .orElse(null);
    }

    public static VotePlayer get(Player player) {
        return get(player.getName());
    }

    public static void set(Player player, int mode) {
        players.add(new VotePlayer(player, mode));
    }

    public static boolean add(Player player, int mode) {
        if (get(player.getName()) == null) {
            set(player, mode);
            return true;
        }
        return false;
    }

    public static List<VotePlayer> winners() {
        return players.stream().filter(vp -> vp.mode == 0).toList();
    }

    public static List<VotePlayer> lost() {
        return players.stream().filter(vp -> vp.mode == 1).toList();
    }

    private final Player player;
    /**
     * 0=反对 1=同意
     */
    private final int mode;

    public String getName() {
        return player.getName();
    }

}
