package net.macmv.tankbattles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.math.Vector3;
import net.macmv.tankbattles.client.ClientThread;
import net.macmv.tankbattles.collision.CollisionManager;
import net.macmv.tankbattles.lib.Game;
import net.macmv.tankbattles.lib.proto.PlayerFireReq;
import net.macmv.tankbattles.lib.proto.Point3;
import net.macmv.tankbattles.lib.proto.TerrainMap;
import net.macmv.tankbattles.player.Player;
import net.macmv.tankbattles.projectile.Projectile;
import net.macmv.tankbattles.terrain.Terrain;

import java.util.HashMap;

public class ClientGame implements Game {
  private final ClientThread client;
  private final HashMap<Integer, Player> players;
  private final Player player;
  private Terrain terrain;
  private final CollisionManager collisionManager;
  private HashMap<Integer, Projectile> projectiles = new HashMap<>();

  public ClientGame() {
    this.client = new ClientThread(this);
    collisionManager = new CollisionManager();
    player = new Player(collisionManager);
    players = client.newPlayer();
  }

  public void loadMap(TerrainMap map) {
    terrain = new Terrain(this, map);
  }

  public void update(float delta, AssetManager assetManager) {
    collisionManager.update(delta);
    int left = 0;
    int right = 0;
    if (Gdx.input.isKeyPressed(Input.Keys.Q)) {
      left += 1;
    }
    if (Gdx.input.isKeyPressed(Input.Keys.A)) {
      left -= 1;
    }
    if (Gdx.input.isKeyPressed(Input.Keys.E)) {
      right += 1;
    }
    if (Gdx.input.isKeyPressed(Input.Keys.D)) {
      right -= 1;
    }
    synchronized (player) {
      player.move(right, left, delta); // check move def to see why we don't call d.nor()
    }
    projectiles.forEach((id, p) -> {
      p.update();
    });
  }

  public Player getPlayer() {
    return player;
  }

  public HashMap<Integer, Player> getPlayers() {
    return players;
  }

  public Terrain getTerrain() {
    return terrain;
  }

  public void requireAssets(AssetManager assetManager) {
    player.requireAssets(assetManager);
    players.forEach((id, p) -> p.requireAssets(assetManager));
    terrain.requireAssets(assetManager);
  }

  public void loadAssets(AssetManager assetManager) {
    player.loadAssets(assetManager);
    players.forEach((id, p) -> p.loadAssets(assetManager));
    terrain.loadAssets(assetManager);
    client.startMoveThread(assetManager);
  }

  public void shutdown() throws InterruptedException {
    client.shutdown();
  }

  public void addProjectile(Point3 pos, Point3 vel, int id) {
    Projectile projectile = new Projectile(
            new Vector3(pos.getX(), pos.getY(), pos.getZ()),
            new Vector3(vel.getX(), vel.getY(), vel.getZ()),
            id,
            this);
    projectiles.put(id, projectile);
  }

  public void sendProjectile(Vector3 pos, Vector3 vel) {
    PlayerFireReq.Builder fireReq = PlayerFireReq.newBuilder();
    fireReq.setPlayerId(player.getId());
    fireReq.setProjectilePos(Point3.newBuilder().setX(pos.x).setY(pos.y).setZ(pos.z).build());
    fireReq.setProjectileVel(Point3.newBuilder().setX(vel.x).setY(vel.y).setZ(vel.z).build());
    client.addFire(fireReq.build());
  }

  public HashMap<Integer, Projectile> getProjectiles() {
    return projectiles;
  }

  @Override
  public void destroyProjectile(Projectile projectile) {
    projectiles.remove(projectile.id);
  }

  @Override
  public void fire() {
    player.fire(this);
  }

  public Projectile getProjectile(int id) {
    return projectiles.get(id);
  }

  public CollisionManager getCollisionManager() {
    return collisionManager;
  }
}
