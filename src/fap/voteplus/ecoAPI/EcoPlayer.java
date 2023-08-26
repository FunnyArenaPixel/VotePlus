package fap.voteplus.ecoAPI;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.ConfigSection;
import cn.nukkit.utils.LoginChainData;
import lombok.AllArgsConstructor;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

@AllArgsConstructor
public class EcoPlayer {

    public static HashMap<String, EcoPlayer> players = new HashMap<>();

    public static void set(String n, Config config) {
        var player = Server.getInstance().getPlayer(n.replaceAll("_", " "));
        if (player == null) return;
        players.put(n, new EcoPlayer(player, config, config.getRootSection(), player.getLoginChainData()));
    }

    public static EcoPlayer get(String n) {
        var player = Server.getInstance().getPlayerExact(
                n.replaceAll("_", " ")
        );
        if (player == null) {
            return null;
        }
        return players.getOrDefault(player.getName(), null);
    }

    public static void remove(String n) {
        EcoPlayer ecoPlayer = get(n);
        if (ecoPlayer != null) {
            players.remove(n);
        }
    }

    protected final Player player;
    private final Config config;
    private final ConfigSection configSection;
    private final LoginChainData loginChainData;

//    public EcoPlayer(Player player, Config config) {
//        this.player = player;
//        this.config = config;
//        this.configSection = config.getRootSection();
//        this.loginChainData = player.getLoginChainData();
//        checkBan();
//    }

    public void set(int money) {
        configSection.set("money", money);
    }

    public int get() {
        return configSection.getInt("money", 0);
    }

    public void reduce(int money) {
        set(Math.max(0, get() - money));
    }

    public void add(int money) {
        set(get() + money);
    }

    public void checkBan() {
        var banXboxId = configSection.getString("banXboxId");
        if (banXboxId.equalsIgnoreCase(loginChainData.getXUID()) && configSection.getBoolean("ban")) {
            long now = new Date().getTime();
            long after = config.getLong("banTime");
            if (now < after) {
                player.kick("你被封禁了！解除时间为：" + getDataFormat(after));
            } else {
                configSection.set("ban", false);
                configSection.set("banTime", 0);
                save();
            }
        }
    }

    public boolean setBan(int day, int sec) {
        if (configSection.getBoolean("ban")) {
            return false;
        }
        configSection.set("ban", true);
        configSection.set("banXboxId", loginChainData.getXUID());
        configSection.set("banTime", getDateAfter(day, sec));
        save();
        checkBan();
        return true;
    }

    public Long getDateAfter(int day, int sec) {
        Calendar now = Calendar.getInstance();
        now.setTime(new Date());
        now.set(Calendar.DATE, now.get(Calendar.DATE) + day);
        now.set(Calendar.SECOND, now.get(Calendar.SECOND) + sec);
        return now.getTime().getTime();
    }

    public String getDataFormat(long time) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日 HH时mm分ss秒");
        return format.format(new Date(time));
    }

    public void save() {
        config.setAll(configSection);
        config.save();
    }

}
