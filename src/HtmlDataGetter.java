import java.net.URL;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;

public class HtmlDataGetter {

    public static String getHtmlFromURL(String URL) {

        URL url;
        InputStream is = null;
        BufferedReader br;
        String line;

        try {
            url = new URL(URL);

            is = url.openStream();  // throws an IOException
            br = new BufferedReader(new InputStreamReader(is));

            StringBuilder texte = new StringBuilder();

            while ((line = br.readLine()) != null){
                texte.append(line);
            }

            return texte.toString();

        } catch (MalformedURLException mue) {
            mue.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (NullPointerException npe){
            npe.printStackTrace();
        } finally {
            try {
                if (is != null) is.close();
            } catch (IOException ioe) {
                // nothing to see here
            }
        }

        return "";
    }
}
