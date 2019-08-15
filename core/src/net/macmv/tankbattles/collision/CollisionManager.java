package net.macmv.tankbattles.collision;

import net.macmv.tankbattles.lib.Game;

import java.util.HashSet;

public class CollisionManager {
  private final Game game;
  private HashSet<Hitbox> objects;

  public CollisionManager(Game game) {
    this(game, true);
  }

  public CollisionManager(Game game, boolean loadTextures) {
    this.game = game;
  }

  public void update() {
    Hitbox player = game.getPlayer().getHitbox();
  }

  public void update(float deltaTime) {
  }

  public void add(Hitbox hitbox) {
    objects.add(hitbox);
  }
}
