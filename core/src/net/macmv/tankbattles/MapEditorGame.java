package net.macmv.tankbattles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.math.Vector3;
import net.macmv.tankbattles.collision.CollisionManager;
import net.macmv.tankbattles.lib.Game;
import net.macmv.tankbattles.player.Player;
import net.macmv.tankbattles.projectile.Projectile;
import net.macmv.tankbattles.terrain.Terrain;

import java.util.ArrayList;
import java.util.HashMap;

public class MapEditorGame implements Game {
  private final Player player;
  private Terrain terrain;
  private final CollisionManager collisionManager;
  private ArrayList<Projectile> projectiles = new ArrayList<>();

  public MapEditorGame() {
    collisionManager = new CollisionManager();
    player = new Player(collisionManager);
    terrain = new Terrain(this, "maps/tmp.map");
  }

  @Override
  public CollisionManager getCollisionManager() {
    return collisionManager;
  }

  @Override
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

  @Override
  public void shutdown() throws InterruptedException {

  }

  @Override
  public Player getPlayer() {
    return player;
  }

  @Override
  public void requireAssets(AssetManager assetManager) {
    player.requireAssets(assetManager);
    terrain.requireAssets(assetManager);
  }

  @Override
  public void loadAssets(AssetManager assetManager) {
    player.loadAssets(assetManager);
    terrain.loadAssets(assetManager);
  }

  @Override
  public void sendProjectile(Vector3 pos, Vector3 vel) {
    projectiles.add(new Projectile(pos, vel, 0));
  }

  @Override
  public Terrain getTerrain() {
    return terrain;
  }

  @Override
  public HashMap<Integer, Player> getPlayers() {
    return new HashMap<>();
  }

  @Override
  public ArrayList<Projectile> getProjectiles() {
    return projectiles;
  }
}
