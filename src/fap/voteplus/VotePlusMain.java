package fap.voteplus;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.event.Listener;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.*;
import fap.voteplus.ecoAPI.EcoPlayer;
import fap.voteplus.menu.VoteMenu;
import fap.voteplus.tasks.VoteTask;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

public class VotePlusMain extends PluginBase implements Listener {

    public VoteMenu menu;
    public Config config;
    /**
     * 投票数据
     */
    @Getter
    @Setter
    public VoteData voteData;
    /**
     * 投票冷却中
     */
    @Getter
    @Setter
    public boolean voteCountDown = false;
    @Getter
    @Setter
    public HashSet<VotePlayer> votingPlayers = new HashSet<>();
    public static final String[] modes = new String[]{"同意", "反对"};

    @Override
    public void onEnable() {
        if (!getDataFolder().mkdirs()) {
            debug("VotePlus Enabled!");
        }

        config = new Config(getDataFolder() + "/config.yml", 2, new ConfigSection() {{
            put("投票冷却时间", 60); // sec
            put("投票结束时间", 60); // sec
            put("未投票玩家默认结果", 1); // 0=赞成 1=反对
            put("同意该投票触发指令", new ArrayList<String>() {{
                add("kill %target"); // 对被投票的玩家
            }});
            put("反对该投票触发指令", new ArrayList<String>() {{
                add("kill %target");
                add("give %player 1 1");
            }});
        }});

        this.menu = new VoteMenu(this);
        getServer().getPluginManager().registerEvents(new VotePlusEvents(this), this);
    }

    public Config getPlayerConfig(Player player) {
        File file = new File(getDataFolder(), "players/" + player.getName());
        return new Config(file, 2, new ConfigSection() {{
            put("money", 0);
            put("ban", false);
            put("banXboxId", "0");
            put("banTime", 0);
        }});
    }

    public void startVote(Player originator, Player target, String reason) {
        // 服务器小于两人不能进行投票
        if (getServer().getOnlinePlayers().values().size() < 3 && !originator.isOp()) {
            originator.sendMessage("§c无法发起投票！人数不足！");
            return;
        }

        // 可以发起投票
        if (voteData == null && !voteCountDown) {
            // 创建投票
            voteData = new VoteData(
                    new AtomicInteger(config.getInt("投票结束时间")),
                    originator,
                    target,
                    reason,
                    config.getInt("未投票玩家默认结果")
            );
            // 发起人加入同意
            VotePlayer.add(originator, 1);
            // 投票倒计时
            getServer().getScheduler().scheduleRepeatingTask(new VoteTask(
                    this,
                    null,
                    voteData.getEndTime().get()
            ), 20);
            originator.sendMessage("§a开始对玩家 " + target.getName() + " 进行投票操作！");
        } else {
            originator.sendMessage("§c发起失败！当前正在投票中或冷却中...");
        }
    }

    public void checkVote() {
        int mode = config.getInt("未投票玩家默认结果");
        // 没有参与投票默认投票
        getServer().getOnlinePlayers().values().stream()
                .filter(player -> VotePlayer.get(player) == null)
                .forEach(player -> {
                    VotePlayer.set(player, mode);
                    player.sendMessage("你未进行投票！默认投票为： " + modes[mode]);
                });
        voteData.result = getVoteResult();
        VotePlayer.players.clear();
    }

    public void overVote() {
        checkVote();
        disCmd();
        getVotingPlayers().clear();
    }

    public void disCmd() {
        String cmdK;
        if (voteData.result == 1) {
            cmdK = "同意";
        } else {
            cmdK = "反对";
        }
        config.getStringList(cmdK + "该投票触发指令").forEach(s -> getServer().dispatchCommand(
                getServer().getConsoleSender(),
                s.replaceAll("%target", "\"" + voteData.target.getName() + "\"")
        ));
    }

    public int getVoteResult() {
        int win = VotePlayer.winners().size();
        int lost = VotePlayer.lost().size();
        int i;
        if (win > lost) {
            i = 0;
        } else if (win < lost) {
            i = 1;
        } else {
            i = 2;
        }
        return i;
    }

    public String getVoteResultMsg() {
        String[] strings = {
                "§a已同意该投票操作",
                "§c已反对该投票操作",
                "§b同意和反对一致，投票操作失效"
        };
        return strings[getVoteResult()];
    }

    public String getVoteStatus() {
        int win = VotePlayer.winners().size();
        int lost = VotePlayer.lost().size();

        String t = "正在对" + voteData.target.getName() + "进行投票操作！ 请在聊天栏输入数字（0=反对 1=同意）";
        String reason = "发起原因： " + voteData.reason;
        String result = "同意: " + win + " ".repeat(20) + " 反对: " + lost;

        return t + "\n\n" + reason + "\n" + result;
    }

    public void vote(Player player, int mode) {
        String t;
        if (voteData == null) {
            t = "当前没有发起投票！";
        } else {
            if (VotePlayer.get(player) == null) {
                VotePlayer.add(player, mode);
                t = "你已" + modes[mode] + "该投票！";
            } else {
                t = "你已参与这次投票了！";
            }
        }
        player.sendMessage(t);
    }

    public DummyBossBar creBossPar(Player player, String text, float health) {
        DummyBossBar.Builder builder = new DummyBossBar.Builder(player);
        builder.text(text);
        builder.length(health);
        builder.color(BossBarColor.PURPLE);

        DummyBossBar build = builder.build();
        build.create();

        return build;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String t = "";
        switch (command.getName()) {
            case "vote", "v" -> {
                // /vote <y/n>
                if (args.length >= 1 && sender instanceof Player player) {
                    switch (args[0]) {
                        case "y", "yes" -> vote(player, 0);
                        case "n", "no" -> vote(player, 1);
                        default -> {
                            // /vote <reason> <player>
                            if (args.length == 2) {
                                Player exact = getServer().getPlayerExact(args[1]);
                                if (exact == null) {
                                    t = "目标 " + args[1] + " 不存在！";
                                } else {
                                    startVote(player, exact, args[0]);
                                }
                            } else if (args.length == 1) { // /vote <reason>
                                menu.openMenu(player, args[0]);
                            }
                        }
                    }
                }
            }
            case "ban" -> { // /ban <player> <day> <sec>
                if (args.length == 3 && sender.isOp()) {
                    EcoPlayer ecoPlayer = EcoPlayer.get(args[0]);
                    if (ecoPlayer == null) {
                        sender.sendMessage("玩家不存在！");
                        return true;
                    }
                    if (ecoPlayer.setBan(Integer.parseInt(args[1]), Integer.parseInt(args[2]))) {
                        t = "已封禁该玩家！";
                    } else {
                        t = "该玩家封禁中！";
                    }
                }
            }
            case "addmoney" -> { // /addMoney <player> <money>
                if (args.length == 2 && sender.isOp()) {
                    EcoPlayer ecoPlayer = EcoPlayer.get(args[0]);
                    if (ecoPlayer == null) {
                        sender.sendMessage("玩家： " + args[0] + " 不存在！");
                        return true;
                    }
                    ecoPlayer.add(Integer.parseInt(args[1]));
                    t = "增加玩家： " + args[0] + " 金币: " + args[1];
                } else {
                    t = "权限不足！";
                }
            }
            case "reducemoney" -> {
                if (args.length == 2 && sender.isOp()) {
                    EcoPlayer ecoPlayer = EcoPlayer.get(args[0]);
                    if (ecoPlayer == null) {
                        sender.sendMessage("玩家： " + args[0] + " 不存在！");
                        return true;
                    }
                    ecoPlayer.reduce(Integer.parseInt(args[1]));
                    t = "减少玩家： " + args[0] + " 金币: " + args[1];
                }
            }
        }
        if (!t.isEmpty()) {
            sender.sendMessage(t);
        }
        return true;
    }

    public static void debug(String debug) {
        MainLogger.getLogger().notice(debug);
    }
}
