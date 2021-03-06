package com.example.demo.generator;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.tool.xml.XMLWorkerFontProvider;

import java.io.IOException;

public class SimsunFontProvider extends XMLWorkerFontProvider {
    @Override
    public Font getFont(String fontname, String encoding, boolean embedded, float size, int style, BaseColor color) {
        BaseFont bf =null;
        try {
            bf = BaseFont.createFont("SIMSUN.TTC"+",1",BaseFont.IDENTITY_H,BaseFont.NOT_EMBEDDED);
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Font font = new Font(bf,size,style,color);
        font.setColor(color);
        return font;
    }
}
