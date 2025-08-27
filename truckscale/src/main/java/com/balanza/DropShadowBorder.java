package com.balanza;

import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class DropShadowBorder extends AbstractBorder {
    private static final int SHADOW_SIZE = 5;
    private static final Color SHADOW_COLOR = new Color(0, 0, 0, 50); // Sombra suave

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        RoundRectangle2D shadow = new RoundRectangle2D.Double(x + SHADOW_SIZE, y + SHADOW_SIZE, width - SHADOW_SIZE * 2, height - SHADOW_SIZE * 2, 10, 10);
        g2d.setColor(SHADOW_COLOR);
        g2d.fill(shadow);
        g2d.dispose();
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return new Insets(SHADOW_SIZE, SHADOW_SIZE, SHADOW_SIZE, SHADOW_SIZE);
    }
}