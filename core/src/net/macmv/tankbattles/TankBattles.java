package net.macmv.tankbattles;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import net.macmv.tankbattles.render.Render;

public class TankBattles extends ApplicationAdapter {
  private Game game;
  private Render render;

  @Override
  public void create() {
    game = new Game();
    render = new Render(game);
  }

  @Override
  public void render() {
    float deltaTime = Gdx.graphics.getDeltaTime();
    render.render(deltaTime);
    game.update(deltaTime, render.getAssetManager());
  }

  @Override
  public void resize(int width, int height) {
    render.resize();
  }

  @Override
  public void dispose() {
    try {
      game.shutdown();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    render.dispose();
  }
}
