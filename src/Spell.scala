class Spell(val nom: String, val DC : Int, val utilisationUnique : Boolean, val valeur1: Int, val valeur2 : Int, val valeur3 : Int) extends Serializable {

}

object Spell {
  val HEAL = "Heal"
  val MASS_HEAL = "Mass Heal"
  val FULL_ATTACK = "Full Attack"
  val BREATH_WEAPON = "Breath weapon"
  val POWER_WORD_STUN = "power word stun"
  val CURE_MODERATE_WOUNDS = "cure moderate wounds"
}