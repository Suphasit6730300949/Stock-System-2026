package com.example;

import javax.swing.*;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class ShadowLabel extends JLabel {

    private static final int PADDING = 6;   // ระยะขอบรอบข้อความ
    private static final int SHADOW_DX = 3; // เงาเลื่อนขวา
    private static final int SHADOW_DY = 1; // เงาเลื่อนลง

    public ShadowLabel(String text) {
        super(text);
        setFont(new Font("Impact", Font.BOLD, 40));
        setForeground(Color.WHITE);
        setOpaque(false);
    }

    // ✅ Fix 1: บอก layout manager ว่า component ต้องการพื้นที่เท่าไหร่
    @Override
    public Dimension getPreferredSize() {
        FontRenderContext frc = getFRC();
        TextLayout layout = new TextLayout(getText(), getFont(), frc);
        java.awt.geom.Rectangle2D bounds = layout.getBounds();
        int w = (int) Math.ceil(bounds.getWidth())  + SHADOW_DX + PADDING * 2;
        int h = (int) Math.ceil(bounds.getHeight()) + SHADOW_DY + PADDING * 2;
        return new Dimension(w, h);
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (getText() == null || getText().isEmpty()) return;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        TextLayout layout = new TextLayout(getText(), getFont(), g2.getFontRenderContext());
        java.awt.geom.Rectangle2D bounds = layout.getBounds();

        // ✅ Fix 2: คำนวณ baseline จาก bounds.getY() ซึ่งเป็นลบ (offset จาก baseline)
        // วาดให้ข้อความอยู่กลาง component แนวตั้ง
        int x = PADDING;
        int y = (int) Math.ceil(-bounds.getY()) + PADDING; // baseline position

        Shape shape = layout.getOutline(AffineTransform.getTranslateInstance(x, y));

        // เงา
        g2.setColor(new Color(0, 0, 0, 130));
        Shape shadow = AffineTransform.getTranslateInstance(SHADOW_DX, SHADOW_DY)
                .createTransformedShape(shape);
        g2.fill(shadow);

        // ตัวหนังสือจริง
        g2.setColor(getForeground());
        g2.fill(shape);

        g2.dispose();
    }

    // helper: ดึง FontRenderContext โดยไม่ต้องมี Graphics
    private FontRenderContext getFRC() {
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        FontRenderContext frc = g2.getFontRenderContext();
        g2.dispose();
        return frc;
    }
}