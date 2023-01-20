package fap.voteplus.tasks;

import cn.nukkit.Server;
import cn.nukkit.utils.BossBarColor;
import cn.nukkit.utils.DummyBossBar;
import cn.nukkit.utils.TextFormat;
import fap.voteplus.VoteData;
import fap.voteplus.VotePlus;

public class VoteTask extends Thread {

    private final VotePlus votePlus;
    private final Server server = Server.getInstance();
    private DummyBossBar dummyBossBar;

    public VoteTask(VotePlus votePlus) {
        this.votePlus = votePlus;
    }

    @Override
    public void run() {
        while (true) {
            try {
                VoteData voteData = votePlus.voteData;
                voteData.minOver--;

                // show status
                if (voteData.minOver > 0) {
                    server.getOnlinePlayers().values().forEach(player -> {
                        String color = voteData.minOver % 2 == 0 ? TextFormat.RED.toString() : TextFormat.WHITE.toString();
                        float time = Math.max(0, voteData.minOver * 100f / voteData.maxOver);
                        color = color + votePlus.getVoteStatus();
                        if (dummyBossBar == null) {
                            dummyBossBar = votePlus.creBossPar(player, color, time);
                        } else {
                            dummyBossBar.setColor(BossBarColor.PURPLE);
                            dummyBossBar.setLength(time);
                            dummyBossBar.setText(color);
                        }
                    });
                }

                if (voteData.minOver == 0) {
                    dummyBossBar.setLength(100f);
                    dummyBossBar.setColor(BossBarColor.GREEN);
                    dummyBossBar.setText(votePlus.getVoteResultMsg());
                    server.broadcastMessage(votePlus.getVoteResultMsg());
                    votePlus.overVote();
                }

                if (voteData.minOver == -5) {
                    dummyBossBar.destroy();
                    // 进入冷却倒计时
                    VotePlus.executor.execute(() -> (votePlus.voteCdTask = new VoteCdTask(votePlus)).start());
                    break;
                }

                sleep(1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
