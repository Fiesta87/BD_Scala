import Exercice2combat1.{atLeastOneFoeRelation, exercice2combat1, sc}
import org.apache.spark.graphx.{Edge, EdgeContext, Graph, TripletFields}
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

import scala.math.sqrt

object Exercice2combat2 extends App {

  val conf = new SparkConf().setAppName("Exercice2").setMaster("local[*]")
  val sc = new SparkContext(conf)
  sc.setLogLevel("ERROR")

  def creategraphe() : Unit = {
    println("Populating armies")

    var vertexArray = Array((0L, CombattantFactory.makeAngelSolar()))

    val ALLY = "ALLY"
    val FOE = "FOE"
    val MY_SELF = "MY_SELF"
    var edgeArray = Array[Edge[String]]()

    edgeArray = edgeArray :+ Edge(0L, 0L, MY_SELF)

    for (i <- 1l to 2L) {
      vertexArray = vertexArray :+ (i, CombattantFactory.makeAngelPlanetar())
      edgeArray = edgeArray :+ Edge(0L, i, ALLY)
      edgeArray = edgeArray :+ Edge(i, 0L, ALLY)
      edgeArray = edgeArray :+ Edge(i, i, MY_SELF)
    }

    for (i <- 3l to 4L) {
      vertexArray = vertexArray :+ (i, CombattantFactory.makeAngelMovanicDeva())
      for (y <- 0L to 2L) {
        edgeArray = edgeArray :+ Edge(y, i, ALLY)
        edgeArray = edgeArray :+ Edge(i, y, ALLY)
      }
      edgeArray = edgeArray :+ Edge(i, i, MY_SELF)
    }

    for (i <- 5l to 9L) {
      vertexArray = vertexArray :+ (i, CombattantFactory.makeAngelAstralDeva())
      for (y <- 0L to 4L) {
        edgeArray = edgeArray :+ Edge(y, i, ALLY)
        edgeArray = edgeArray :+ Edge(i, y, ALLY)
      }
      edgeArray = edgeArray :+ Edge(i, i, MY_SELF)
    }


    vertexArray = vertexArray :+ (10L, CombattantFactory.makeRedDragon())
    edgeArray = edgeArray :+ Edge(10L, 10L, MY_SELF)
    for (i <- 0L to 9L) {
      edgeArray = edgeArray :+ Edge(10L, i, FOE)
      edgeArray = edgeArray :+ Edge(i, 10L, FOE)
    }

    for (i <- 11L to 210L) {
      vertexArray = vertexArray :+ (i, CombattantFactory.makeOrcGreatAxe())
      edgeArray = edgeArray :+ Edge(10L, i, ALLY)
      edgeArray = edgeArray :+ Edge(i, 10L, ALLY)
      for (y <- 0L to 9L) {
        edgeArray = edgeArray :+ Edge(y, i, FOE)
        edgeArray = edgeArray :+ Edge(i, y, FOE)
      }
      edgeArray = edgeArray :+ Edge(i, i, MY_SELF)
    }

    for (i <- 211L to 220L) {
      vertexArray = vertexArray :+ (i, CombattantFactory.makeOrcAngelSlayer())
      edgeArray = edgeArray :+ Edge(10L, i, ALLY)
      edgeArray = edgeArray :+ Edge(i, 10L, ALLY)
      for (y <- 0L to 9L) {
        edgeArray = edgeArray :+ Edge(y, i, FOE)
        edgeArray = edgeArray :+ Edge(i, y, FOE)
      }
      edgeArray = edgeArray :+ Edge(i, i, MY_SELF)
    }

    val vertexRDD: RDD[(Long, Combattant)] = sc.parallelize(vertexArray)
    val edgeRDD: RDD[Edge[String]] = sc.parallelize(edgeArray)
    var myGraph: Graph[Combattant, String] = Graph(vertexRDD, edgeRDD)

    val ACTION_ATTAQUER = "attaquer"
    val ACTION_HEAL = "heal"
    val ACTION_DEPLACEMENT = "deplacement"
    val ACTION_REGENERATION = "regeneration"

    def sendMessagesChoixAction(ctx: EdgeContext[Combattant, String, Array[MessageChoixAction]]): Unit = {

      if(ctx.srcAttr.pvActuel > 0 && ctx.dstAttr.pvActuel > 0){

        if(ctx.attr == FOE) {

          atLeastOneFoeRelation = true

          val distance = calculeDistance(ctx.srcAttr, ctx.dstAttr)

          if(ctx.srcAttr.name == Combattant.ANGEL_SOLAR) {

            if(distance > 110) {
              ctx.sendToSrc(Array(MessageFactory.makeMessageChoixAction(distance, ACTION_DEPLACEMENT, ctx.dstId)))
            }

            else if(distance <= 10){
              ctx.sendToSrc(Array(MessageFactory.makeMessageChoixAction(distance, ACTION_ATTAQUER, ctx.dstId, Attaque.GREAT_SWORD)))
            }

            else if(distance <= 110){
              ctx.sendToSrc(Array(MessageFactory.makeMessageChoixAction(distance, ACTION_ATTAQUER, ctx.dstId, Attaque.LONG_BOW)))
            }
          }

          else if(ctx.srcAttr.name == Combattant.ANGEL_ASTRAL_DEVA || ctx.srcAttr.name == Combattant.ANGEL_MOVANIC_DEVA || ctx.srcAttr.name == Combattant.ANGEL_PLANETAR){

            if(distance > 10) {
              ctx.sendToSrc(Array(MessageFactory.makeMessageChoixAction(distance, ACTION_DEPLACEMENT, ctx.dstId)))
            }

            else{

              if(ctx.srcAttr.name == Combattant.ANGEL_MOVANIC_DEVA || ctx.srcAttr.name == Combattant.ANGEL_PLANETAR){
                ctx.sendToSrc(Array(MessageFactory.makeMessageChoixAction(distance, ACTION_ATTAQUER, ctx.dstId, Attaque.GREAT_SWORD)))
              }

              else if(ctx.srcAttr.name == Combattant.ANGEL_ASTRAL_DEVA){
                ctx.sendToSrc(Array(MessageFactory.makeMessageChoixAction(distance, ACTION_ATTAQUER, ctx.dstId, Attaque.WARHAMMER)))
              }

            }

          }

          else if(ctx.srcAttr.name == Combattant.RED_DRAGON){

            if(ctx.srcAttr.pvActuel < 150){
              ctx.sendToSrc(Array(MessageFactory.makeMessageChoixAction(distance, ACTION_ATTAQUER, ctx.dstId, Spell.POWER_WORD_STUN)))
            }else if(distance <= 10){
              ctx.sendToSrc(Array(MessageFactory.makeMessageChoixAction(distance, ACTION_ATTAQUER, ctx.dstId, Attaque.CLAW)))
            }

          }

          else if(ctx.srcAttr.name == Combattant.ORC_ANGEL_SLAYER){

            if(distance > 110) {
              ctx.sendToSrc(Array(MessageFactory.makeMessageChoixAction(distance, ACTION_DEPLACEMENT, ctx.dstId)))
            }

            else if(distance <= 10){
              ctx.sendToSrc(Array(MessageFactory.makeMessageChoixAction(distance, ACTION_ATTAQUER, ctx.dstId, Attaque.DOUBLE_AXE)))
            }

            else if(distance <= 110){
              ctx.sendToSrc(Array(MessageFactory.makeMessageChoixAction(distance, ACTION_ATTAQUER, ctx.dstId, Attaque.COMPOSITE_LONG_BOW)))
            }
          }

          else {

            if(distance > 10) {
              ctx.sendToSrc(Array(MessageFactory.makeMessageChoixAction(distance, ACTION_DEPLACEMENT, ctx.dstId)))
            }

            else {
              ctx.sendToSrc(Array(MessageFactory.makeMessageChoixAction(distance, ACTION_ATTAQUER, ctx.dstId, Attaque.GREATAXE)))
            }

          }

        }

        else if(ctx.attr == ALLY) {

          val distance = calculeDistance(ctx.srcAttr, ctx.dstAttr)

          if(ctx.srcAttr.name == Combattant.ANGEL_SOLAR) {

            if(ctx.dstAttr.pvActuel < ctx.dstAttr.pvMax * 0.75)
              {
                ctx.sendToSrc(Array(MessageFactory.makeMessageChoixAction(distance, ACTION_HEAL, ctx.dstId, Spell.HEAL)))
              }
          }
          else if(ctx.dstAttr.name == Combattant.ORC_ANGEL_SLAYER && ctx.srcAttr.name == Combattant.RED_DRAGON)
            {
              if(ctx.srcAttr.pvActuel < ctx.srcAttr.pvMax * 0.75)
                {
                  ctx.sendToDst(Array(MessageFactory.makeMessageChoixAction(distance, ACTION_HEAL, ctx.srcId, Spell.CURE_MODERATE_WOUNDS)))
                }

            }

        }

        else if(ctx.attr == MY_SELF) {

          if(ctx.srcAttr.name == Combattant.ANGEL_SOLAR) {

            if(ctx.srcAttr.pvActuel < ctx.srcAttr.pvMax * 0.75)
              {
                ctx.sendToSrc(Array(MessageFactory.makeMessageChoixAction(0.0f, ACTION_HEAL, ctx.srcId, Spell.HEAL)))
              }

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
          if(msg != null && msg.action == ACTION_REGENERATION) {
            msgsResult = msgsResult :+ msg
          }

          if(msg != null && msg.action == ACTION_HEAL && msg.distance == closestDistance && ! healChoisit) {
            healChoisit = true
            msgsResult = msgsResult :+ msg
          }
        })

        return msgsResult
      }

      // retour par défaut qui ne sera jamais exécuté mais permet de compiler
      msgsMerged
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
          if(ctx.srcAttr.name == Combattant.ANGEL_SOLAR) {
            ctx.sendToSrc(Array(MessageFactory.makeMessageRealisationAction(ACTION_REGENERATION, ctx.srcAttr.name, ctx.srcId, 15)))
          }
        }
      })

      // si on a retenu une ou plusieurs attaques
      if(plusHautePrioriteAction == ACTION_ATTAQUER){

        attaquerEnnemie(ctx)
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
            var nomHeal : String = msg.extraInfo

            if(nomHeal == "") return

            val heal : Spell = ctx.srcAttr.spells.filter(a => a.nom == nomHeal)(0)
            if(heal.valeur2 == 0 && heal.valeur3 == 0)
              {
                ctx.sendToDst(Array(MessageFactory.makeMessageRealisationAction(ACTION_HEAL, ctx.srcAttr.name, ctx.srcId, heal.valeur1)))
              }
            else
              {
                ctx.sendToDst(Array(MessageFactory.makeMessageRealisationAction(ACTION_HEAL, ctx.srcAttr.name, ctx.srcId,DiceCalculator._x_Dy_plus_z_(heal.valeur1, heal.valeur2, heal.valeur3) )))
              }
          }
        })
      }
    }

    def attaquerEnnemie(ctx: EdgeContext[Combattant, String, Array[MessageRealisationAction]]) : Unit = {

      var nomAttaque : String = ""

      ctx.srcAttr.msgsRetenu.foreach(msg => if(msg != null && msg.action == ACTION_ATTAQUER && msg.cible == ctx.dstId) nomAttaque = msg.extraInfo)

      if(nomAttaque == "") return

      val attaque : Attaque = ctx.srcAttr.attaques.filter(a => a.nom == nomAttaque)(0)

      // max X attaques
      val nbAttaqueMax : Int = attaque.touches.length
      var nbAttaqueDone : Int = 0

      // tant qu'on a pas fait les X attaques
      while(nbAttaqueDone < nbAttaqueMax){

        // pour chaque ennemi attaquable
        ctx.srcAttr.msgsRetenu.foreach(msg => {

          // si le message est bien une attaque et pas une régénération
          if(msg != null && msg.action == ACTION_ATTAQUER){

            // si on a pas dépassé le nombre d'attaque max
            if(nbAttaqueDone < nbAttaqueMax){

              // et que la l'ennemie traité est celui de cet arc du graphe
              if(msg.cible == ctx.dstId) {

                // on attaque avec une diminution de notre bonus pour toucher au fur et à mesure des attaques

                val toucheValeur : Int = DiceCalculator.jetToucher(attaque.touches(nbAttaqueDone))
                val degatValeur : Int = DiceCalculator._x_Dy_plus_z_(attaque.nbDes, attaque.valeurDes, attaque.degatFixe)

                ctx.sendToDst(Array(MessageFactory.makeMessageRealisationAction(ACTION_ATTAQUER, ctx.srcAttr.name, ctx.srcId, toucheValeur, degatValeur, attaque.nom)))
              }

              nbAttaqueDone = nbAttaqueDone + 1
            }
          }
        })
      }
    }

    def combineAction(msg1: Array[MessageRealisationAction], msg2: Array[MessageRealisationAction]): Array[MessageRealisationAction] = {

      msg1 ++ msg2
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


    def afficherStatus(graphToDisplay: Graph[Combattant, String]): Unit = {

      var affichageStatus = true

      var affichageEnVie = true

      graphToDisplay.vertices.collect.foreach { case (id, combattant: Combattant) => {

        if (affichageStatus) {
          affichageStatus = false
          println("")
          println("****************** Status ******************")
        }

        if (combattant.pvActuel > 0) {

          if (affichageEnVie) {
            affichageEnVie = false
            println("")
            println("En vie :")
          }

          println("  - " + Console.BLUE + Console.BOLD + combattant.name + " " + id + Console.WHITE + " : " + Console.GREEN + Console.BOLD + combattant.pvActuel + "/" + combattant.pvMax + " PV" + Console.WHITE + ", position : (" + combattant.positionX + ", " + combattant.positionY + ", " + combattant.positionZ + ")")
        }
      }
      }

      var affichageMort = true

      graphToDisplay.vertices.collect.foreach { case (id, combattant: Combattant) => {

        if (combattant.pvActuel <= 0) {

          if (affichageMort) {
            affichageMort = false
            println("")
            println("Mort :")
          }

          println("  - " + Console.RED + Console.BOLD + combattant.name + " " + id + Console.WHITE)
        }
      }
      }
    }

    afficherStatus(myGraph)

    println("")
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

          if(combattant.name == Combattant.ANGEL_SOLAR)          nbAttaquesAfficheesMax = 4
          if(combattant.name == Combattant.ANGEL_PLANETAR)          nbAttaquesAfficheesMax = 3
          if(combattant.name == Combattant.ANGEL_MOVANIC_DEVA)          nbAttaquesAfficheesMax = 3
          if(combattant.name == Combattant.ANGEL_ASTRAL_DEVA)          nbAttaquesAfficheesMax = 3
          if(combattant.name == Combattant.RED_DRAGON)          nbAttaquesAfficheesMax = 6
          if(combattant.name == Combattant.ORC_GREAT_AXE)          nbAttaquesAfficheesMax = 1
          if(combattant.name == Combattant.ORC_ANGEL_SLAYER)          nbAttaquesAfficheesMax = 6


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

          val nameCombattantResult : String = Console.BLUE + Console.BOLD + combattantResult.name + " " + id + Console.WHITE

          msgs.foreach( msg => {

            val nameCombattantMsg : String = Console.BLUE + Console.BOLD + msg.combattant + " " + msg.idCombattant + Console.WHITE

            if(msg.action == ACTION_ATTAQUER) {

              val arme : String = Console.YELLOW + Console.BOLD + msg.extraInfo + Console.WHITE

              if(msg.valeur1 == DiceCalculator.CRITIQUE) {
                val degat = Math.max(0, msg.valeur2.asInstanceOf[Int] - combattantResult.DR)
                println("Attaque critique de " + nameCombattantMsg + " avec " + arme +  " contre " + nameCombattantResult + " : " + Console.RED + Console.BOLD + degat + " dégâts" + Console.WHITE)
                combattantResult.pvActuel = combattantResult.pvActuel - degat
                if(combattantResult.pvActuel < 0){
                  combattantResult.pvActuel = 0
                }
              }
              else if(msg.valeur1 >= combattantResult.AC) {
                val degat = Math.max(0, msg.valeur2.asInstanceOf[Int] - combattantResult.DR)
                println("Attaque réussie de " + nameCombattantMsg + " avec " + arme + " contre " + nameCombattantResult + " : " + Console.RED + Console.BOLD + degat + " dégâts" + Console.WHITE)
                combattantResult.pvActuel = combattantResult.pvActuel - degat
                if(combattantResult.pvActuel < 0){
                  combattantResult.pvActuel = 0
                }
              } else {
                println("Attaque ratée de " + nameCombattantMsg + " avec " + arme + " contre " + nameCombattantResult)
              }
            }

            else if(msg.action == ACTION_HEAL) {

              println("Heal de " + nameCombattantMsg + " pour " + nameCombattantResult + " : " + Console.GREEN + Console.BOLD + "soin de " + msg.valeur1 + Console.WHITE)
              combattantResult.pvActuel = combattantResult.pvActuel + msg.valeur1.asInstanceOf[Int]
              if(combattantResult.pvActuel > combattantResult.pvMax){
                combattantResult.pvActuel = combattantResult.pvMax
              }
            }

            else if(msg.action == ACTION_DEPLACEMENT) {

              println("Déplacement de " + nameCombattantResult + " vers " + nameCombattantMsg + " sur une distance de " + calculeNorme(msg.valeur1, msg.valeur2, msg.valeur3))
              combattantResult.positionX = combattantResult.positionX + msg.valeur1
              combattantResult.positionY = combattantResult.positionY + msg.valeur2
              combattantResult.positionZ = combattantResult.positionZ + msg.valeur3
            }

            else if(msg.action == ACTION_REGENERATION) {

              println("Régénération de " + nameCombattantResult + " : " + Console.GREEN + Console.BOLD + "soin de " + msg.valeur1 + Console.WHITE)
              combattantResult.pvActuel = combattantResult.pvActuel + msg.valeur1.asInstanceOf[Int]
              if(combattantResult.pvActuel > combattantResult.pvMax){
                combattantResult.pvActuel = combattantResult.pvMax
              }
            }
          })

          combattantResult
        })

      afficherStatus(myGraph)

      tourCombat = tourCombat + 1
    }
  }

  creategraphe()
}
