package net.macmv.tankbattles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import net.macmv.tankbattles.player.Player;

import java.util.HashMap;

public class Game {
  private final TankBattlesClient client;
  private final HashMap<Integer, Player> players;
  private final Player player;

  public Game(TankBattlesClient client) {
    this.client = client;
    player = new Player();
    players = client.newPlayer(player, this);
    System.out.println("Players: " + players);
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
    player.move(d.nor(), delta);
    client.move(this, player);
  }

  public void dispose() {
  }

  public Player getPlayer() {
    return player;
  }

  public HashMap<Integer, Player> getPlayers() {
    return players;
  }
}
