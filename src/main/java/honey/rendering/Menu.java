package honey.rendering;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import honey.HoneySuckle;
import honey.mechanics.ConfigManager;
import honey.mechanics.InputHandler;

public class Menu {

    public static ConfigManager config;

    public static final Color BROWN = new Color(74, 56, 54);

    public static enum MenuType {
        MAIN_MENU, GAME_OVER_MENU, RESTART_MENU
    }

    public MenuType menuType;

    private int buttonIndex = -1;
    private int frame = 0;
    public boolean complete = false;

    public Menu(MenuType menuType) {
        this.menuType = menuType;
    }

    public void render(Graphics2D g) {
        switch (menuType) {
            case MAIN_MENU -> {
                final double dt = frame / 5.0;
                final int offset = (int) Math.floor(3 * (dt * dt * dt * dt - 9.9 * dt * dt * dt + 25 * dt * dt));

                g.setColor(BROWN);
                g.fillRect(0, -offset, config.gameWidth, config.gameHeight);

                g.drawImage(Rendering.image("HoneySuckle"), config.gameWidth / 4, config.gameHeight / 16 - offset, config.gameWidth / 2, config.gameWidth / 2, null);

                if (!HoneySuckle.play) {
                    if (buttonIndex == 0) {
                        g.setStroke(new BasicStroke(4));
                        g.setColor(Color.WHITE);
                    } else {
                        g.setStroke(new BasicStroke(2));
                        g.setColor(Color.GRAY);
                    }
                    g.drawRect(config.gameWidth / 4, config.gameHeight * 3 / 4, config.gameWidth / 2, config.gameHeight / 16);

                    g.setFont(new Font("VT323 Regular", Font.PLAIN, 32));
                    Rendering.centeredText(g, "Start", config.gameWidth / 2, config.gameHeight * 51 / 64);
                }
            }
            case GAME_OVER_MENU -> {
                g.setColor(new Color(192, 0, 0, frame * 2));
                g.fillRect(0, 0, config.gameWidth, config.gameHeight);

                if (buttonIndex == 0) {
                    g.setStroke(new BasicStroke(4));
                    g.setColor(new Color(255, 255, 255, frame * 3));
                } else {
                    g.setStroke(new BasicStroke(2));
                    g.setColor(new Color(200, 200, 200, frame * 3));
                }
                g.drawRect(config.gameWidth / 4, config.gameHeight * 3 / 4, config.gameWidth / 2, config.gameHeight / 16);

                g.setFont(new Font("VT323 Regular", Font.PLAIN, 32));
                Rendering.centeredText(g, "Main Menu", config.gameWidth / 2, config.gameHeight * 51 / 64);

                g.setFont(new Font("VT323 Regular", Font.PLAIN, 96));
                g.setColor(new Color(255, 255, 255, frame * 3));
                Rendering.centeredText(g, "Game Over", config.gameWidth / 2, config.gameHeight / 2);

            }
            case RESTART_MENU -> {
                g.setColor(new Color(192, 0, 0, Math.min(160 + frame * 2, 255)));
                g.fillRect(0, 0, config.gameWidth, config.gameHeight);

                g.setFont(new Font("VT323 Regular", Font.PLAIN, 96));
                g.setColor(new Color(255, 255, 255, 240));
                Rendering.centeredText(g, "Game Over", config.gameWidth / 2, config.gameHeight / 2);

                if (frame > 40) {
                    final double dt = (frame - 40) / 2.0;
                    final int offset = (int) Math.floor((1+Math.sin(dt * 1.25))/Math.pow(dt, 2)* 800);

                    g.setColor(BROWN);
                    g.fillRect(0, -offset, config.gameWidth, config.gameHeight);

                    g.drawImage(Rendering.image("HoneySuckle"), config.gameWidth / 4, config.gameHeight / 16 - offset, config.gameWidth / 2, config.gameWidth / 2, null);
                }
            }
        }
    }

    public void update(InputHandler input) {
        switch (menuType) {
            case MAIN_MENU -> {
                if (HoneySuckle.play) {
                    frame++;
                    if (frame > 35) {
                        complete = true;
                    }
                } else {
                    buttonIndex = -1;
                    if (Math.abs(input.mousePos[0] - config.gameWidth / 2) < config.gameWidth / 4) {
                        double highlight = (input.mousePos[1] - (config.gameHeight * 3 / 4));
                        if (highlight % (config.gameHeight * 3 / 32) <= config.gameHeight / 16) {
                            buttonIndex = (int) Math.floor(highlight / (config.gameHeight * 3 / 32));
                        }
                    }
                    if (input.clickPressed(1)) {
                        switch (buttonIndex) {
                            case 0 -> {
                                HoneySuckle.start();
                            }
                        }
                    }
                }
            }
            case GAME_OVER_MENU -> {
                if (frame < 80) {
                    frame++;
                }
                buttonIndex = -1;
                if (Math.abs(input.mousePos[0] - config.gameWidth / 2) < config.gameWidth / 4) {
                    double highlight = (input.mousePos[1] - (config.gameHeight * 3 / 4));
                    if (highlight % (config.gameHeight * 3 / 32) <= config.gameHeight / 16) {
                        buttonIndex = (int) Math.floor(highlight / (config.gameHeight * 3 / 32));
                    }
                }
                if (input.clickPressed(1) && buttonIndex == 0) {
                    setMenuType(MenuType.RESTART_MENU);
                }
            }
            case RESTART_MENU -> {
                frame++;
                if(frame == 107){
                    HoneySuckle.stop();
                    setMenuType(MenuType.MAIN_MENU);
                }
            }
        }
    }

    public void setMenuType(MenuType menuType){
        this.menuType = menuType;
        frame = 0;
        buttonIndex = -1;
    }
}
