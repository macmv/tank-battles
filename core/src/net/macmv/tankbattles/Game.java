package net.macmv.tankbattles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.math.Vector2;
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
    Vector2 d = new Vector2();
    if (Gdx.input.isKeyPressed(Input.Keys.W)) {
      d.y -= 1;
    }
    if (Gdx.input.isKeyPressed(Input.Keys.A)) {
      d.x -= 1;
    }
    if (Gdx.input.isKeyPressed(Input.Keys.S)) {
      d.y += 1;
    }
    if (Gdx.input.isKeyPressed(Input.Keys.D)) {
      d.x += 1;
    }
    if (!d.equals(Vector2.Zero)) {
      synchronized (player) {
        player.move(d, delta); // check move def to see why we don't call d.nor()
      }
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
