import scala.math.sqrt

class Combattant(val name : String, var pvActuel : Int, val pvMax : Int, val AC : Int, var positionX : Float, var positionY : Float, var positionZ : Float, val speed : Float, val msgsRetenu : Array[MessageChoixAction], var attaques: Array[Attaque], var spells: Array[Spell], var status : String, var nbTourStatus : Int , val SR : Int, val DR : Int, val Reflex : Int, val Will : Int) extends Serializable {

  // retourne le vecteur de déplacement qui doit être appliqué au combattant pour le rapprocher de sa cible
  def moveToward(combattant: Combattant): (Float, Float, Float) = {
    var dx = combattant.positionX - this.positionX
    var dy = combattant.positionY - this.positionY
    var dz = combattant.positionZ - this.positionZ

    val norme = sqrt(dx*dx + dy*dy + dz*dz).asInstanceOf[Float]

    var usefullSpeed : Float = speed

    if(speed > norme){
      usefullSpeed = norme
    }

    dx = (dx / norme) * usefullSpeed
    dy = (dy / norme) * usefullSpeed
    dz = (dz / norme) * usefullSpeed

    (dx,dy, dz)
  }

  def addAttaque(attaque: Attaque): Unit ={
    attaques = attaques :+ attaque
  }

  def addSpell(spell: Spell): Unit ={
    spells = spells :+ spell
  }
}

object Combattant{
  val STATUS_VIVANT = "vivant"
  val STATUS_STUNNED = "stunned"
}