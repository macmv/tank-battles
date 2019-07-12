package net.macmv.tankbattles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.math.Vector3;
import net.macmv.tankbattles.client.ClientThread;
import net.macmv.tankbattles.collision.CollisionManager;
import net.macmv.tankbattles.lib.Game;
import net.macmv.tankbattles.lib.proto.Point3;
import net.macmv.tankbattles.player.Player;
import net.macmv.tankbattles.projectile.Projectile;
import net.macmv.tankbattles.terrain.Terrain;

import java.util.ArrayList;
import java.util.HashMap;

public class ClientGame implements Game {
  private final ClientThread client;
  private final HashMap<Integer, Player> players;
  private final Player player;
  private final Terrain terrain;
  private final CollisionManager collisionManager;
  private ArrayList<Projectile> projectiles = new ArrayList<>();
  private Projectile newProjectile;

  public ClientGame() {
    this.client = new ClientThread(this);
    collisionManager = new CollisionManager();
    player = new Player(collisionManager);
    players = client.newPlayer();
    terrain = new Terrain(this, Terrain.Type.GRASS);
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
    projectiles.forEach(p -> {
      p.update(delta);
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

  public long currentTick() {
    return client.getTick();
  }

  public void shutdown() throws InterruptedException {
    client.shutdown();
  }

  public void addProjectile(Point3 pos, Point3 vel, int id) {
    projectiles.add(new Projectile(
            new Vector3(pos.getX(), pos.getY(), pos.getZ()),
            new Vector3(vel.getX(), vel.getY(), vel.getZ()), id));
  }

  public void sendProjectile(Vector3 pos, Vector3 vel) {
    newProjectile = new Projectile(pos, vel, 0);
  }

  public ArrayList<Projectile> getProjectiles() {
    return projectiles;
  }

  public Projectile getProjectile(int id) {
    for (Projectile p : projectiles) {
      if (p.id == id) {
        return p;
      }
    }
    return null;
  }

  public Projectile getNewProjectile() {
    return newProjectile;
  }

  public void clearNewProjectile() {
    newProjectile = null;
  }

  public CollisionManager getCollisionManager() {
    return collisionManager;
  }
}
