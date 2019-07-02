package net.macmv.tankbattles.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import net.macmv.tankbattles.Game;
import net.macmv.tankbattles.lib.proto.*;

import java.io.IOException;
import java.util.logging.Logger;

public class ServerMain {

  private static final Logger logger = Logger.getLogger(ServerMain.class.getName());
  private static ServerGame game;

  private Server server;

  public static void main(String[] args) throws IOException, InterruptedException {
    game = new ServerGame();
    final ServerMain server = new ServerMain();
    server.start();
    server.blockUntilShutdown();
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
      game.addPlayer(req.getId(), req.getTank());
      logger.info("Current Players: " + game.getPlayers());
      PlayerJoinRes.Builder reply = PlayerJoinRes.newBuilder();
      reply.addAllPlayer(game.getPlayers().values());
      responseObserver.onNext(reply.build());
      responseObserver.onCompleted();
      logger.info("Player joined, res: " + reply);
    }

    @Override
    public void playerMove(PlayerMoveReq req, StreamObserver<PlayerMoveRes> responseObserver) {
      if (game.checkAndMove(req)) {
        logger.info("Player move checked out");
      } else {
        logger.info("Player made illegal move!");
      }
      PlayerMoveRes.Builder reply = PlayerMoveRes.newBuilder();
      reply.addAllPlayer(game.getPlayers().values());
      responseObserver.onNext(reply.build());
      responseObserver.onCompleted();
    }
  }
}
