package com.example;

import javax.swing.*;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 * View utility: JLabel ที่วาดข้อความพร้อม drop-shadow ด้วย Graphics2D
 *
 * ใช้ TextLayout.getOutline() แทน drawString เพื่อให้ควบคุมเงาได้ละเอียด
 * และ override getPreferredSize() ด้วย ascent+descent+leading
 * เพื่อให้ BoxLayout คำนวณความสูงถูกต้องและไม่ตัดหัวตัวอักษร
 */
public class ShadowLabel extends JLabel {

    private static final int PADDING   = 6; // ระยะขอบรอบข้อความ (px)
    private static final int SHADOW_DX = 3; // เงาเลื่อนขวา (px)
    private static final int SHADOW_DY = 1; // เงาเลื่อนลง (px)

    public ShadowLabel(String text) {
        super(text);
        setFont(new Font("Impact", Font.BOLD, 40));
        setForeground(Color.WHITE);
        setOpaque(false);
    }

    /**
     * คำนวณขนาดที่ต้องการจาก font ปัจจุบัน
     * ใช้ advance (ความกว้างจริงพร้อม bearing) และ ascent+descent+leading
     * แทน bounds.getWidth/Height() ที่อาจตัด glyph บางส่วนออก
     */
    @Override
    public Dimension getPreferredSize() {
        FontRenderContext frc    = getFRC();
        TextLayout        layout = new TextLayout(getText(), getFont(), frc);
        int w = (int) Math.ceil(layout.getAdvance())
                + SHADOW_DX + PADDING * 2 + 10;
        int h = (int) Math.ceil(layout.getAscent() + layout.getDescent() + layout.getLeading())
                + SHADOW_DY + PADDING * 2;
        return new Dimension(w, h);
    }

    /** วาด drop-shadow แล้วตามด้วยตัวอักษรจริงบนตำแหน่ง baseline ที่ถูกต้อง */
    @Override
    protected void paintComponent(Graphics g) {
        if (getText() == null || getText().isEmpty()) return;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);

        TextLayout              layout = new TextLayout(getText(), getFont(), g2.getFontRenderContext());
        java.awt.geom.Rectangle2D bounds = layout.getBounds();

        // bounds.getY() เป็นค่าลบ (offset จาก baseline ขึ้นไป)
        // ดังนั้น -bounds.getY() = ระยะจาก top ถึง baseline
        int x        = PADDING;
        int baseline = (int) Math.ceil(-bounds.getY()) + PADDING;

        Shape outline = layout.getOutline(AffineTransform.getTranslateInstance(x, baseline));

        // เงา (วาดก่อนตัวอักษรจริง)
        g2.setColor(new Color(0, 0, 0, 130));
        g2.fill(AffineTransform.getTranslateInstance(SHADOW_DX, SHADOW_DY)
                               .createTransformedShape(outline));

        // ตัวอักษรจริง
        g2.setColor(getForeground());
        g2.fill(outline);

        g2.dispose();
    }

    /** สร้าง FontRenderContext โดยไม่ต้องมี Graphics จริง (ใช้ใน getPreferredSize) */
    private FontRenderContext getFRC() {
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D    g2  = img.createGraphics();
        FontRenderContext frc = g2.getFontRenderContext();
        g2.dispose();
        return frc;
    }
}