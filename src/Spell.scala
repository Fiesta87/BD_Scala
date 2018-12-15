class Spell(val nom: String, val DC : Int, val utilisationUnique : Boolean, var disponible : Boolean, val valeur1: Int, val valeur2 : Int, val valeur3 : Int) extends Serializable {

}

object Spell {
  val HEAL = "Heal"
  val MASS_HEAL = "Mass Heal"
  val FULL_ATTACK = "Full Attack"
  val BREATH_WEAPON = "Breath Weapon"
  val POWER_WORD_STUN = "Power Word Stun"
  val SUMMON_DIRE_TIGER = "Summon Dire Tiger"
  val CURE_MODERATE_WOUNDS = "Cure Moderate Wounds"
}