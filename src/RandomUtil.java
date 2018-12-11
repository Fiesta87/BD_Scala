import java.util.Random;

public class RandomUtil {

    private static Random random = new Random();

    public static int getRandomInt(int range) {
        return random.nextInt(range);
    }
}
