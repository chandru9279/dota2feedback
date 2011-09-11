package zasz.me.disorganizerfonts;

import java.awt.*;
import java.io.IOException;

public enum DisorganizerFonts
{

    AlphaFridgeMagnets,
    AlphaFridgeMagnetsAllCaps,
    Gnuolane,
    KenyanCoffee,
    Sexsmith,
    Steelfish,
    VenusRising;

    public static Font GetFont(DisorganizerFonts FontName) throws IOException, FontFormatException
    {
       return Font.createFont(
               Font.TRUETYPE_FONT,
               DisorganizerFonts.class.getResourceAsStream(FontName.name() + ".ttf")
       );
    }
}

