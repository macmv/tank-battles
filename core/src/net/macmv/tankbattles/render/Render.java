package net.macmv.tankbattles.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.math.Vector3;
import net.macmv.tankbattles.Game;

public class Render {
  private final Game game;
  private final PerspectiveCamera cam;
  private final ModelBatch batch;
  private final Environment env;

  public Render(Game game) {
    this.game = game;
    cam = new PerspectiveCamera(75, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    cam.position.set(0, 30, 10);
    cam.lookAt(0, 0, 0);
    cam.near = 0.1f;

    batch = new ModelBatch();
    env = new Environment();
    env.set(new ColorAttribute(ColorAttribute.Ambient, 0.8f, 0.8f, 0.8f, 1));
  }

  public void render(float delta) {
    Gdx.gl.glClearColor(0.3f, 0.3f, 0.3f, 1);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

    cam.update();
    batch.begin(cam);

    game.getPlayer().getTank().render(batch, delta);
    game.getPlayers().forEach((id, p) -> {
      p.getTank().render(batch, delta);
    });

    batch.end();
  }

  public void dispose() {
  }
}
