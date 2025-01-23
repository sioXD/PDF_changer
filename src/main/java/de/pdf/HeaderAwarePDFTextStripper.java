package de.pdf;

import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import java.io.IOException;
import java.util.List;

class HeaderAwarePDFTextStripper extends PDFTextStripper {

    private final List<Float> headerFontSizes; // Known font sizes for headings

    public HeaderAwarePDFTextStripper(List<Float> headerFontSizes) throws IOException {
        super();
        this.headerFontSizes = headerFontSizes;
    }

    @Override
    protected void writeString(String text, List<TextPosition> textPositions) throws IOException {
        if (!textPositions.isEmpty()) {
            float fontSize = textPositions.get(0).getFontSizeInPt();

            if (headerFontSizes.contains(fontSize)) {
                // Heading detected: Insert line break explicitly
                //System.out.println("Header detected: " + text);
                super.writeString("ß" + text.trim() + "ßß"); // Double line breaks for headers
            } else {
                // Normal text
                super.writeString(text.trim() + " ");
            }
        }
    }
}
