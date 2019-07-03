package net.macmv.tankbattles;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import net.macmv.tankbattles.render.Render;

public class TankBattles extends ApplicationAdapter {
  private Game game;
  private Render render;
  private TankBattlesClient client;

  @Override
  public void create() {
    client = new TankBattlesClient("localhost", 8001);
    game = new Game(client);
    render = new Render(game);
  }

  @Override
  public void render() {
    float deltaTime = Gdx.graphics.getDeltaTime();
    render.render(deltaTime);
    game.update(deltaTime);
  }

  @Override
  public void resize(int width, int height) {
    render.resize();
  }

  @Override
  public void dispose() {
    try {
      client.shutdown();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    render.dispose();
  }
}
