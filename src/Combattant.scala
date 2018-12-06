import scala.math.sqrt

class Combattant(val name : String, var pvActuel : Int, val pvMax : Int, val AC : Int, var positionX : Float, var positionY : Float, var positionZ : Float, val speed : Float, val msgsRetenu : Array[MessageChoixAction]) extends Serializable {

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
}