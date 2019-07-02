package net.macmv.tankbattles.server;

import com.badlogic.gdx.math.Vector2;
import net.macmv.tankbattles.lib.proto.PlayerMoveReq;
import net.macmv.tankbattles.lib.proto.Point;
import net.macmv.tankbattles.lib.proto.Tank;
import net.macmv.tankbattles.player.Player;

import java.util.HashMap;

public class ServerGame {

  private HashMap<Integer, Player> players = new HashMap<>();
  private HashMap<Integer, Long> lastMove = new HashMap<>();

  public ServerGame() {

  }

  public void addPlayer(int id, Tank tank) {
    System.out.println("Adding Player");
    Player player = new Player(id, tank, false);
    player.updatePos(Vector2.Zero);
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
      lastMove.put(p.getId(), System.nanoTime());
      return true;
    }
    Vector2 newPos = new Vector2(p.getPos().getX(), p.getPos().getY());
    Vector2 oldPos = players.get(p.getId()).getPos();
    float distance = newPos.dst(oldPos);
    long time = System.nanoTime() - lastMove.get(p.getId());
    float speed = distance / ((float) time / 1000000000);
    lastMove.put(p.getId(), System.nanoTime());
    // TODO: implement getBaseStats(p.getTank().getBase().getId()).getSpeed()
    float allowedSpeed = 1;
    if (speed < allowedSpeed * 1.5) { // 1.5 is to account for lag; this may allow players to speed hack,
      players.get(p.getId()).updatePos(newPos); //      but we need to worry about slow connections more
      return true;
    } else {
      players.get(p.getId()).updatePos(oldPos);
      return false;
    }
  }
}
