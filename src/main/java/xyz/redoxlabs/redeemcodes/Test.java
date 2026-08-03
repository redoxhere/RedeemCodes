package xyz.redoxlabs.redeemcodes;
import com.tcoded.folialib.FoliaLib;
import org.bukkit.entity.Player;
public class Test {
    public void foo(FoliaLib foliaLib, Player p) {
        foliaLib.getImpl().runLater((task) -> {}, 1L, java.util.concurrent.TimeUnit.SECONDS);
    }
}
