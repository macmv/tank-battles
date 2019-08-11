package net.macmv.tankbattles.server;

import java.io.IOException;

public class ServerThread extends Thread {

  private final ServerMain serverMain;

  public ServerThread(ServerMain server) throws IOException {
    this.serverMain = server;
  }

  public void run() {
    long lastTick = System.currentTimeMillis();
    while (true) {
      float deltaTime = System.currentTimeMillis() - lastTick;
      if (deltaTime >= 50) {
        update(deltaTime);
        lastTick = System.currentTimeMillis();
      }
    }
  }

  private void update(float deltaTime) {
    System.out.println("Updating");
    serverMain.update(deltaTime);
  }
}
