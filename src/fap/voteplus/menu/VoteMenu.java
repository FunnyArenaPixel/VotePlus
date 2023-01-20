package fap.voteplus.menu;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.window.FormWindowSimple;
import fap.voteplus.utils.MyForm;
import fap.voteplus.VotePlus;

public class VoteMenu {

    private final fap.voteplus.VotePlus votePlus;

    public VoteMenu(VotePlus votePlus) {
        this.votePlus = votePlus;
    }

    public void openMenu(Player player, String reason) {
        FormWindowSimple f = new FormWindowSimple(
                "Vote Menu",
                "请选择下面玩家进行投票操作\n" +
                        "发起原因： " + reason
        );

        Server.getInstance().getOnlinePlayers().values().forEach(
                player1 -> f.addButton(new ElementButton(player1.getName()))
        );

        MyForm myForm = new MyForm(player, f) {
            @Override
            public void call() {
                if (wasClosed()) {
                    return;
                }
                sureMenu(player, Server.getInstance().getPlayerExact(getButtonText()), reason);
            }
        };
        myForm.sendToPlayer(player);
    }

    private void sureMenu(Player player, Player target, String reason) {
        FormWindowSimple f = new FormWindowSimple("Vote Menu", "你确定要对该玩家进行投票操作吗？");

        for (String s : new String[]{"§a确定", "§c取消"}) {
            f.addButton(new ElementButton(s));
        }

        MyForm myForm = new MyForm(player, f) {
            @Override
            public void call() {
                if (wasClosed()) {
                    return;
                }
                if (getButtonText().equalsIgnoreCase("§c取消")) {
                    openMenu(player, reason);
                } else {
                    Server.getInstance().dispatchCommand(
                            player, "vote " + reason + " " + "\"" + target.getName() + "\""
                    );
                }
            }
        };
        myForm.sendToPlayer(player);
    }

}
