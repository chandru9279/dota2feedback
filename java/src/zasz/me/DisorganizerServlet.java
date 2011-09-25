package zasz.me;

import com.google.gson.Gson;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import zasz.me.disorganizerfonts.DisorganizerFonts;
import zasz.me.enums.Style;
import zasz.me.enums.TagDisplayStrategy;
import zasz.me.enums.Theme;
import zasz.me.models.TermsResponse;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "DisorganizerServlet")
public class DisorganizerServlet extends javax.servlet.http.HttpServlet
{
    DefaultHttpClient httpClient = new DefaultHttpClient();
    Gson gson = new Gson();
    private static final String url = "http://localhost:5000/solr/cloud?terms.fl=COMMENTS_OF_";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        response.setContentType("text/html");
        response.getWriter().println("Use the HTTP GET method for this URL");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        try
        {
            long getStartTime = System.nanoTime();
        String commentable = request.getParameter("commentable");
        if(null == commentable || commentable.trim().isEmpty()) die("commentable parameter is required");
            long startTime = System.nanoTime();
        TermsResponse solrResponse = gson.fromJson(doGetRequest(url + commentable), TermsResponse.class);
            long stopTime = System.nanoTime();
            long responseTime = stopTime - startTime;
        Map<String,Integer> map = solrResponse.toWeightedMap(); /* getSample() */
        if(0 == map.size()) die("No words from Solr.");
        Disorganizer disorganizer = new Disorganizer(map, 1000, 1000);
        disorganizer.setAngle(0); // Not tested yet, but just might work.
        disorganizer.setMargin(10d);
        disorganizer.setSpiralRoom(15); // This determines performance : higher = faster (but higer also = more room)
        disorganizer.setMaximumFontSize(130f);
        disorganizer.setMinimumFontSize(30f);
        disorganizer.setSelectedFont(DisorganizerFonts.getFont(DisorganizerFonts.KenyanCoffee()));
        disorganizer.setDisplayChoice(DisplayStrategy.Get(TagDisplayStrategy.RandomHorizontalOrVertical()));
        disorganizer.setColorChoice(ColorStrategy.Get(Theme.LightBgDarkFg(), Style.Varied(), Color.WHITE, Color.RED));
        disorganizer.setVerticalTextRight(true); // Not yet implemented ('getting out borders' part also not implemented)
        disorganizer.setShowWordBoundaries(false);
        disorganizer.setCrop(true);
            startTime = System.nanoTime();
        BufferedImage cloud = disorganizer.Construct();
            stopTime = System.nanoTime();
            long constructTime = stopTime - startTime;
        response.setContentType("image/png");
        ImageIO.write(cloud, "png", response.getOutputStream());
            long getTime = System.nanoTime() - getStartTime;
            System.out.println("GET TIME : " + getTime);
            System.out.println("SOLR RESPONSE : " + (responseTime / (double)getTime * 100) + " %");
            System.out.println("CONSTRUCT : " + (constructTime / (double)getTime * 100) + " %");
        }
        catch (Exception ex)
        {
            response.setContentType("text/html");
            response.getWriter().println("Exception : " + ex.getMessage());
        }
    }

    private void die(String message) throws Exception
    {
        throw new Exception(message);
    }

    public HashMap<String, Integer> getSample()
    {
        String DefaultWordList = "asp.net,15:games,10:fun,15:books,5:music,8:crapo,4:dota,5:concept,2:"
                        + ".net,9:fiction,2:sci-fi,3:mystery,5:romance,4";
        HashMap<String, Integer> tags = new HashMap<String, Integer>();
        for (String s : Arrays.asList(DefaultWordList.split(":")))
        {
            if(s.trim().isEmpty()) continue;
            String[] split = s.split(",");
            tags.put(split[0], Integer.parseInt(split[1]));
        }
        return tags;
    }

    private String doGetRequest(String url) throws IOException {
        HttpGet httpGet = new HttpGet(url);
        HttpResponse response = httpClient.execute(httpGet);
        HttpEntity entity = response.getEntity();
        String JSON = new BufferedReader(new InputStreamReader(entity.getContent())).readLine();
        // This utility method clears the InputStream of the entity making the httpClient available for the next request
        EntityUtils.consume(entity);
        return JSON;
    }
}
