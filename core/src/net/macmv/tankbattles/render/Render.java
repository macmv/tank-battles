package net.macmv.tankbattles.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import net.macmv.tankbattles.lib.Game;
import net.macmv.tankbattles.mapeditor.MapEditor;

public class Render {
  private final PerspectiveCamera cam;
  private final ModelBatch batch;
  private final AssetManager assetManager;
  private final Game game;
  private Mode viewMode;
  private boolean loading;
  private float prevDirection;
  private boolean debug = false;
  private final MapEditor mapEditor;
  private final Vector3 spectatorPos;
  private final Vector2 camAngle;

  public Render(Game game, Mode viewMode) {
    this.game = game;
    cam = new PerspectiveCamera(60, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    cam.near = 0.1f;
    batch = new ModelBatch();

    assetManager = new AssetManager();

    game.requireAssets(assetManager);
    loading = true;

    this.viewMode = viewMode;

    if (viewMode == Mode.SPECTATOR) {
      mapEditor = new MapEditor(this, game);
      spectatorPos = new Vector3(-10, 10, 0);
      camAngle = new Vector2(0, 70);
    } else {
      mapEditor = null;
      spectatorPos = null;
      camAngle = null;
    }
  }

  public void resize() {
    cam.viewportWidth = Gdx.graphics.getWidth();
    cam.viewportHeight = Gdx.graphics.getHeight();
  }

  private final Vector2 tmp2 = new Vector2();
  private final Vector3 tmp3 = new Vector3();

  public void render(float delta) {
    Gdx.gl.glClearColor(0.3f, 0.3f, 0.3f, 1);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
    if (loading) {
      if (assetManager.update()) {
        System.out.println("DONE");
        game.loadAssets(assetManager);
        if (viewMode == Mode.SPECTATOR) {
          mapEditor.loadAssets(assetManager, game.getTerrain().getTileSkin());
        }
        loading = false;
      } else { // TODO: progress bar here for loading
        System.out.println(assetManager.getProgress()); // is percent 0.0 to 1.0
        return;
      }
    }

    updateCamera();

    if (viewMode == Mode.PLAYER) {
      System.out.println("Setting turret target to: " + cam.direction.y);
      game.getPlayer().setTurretTarget(tmp2.set(cam.direction.x, cam.direction.z).angle() + 90, cam.direction.y);
    }

    Environment env = game.getTerrain().getEnvironment();

    batch.begin(cam);

    game.getTerrain().render(batch);

    game.getPlayer().getTank().render(batch, env, delta);
    game.getPlayers().forEach((id, p) -> {
      p.getTank().render(batch, env, delta);
    });
    game.getProjectiles().forEach((id, p) -> {
      p.render(batch, env);
    });

    if (viewMode == Mode.SPECTATOR) {
      mapEditor.render(batch, env);
    }

    batch.end();

    if (debug) {
      game.getCollisionManager().getDebugDrawer().begin(cam);
      game.getCollisionManager().debugDrawWorld();
      game.getCollisionManager().getDebugDrawer().end();
    }
  }

  // everything here is magic, do not edit; top section is player, bottom is map editor
  private void updateCamera() {
    if (viewMode == Mode.PLAYER) {
      Gdx.input.setCursorCatched(true);
      Gdx.input.setCursorPosition(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
      float playerRot = (game.getPlayer().getBodyRot() * -1 + 360) % 360;

      Vector3 pos = game.getPlayer().getPos();
      float deltaX = -Gdx.input.getDeltaX() * 0.5f + prevDirection - playerRot;
      prevDirection = playerRot;
      float deltaY = -Gdx.input.getDeltaY() * 0.5f;
      cam.direction.rotate(cam.up, deltaX);
      cam.direction.rotate(tmp3.set(cam.direction).crs(cam.up).nor(), deltaY);
      float x = (float) Math.sin((-playerRot - 180) / 180.0 * Math.PI);
      float z = (float) Math.cos((-playerRot - 180) / 180.0 * Math.PI);
      cam.position.set(pos.x + x, 3, pos.z + z);
    } else if (viewMode == Mode.SPECTATOR) {
      Gdx.input.setCursorCatched(false);

      if (Gdx.input.isButtonPressed(Input.Buttons.MIDDLE)) {
        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) { // below is magic; YOU HAVE BEEN WARNED
          float xDelta = (float) Math.sin((camAngle.x) / 180.0 * Math.PI);
          float zDelta = (float) Math.cos((camAngle.x) / 180.0 * Math.PI);
          spectatorPos.add(new Vector3(xDelta, 0, zDelta).scl(Gdx.input.getDeltaX()).scl(-0.2f));
          float invVertDelta = (float) Math.sin((camAngle.y + 90) / 180.0 * Math.PI); // 0 forward; 1 when up or down
          float vertDelta = (float) Math.sin((camAngle.y) / 180.0 * Math.PI); // 1 when facing forward; 0 when up or down
          spectatorPos.add(new Vector3(invVertDelta * zDelta, vertDelta, invVertDelta * -xDelta).scl(Gdx.input.getDeltaY()).scl(0.2f));
        } else {
          camAngle.x += Gdx.input.getDeltaX();
          camAngle.y += Gdx.input.getDeltaY();
        }
      }
      cam.direction.set(1, -90, 0);
      cam.direction.rotate(cam.up, camAngle.x);
      cam.direction.rotate(tmp3.set(cam.direction).crs(cam.up).nor(), camAngle.y); // little more magic

      cam.position.set(spectatorPos.x, spectatorPos.y, spectatorPos.z);

      mapEditor.updateTarget(game.getCollisionManager());
    }
    cam.update();
  }

  public void scroll(int amount) {
    Vector3 unprojected = cam.unproject(new Vector3(Gdx.input.getX(),
            Gdx.input.getY(),
            0));
    Vector3 posDelta = unprojected.sub(cam.position);
    posDelta.scl(-amount * 5); // change to increase sensitivity
    spectatorPos.add(posDelta);
  }

  public void dispose() {
    batch.dispose();
    assetManager.dispose();
  }

  public AssetManager getAssetManager() {
    return assetManager;
  }

  public void toggleDebug() {
    debug = !debug;
  }

  public Camera getCamera() {
    return cam;
  }

  public void toggleSpectator() {
    if (viewMode == Mode.SPECTATOR) {
      viewMode = Mode.PLAYER;
    } else {
      viewMode = Mode.SPECTATOR;
    }
  }

  public void leftClick() {
    if (viewMode == Mode.SPECTATOR) {
      mapEditor.placeTile();
    } else {
      synchronized (game.getPlayer()) {
        game.getPlayer().fire(game);
      }
    }
  }

  public enum Mode {
    PLAYER, SPECTATOR
  }
}
