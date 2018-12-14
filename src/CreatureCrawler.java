import java.util.ArrayList;
import java.util.List;

public class CreatureCrawler {

    public static List<Creature> findCreatures () {

        System.out.println("Crawling started");

        List<Creature> result = new ArrayList<>();

        String urlBestiary1 = "http://legacy.aonprd.com/bestiary/monsterIndex.html";

        String urlBestiary2 = "http://legacy.aonprd.com/bestiary2/additionalMonsterIndex.html";

        CreatureCrawler.addCreaturesFromBestiaryURL(result, urlBestiary1);

        CreatureCrawler.addCreaturesFromBestiaryURL(result, urlBestiary2);

        System.out.println("Crawling done");
        System.out.println(result.size() + " creatures founded");

        return result;
    }

    private static void addCreaturesFromBestiaryURL (List<Creature> list, String url) {

        System.out.println("Bestiary : " + url);

        String index = HtmlDataGetter.getHtmlFromURL(url);

        index = index.split("[<]div id=\"monster-index-wrapper\" class=\"index\"[>]")[1];

        String[] creatureEntries = index.split("<li><a href=\"");

        System.out.println(creatureEntries.length-1 + " creatures pages to check");

        for(int entryIndex=1; entryIndex<creatureEntries.length; entryIndex++) {

            System.out.println("Check page " + entryIndex + " of " + (creatureEntries.length-1));

            String creaturePageName = creatureEntries[entryIndex].split("\"")[0];

            String urlCreaturePage = url.substring(0, url.length() - url.split("/")[4].length()) + creaturePageName;

            String creaturePage = HtmlDataGetter.getHtmlFromURL(urlCreaturePage);

            String[] pageSubCreatures = creaturePage.split("<h1");

            for(int i=1; i<pageSubCreatures.length; i++) {

                String name = pageSubCreatures[i].split(">")[1].split("</h1")[0].replace("," , "");

                Creature creature = new Creature(name);

                String[] spells = pageSubCreatures[i].split("<a href=\"..[/]coreRulebook[/]spells[/]");

                for(int j=1; j<spells.length; j++) {

                    creature.addspell(spells[j].split(">")[1].split("</a")[0]);
                }

                if(spells.length > 0) {
                    list.add(creature);
                }
            }
        }
    }
}
