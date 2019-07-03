package net.macmv.tankbattles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.math.Vector2;
import net.macmv.tankbattles.player.Player;
import net.macmv.tankbattles.terrain.Terrain;

import java.util.HashMap;

public class Game {
  private final TankBattlesClient client;
  private final HashMap<Integer, Player> players;
  private final Player player;
  private final Terrain terrain;

  public Game(TankBattlesClient client) {
    this.client = client;
    player = new Player();
    players = client.newPlayer(player, this);
    terrain = new Terrain(this, Terrain.Type.GRASS);
  }

  public void update(float delta) {
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
      player.move(d, delta); // check move def to see why we don't call d.nor()
    }
    client.move(this, player);
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

  public void loadAssets(AssetManager assetManager) {
    player.loadAssets(assetManager);
    players.forEach((id, p) -> p.loadAssets(assetManager));
    terrain.loadAssets(assetManager);
  }

  public void finishLoading(AssetManager assetManager) {
    player.finishLoading(assetManager);
    players.forEach((id, p) -> p.finishLoading(assetManager));
    terrain.finishLoading(assetManager);
  }
}
