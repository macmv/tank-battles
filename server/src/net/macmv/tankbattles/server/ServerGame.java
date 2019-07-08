package net.macmv.tankbattles.server;

import com.badlogic.gdx.math.Vector3;
import net.macmv.tankbattles.lib.proto.*;
import net.macmv.tankbattles.player.Player;
import net.macmv.tankbattles.projectile.Projectile;

import java.util.HashMap;

public class ServerGame {

  private final long tickStartTime;
  private HashMap<Integer, Player> players = new HashMap<>();
  private HashMap<Integer, Long> lastMove = new HashMap<>();
  private HashMap<Integer, Projectile> projectiles = new HashMap<>();
  private long lastProjectileUpdate;

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

  public long getTick() {
    return (System.currentTimeMillis() - tickStartTime) / 50;
  }

  public PlayerMoveRes checkAndMove(PlayerMoveReq req, long tick) {
    net.macmv.tankbattles.lib.proto.Player p = req.getPlayer();
    if (!lastMove.containsKey(p.getId())) { // new player
      lastMove.put(p.getId(), getTick());
      return null;
    }
    if (tick == lastMove.get(p.getId())) {
      System.out.println("Skipping move req as they sent it twice in one tick");
      return null;
    }
    Vector3 newPos = new Vector3(p.getPos().getX(), p.getPos().getY(), p.getPos().getZ());
    Vector3 oldPos = players.get(p.getId()).getPos();
    float distance = newPos.dst(oldPos);
    long ticks = tick - lastMove.get(p.getId());
    float speed = distance / (float) ticks; // tiles per tick
    lastMove.put(p.getId(), tick);
    // TODO: implement getBaseStats(p.getTank().getBase().getId()).getSpeed()
    players.get(p.getId()).setTurretDirection(p.getTurretDirection());
    float allowedSpeed = 0.1f; // 0.1 tiles / tick allowed, aka 2 tiles / second
    if (speed < allowedSpeed * 1.5) { // 1.5 is to account for lag; this may allow players to speed hack, but we need to worry about slow connections more
      players.get(p.getId()).updatePos(newPos, p.getDirection());
    } else {
      players.get(p.getId()).updatePos(oldPos, p.getDirection());
    }
    PlayerMoveRes.Builder res = PlayerMoveRes.newBuilder();
    players.values().forEach(player -> {
      res.addPlayer(player.toProto());
    });
    return res.build();
  }

  public void checkFire(PlayerFireReq req, long tick) {
    Player player = players.get(req.getPlayerId());
    Vector3 pos = new Vector3(req.getProjectilePos().getX(), req.getProjectilePos().getY(), req.getProjectilePos().getZ());
    Vector3 vel = new Vector3(req.getProjectileVel().getX(), req.getProjectileVel().getY(), req.getProjectileVel().getZ());
    if (pos.dst(player.getPos()) < 5) { // close enough
      int id = (int) (Math.random() * Integer.MAX_VALUE);
      projectiles.put(id, new Projectile(pos, vel, id, false));
    } else {
      System.out.println("Player sent invalid projectile");
    }
  }

  public PlayerFireRes generateFireRes() {
    PlayerFireRes.Builder res = PlayerFireRes.newBuilder();
    if (lastProjectileUpdate == 0) {
      lastProjectileUpdate = System.currentTimeMillis();
    }
    float projectileUpdateDelta = (System.currentTimeMillis() - lastProjectileUpdate) / 1000f;
    projectiles.values().forEach(projectile -> {
      projectile.update(projectileUpdateDelta);
      res.addProjectile(projectile.toProto());
    });
    lastProjectileUpdate = System.currentTimeMillis();
    System.out.println("Sending fireRes " + res);
    return res.build();
  }
}
