package net.macmv.tankbattles.server;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.math.Vector3;
import net.macmv.tankbattles.collision.CollisionManager;
import net.macmv.tankbattles.lib.Game;
import net.macmv.tankbattles.lib.proto.*;
import net.macmv.tankbattles.player.Player;
import net.macmv.tankbattles.projectile.Projectile;
import net.macmv.tankbattles.terrain.Terrain;

import java.util.ArrayList;
import java.util.HashMap;

public class ServerGame implements Game {

  private final long tickStartTime;
  private final Terrain terrain;
  private HashMap<Integer, Player> players = new HashMap<>();
  private HashMap<Integer, Long> lastMove = new HashMap<>();
  private HashMap<Integer, Projectile> projectiles = new HashMap<>();
  private long lastProjectileUpdate;
  private CollisionManager collisionManager;
  private long lastCollisionUpdate;

  public ServerGame() {
    tickStartTime = System.currentTimeMillis();
    lastCollisionUpdate = System.currentTimeMillis();
    collisionManager = new CollisionManager(this, false);
    terrain = new Terrain(this, "maps/tmp.map", false);
  }

  public void addPlayer(int id, Tank tank) {
    System.out.println("Adding Player");
    Player player = new Player(collisionManager, id, tank, false);
    players.put(id, player);
  }

  public HashMap<Integer, net.macmv.tankbattles.lib.proto.Player> getProtoPlayers() {
    HashMap<Integer, net.macmv.tankbattles.lib.proto.Player> hash = new HashMap<>();
    players.forEach((id, p) -> {
      hash.put(id, p.toProto());
    });
    return hash;
  }

  @Override
  public HashMap<Integer, Projectile> getProjectiles() {
    return null;
  }

  @Override
  public void destroyProjectile(Projectile projectile) {
    System.out.println("DELETING PROJECTILE: " + projectile);
    projectile.destroy();
    projectiles.remove(projectile.id);
  }

  @Override
  public void fire() {
    // client only
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
    Vector3 newPos = new Vector3(p.getPos().getX(), 0, p.getPos().getZ());
    Vector3 oldPos = players.get(p.getId()).getPos().cpy();
    float distance = newPos.dst(oldPos);
    long ticks = tick - lastMove.get(p.getId());
    float speed = distance / (float) ticks; // tiles per tick
    lastMove.put(p.getId(), tick);
    // TODO: implement getBaseStats(p.getTank().getBase().getId()).getSpeed()
    players.get(p.getId()).setTurretDirection(p.getTurretDirection());
    float allowedSpeed = 1f; // 0.1 tiles / tick allowed, aka 2 tiles / second
    lastCollisionUpdate = System.currentTimeMillis();
//    System.out.println("Speed: " + speed);
    if (speed < allowedSpeed * 1.5) { // 1.5 is to account for lag; this may allow players to speed hack, but we need to worry about slow connections more
      players.get(p.getId()).setPos(newPos, p.getDirection());
//      System.out.println("Sending player move res at newPos: " + newPos);
    } else {
      players.get(p.getId()).setPos(oldPos, p.getDirection());
//      System.out.println("Sending player move res at oldPos: " + oldPos);
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
      System.out.println("Adding projectile, vel: " + vel);
      addProjectile(id, new Projectile(pos, vel, id, this, false));
    } else {
      System.out.println("Player sent invalid projectile");
    }
  }

  private void addProjectile(int id, Projectile projectile) {
    projectiles.put(id, projectile);
//    projectile.cl = new CollisionManager.OnContact();
//    projectile.cl.onContactStarted(projectile.getHitbox(), null);
  }

  public PlayerFireRes generateFireRes() {
    PlayerFireRes.Builder res = PlayerFireRes.newBuilder();
    if (lastProjectileUpdate == 0) {
      lastProjectileUpdate = System.currentTimeMillis();
    }
    projectiles.values().forEach(projectile -> {
      res.addProjectile(projectile.toProto());
    });
    lastProjectileUpdate = System.currentTimeMillis();
    return res.build();
  }

  @Override
  public CollisionManager getCollisionManager() {
    return collisionManager;
  }

  @Override
  public void update(float deltaTime, AssetManager assetManager) {
    collisionManager.update(deltaTime / 1000);
//    printDebugWorld();
  }

  private void printDebugWorld() {
    ArrayList<ArrayList<Character>> outputString = new ArrayList<>();
    for(int y = 0; y < 20; y++) {
      ArrayList<Character> line = new ArrayList<>();
      for(int x = 0; x < 20; x++) {
        line.add('_');
      }
      outputString.add(line);
    }
//    HashMap<Vector3, Tile> tiles = terrain.getTiles();
//    tiles.forEach((pos, tile) -> {
//      outputString.get((int) pos.y).set((int) pos.z, 'T');
//    });
    players.forEach((id, p) -> {
      outputString.get((int) p.getPos().y).set((int) p.getPos().z, 'P');
    });
    projectiles.forEach((id, p) -> {
      outputString.get((int) p.getPos().y).set((int) p.getPos().z, 'S');
    });
    System.out.println("World from YZ plane:");
    for (int y = outputString.size() - 1; y >= 0; y--) {
      for (char character : outputString.get(y)) {
        System.out.print(character);
        System.out.print(character);
      }
      System.out.println();
    }
  }

  @Override
  public void shutdown() throws InterruptedException {
    // client only
  }

  @Override
  public Player getPlayer() {
    // client only
    return null;
  }

  @Override
  public void requireAssets(AssetManager assetManager) {
    // client only
  }

  @Override
  public void loadAssets(AssetManager assetManager) {
    // client only
  }

  @Override
  public void sendProjectile(Vector3 pos, Vector3 direction) {
    // client only
  }

  public Terrain getTerrain() {
    return terrain;
  }

  @Override
  public HashMap<Integer, Player> getPlayers() {
    // client only, use getProtoPlayers() for server, as they return different objects
    return null;
  }
}
