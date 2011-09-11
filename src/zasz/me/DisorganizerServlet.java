package zasz.me;

import zasz.me.disorganizerfonts.DisorganizerFonts;
import zasz.me.enums.Style;
import zasz.me.enums.TagDisplayStrategy;
import zasz.me.enums.Theme;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

@WebServlet(name = "DisorganizerServlet")
public class DisorganizerServlet extends javax.servlet.http.HttpServlet
{
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {


    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        String DefaultWordList = "asp.net,15:games,10:fun,15:books,5:music,5:crapo,4:dota,5:concept,2:"
                + ".net,5:fiction,5:sci-fi,3:mystery,5:romance,4";

        HashMap<String, Integer> tags = new HashMap<String, Integer>();
        for (String s : Arrays.asList(DefaultWordList.split(":")))
        {
            if(s.trim().isEmpty()) continue;
            String[] split = s.split(",");
            tags.put(split[0], Integer.parseInt(split[1]));
        }
        Disorganizer disorganizer = new Disorganizer(tags, 500, 500);
        disorganizer.setAngle(0);
        disorganizer.setMargin(0d);
        disorganizer.setSelectedFont(DisorganizerFonts.getFont(DisorganizerFonts.KenyanCoffee(), getServletContext()));
        disorganizer.setDisplayChoice(DisplayStrategy.Get(TagDisplayStrategy.EqualHorizontalAndVertical()));
        disorganizer.setColorChoice(ColorStrategy.Get(Theme.DarkBgLightFg(), Style.Varied(), Color.BLACK, Color.GREEN));
        disorganizer.setVerticalTextRight(false);
        disorganizer.setShowWordBoundaries(false);
        disorganizer.setCrop(false);
        BufferedImage cloud = disorganizer.Construct();
        response.setContentType("image/png");
        ImageIO.write(cloud, "png", response.getOutputStream());
    }
}
