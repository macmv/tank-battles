package net.macmv.tankbattles.client;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.Timer;
import net.macmv.tankbattles.ClientGame;
import net.macmv.tankbattles.lib.proto.PlayerFireReq;
import net.macmv.tankbattles.player.Player;

import java.util.ArrayList;
import java.util.HashMap;

public class ClientThread {

  private ClientGame game;
  private TankBattlesClient client;
  private ArrayList<PlayerFireReq> fireQueue = new ArrayList<>();

  public ClientThread(ClientGame game) {
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
    }
    fireQueue.forEach(e -> {
      client.sendEvent(e);
    });
    fireQueue.clear();
  }

  public long getTick() {
    return client.getTick();
  }

  public void shutdown() throws InterruptedException {
    client.shutdown();
  }

  public void addFire(PlayerFireReq e) {
    fireQueue.add(e);
  }
}
