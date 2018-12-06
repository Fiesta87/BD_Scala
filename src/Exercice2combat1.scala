import org.apache.spark.graphx._
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

import scala.math.sqrt

object Exercice2combat1 extends App
{
  val conf = new SparkConf().setAppName("Exercice2").setMaster("local[*]")
  val sc = new SparkContext(conf)
  sc.setLogLevel("ERROR")

  var atLeastOneFoeRelation : Boolean = false

  def exercice2combat1() : Unit = {

    println("Populating armies")

    val ANGEL_SOLAR = "Angel Solar"

    var vertexArray = Array((0L, CombattantFactory.makeSolar()))

    val ALLY = true
    val FOE = false
    var edgeArray = Array[Edge[Boolean]]()

    val ORC_WORG_RIDER = "Orc Worg Rider"

    for( i <- 1L to 9L){
      vertexArray = vertexArray :+ (i, CombattantFactory.makeOrcWorgRider())
      edgeArray = edgeArray :+ Edge(0L, i, FOE)
      edgeArray = edgeArray :+ Edge(i, 0L, FOE)
    }

    val ORC_BRUTAL_WARLORD = "Orc Brutal Warlord"

    vertexArray = vertexArray :+ (10L, CombattantFactory.makeOrcBrutalWarlord())
    edgeArray = edgeArray :+ Edge(0L, 10L, FOE)
    edgeArray = edgeArray :+ Edge(10L, 0L, FOE)

    val ORC_DOUBLE_AXE_FURY = "Orc Double Axe Fury"

    for( i <- 11L to 14L){
      vertexArray = vertexArray :+ (i, CombattantFactory.makeOrcDoubleAxeFury())
      edgeArray = edgeArray :+ Edge(0L, i, FOE)
      edgeArray = edgeArray :+ Edge(i, 0L, FOE)
    }

    val vertexRDD: RDD[(Long, Combattant)] = sc.parallelize(vertexArray)
    val edgeRDD: RDD[Edge[Boolean]] = sc.parallelize(edgeArray)
    var myGraph: Graph[Combattant, Boolean] = Graph(vertexRDD, edgeRDD)

    val ACTION_ATTAQUER = "attaquer"
    val ACTION_HEAL = "heal"
    val ACTION_DEPLACEMENT = "deplacement"

    // messages des actions possibles / demandées
    def sendMessagesChoixAction(ctx: EdgeContext[Combattant, Boolean, Array[MessageChoixAction]]): Unit = {

      if(ctx.srcAttr.pvActuel > 0 && ctx.dstAttr.pvActuel > 0){

        if(ctx.attr == FOE) {

          atLeastOneFoeRelation = true

          val distance = calculeDistance(ctx.srcAttr, ctx.dstAttr)

          if(ctx.srcAttr.name == ANGEL_SOLAR) {

            if(distance <= 110){
              ctx.sendToSrc(Array(MessageFactory.makeMessageChoixAction(distance, ACTION_ATTAQUER, ctx.dstId)))
            }
          }

          else {

            if(distance > 10) {
              ctx.sendToSrc(Array(MessageFactory.makeMessageChoixAction(distance, ACTION_DEPLACEMENT, ctx.dstId)))
            }

            else {
              ctx.sendToSrc(Array(MessageFactory.makeMessageChoixAction(distance, ACTION_ATTAQUER, ctx.dstId)))
            }
          }
        }
      }
    }

    def selectBestAction(msgs1: Array[MessageChoixAction], msgs2: Array[MessageChoixAction]): Array[MessageChoixAction] = {

      // On priorise le heal
      if(msgs1(0).action == ACTION_HEAL) return msgs1
      if(msgs2(0).action == ACTION_HEAL) return msgs2

      // si on doit choisir entre se déplacer et attaquer, on attaque
      if(msgs1(0).action == ACTION_DEPLACEMENT && msgs2(0).action == ACTION_ATTAQUER) return msgs2
      if(msgs2(0).action == ACTION_DEPLACEMENT && msgs1(0).action == ACTION_ATTAQUER) return msgs1

      // si on a que des messages de déplacement, on va vers le plus proche
      if(msgs1(0).action == ACTION_DEPLACEMENT && msgs2(0).action == ACTION_DEPLACEMENT){
        if(msgs2(0).distance > msgs1(0).distance) return msgs1
        else return msgs2
      }

      // sinon on tri par ordre de proximité les attaques à effectuer

      val msgs = msgs1 ++ msgs2

      // on cherche la distance vers l'ennemi le plus proche
      var closestDistance : Float = -1.0f
      msgs.foreach( msg => {
        if(msg != null && (closestDistance == -1.0f || msg.distance < closestDistance)){
          closestDistance = msg.distance
        }
      })

      var maxDistance : Float = 110.0f

      // si l'ennemie le plus proche est à moins de 10ft, on ne peut attaquer que ceux à moins de 10ft, 4 fois au plus (car le combattant qui attaque le plus attaque 4 fois)
      if(closestDistance <= 10.0f) {
        maxDistance = 10.0f
      }

      var ennemieNumero1 : MessageChoixAction = null
      var ennemieNumero2 : MessageChoixAction = null
      var ennemieNumero3 : MessageChoixAction = null
      var ennemieNumero4 : MessageChoixAction = null

      msgs.foreach( msg => {

        if(msg != null && msg.distance <= maxDistance){

          if(ennemieNumero1 == null){
            ennemieNumero1 = msg
          }
          else if(msg.distance <= ennemieNumero1.distance){

            // on décale dans la liste
            ennemieNumero4 = ennemieNumero3
            ennemieNumero3 = ennemieNumero2
            ennemieNumero2 = ennemieNumero1

            ennemieNumero1 = msg
          }
          else if(ennemieNumero2 == null){
            ennemieNumero2 = msg
          }
          else if(msg.distance <= ennemieNumero2.distance){

            // on décale dans la liste
            ennemieNumero4 = ennemieNumero3
            ennemieNumero3 = ennemieNumero2

            ennemieNumero2 = msg
          }
          else if(ennemieNumero3 == null){
            ennemieNumero3 = msg
          }
          else if(msg.distance <= ennemieNumero3.distance){

            // on décale dans la liste
            ennemieNumero4 = ennemieNumero3

            ennemieNumero3 = msg
          }
          else if(ennemieNumero4 == null){
            ennemieNumero4 = msg
          }
          else if(msg.distance <= ennemieNumero4.distance){

            ennemieNumero4 = msg
          }
        }
      })

      val msgsResult = Array[MessageChoixAction](ennemieNumero1, ennemieNumero2, ennemieNumero3, ennemieNumero4)

      msgsResult
    }

    def calculeDistance(combattant1: Combattant, combattant2: Combattant) : Float = {
      val dx = combattant1.positionX - combattant2.positionX
      val dy = combattant1.positionY - combattant2.positionY
      val dz = combattant1.positionZ - combattant2.positionZ

      sqrt(dx*dx + dy*dy + dz*dz).asInstanceOf[Float]
    }

    def sendMessagesRealisationAction(ctx: EdgeContext[Combattant, Boolean, Array[MessageRealisationAction]]): Unit = {

      // si pas de message retenu, on ne fait rien
      if(ctx.srcAttr.msgsRetenu == null || ctx.srcAttr.msgsRetenu.length == 0) return

      // si on a retenu une ou plusieurs attaques
      if(ctx.srcAttr.msgsRetenu(0).action == ACTION_ATTAQUER){

        // si on est le angel solar
        if(ctx.srcAttr.name == ANGEL_SOLAR) {

          // max 4 attaques
          val nbAttaqueMax : Int = 4
          var nbAttaqueDone : Int = 0

          // tant qu'on a pas fait les 4 attaques
          while(nbAttaqueDone < nbAttaqueMax){

            // pour chaque ennemi attaquable
            ctx.srcAttr.msgsRetenu.foreach(msg => {

              // si on a pas dépassé le nombre d'attaque max
              if(msg != null && nbAttaqueDone < nbAttaqueMax){

                // et que la l'ennemie traité est celui de cet arc du graphe
                if(msg.cible == ctx.dstId) {

                  // on attaque soit au CàC soit à distance avec une diminution de notre bonus pour toucher au fur et à mesure des attaques

                  if(msg.distance <= 10) {  // attaque au CàC avec greatsword

                    var toucheValeur : Int = 0
                    if(nbAttaqueDone == 0)      toucheValeur = DiceCalculator.jetToucher(35)
                    else if(nbAttaqueDone == 1) toucheValeur = DiceCalculator.jetToucher(30)
                    else if(nbAttaqueDone == 2) toucheValeur = DiceCalculator.jetToucher(25)
                    else if(nbAttaqueDone == 3) toucheValeur = DiceCalculator.jetToucher(20)

                    ctx.sendToDst(Array(MessageFactory.makeMessageRealisationAction(ACTION_ATTAQUER, toucheValeur, DiceCalculator._x_Dy_plus_z_(3, 6, 18))))

                  } else if(msg.distance <= 110) {  // attaque à distance avec Arc

                    var toucheValeur : Int = 0
                    if(nbAttaqueDone == 0)      toucheValeur = DiceCalculator.jetToucher(31)
                    else if(nbAttaqueDone == 1) toucheValeur = DiceCalculator.jetToucher(26)
                    else if(nbAttaqueDone == 2) toucheValeur = DiceCalculator.jetToucher(21)
                    else if(nbAttaqueDone == 3) toucheValeur = DiceCalculator.jetToucher(16)

                    ctx.sendToDst(Array(MessageFactory.makeMessageRealisationAction(ACTION_ATTAQUER, toucheValeur, DiceCalculator._x_Dy_plus_z_(2, 6, 14))))
                  }
                }

                nbAttaqueDone = nbAttaqueDone + 1
              }
            })
          }
        }

        // cet orc n'a qu'une seule attaque
        else if(ctx.srcAttr.name == ORC_WORG_RIDER) {

          // si l'ennemie le plus proche est celui de cet arc du graphe
          if(ctx.srcAttr.msgsRetenu(0).cible == ctx.dstId) {
            ctx.sendToDst(Array(MessageFactory.makeMessageRealisationAction(ACTION_ATTAQUER, DiceCalculator.jetToucher(6), DiceCalculator._x_Dy_plus_z_(1, 8, 2))))
          }
        }

        // cet orc a 3 attaques
        else if(ctx.srcAttr.name == ORC_BRUTAL_WARLORD) {

          // max 3 attaques
          val nbAttaqueMax : Int = 3
          var nbAttaqueDone : Int = 0

          // tant qu'on a pas fait les 3 attaques
          while(nbAttaqueDone < nbAttaqueMax){

            // pour chaque ennemi attaquable
            ctx.srcAttr.msgsRetenu.foreach(msg => {

              // si on a pas dépassé le nombre d'attaque max
              if(msg != null && nbAttaqueDone < nbAttaqueMax){

                // et que la l'ennemie traité est celui de cet arc du graphe
                if(msg.cible == ctx.dstId) {

                  // on attaque au CàC avec une diminution de notre bonus pour toucher au fur et à mesure des attaques

                  var toucheValeur : Int = 0
                  if(nbAttaqueDone == 0)      toucheValeur = DiceCalculator.jetToucher(20)
                  else if(nbAttaqueDone == 1) toucheValeur = DiceCalculator.jetToucher(15)
                  else if(nbAttaqueDone == 2) toucheValeur = DiceCalculator.jetToucher(10)

                  ctx.sendToDst(Array(MessageFactory.makeMessageRealisationAction(ACTION_ATTAQUER, toucheValeur, DiceCalculator._x_Dy_plus_z_(1, 8, 10))))
                }

                nbAttaqueDone = nbAttaqueDone + 1
              }
            })
          }
        }

        // cet orc a 3 attaques
        else if(ctx.srcAttr.name == ORC_DOUBLE_AXE_FURY) {

          // max 3 attaques
          val nbAttaqueMax : Int = 3
          var nbAttaqueDone : Int = 0

          // tant qu'on a pas fait les 3 attaques
          while(nbAttaqueDone < nbAttaqueMax){

            // pour chaque ennemi attaquable
            ctx.srcAttr.msgsRetenu.foreach(msg => {

              // si on a pas dépassé le nombre d'attaque max
              if(msg != null && nbAttaqueDone < nbAttaqueMax){

                // et que la l'ennemie traité est celui de cet arc du graphe
                if(msg.cible == ctx.dstId) {

                  // on attaque au CàC avec une diminution de notre bonus pour toucher au fur et à mesure des attaques

                  var toucheValeur : Int = 0
                  if(nbAttaqueDone == 0)      toucheValeur = DiceCalculator.jetToucher(19)
                  else if(nbAttaqueDone == 1) toucheValeur = DiceCalculator.jetToucher(14)
                  else if(nbAttaqueDone == 2) toucheValeur = DiceCalculator.jetToucher(9)

                  ctx.sendToDst(Array(MessageFactory.makeMessageRealisationAction(ACTION_ATTAQUER, toucheValeur, DiceCalculator._x_Dy_plus_z_(1, 8, 10))))
                }

                nbAttaqueDone = nbAttaqueDone + 1
              }
            })
          }
        }
      }

      else if(ctx.srcAttr.msgsRetenu(0).cible == ctx.dstId) {

        if(ctx.srcAttr.msgsRetenu(0).action == ACTION_HEAL) {

          // test de qui on est pour la valeur du heal
          ctx.sendToSrc(Array(MessageFactory.makeMessageRealisationAction(ACTION_HEAL, 0)))

        } else if(ctx.srcAttr.msgsRetenu(0).action == ACTION_DEPLACEMENT) {

          val deplacement = ctx.srcAttr.moveToward(ctx.dstAttr)

          ctx.sendToSrc(Array(MessageFactory.makeMessageRealisationAction(ACTION_DEPLACEMENT, deplacement._1, deplacement._2, deplacement._3)))

        }
      }
    }

    def combineAction(msg1: Array[MessageRealisationAction], msg2: Array[MessageRealisationAction]): Array[MessageRealisationAction] = {

      msg1 ++ msg2
    }

    println("Fighting")

    var tourCombat = 1

    while(true){

      val messagesChoixActions = myGraph.aggregateMessages[Array[MessageChoixAction]](
        sendMessagesChoixAction,
        selectBestAction,
        TripletFields.All
      )

      if (messagesChoixActions.isEmpty() || !atLeastOneFoeRelation) {
        println("")
        println("#########")
        println("Fin du combat au tour " + (tourCombat-1))
        println("#########")
        return
      }

      atLeastOneFoeRelation = false

      println("")
      println("#########")
      println("Tour " + tourCombat)
      println("#########")
      println("")

      myGraph = myGraph.joinVertices(messagesChoixActions)(
        (id, combattant, msgsRetenu) => {

          val combattantResult = CombattantFactory.copyCombattantWithMsg(combattant, msgsRetenu)

          combattantResult
        })

      var affichageChoixActions = true

      myGraph.vertices.collect.foreach { case (id, combattant: Combattant) => {

        if(affichageChoixActions){
          affichageChoixActions = false
          println("Choix des Actions")
          println("")
        }

        if(combattant.msgsRetenu != null) {
          println(combattant.name + " " + id + " : ")
          combattant.msgsRetenu.foreach(msg => {
            if(msg != null){
              println("  - " + msg.action + ", cible : " + msg.cible)
            }
          })
          println("")
        }
      }}

      println("***************************************")

      val messagesRealisationActions = myGraph.aggregateMessages[Array[MessageRealisationAction]](
        sendMessagesRealisationAction,
        combineAction,
        TripletFields.All
      )

      myGraph = myGraph.joinVertices(messagesRealisationActions)(
        (id, combattant, msgs) => {

          val combattantResult = CombattantFactory.copyCombattant(combattant)

          msgs.foreach( msg => {

            //println(msg.action + " : " + combattantResult.name + " " + id)

            if(msg.action == ACTION_ATTAQUER) {

              if(msg.valeur1 >= combattantResult.AC) {
                combattantResult.pvActuel = combattantResult.pvActuel - msg.valeur2.asInstanceOf[Int]
                if(combattantResult.pvActuel < 0){
                  combattantResult.pvActuel = 0
                }
              }
            }

            else if(msg.action == ACTION_HEAL) {
              combattantResult.pvActuel = combattantResult.pvActuel + msg.valeur2.asInstanceOf[Int]
              if(combattantResult.pvActuel > combattantResult.pvMax){
                combattantResult.pvActuel = combattantResult.pvMax
              }
            }

            else if(msg.action == ACTION_DEPLACEMENT) {
              combattantResult.positionX = combattantResult.positionX + msg.valeur1
              combattantResult.positionY = combattantResult.positionY + msg.valeur2
              combattantResult.positionZ = combattantResult.positionZ + msg.valeur3
            }
          })

          combattantResult
        })

      var affichageStatus = true

      myGraph.vertices.collect.foreach { case (id, combattant: Combattant) => {

        if(affichageStatus){
          affichageStatus = false
          println("")
          println("Status")
          println("")
          println("En vie :")
        }

        if(combattant.pvActuel > 0){
          println("  - " + combattant.name + " " + id  + " : " + combattant.pvActuel + "/" + combattant.pvMax)
        }
      }}

      affichageStatus = true

      myGraph.vertices.collect.foreach { case (id, combattant: Combattant) => {

        if(affichageStatus){
          affichageStatus = false
          println("")
          println("Mort :")
        }

        if(combattant.pvActuel <= 0){
          println("  - " + combattant.name + " " + id)
        }
      }}

      tourCombat = tourCombat + 1
    }
  }

  exercice2combat1()

}
