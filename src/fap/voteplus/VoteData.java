package fap.voteplus;

import cn.nukkit.Player;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.concurrent.atomic.AtomicInteger;

@AllArgsConstructor
@Data
public class VoteData {

    public AtomicInteger endTime;
    public Player orig; // 发起人
    public Player target; // 被发起
    public String reason; // 发起原因
    public int result; // 0=同意方 1=反对方 2=平局

}
