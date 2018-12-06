public class MessageFactory {

    public static MessageChoixAction makeMessageChoixAction (float distance, String action, long cible) {
        return new MessageChoixAction(distance, action, cible);
    }

    public static MessageRealisationAction makeMessageRealisationAction (String action, float valeur1) {
        return new MessageRealisationAction(action, valeur1, 0, 0);
    }

    public static MessageRealisationAction makeMessageRealisationAction (String action, float valeur1, float valeur2) {
        return new MessageRealisationAction(action, valeur1, valeur2, 0);
    }

    public static MessageRealisationAction makeMessageRealisationAction (String action, float valeur1, float valeur2, float valeur3) {
        return new MessageRealisationAction(action, valeur1, valeur2, valeur3);
    }
}
