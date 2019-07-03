package net.macmv.tankbattles;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.math.Vector2;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import net.macmv.tankbattles.lib.proto.*;
import net.macmv.tankbattles.player.Player;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class TankBattlesClient {
  private static final Logger logger = Logger.getLogger(TankBattlesClient.class.getName());

  private final ManagedChannel channel;
  private final TankBattlesGrpc.TankBattlesBlockingStub blockingStub;

  public TankBattlesClient(String host, int port) {
    this(ManagedChannelBuilder.forAddress(host, port)
            .usePlaintext()
            .build());
  }

  TankBattlesClient(ManagedChannel channel) {
    this.channel = channel;
    blockingStub = TankBattlesGrpc.newBlockingStub(channel);
  }

  public void shutdown() throws InterruptedException {
    channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
  }

  public void move(Game game, Player player, AssetManager assetManager) {
    PlayerMoveReq.Builder req = PlayerMoveReq.newBuilder();
    Player sentPlayer = player;
    Vector2 sentPos = sentPlayer.getPos();
    req.setPlayer(sentPlayer.toProto());
    req.setNewPos(Point.newBuilder().setX(player.getPos().x).setY(player.getPos().y).build());
    PlayerMoveRes res;
    try {
      res = blockingStub.playerMove(req.build());
    } catch (StatusRuntimeException e) {
      try {
        shutdown();
      } catch (InterruptedException ex) {
        ex.printStackTrace();
      }
      e.printStackTrace();
      throw new RuntimeException("Could not connect to server");
    }
    HashMap<Integer, Player> hash = game.getPlayers();
    res.getPlayerList().forEach(p -> {
      if (p.getId() == game.getPlayer().getId()) {
        if (!(new Vector2(p.getPos().getX(), p.getPos().getY()).equals(sentPos))) {
          game.getPlayer().updatePos(p.getPos());
        }
      } else {
        if (hash.containsKey(p.getId())) {
          hash.get(p.getId()).updatePos(p.getPos());
        } else { // new player
          Player newPlayer = Player.fromProto(p);
          newPlayer.loadAssets(assetManager);
          hash.put(p.getId(), newPlayer);
        }
      }
    });
  }

  public HashMap<Integer, Player> newPlayer(Player player, Game game) {
    PlayerJoinReq.Builder req = PlayerJoinReq.newBuilder();
    req.setId(player.getId());
    req.setTank(player.getTank().toProto());
    PlayerJoinRes res;
    try {
      res = blockingStub.playerJoin(req.build());
    } catch (StatusRuntimeException e) {
      try {
        shutdown();
      } catch (InterruptedException ex) {
        ex.printStackTrace();
      }
      e.printStackTrace();
      throw new RuntimeException("Could not connect to server");
    }
    HashMap<Integer, Player> hash = new HashMap<>();
    res.getPlayerList().forEach(p -> {
      if (p.getId() != game.getPlayer().getId()) {
        hash.put(p.getId(), Player.fromProto(p));
      }
    });
    return hash;
  }
}
