package net.macmv.tankbattles.server;

import com.badlogic.gdx.math.Vector2;
import net.macmv.tankbattles.lib.proto.PlayerMoveReq;
import net.macmv.tankbattles.lib.proto.Tank;
import net.macmv.tankbattles.player.Player;

import java.util.HashMap;

public class ServerGame {

  private final long tickStartTime;
  private HashMap<Integer, Player> players = new HashMap<>();
  private HashMap<Integer, Long> lastMove = new HashMap<>();

  public ServerGame() {
    tickStartTime = System.currentTimeMillis();
  }

  public void addPlayer(int id, Tank tank) {
    System.out.println("Adding Player");
    Player player = new Player(id, tank, false);
    players.put(id, player);
  }

  public HashMap<Integer, net.macmv.tankbattles.lib.proto.Player> getPlayers() {
    HashMap<Integer, net.macmv.tankbattles.lib.proto.Player> hash = new HashMap<>();
    players.forEach((id, p) -> {
      hash.put(id, p.toProto());
    });
    return hash;
  }

  public boolean checkAndMove(PlayerMoveReq req) {
    net.macmv.tankbattles.lib.proto.Player p = req.getPlayer();
    if (!lastMove.containsKey(p.getId())) {
      lastMove.put(p.getId(), getTick());
      return true;
    }
    if (req.getTick() == lastMove.get(p.getId())) { // new player, so no entry, or made two requests on same tick
      System.out.println("Skipping move req as they sent it twice in one tick");
      return true;
    }
    Vector2 newPos = new Vector2(p.getPos().getX(), p.getPos().getY());
    Vector2 oldPos = players.get(p.getId()).getPos();
    float distance = newPos.dst(oldPos);
    long ticks = req.getTick() - lastMove.get(p.getId());
    float speed = distance / (float) ticks; // tiles per tick
    lastMove.put(p.getId(), req.getTick());
    // TODO: implement getBaseStats(p.getTank().getBase().getId()).getSpeed()
    players.get(p.getId()).setTurretDirection(p.getTurretDirection());
    float allowedSpeed = 0.1f; // 0.1 tiles / tick allowed, aka 2 tiles / second
    if (speed < allowedSpeed * 1.5) { // 1.5 is to account for lag; this may allow players to speed hack, but we need to worry about slow connections more
      players.get(p.getId()).updatePos(newPos, p.getDirection());
      return true;
    } else {
      players.get(p.getId()).updatePos(oldPos, p.getDirection());
      return false;
    }
  }

  public long getTick() {
    return (System.currentTimeMillis() - tickStartTime) / 50;
  }
}
