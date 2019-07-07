package net.macmv.tankbattles.client;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.math.Vector3;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import net.macmv.tankbattles.Game;
import net.macmv.tankbattles.lib.proto.*;
import net.macmv.tankbattles.player.Player;

import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class TankBattlesClient {
  private static final Logger logger = Logger.getLogger(TankBattlesClient.class.getName());

  private final ManagedChannel channel;
  private final TankBattlesGrpc.TankBattlesBlockingStub blockingStub;
  private long timeTicksStart;

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
    req.setPlayer(player.toProto());
    PlayerMoveRes res = sendEvent(req.build());
    HashMap<Integer, Player> localPlayers = game.getPlayers();
    res.getPlayerList().forEach(serverPlayer -> {
      if (serverPlayer.getId() == game.getPlayer().getId()) { // serverPlayer is me
        if (!(new Vector3(serverPlayer.getPos().getX(), serverPlayer.getPos().getY(), serverPlayer.getPos().getZ()).equals(player.getPos()))) { // if server corrected my move
          game.getPlayer().setPos(serverPlayer.getPos());
        }
      } else { // serverPlayer is not me
        if (localPlayers.containsKey(serverPlayer.getId())) { // stored this player
          localPlayers.get(serverPlayer.getId()).setTurretDirection(serverPlayer.getTurretDirection());
          localPlayers.get(serverPlayer.getId()).updateAnimations(serverPlayer.getPos(), serverPlayer.getDirection()); // update local player to server
        } else { // new player
          Player newPlayer = Player.fromProto(serverPlayer);
          newPlayer.loadAssets(assetManager);
          localPlayers.put(serverPlayer.getId(), newPlayer);
        }
      }
    });
  }

  public void fire(Game game, Vector3 projPos, Vector3 projVel) {
    PlayerFireReq.Builder req = PlayerFireReq.newBuilder();
    req.setProjectilePos(Point3.newBuilder().setX(projPos.x).setY(projPos.y).setZ(projPos.z));
    req.setProjectileVel(Point3.newBuilder().setX(projVel.x).setY(projVel.y).setZ(projVel.z));
    req.setId(game.getPlayer().getId());
    PlayerFireRes res = sendEvent(req.build());
    System.out.println("Got res, projectiles: " + res);
    res.getProjectileList().forEach(serverProj -> {
      if (game.getProjectile(serverProj.getId()) != null) {
        game.getProjectile(serverProj.getId()).updatePos(serverProj.getPos(), serverProj.getVel());
      } else {
        game.addProjectile(serverProj.getPos(), serverProj.getVel(), serverProj.getId());
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
    timeTicksStart = System.currentTimeMillis() - res.getTick() * 50;
    System.out.println("Server start time: " + new Date(timeTicksStart));
    return hash;
  }

  public long getTick() {
    return (System.currentTimeMillis() - timeTicksStart) / 50; // ticks are 50 millis
  }

  public PlayerMoveRes sendEvent(PlayerMoveReq e) {
    return sendEvent(PlayerEventReq.newBuilder().setMoveReq(e).setMoveReqBool(true)).getMoveRes();
  }

  public PlayerFireRes sendEvent(PlayerFireReq e) {
    return sendEvent(PlayerEventReq.newBuilder().setFireReq(e).setFireReqBool(true)).getFireRes();
  }

  private PlayerEventRes sendEvent(PlayerEventReq.Builder e) {
    e.setTick(getTick());
    try {
      return blockingStub.playerEvent(e.build());
    } catch (StatusRuntimeException e1) {
      try {
        shutdown();
      } catch (InterruptedException e2) {
        e2.printStackTrace();
      }
      e1.printStackTrace();
      throw new RuntimeException("Could not connect to server");
    }
  }
}
