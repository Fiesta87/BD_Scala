public class MessageFactory {

    public static MessageChoixAction makeMessageChoixAction (float distance, String action, long cible) {
        return new MessageChoixAction(distance, action, cible, 0L, 0L, "");
    }

    public static MessageChoixAction makeMessageChoixAction (float distance, String action, long cible, String extraInfo) {
        return new MessageChoixAction(distance, action, cible, 0L, 0L, extraInfo);
    }

    public static MessageChoixAction makeMessageChoixAction (float distance, String action, long cible, long cible2, long cible3, String extraInfo) {
        return new MessageChoixAction(distance, action, cible, cible2, cible3, extraInfo);
    }

    public static MessageRealisationAction makeMessageRealisationAction (String action, String combattant, long idCombattant) {
        return new MessageRealisationAction(action, combattant, idCombattant, 0, 0, 0, "");
    }

    public static MessageRealisationAction makeMessageRealisationAction (String action, String combattant, long idCombattant, String extraInfo) {
        return new MessageRealisationAction(action, combattant, idCombattant, 0, 0, 0, extraInfo);
    }

    public static MessageRealisationAction makeMessageRealisationAction (String action, String combattant, long idCombattant, float valeur1) {
        return new MessageRealisationAction(action, combattant, idCombattant, valeur1, 0, 0, "");
    }

    public static MessageRealisationAction makeMessageRealisationAction (String action, String combattant, long idCombattant, float valeur1, String extraInfo) {
        return new MessageRealisationAction(action, combattant, idCombattant, valeur1, 0, 0, extraInfo);
    }

    public static MessageRealisationAction makeMessageRealisationAction (String action, String combattant, long idCombattant, float valeur1, float valeur2) {
        return new MessageRealisationAction(action, combattant, idCombattant, valeur1, valeur2, 0, "");
    }

    public static MessageRealisationAction makeMessageRealisationAction (String action, String combattant, long idCombattant, float valeur1, float valeur2, String extraInfo) {
        return new MessageRealisationAction(action, combattant, idCombattant, valeur1, valeur2, 0, extraInfo);
    }

    public static MessageRealisationAction makeMessageRealisationAction (String action, String combattant, long idCombattant, float valeur1, float valeur2, float valeur3) {
        return new MessageRealisationAction(action, combattant, idCombattant, valeur1, valeur2, valeur3, "");
    }

    public static MessageRealisationAction makeMessageRealisationAction (String action, String combattant, long idCombattant, float valeur1, float valeur2, float valeur3, String extraInfo) {
        return new MessageRealisationAction(action, combattant, idCombattant, valeur1, valeur2, valeur3, extraInfo);
    }
}
