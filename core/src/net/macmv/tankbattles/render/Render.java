package net.macmv.tankbattles.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import net.macmv.tankbattles.Game;

public class Render {
  private final PerspectiveCamera cam;
  private final ModelBatch batch;
  private final AssetManager assetManager;
  private final Game game;
  private boolean loading;
  private float prevDirection;

  public Render(Game game) {
    this.game = game;
    cam = new PerspectiveCamera(60, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    cam.near = 0.1f;
    batch = new ModelBatch();

    assetManager = new AssetManager();

    game.requireAssets(assetManager);
    loading = true;
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
        loading = false;
      } else { // TODO: progress bar here for loading
        System.out.println(assetManager.getProgress()); // is percent 0.0 to 1.0
        return;
      }
    }

    Gdx.input.setCursorCatched(true);
    Gdx.input.setCursorPosition(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);

    Vector3 pos = game.getPlayer().getPos();
    float deltaX = -Gdx.input.getDeltaX() * 0.5f + prevDirection - game.getPlayer().getDirection();
    prevDirection = game.getPlayer().getDirection();
    float deltaY = -Gdx.input.getDeltaY() * 0.5f;
    cam.direction.rotate(cam.up, deltaX);
    cam.direction.rotate(tmp3.set(cam.direction).crs(cam.up).nor(), deltaY);
    float x = (float) Math.cos((game.getPlayer().getDirection() + 90) / 180.0 * Math.PI);
    float z = (float) Math.sin((game.getPlayer().getDirection() + 90) / 180.0 * Math.PI);
    cam.position.set(pos.x + x, 3, pos.z + z);
    cam.update();

    game.getPlayer().setTurretTarget(tmp2.set(cam.direction.x, cam.direction.z).angle() + 90, cam.direction.y);

    Environment env = game.getTerrain().getEnvironment();

    batch.begin(cam);

    game.getTerrain().render(batch);

    game.getPlayer().getTank().render(batch, env, delta);
    game.getPlayers().forEach((id, p) -> {
      p.getTank().render(batch, env, delta);
    });
    game.getProjectiles().forEach((p) -> {
      p.render(batch, env);
    });

    batch.end();

    game.getCollisionManager().getDebugDrawer().begin(cam);
    game.getCollisionManager().debugDrawWorld();
    game.getCollisionManager().getDebugDrawer().end();
  }

  public void dispose() {
    batch.dispose();
    assetManager.dispose();
  }

  public AssetManager getAssetManager() {
    return assetManager;
  }
}
