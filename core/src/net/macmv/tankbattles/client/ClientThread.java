package net.macmv.tankbattles.client;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.Timer;
import net.macmv.tankbattles.Game;
import net.macmv.tankbattles.player.Player;

import java.util.HashMap;

public class ClientThread {

  private Game game;
  private TankBattlesClient client;

  public ClientThread(Game game) {
    this.client = new TankBattlesClient("192.168.0.45", 8001);
    this.game = game;
  }

  public HashMap<Integer, Player> newPlayer() {
    return client.newPlayer(game.getPlayer(), game);
  }

  public void startMoveThread(AssetManager assetManager) {
    Timer.schedule(new Timer.Task() {
      @Override
      public void run() {
        move(assetManager);
      }
    }, 0, 0.05f);
  }

  private void move(AssetManager assetManager) {
    synchronized (game.getPlayer()) {
      client.move(game, game.getPlayer(), assetManager);
    }
  }

  public long getTick() {
    return client.getTick();
  }

  public void shutdown() throws InterruptedException {
    client.shutdown();
  }
}
