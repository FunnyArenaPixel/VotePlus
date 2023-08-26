package fap.voteplus.tasks;

import cn.nukkit.Server;
import cn.nukkit.scheduler.Task;
import cn.nukkit.utils.BossBarColor;
import cn.nukkit.utils.DummyBossBar;
import cn.nukkit.utils.TextFormat;
import fap.voteplus.VotePlusMain;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class VoteTask extends Task {

    private VotePlusMain votePlusMain;
    private final Server server = Server.getInstance();
    private DummyBossBar dummyBossBar;
    private int endTime;

    @Override
    public void onRun(int i) {
        var voteData = votePlusMain.getVoteData();
        if (voteData != null) {
            var decrementAndGet = voteData.getEndTime().decrementAndGet();
            if (decrementAndGet == 0) {
                dummyBossBar.setLength(100f);
                dummyBossBar.setColor(BossBarColor.GREEN);
                dummyBossBar.setText(votePlusMain.getVoteResultMsg());
                server.broadcastMessage(votePlusMain.getVoteResultMsg());
                votePlusMain.overVote();
            }
            if (decrementAndGet == -5) {
                this.cancel();
                dummyBossBar.destroy();
                // 进入冷却倒计时
                votePlusMain.setVoteCountDown(true);
                Server.getInstance().getScheduler().scheduleDelayedTask(new Task() {
                    @Override
                    public void onRun(int i) {
                        votePlusMain.setVoteData(null);
                        votePlusMain.setVoteCountDown(false);
                    }
                }, votePlusMain.getConfig().getInt("投票冷却时间") * 20);
            }

            server.getOnlinePlayers().values().forEach(player -> {
                String color = decrementAndGet % 2 == 0 ? TextFormat.RED.toString() : TextFormat.WHITE.toString();
                float time = Math.max(0, decrementAndGet * 100f / endTime);
                color = color + votePlusMain.getVoteStatus();
                if (dummyBossBar == null) {
                    dummyBossBar = votePlusMain.creBossPar(player, color, time);
                } else {
                    dummyBossBar.setColor(BossBarColor.PURPLE);
                    dummyBossBar.setLength(time);
                    dummyBossBar.setText(color);
                }
            });
        }
    }

}
