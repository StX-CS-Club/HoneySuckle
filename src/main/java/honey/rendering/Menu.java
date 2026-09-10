package honey.rendering;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;

import honey.HoneySuckle;
import honey.mechanics.ConfigManager;
import honey.mechanics.InputHandler;
import honey.mechanics.SaveStateManager;
import honey.world.World;

public class Menu {

    public static ConfigManager config;

    public static final Color BROWN = new Color(74, 56, 54);

    public static enum MenuType {
        MAIN_MENU, GAME_OVER_MENU, RESTART_MENU, PAUSE_MENU, EXIT_MENU, LOADING_MENU
    }

    //Vertical scroll layout shared between PAUSE_MENU's render() and update() hit-testing
    private static final int PAUSE_SCROLL_LENGTH = 12;
    private static final double[] PAUSE_BUTTON_Y_FRACTIONS = {0.38, 0.52, 0.66};
    private static final Color BUTTON_DARK = new Color(60, 60, 60);

    private static final Color NOTIFICATION_BG = Color.BLACK;
    private static final Color NOTIFICATION_TEXT = Color.LIGHT_GRAY;
    private static final Color NOTIFICATION_CHECK = new Color(46, 204, 113);

    private static final double LOADING_CURTAIN_SECONDS = 0.25;

    public MenuType menuType;

    private int buttonIndex = -1;
    private int frame = 0;
    public boolean complete = false;
    private long notificationEndMillis = 0;

    //LOADING_MENU state: whether the world/level swap has happened yet, and the reveal-phase frame counter
    private boolean loadingPublished = false;
    private int revealFrame = 0;

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

                if (!HoneySuckle.running) {
                    renderBoxButton(g, 0, "New Game", config.gameWidth / 4, config.gameHeight * 3 / 4, config.gameWidth / 2, config.gameHeight / 16);
                    renderBoxButton(g, 1, "Load Game", config.gameWidth / 4, config.gameHeight * 27 / 32, config.gameWidth / 2, config.gameHeight / 16);
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
            case PAUSE_MENU -> {
                final int alpha = (int) (Math.min(frame, 15) / 15.0 * 127);
                g.setColor(new Color(128, 128, 128, alpha));
                g.fillRect(0, 0, config.gameWidth, config.gameHeight);

                final int renderedH = config.gameHeight / 2;
                final int renderedW = config.gameWidth * 3 / 10;
                final int scrollX = (config.gameWidth - renderedW) / 2;
                final int scrollY = (config.gameHeight - renderedH) / 2;
                final int scrollCenterX = scrollX + renderedW / 2;

                g.drawImage(Rendering.rotateImage(Rendering.scroll(PAUSE_SCROLL_LENGTH), 90), scrollX, scrollY, renderedW, renderedH, null);

                g.setColor(Color.BLACK);
                g.setFont(new Font("VT323 Regular", Font.PLAIN, 32));
                Rendering.centeredText(g, "-Game Paused-", scrollCenterX, scrollY + (int) (renderedH * 0.12));

                renderTextButton(g, 0, "Resume", scrollCenterX, pauseButtonY(scrollY, renderedH, 0));
                renderTextButton(g, 1, "Save Game", scrollCenterX, pauseButtonY(scrollY, renderedH, 1));
                renderTextButton(g, 2, "Exit", scrollCenterX, pauseButtonY(scrollY, renderedH, 2));

                g.setColor(Color.BLACK);
                g.setFont(new Font("VT323 Regular", Font.PLAIN, 32));
                Rendering.centeredText(g, "-Level " + World.level + "-", scrollCenterX, scrollY + (int) (renderedH * 0.92));

                renderNotification(g);
            }
            case EXIT_MENU -> {
                final double dt = (frame + 1) / 2.0;
                final int offset = (int) Math.floor((1 + Math.sin(dt * 1.25)) / Math.pow(dt, 2) * 800);

                g.setColor(BROWN);
                g.fillRect(0, -offset, config.gameWidth, config.gameHeight);

                g.drawImage(Rendering.image("HoneySuckle"), config.gameWidth / 4, config.gameHeight / 16 - offset, config.gameWidth / 2, config.gameWidth / 2, null);
            }
            case LOADING_MENU -> {
                final int curtainFrames = curtainFrames();
                final int offsetY;
                if (!loadingPublished) {
                    //COVER: slides up from fully off-screen-bottom (+gameHeight) to fully covering (0)
                    final int coverFrame = Math.min(frame, curtainFrames);
                    offsetY = config.gameHeight - (int) (coverFrame / (double) curtainFrames * config.gameHeight);
                } else {
                    //REVEAL: continues from fully covering (0) to fully off-screen-top (-gameHeight)
                    final int revealClamped = Math.min(revealFrame, curtainFrames);
                    offsetY = -(int) (revealClamped / (double) curtainFrames * config.gameHeight);
                }

                g.setColor(Color.BLACK);
                g.fillRect(0, offsetY, config.gameWidth, config.gameHeight);

                if (!loadingPublished && frame >= curtainFrames) {
                    g.setColor(Color.WHITE);
                    g.setFont(new Font("VT323 Regular", Font.PLAIN, 32));
                    Rendering.centeredText(g, "Loading...", config.gameWidth / 2, config.gameHeight / 2);
                }
            }
        }
    }

    private void renderNotification(Graphics2D g) {
        if (System.currentTimeMillis() > notificationEndMillis) return;

        final String text = "Successfully saved game";
        g.setFont(new Font("VT323 Regular", Font.PLAIN, 18));
        final var metrics = g.getFontMetrics();
        final int textWidth = metrics.stringWidth(text);
        final int textHeight = metrics.getAscent();

        final int checkSize = 12;
        final int pad = 8;
        final int gap = 8;
        final int boxWidth = pad * 2 + checkSize + gap + textWidth;
        final int boxHeight = pad * 2 + textHeight;
        final int margin = 10;
        final int boxX = config.gameWidth - boxWidth - margin;
        final int boxY = config.gameHeight - boxHeight - margin;

        g.setColor(NOTIFICATION_BG);
        g.fillRoundRect(boxX, boxY, boxWidth, boxHeight, 12, 12);

        final int checkX = boxX + pad;
        final int checkY = boxY + boxHeight / 2;
        g.setColor(NOTIFICATION_CHECK);
        g.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(checkX, checkY, checkX + (int) (checkSize * 0.35), checkY + (int) (checkSize * 0.35));
        g.drawLine(checkX + (int) (checkSize * 0.35), checkY + (int) (checkSize * 0.35), checkX + checkSize, checkY - (int) (checkSize * 0.4));

        g.setColor(NOTIFICATION_TEXT);
        g.drawString(text, checkX + checkSize + gap, boxY + boxHeight / 2 + textHeight / 2 - 2);
    }

    private void renderBoxButton(Graphics2D g, int index, String text, int x, int y, int width, int height) {
        if (buttonIndex == index) {
            g.setStroke(new BasicStroke(4));
            g.setColor(Color.WHITE);
        } else {
            g.setStroke(new BasicStroke(2));
            g.setColor(Color.GRAY);
        }
        g.drawRect(x, y, width, height);

        g.setFont(new Font("VT323 Regular", Font.PLAIN, 32));
        Rendering.centeredText(g, text, x + width / 2, y + height * 3 / 4);
    }

    private void renderTextButton(Graphics2D g, int index, String text, int x, int y) {
        final boolean hovered = buttonIndex == index;
        final Font normalFont = new Font("VT323 Regular", Font.PLAIN, 32);
        final Font hoverFont = new Font("VT323 Regular", Font.PLAIN, 40);
        final Font font = hovered ? hoverFont : normalFont;
        final int yOffset = hovered
                ? (g.getFontMetrics(hoverFont).getAscent() - g.getFontMetrics(normalFont).getAscent()) / 2
                : 0;
        g.setFont(font);
        g.setColor(hovered ? Color.GRAY : BUTTON_DARK);
        Rendering.centeredText(g, text, x, y + yOffset);
    }

    private int pauseButtonY(int scrollY, int renderedH, int index) {
        return scrollY + (int) (renderedH * PAUSE_BUTTON_Y_FRACTIONS[index]);
    }

    //Frame count for one LOADING_MENU curtain sweep (cover or reveal), derived from
    //LOADING_CURTAIN_SECONDS at the current config.fps
    private static int curtainFrames() {
        return (int) Math.round(config.fps * LOADING_CURTAIN_SECONDS);
    }

    public void update(InputHandler input) {
        switch (menuType) {
            case MAIN_MENU -> {
                if (HoneySuckle.rendering) {
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
                    if (input.clickPressed(MouseEvent.BUTTON1)) {
                        switch (buttonIndex) {
                            case 0 -> {
                                HoneySuckle.start();
                            }
                            case 1 -> {
                                SaveStateManager.loadGame();
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
                if (input.clickPressed(MouseEvent.BUTTON1) && buttonIndex == 0) {
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
            case PAUSE_MENU -> {
                frame++;

                final int renderedH = config.gameHeight / 2;
                final int renderedW = config.gameWidth * 3 / 10;
                final int scrollY = (config.gameHeight - renderedH) / 2;
                final int scrollCenterX = (config.gameWidth - renderedW) / 2 + renderedW / 2;

                buttonIndex = -1;
                if (Math.abs(input.mousePos[0] - scrollCenterX) < renderedW / 2) {
                    for (int i = 0; i < PAUSE_BUTTON_Y_FRACTIONS.length; i++) {
                        if (Math.abs(input.mousePos[1] - pauseButtonY(scrollY, renderedH, i)) < 16) {
                            buttonIndex = i;
                        }
                    }
                }

                if (input.clickPressed(MouseEvent.BUTTON1)) {
                    switch (buttonIndex) {
                        case 0 -> {
                            HoneySuckle.play();
                            complete = true;
                        }
                        case 1 -> {
                            if (SaveStateManager.saveGame()) {
                                notificationEndMillis = System.currentTimeMillis() + 3000;
                            }
                        }
                        case 2 -> setMenuType(MenuType.EXIT_MENU);
                    }
                }
            }
            case EXIT_MENU -> {
                frame++;
                if (frame == 66) {
                    HoneySuckle.stop();
                    setMenuType(MenuType.MAIN_MENU);
                }
            }
            case LOADING_MENU -> {
                final int curtainFrames = curtainFrames();
                if (!loadingPublished) {
                    //Slides up to cover, then holds fully covered until the background world is ready
                    frame = Math.min(frame + 1, curtainFrames);
                    if (frame >= curtainFrames && World.pendingNextWorld != null) {
                        World.publishPendingWorld(HoneySuckle.player);
                        loadingPublished = true;
                    }
                } else {
                    //Slides the rest of the way up and off-screen; only resumes game logic once fully revealed
                    revealFrame++;
                    if (revealFrame >= curtainFrames) {
                        HoneySuckle.play();
                        complete = true;
                    }
                }
            }
        }
    }

    public void setMenuType(MenuType menuType){
        this.menuType = menuType;
        frame = 0;
        buttonIndex = -1;
        complete = false;
        loadingPublished = false;
        revealFrame = 0;
    }
}
