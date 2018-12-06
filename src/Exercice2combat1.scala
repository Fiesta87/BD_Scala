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

    val ALLY = "ALLY"
    val FOE = "FOE"
    val MY_SELF = "MY_SELF"
    var edgeArray = Array[Edge[String]]()

    edgeArray = edgeArray :+ Edge(0L, 0L, MY_SELF)

    val ORC_WORG_RIDER = "Orc Worg Rider"

    for( i <- 1L to 9L){
      vertexArray = vertexArray :+ (i, CombattantFactory.makeOrcWorgRider())
      edgeArray = edgeArray :+ Edge(0L, i, FOE)
      edgeArray = edgeArray :+ Edge(i, 0L, FOE)
      edgeArray = edgeArray :+ Edge(i, i, MY_SELF)
    }

    val ORC_BRUTAL_WARLORD = "Orc Brutal Warlord"

    vertexArray = vertexArray :+ (10L, CombattantFactory.makeOrcBrutalWarlord())
    edgeArray = edgeArray :+ Edge(0L, 10L, FOE)
    edgeArray = edgeArray :+ Edge(10L, 0L, FOE)
    edgeArray = edgeArray :+ Edge(10L, 10L, MY_SELF)

    val ORC_DOUBLE_AXE_FURY = "Orc Double Axe Fury"

    for( i <- 11L to 14L){
      vertexArray = vertexArray :+ (i, CombattantFactory.makeOrcDoubleAxeFury())
      edgeArray = edgeArray :+ Edge(0L, i, FOE)
      edgeArray = edgeArray :+ Edge(i, 0L, FOE)
      edgeArray = edgeArray :+ Edge(i, i, MY_SELF)
    }

    val vertexRDD: RDD[(Long, Combattant)] = sc.parallelize(vertexArray)
    val edgeRDD: RDD[Edge[String]] = sc.parallelize(edgeArray)
    var myGraph: Graph[Combattant, String] = Graph(vertexRDD, edgeRDD)

    val ACTION_ATTAQUER = "attaquer"
    val ACTION_HEAL = "heal"
    val ACTION_DEPLACEMENT = "deplacement"
    val ACTION_REGENERATION = "regeneration"

    // messages des actions possibles / demandées
    def sendMessagesChoixAction(ctx: EdgeContext[Combattant, String, Array[MessageChoixAction]]): Unit = {

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

        else if(ctx.attr == ALLY) {


        }

        else if(ctx.attr == MY_SELF) {

          if(ctx.srcAttr.name == ANGEL_SOLAR) {

            ctx.sendToSrc(Array(MessageFactory.makeMessageChoixAction(0.0f, ACTION_REGENERATION, ctx.srcId)))
          }
        }
      }
    }

    def selectBestAction(msgs1: Array[MessageChoixAction], msgs2: Array[MessageChoixAction]): Array[MessageChoixAction] = {

      val msgsMerged = msgs1 ++ msgs2

      // on cherche quel est le degrès de priorité le plus important
      val plusHautePrioriteAction : String = getPlusHautePrioriteAction(msgsMerged)

      // si le plus important est la régénération, c'est que l'on a que des régénérations, donc on envoie tout
      if(plusHautePrioriteAction == ACTION_REGENERATION) {
        return msgsMerged
      }

      // si le plus important est un déplacement, on choisit le déplacement vers la cible la plus proche
      else if(plusHautePrioriteAction == ACTION_DEPLACEMENT) {

        // on cherche la distance vers la cible la plus proche pour l'action de déplacement
        val closestDistance : Float = getClosestDistance(msgsMerged, ACTION_DEPLACEMENT)

        var msgsResult = Array[MessageChoixAction]()

        // on ne se déplacera qu'une seule fois
        var deplacementChoisit : Boolean = false

        msgsMerged.foreach(msg => {

          // on stack toujours les régénération
          if(msg.action == ACTION_REGENERATION) {
            msgsResult = msgsResult :+ msg
          }

          if(msg.action == ACTION_DEPLACEMENT && msg.distance == closestDistance && ! deplacementChoisit) {
            deplacementChoisit = true
            msgsResult = msgsResult :+ msg
          }
        })

        return msgsResult
      }

      // si le plus important est d'attaquer, on liste les ennemies par ordre de proximité
      else if(plusHautePrioriteAction == ACTION_ATTAQUER) {

        val closestDistance : Float = getClosestDistance(msgsMerged, ACTION_ATTAQUER)

        var maxDistance : Float = 110.0f

        // si l'ennemie le plus proche est à moins de 10ft, on ne peut attaquer que ceux à moins de 10ft, 6 fois au plus (car le combattant qui attaque le plus attaque 6 fois)
        if(closestDistance <= 10.0f) {
          maxDistance = 10.0f
        }

        var ennemieNumero1 : MessageChoixAction = null
        var ennemieNumero2 : MessageChoixAction = null
        var ennemieNumero3 : MessageChoixAction = null
        var ennemieNumero4 : MessageChoixAction = null
        var ennemieNumero5 : MessageChoixAction = null
        var ennemieNumero6 : MessageChoixAction = null

        var msgsResult = Array[MessageChoixAction]()

        msgsMerged.foreach( msg => {

          // on conserve toutes les régénérations
          if(msg != null && msg.action == ACTION_REGENERATION){
            msgsResult = msgsResult :+ msg
          }

          else if(msg != null && msg.action == ACTION_ATTAQUER && msg.distance <= maxDistance){

            if(ennemieNumero1 == null){
              ennemieNumero1 = msg
            }
            else if(msg.distance <= ennemieNumero1.distance){

              // on décale dans la liste
              ennemieNumero6 = ennemieNumero5
              ennemieNumero5 = ennemieNumero4
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
              ennemieNumero6 = ennemieNumero5
              ennemieNumero5 = ennemieNumero4
              ennemieNumero4 = ennemieNumero3
              ennemieNumero3 = ennemieNumero2

              ennemieNumero2 = msg
            }
            else if(ennemieNumero3 == null){
              ennemieNumero3 = msg
            }
            else if(msg.distance <= ennemieNumero3.distance){

              // on décale dans la liste
              ennemieNumero6 = ennemieNumero5
              ennemieNumero5 = ennemieNumero4
              ennemieNumero4 = ennemieNumero3

              ennemieNumero3 = msg
            }
            else if(ennemieNumero4 == null){
              ennemieNumero4 = msg
            }
            else if(msg.distance <= ennemieNumero4.distance){

              // on décale dans la liste
              ennemieNumero6 = ennemieNumero5
              ennemieNumero5 = ennemieNumero4

              ennemieNumero4 = msg
            }
            else if(ennemieNumero5 == null){
              ennemieNumero5 = msg
            }
            else if(msg.distance <= ennemieNumero5.distance){

              // on décale dans la liste
              ennemieNumero6 = ennemieNumero5

              ennemieNumero5 = msg
            }
            else if(ennemieNumero6 == null){
              ennemieNumero6 = msg
            }
            else if(msg.distance <= ennemieNumero6.distance){

              ennemieNumero6 = msg
            }
          }
        })

        // on retrourne les régénérations et les attaques sélectionnées
        return msgsResult ++ Array[MessageChoixAction](ennemieNumero1, ennemieNumero2, ennemieNumero3, ennemieNumero4, ennemieNumero5, ennemieNumero6)
      }
      // si le plus important est de heal, on heal le plus proche
      else if(plusHautePrioriteAction == ACTION_HEAL) {

        // on cherche la distance vers la cible la plus proche
        val closestDistance : Float = getClosestDistance(msgsMerged, ACTION_HEAL)

        var msgsResult = Array[MessageChoixAction]()

        // on ne healera qu'une seule fois
        var healChoisit : Boolean = false

        msgsMerged.foreach(msg => {

          // on stack toujours les régénération
          if(msg.action == ACTION_REGENERATION) {
            msgsResult = msgsResult :+ msg
          }

          if(msg.action == ACTION_HEAL && msg.distance == closestDistance && ! healChoisit) {
            healChoisit = true
            msgsResult = msgsResult :+ msg
          }
        })

        return msgsResult
      }

      // retour par défaut qui ne sera jamais exécuté mais permet de compiler
      msgsMerged
    }

    def calculeDistance(combattant1: Combattant, combattant2: Combattant) : Float = {

      calculeNorme(combattant1.positionX - combattant2.positionX, combattant1.positionY - combattant2.positionY, combattant1.positionZ - combattant2.positionZ)
    }

    def calculeNorme(dx : Float, dy : Float, dz : Float) : Float = {

      sqrt(dx*dx + dy*dy + dz*dz).asInstanceOf[Float]
    }

    def getClosestDistance(msgs : Array[MessageChoixAction], action : String) : Float = {
      var closestDistance : Float = -1.0f
      msgs.foreach( msg => {
        if(msg != null && (msg.action == action || action == null) && (closestDistance == -1.0f || msg.distance < closestDistance)){
          closestDistance = msg.distance
        }
      })

      closestDistance
    }

    def getPlusHautePrioriteAction(msgs : Array[MessageChoixAction]) : String = {

      var plusHautePrioriteAction : String = ACTION_REGENERATION

      // pour tout les message qu'on a à merge
      msgs.foreach(msg => {

        if(msg == null){
          // nothing to do
        }

        else if(msg.action == ACTION_HEAL) {
          plusHautePrioriteAction = ACTION_HEAL
        }

        else if(plusHautePrioriteAction != ACTION_HEAL && msg.action == ACTION_ATTAQUER) {
          plusHautePrioriteAction = ACTION_ATTAQUER
        }

        else if(plusHautePrioriteAction != ACTION_HEAL && plusHautePrioriteAction != ACTION_ATTAQUER && msg.action == ACTION_DEPLACEMENT) {
          plusHautePrioriteAction = ACTION_DEPLACEMENT
        }
      })

      plusHautePrioriteAction
    }

    def sendMessagesRealisationAction(ctx: EdgeContext[Combattant, String, Array[MessageRealisationAction]]): Unit = {

      // si pas de message retenu, on ne fait rien
      if(ctx.srcAttr.msgsRetenu == null || ctx.srcAttr.msgsRetenu.length == 0) return

      // on cherche quel est le degrès de priorité le plus important
      val plusHautePrioriteAction : String = getPlusHautePrioriteAction(ctx.srcAttr.msgsRetenu)

      // on traite tout les messages de régénération
      ctx.srcAttr.msgsRetenu.foreach(msg => {

        if(msg != null && msg.action == ACTION_REGENERATION && msg.cible == ctx.dstId) {

          // seul le solar est capable de se régénérer
          if(ctx.srcAttr.name == ANGEL_SOLAR) {
            ctx.sendToSrc(Array(MessageFactory.makeMessageRealisationAction(ACTION_REGENERATION, ctx.srcAttr.name, ctx.srcId, 15)))
          }
        }
      })

      // si on a retenu une ou plusieurs attaques
      if(plusHautePrioriteAction == ACTION_ATTAQUER){

        // si on est le angel solar
        if(ctx.srcAttr.name == ANGEL_SOLAR) {

          // max 4 attaques
          val nbAttaqueMax : Int = 4
          var nbAttaqueDone : Int = 0

          // tant qu'on a pas fait les 4 attaques
          while(nbAttaqueDone < nbAttaqueMax){

            // pour chaque ennemi attaquable
            ctx.srcAttr.msgsRetenu.foreach(msg => {

              // si le message est bien une attaque et pas une régénération
              if(msg != null && msg.action == ACTION_ATTAQUER){

                // si on a pas dépassé le nombre d'attaque max
                if(nbAttaqueDone < nbAttaqueMax){

                  // et que la l'ennemie traité est celui de cet arc du graphe
                  if(msg.cible == ctx.dstId) {

                    // on attaque soit au CàC soit à distance avec une diminution de notre bonus pour toucher au fur et à mesure des attaques

                    if(msg.distance <= 10) {  // attaque au CàC avec greatsword

                      var toucheValeur : Int = 0
                      if(nbAttaqueDone == 0)      toucheValeur = DiceCalculator.jetToucher(35)
                      else if(nbAttaqueDone == 1) toucheValeur = DiceCalculator.jetToucher(30)
                      else if(nbAttaqueDone == 2) toucheValeur = DiceCalculator.jetToucher(25)
                      else if(nbAttaqueDone == 3) toucheValeur = DiceCalculator.jetToucher(20)

                      ctx.sendToDst(Array(MessageFactory.makeMessageRealisationAction(ACTION_ATTAQUER, ctx.srcAttr.name, ctx.srcId, toucheValeur, DiceCalculator._x_Dy_plus_z_(3, 6, 18))))

                    } else if(msg.distance <= 110) {  // attaque à distance avec Arc

                      var toucheValeur : Int = 0
                      if(nbAttaqueDone == 0)      toucheValeur = DiceCalculator.jetToucher(31)
                      else if(nbAttaqueDone == 1) toucheValeur = DiceCalculator.jetToucher(26)
                      else if(nbAttaqueDone == 2) toucheValeur = DiceCalculator.jetToucher(21)
                      else if(nbAttaqueDone == 3) toucheValeur = DiceCalculator.jetToucher(16)

                      ctx.sendToDst(Array(MessageFactory.makeMessageRealisationAction(ACTION_ATTAQUER, ctx.srcAttr.name, ctx.srcId, toucheValeur, DiceCalculator._x_Dy_plus_z_(2, 6, 14))))
                    }
                  }

                  nbAttaqueDone = nbAttaqueDone + 1
                }
              }
            })
          }
        }

        // cet orc n'a qu'une seule attaque et pas de régénération
        else if(ctx.srcAttr.name == ORC_WORG_RIDER) {

          // si l'ennemie le plus proche est celui de cet arc du graphe
          if(ctx.srcAttr.msgsRetenu(0).cible == ctx.dstId) {
            ctx.sendToDst(Array(MessageFactory.makeMessageRealisationAction(ACTION_ATTAQUER, ctx.srcAttr.name, ctx.srcId, DiceCalculator.jetToucher(6), DiceCalculator._x_Dy_plus_z_(1, 8, 2))))
          }
        }

        // cet orc a 3 attaques et pas de régénération
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

                  ctx.sendToDst(Array(MessageFactory.makeMessageRealisationAction(ACTION_ATTAQUER, ctx.srcAttr.name, ctx.srcId, toucheValeur, DiceCalculator._x_Dy_plus_z_(1, 8, 10))))
                }

                nbAttaqueDone = nbAttaqueDone + 1
              }
            })
          }
        }

        // cet orc a 3 attaques et pas de régénération
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

                  ctx.sendToDst(Array(MessageFactory.makeMessageRealisationAction(ACTION_ATTAQUER, ctx.srcAttr.name, ctx.srcId, toucheValeur, DiceCalculator._x_Dy_plus_z_(1, 8, 10))))
                }

                nbAttaqueDone = nbAttaqueDone + 1
              }
            })
          }
        }
      }

      else if(plusHautePrioriteAction == ACTION_DEPLACEMENT) {

        ctx.srcAttr.msgsRetenu.foreach(msg => {

          if(msg != null && msg.action == ACTION_DEPLACEMENT && msg.cible == ctx.dstId) {

            val deplacement = ctx.srcAttr.moveToward(ctx.dstAttr)

            ctx.sendToSrc(Array(MessageFactory.makeMessageRealisationAction(ACTION_DEPLACEMENT, ctx.dstAttr.name, ctx.dstId, deplacement._1, deplacement._2, deplacement._3)))
          }
        })
      }

      else if(plusHautePrioriteAction == ACTION_HEAL) {

        ctx.srcAttr.msgsRetenu.foreach(msg => {

          if(msg != null && msg.action == ACTION_HEAL && msg.cible == ctx.dstId) {

            // test de qui on est pour la valeur du heal
            ctx.sendToDst(Array(MessageFactory.makeMessageRealisationAction(ACTION_HEAL, ctx.srcAttr.name, ctx.srcId, 0)))
          }
        })
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
          println("****************** Choix des Actions ******************")
          println("")
        }

        if(combattant.msgsRetenu != null) {
          println(combattant.name + " " + id + " : ")

          var nbAttaquesAffichees : Int = 0
          var nbAttaquesAfficheesMax : Int = 1

          if(combattant.name == ANGEL_SOLAR)          nbAttaquesAfficheesMax = 4
          if(combattant.name == ORC_BRUTAL_WARLORD)   nbAttaquesAfficheesMax = 3
          if(combattant.name == ORC_DOUBLE_AXE_FURY)  nbAttaquesAfficheesMax = 3
          if(combattant.name == ORC_WORG_RIDER)       nbAttaquesAfficheesMax = 1

          combattant.msgsRetenu.foreach(msg => {
            if(msg != null){

              if(msg.action == ACTION_ATTAQUER){
                if(nbAttaquesAffichees < nbAttaquesAfficheesMax){
                  nbAttaquesAffichees = nbAttaquesAffichees + 1
                  println("  - " + msg.action + ", cible : " + msg.cible)
                }
              }
              else {
                println("  - " + msg.action + ", cible : " + msg.cible)
              }
            }
          })
          println("")
        }
      }}

      println("")
      println("****************** Actions réalisées ******************")
      println("")

      val messagesRealisationActions = myGraph.aggregateMessages[Array[MessageRealisationAction]](
        sendMessagesRealisationAction,
        combineAction,
        TripletFields.All
      )

      myGraph = myGraph.joinVertices(messagesRealisationActions)(
        (id, combattant, msgs) => {

          val combattantResult = CombattantFactory.copyCombattant(combattant)

          msgs.foreach( msg => {

            if(msg.action == ACTION_ATTAQUER) {

              if(msg.valeur1 == DiceCalculator.CRITIQUE) {

                println("Attaque critique de " + msg.combattant + " " + msg.idCombattant + " contre " + combattantResult.name + " " + id + " : " + msg.valeur2 + " dégâts")
                combattantResult.pvActuel = combattantResult.pvActuel - msg.valeur2.asInstanceOf[Int]
                if(combattantResult.pvActuel < 0){
                  combattantResult.pvActuel = 0
                }
              }
              else if(msg.valeur1 >= combattantResult.AC) {

                println("Attaque réussie de " + msg.combattant + " " + msg.idCombattant + " contre " + combattantResult.name + " " + id + " : " + msg.valeur2 + " dégâts")
                combattantResult.pvActuel = combattantResult.pvActuel - msg.valeur2.asInstanceOf[Int]
                if(combattantResult.pvActuel < 0){
                  combattantResult.pvActuel = 0
                }
              } else {
                println("Attaque ratée de " + msg.combattant + " " + msg.idCombattant + " contre " + combattantResult.name + " " + id)
              }
            }

            else if(msg.action == ACTION_HEAL) {

              println("Heal de " + msg.combattant + " " + msg.idCombattant + " pour " + combattantResult.name + " " + id + " : soin de " + msg.valeur1)
              combattantResult.pvActuel = combattantResult.pvActuel + msg.valeur1.asInstanceOf[Int]
              if(combattantResult.pvActuel > combattantResult.pvMax){
                combattantResult.pvActuel = combattantResult.pvMax
              }
            }

            else if(msg.action == ACTION_DEPLACEMENT) {

              println("Déplacement de " + combattantResult.name + " " + id + " vers " + msg.combattant + " " + msg.idCombattant + " sur une distance de " + calculeNorme(msg.valeur1, msg.valeur2, msg.valeur3))
              combattantResult.positionX = combattantResult.positionX + msg.valeur1
              combattantResult.positionY = combattantResult.positionY + msg.valeur2
              combattantResult.positionZ = combattantResult.positionZ + msg.valeur3
            }

            else if(msg.action == ACTION_REGENERATION) {

              println("Régénération de " + combattantResult.name + " " + id + " : soin de " + msg.valeur1)
              combattantResult.pvActuel = combattantResult.pvActuel + msg.valeur1.asInstanceOf[Int]
              if(combattantResult.pvActuel > combattantResult.pvMax){
                combattantResult.pvActuel = combattantResult.pvMax
              }
            }
          })

          combattantResult
        })

      var affichageStatus = true

      myGraph.vertices.collect.foreach { case (id, combattant: Combattant) => {

        if(affichageStatus){
          affichageStatus = false
          println("")
          println("****************** Status ******************")
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
