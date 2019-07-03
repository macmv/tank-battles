package net.macmv.tankbattles.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.Vector2;
import net.macmv.tankbattles.Game;

public class Render {
  private final PerspectiveCamera cam;
  private final ModelBatch batch;
  private final AssetManager assetManager;
  private final Game game;
  private boolean loading;

  public Render(Game game) {
    this.game = game;
    cam = new PerspectiveCamera(60, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    cam.near = 0.1f;
    batch = new ModelBatch();

    assetManager = new AssetManager();

    game.loadAssets(assetManager);
    loading = true;
  }

  public void resize() {
    cam.viewportWidth = Gdx.graphics.getWidth();
    cam.viewportHeight = Gdx.graphics.getHeight();
  }

  public void render(float delta) {
    Gdx.gl.glClearColor(0.3f, 0.3f, 0.3f, 1);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
    if (loading) {
      if (assetManager.update()) {
        System.out.println("DONE");
        game.finishLoading(assetManager);
        loading = false;
      } else { // TODO: progress bar here for loading
        System.out.println(assetManager.getProgress()); // is percent 0.0 to 1.0
        return;
      }
    }

    Vector2 pos = game.getPlayer().getPos();
    cam.position.set(pos.x, 10, pos.y + 15);
    cam.lookAt(pos.x, 0, pos.y);
    cam.update();

    Environment env = game.getTerrain().getEnvironment();

    batch.begin(cam);

    game.getTerrain().render(batch);

    game.getPlayer().getTank().render(batch, delta, env);
    game.getPlayers().forEach((id, p) -> {
      p.getTank().render(batch, delta, env);
    });

    batch.end();
  }

  public void dispose() {
    batch.dispose();
    assetManager.dispose();
  }
}
