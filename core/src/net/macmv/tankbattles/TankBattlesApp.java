package net.macmv.tankbattles;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import net.macmv.tankbattles.render.Render;

public class TankBattlesApp extends ApplicationAdapter implements InputProcessor {
  private ClientGame game;
  private Render render;

  @Override
  public void create() {
    game = new ClientGame();
    render = new Render(game, Render.Mode.PLAYER);
    Gdx.input.setInputProcessor(this);
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

  @Override
  public boolean keyDown(int keycode) {
    if (keycode == Input.Keys.ESCAPE) {
      Gdx.app.exit();
      return true;
    } else if (keycode == Input.Keys.F1) {
      render.toggleDebug();
      return true;
    }
    return false;
  }

  @Override
  public boolean keyUp(int keycode) {
    return false;
  }

  @Override
  public boolean keyTyped(char character) {
    return false;
  }

  @Override
  public boolean touchDown(int screenX, int screenY, int pointer, int button) {
    if (button != Input.Buttons.LEFT) {
      return false;
    }
    synchronized (game.getPlayer()) {
      game.fire();
    }
    return true;
  }

  @Override
  public boolean touchUp(int screenX, int screenY, int pointer, int button) {
    return false;
  }

  @Override
  public boolean touchDragged(int screenX, int screenY, int pointer) {
    return false;
  }

  @Override
  public boolean mouseMoved(int screenX, int screenY) {
    return false;
  }

  @Override
  public boolean scrolled(int amount) {
    return false;
  }
}
