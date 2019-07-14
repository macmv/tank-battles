package net.macmv.tankbattles.lib;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.math.Vector3;
import net.macmv.tankbattles.collision.CollisionManager;
import net.macmv.tankbattles.player.Player;
import net.macmv.tankbattles.projectile.Projectile;
import net.macmv.tankbattles.terrain.Terrain;

import java.util.ArrayList;
import java.util.HashMap;

public interface Game {
  CollisionManager getCollisionManager();

  void update(float deltaTime, AssetManager assetManager);

  void shutdown() throws InterruptedException;

  Player getPlayer();

  void requireAssets(AssetManager assetManager);

  void loadAssets(AssetManager assetManager);

  void sendProjectile(Vector3 pos, Vector3 direction);

  Terrain getTerrain();

  HashMap<Integer, Player> getPlayers();

  ArrayList<Projectile> getProjectiles();
}
