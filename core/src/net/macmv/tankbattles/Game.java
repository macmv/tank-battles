package net.macmv.tankbattles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import net.macmv.tankbattles.client.ClientThread;
import net.macmv.tankbattles.player.Player;
import net.macmv.tankbattles.terrain.Terrain;

import java.util.HashMap;

public class Game {
  private final ClientThread client;
  private final HashMap<Integer, Player> players;
  private final Player player;
  private final Terrain terrain;

  public Game() {
    this.client = new ClientThread(this);
    player = new Player();
    players = client.newPlayer();
    terrain = new Terrain(this, Terrain.Type.GRASS);
  }

  public void update(float delta, AssetManager assetManager) {
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
}
