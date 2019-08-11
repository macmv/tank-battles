package net.macmv.tankbattles.server;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;
import com.badlogic.gdx.backends.lwjgl.LwjglNativesLoader;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import net.macmv.tankbattles.lib.proto.*;

import java.io.IOException;
import java.util.logging.Logger;

public class ServerMain {

  private static final Logger logger = Logger.getLogger(ServerMain.class.getName());
  private static ServerGame serverGame;

  private Server server;

  public static void main(String[] args) throws IOException, InterruptedException {
    LwjglNativesLoader.load();
    Gdx.files = new LwjglFiles();
    serverGame = new ServerGame();
    final ServerMain server = new ServerMain();
    ServerThread updateThread = new ServerThread(server);
    updateThread.start();
    server.start();
    server.blockUntilShutdown();
  }

  public void update(float deltaTime) {
    serverGame.update(deltaTime, null);
  }

  private void start() throws IOException {
    /* The port on which the server should run */
    int port = 8001;
    server = ServerBuilder.forPort(port)
            .addService(new TankBattlesImpl())
            .build()
            .start();
    logger.info("Server started, listening on " + port);
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        // Use stderr here since the logger may have been reset by its JVM shutdown hook.
        System.err.println("*** shutting down gRPC server since JVM is shutting down");
        ServerMain.this.stop();
        System.err.println("*** server shut down");
      }
    });
  }

  private void stop() {
    if (server != null) {
      server.shutdown();
    }
  }

  private void blockUntilShutdown() throws InterruptedException {
    if (server != null) {
      server.awaitTermination();
    }
  }

  private static class TankBattlesImpl extends TankBattlesGrpc.TankBattlesImplBase {
    @Override
    public void playerJoin(PlayerJoinReq req, StreamObserver<PlayerJoinRes> responseObserver) {
      logger.info("Player joining, req: " + req);
      serverGame.addPlayer(req.getId(), req.getTank());
      logger.info("Current Players: " + serverGame.getProtoPlayers());
      PlayerJoinRes.Builder reply = PlayerJoinRes.newBuilder();
      reply.addAllPlayer(serverGame.getProtoPlayers().values());
      reply.setTick(serverGame.getTick());
      reply.setMap(serverGame.getTerrain().toProto());
      responseObserver.onNext(reply.build());
      responseObserver.onCompleted();
      logger.info("Player joined, res: " + reply);
    }

    @Override
    public void playerEvent(PlayerEventReq req, StreamObserver<PlayerEventRes> responseObserver) {
      PlayerMoveRes moveRes = null;
      if (req.getMoveReqBool()) {
        moveRes = serverGame.checkAndMove(req.getMoveReq(), req.getTick());
      }
      if (req.getFireReqBool()) {
        serverGame.checkFire(req.getFireReq(), req.getTick());
      }
      PlayerEventRes.Builder res = PlayerEventRes.newBuilder();
      if (moveRes != null) {
        res.setMoveRes(moveRes);
      }
      res.setFireRes(serverGame.generateFireRes());
      responseObserver.onNext(res.build());
      responseObserver.onCompleted();
    }
  }
}
