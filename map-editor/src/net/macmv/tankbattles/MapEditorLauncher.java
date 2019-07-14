package net.macmv.tankbattles;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class MapEditorLauncher {
  public static void main(String[] arg) {
    LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
    config.width = 1920;
    config.height = 1080;
    new LwjglApplication(new TankBattlesMapEditor(), config);
  }
}
