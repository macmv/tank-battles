package net.macmv.tankbattles.client;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.Timer;
import net.macmv.tankbattles.Game;
import net.macmv.tankbattles.lib.proto.PlayerEventReq;
import net.macmv.tankbattles.player.Player;

import java.util.ArrayList;
import java.util.HashMap;

public class ClientThread {

  private Game game;
  private TankBattlesClient client;
  private ArrayList<PlayerEventReq> eventQueue = new ArrayList<>();

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
        update(assetManager);
      }
    }, 0, 0.05f);
  }

  private void update(AssetManager assetManager) {
    synchronized (game.getPlayer()) {
      client.move(game, game.getPlayer(), assetManager);
      if (game.getNewProjectile() != null) {
        client.fire(game, game.getNewProjectile().getPos(), game.getNewProjectile().getVel());
        game.clearNewProjectile();
      }
    }
  }

  public long getTick() {
    return client.getTick();
  }

  public void shutdown() throws InterruptedException {
    client.shutdown();
  }

  public void addQueue(PlayerEventReq e) {
    eventQueue.add(e);
  }
}
