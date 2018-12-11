import org.apache.spark.graphx._
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

import scala.math.sqrt

object Exercice2combat2 extends App {

  val conf = new SparkConf().setAppName("Exercice2").setMaster("local[*]")
  val sc = new SparkContext(conf)
  sc.setLogLevel("ERROR")

  var atLeastOneFoeRelation : Boolean = false

  def exercice2combat2() : Unit = {
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
    val ACTION_SPELL = "spell"
    val ACTION_SPELL_UTILISE = "spell_utilise"
    val ACTION_DEPLACEMENT = "deplacement"
    val ACTION_REGENERATION = "regeneration"
    val ACTION_ENVOL = "envol"
    val ACTION_ATTERRI = "atterri"
    val ACTION_ALTERATION_ETAT_NB_TOUR_MOINS_1 = "alteration"

    var angels : Array[Combattant] = Array()

    var nbOrcGreatAxeDead : Int = 0

    def sendMessagesChoixAction(ctx: EdgeContext[Combattant, String, Array[MessageChoixAction]]): Unit = {

      if(ctx.srcAttr.pvActuel > 0 && ctx.dstAttr.pvActuel > 0){

        if(ctx.attr == FOE) atLeastOneFoeRelation = true

        if(ctx.attr == FOE && ctx.dstAttr.status != Status.DEGUISE && ctx.srcAttr.status != Status.STUNNED) {

          val distance = calculeDistance(ctx.srcAttr, ctx.dstAttr)

          val distanceAuSol = calculeDistanceAuSol(ctx.srcAttr, ctx.dstAttr)

          if(ctx.srcAttr.name == Combattant.ANGEL_SOLAR) {

            if(distance > 110.0f) {
              if(distanceAuSol > 10.0f){
                ctx.sendToSrc(Array(MessageFactory.makeMessageChoixAction(distance, ACTION_DEPLACEMENT, ctx.dstId)))
              }
            }

            else if(distance <= 10.0f){
              ctx.sendToSrc(Array(MessageFactory.makeMessageChoixAction(distance, ACTION_ATTAQUER, ctx.dstId, Attaque.GREAT_SWORD)))
            }

            else if(distance <= 110.0f){
              ctx.sendToSrc(Array(MessageFactory.makeMessageChoixAction(distance, ACTION_ATTAQUER, ctx.dstId, Attaque.LONG_BOW)))
            }
          }

          else if(ctx.srcAttr.name == Combattant.ANGEL_ASTRAL_DEVA || ctx.srcAttr.name == Combattant.ANGEL_MOVANIC_DEVA || ctx.srcAttr.name == Combattant.ANGEL_PLANETAR){

            if(distance > 10.0f) {
              if(distanceAuSol > 10.0f){
                ctx.sendToSrc(Array(MessageFactory.makeMessageChoixAction(distance, ACTION_DEPLACEMENT, ctx.dstId)))
              }
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

            if(ctx.srcAttr.status == Status.DEGUISE) {

              if (ctx.dstAttr.name == Combattant.ANGEL_SOLAR) {

                // si on n'a pas encore fait la full-attack
                if(isSpellDisponible(ctx.srcAttr, Spell.FULL_ATTACK)) {

                  // si au CàC, full-attack
                  if (distance <= 10.0f) {

                    ctx.sendToSrc(Array(MessageFactory.makeMessageChoixAction(distance, ACTION_SPELL, ctx.dstId, Spell.FULL_ATTACK)))
                  }

                  // sinon déplacement
                  else {
                    ctx.sendToSrc(Array(MessageFactory.makeMessageChoixAction(distance, ACTION_DEPLACEMENT, ctx.dstId)))
                  }
                }
              }
            }

            else {

              // si on est en vol
              if( ! estAuSol(ctx.srcAttr)) {

                // si l'ennemi a 150 PV ou moins, on le stun
                if(ctx.dstAttr.pvActuel <= 150 && ctx.dstAttr.status != Status.STUNNED){

                  ctx.sendToSrc(Array(MessageFactory.makeMessageChoixAction(distance, ACTION_SPELL, ctx.dstId, Spell.POWER_WORD_STUN)))
                }

                // sinon on souffle sur 3 angel random
                else {

                  val listeAngelCible : Array[Long] = find3AngelAlive()

                  if(listeAngelCible(0) != -1L) {
                    ctx.sendToSrc(Array(MessageFactory.makeMessageChoixAction(distance, ACTION_SPELL, listeAngelCible(0), listeAngelCible(1), listeAngelCible(2), Spell.BREATH_WEAPON)))
                  }
                }
              }

              // si on est revenu au sol
              else {

                // si au CàC, attaque
                if (distance <= 10.0f) {

                  ctx.sendToSrc(Array(MessageFactory.makeMessageChoixAction(distance, ACTION_ATTAQUER, ctx.dstId, Attaque.BITE)))
                }

                // sinon déplacement
                else {
                  ctx.sendToSrc(Array(MessageFactory.makeMessageChoixAction(distance, ACTION_DEPLACEMENT, ctx.dstId)))
                }
              }
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

        else if(ctx.attr == ALLY && ctx.srcAttr.status != Status.STUNNED) {

          val distance = calculeDistance(ctx.srcAttr, ctx.dstAttr)

          if(ctx.srcAttr.name == Combattant.ANGEL_SOLAR) {

            if(ctx.dstAttr.pvActuel < ctx.dstAttr.pvMax * 0.75) {

              if(isSpellDisponible(ctx.srcAttr, Spell.HEAL)) {
                ctx.sendToSrc(Array(MessageFactory.makeMessageChoixAction(distance, ACTION_SPELL, ctx.dstId, Spell.HEAL)))
              }
            }
          }
          else if(ctx.srcAttr.name == Combattant.RED_DRAGON && ctx.dstAttr.name == Combattant.ORC_ANGEL_SLAYER) {

            if(ctx.srcAttr.pvActuel < ctx.srcAttr.pvMax * 0.75) {

              if(isSpellDisponible(ctx.srcAttr, Spell.CURE_MODERATE_WOUNDS)) {
                ctx.sendToDst(Array(MessageFactory.makeMessageChoixAction(distance, ACTION_SPELL, ctx.srcId, Spell.CURE_MODERATE_WOUNDS)))
              }
            }
          }
        }

        else if(ctx.attr == MY_SELF) {

          if(ctx.srcAttr.name == Combattant.ANGEL_SOLAR) {

            ctx.sendToSrc(Array(MessageFactory.makeMessageChoixAction(0.0f, ACTION_REGENERATION, ctx.srcId)))
          }

          if(ctx.srcAttr.status == Status.STUNNED) {
            ctx.sendToSrc(Array(MessageFactory.makeMessageChoixAction(0.0f, ACTION_ALTERATION_ETAT_NB_TOUR_MOINS_1, ctx.srcId)))
          }

          else if(ctx.srcAttr.name == Combattant.ANGEL_SOLAR) {

            if(ctx.srcAttr.pvActuel < ctx.srcAttr.pvMax * 0.75) {

              if(isSpellDisponible(ctx.srcAttr, Spell.HEAL)) {
                ctx.sendToSrc(Array(MessageFactory.makeMessageChoixAction(0.0f, ACTION_SPELL, ctx.srcId, Spell.HEAL)))
              }
            }
          }

          else if(ctx.srcAttr.name == Combattant.RED_DRAGON) {

            // si le dragon est encore au sol alors qu'il n'est plus déguisé et qu'il a encore des alliés
            if(estAuSol(ctx.srcAttr) && ctx.srcAttr.status != Status.DEGUISE && nbOrcGreatAxeDead < 200) {

              // on s'envole (on ne fait rien d'autre à ce tour)
              ctx.sendToSrc(Array(MessageFactory.makeMessageChoixAction(0.0f, ACTION_ENVOL, ctx.srcId)))

            } else if( ! estAuSol(ctx.srcAttr) && nbOrcGreatAxeDead == 200) {
              
              // on atterri pour se battre au sol (on ne fait rien d'autre à ce tour)
              ctx.sendToSrc(Array(MessageFactory.makeMessageChoixAction(0.0f, ACTION_ATTERRI, ctx.srcId)))
            }
          }
        }
      }
    }

    def estAuSol(combattant : Combattant) : Boolean = {
      combattant.positionY == 0.0f
    }

    def isSpellDisponible(combattant : Combattant, nomSpell : String) : Boolean = {

      val spells : Array[Spell] = combattant.spells.filter(s => s.nom == nomSpell)

      if(spells == null || spells.length != 1) return false

      spells(0).disponible
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

      // si le plus important est de lancer un spell
      else if(plusHautePrioriteAction == ACTION_SPELL) {

        // on cherche le spell de plus haute priorité
        val plusHautePrioriteSpell = getPlusHautePrioriteSpell(msgsMerged)

        var msgsResult = Array[MessageChoixAction]()

        // on ne fera qu'un seul spell
        var spellChoisit : Boolean = false

        msgsMerged.foreach(msg => {

          // on stack toujours les régénération
          if(msg != null && msg.action == ACTION_REGENERATION) {
            msgsResult = msgsResult :+ msg
          }

          if(msg != null && msg.action == ACTION_SPELL && msg.extraInfo == plusHautePrioriteSpell && ! spellChoisit) {
            spellChoisit = true
            msgsResult = msgsResult :+ msg
          }
        })

        return msgsResult
      }

      // si le plus important est de s'envoler (pour le dragon)
      else if(plusHautePrioriteAction == ACTION_ENVOL) {

        var msgsResult = Array[MessageChoixAction]()

        // on ne s'envol qu'une seul fois
        var envolChoisit : Boolean = false

        msgsMerged.foreach(msg => {

          // on stack toujours les régénération
          if(msg != null && msg.action == ACTION_REGENERATION) {
            msgsResult = msgsResult :+ msg
          }

          else if(msg != null && msg.action == ACTION_ENVOL && ! envolChoisit) {
            envolChoisit = true
            msgsResult = msgsResult :+ msg
          }
        })

        return msgsResult
      }

      // si le plus important est d'atterir (pour le dragon)
      else if(plusHautePrioriteAction == ACTION_ATTERRI) {

        var msgsResult = Array[MessageChoixAction]()

        // on n'atterri qu'une seul fois
        var atterriChoisit : Boolean = false

        msgsMerged.foreach(msg => {

          // on stack toujours les régénération
          if(msg != null && msg.action == ACTION_REGENERATION) {
            msgsResult = msgsResult :+ msg
          }

          else if(msg != null && msg.action == ACTION_ATTERRI && ! atterriChoisit) {
            atterriChoisit = true
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

      // on traite tout les messages de régénération et de tour d'altération d'état passé
      ctx.srcAttr.msgsRetenu.foreach(msg => {

        if(msg != null && msg.action == ACTION_REGENERATION && msg.cible == ctx.dstId) {

          // seul le solar est capable de se régénérer
          if(ctx.srcAttr.name == Combattant.ANGEL_SOLAR) {
            ctx.sendToSrc(Array(MessageFactory.makeMessageRealisationAction(ACTION_REGENERATION, ctx.srcAttr.name, ctx.srcId, 15)))
          }
        }

        if(msg != null && msg.action == ACTION_ALTERATION_ETAT_NB_TOUR_MOINS_1 && msg.cible == ctx.dstId) {
          ctx.sendToSrc(Array(MessageFactory.makeMessageRealisationAction(ACTION_ALTERATION_ETAT_NB_TOUR_MOINS_1, ctx.srcAttr.name, ctx.srcId)))
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

      else if(plusHautePrioriteAction == ACTION_SPELL) {

        lancerSpell(ctx)
      }

      else if(plusHautePrioriteAction == ACTION_ENVOL) {

        ctx.srcAttr.msgsRetenu.foreach(msg => {

          if(msg != null && msg.action == ACTION_ENVOL && msg.cible == ctx.dstId) {

            ctx.sendToSrc(Array(MessageFactory.makeMessageRealisationAction(ACTION_ENVOL, ctx.srcAttr.name, ctx.srcId)))
          }
        })
      }

      else if(plusHautePrioriteAction == ACTION_ATTERRI) {

        ctx.srcAttr.msgsRetenu.foreach(msg => {

          if(msg != null && msg.action == ACTION_ATTERRI && msg.cible == ctx.dstId) {

            ctx.sendToSrc(Array(MessageFactory.makeMessageRealisationAction(ACTION_ATTERRI, ctx.srcAttr.name, ctx.srcId)))
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

    def lancerSpell(ctx: EdgeContext[Combattant, String, Array[MessageRealisationAction]]) : Unit = {

      ctx.srcAttr.msgsRetenu.foreach(msg => {

        if(msg != null &&
          msg.action == ACTION_SPELL &&
          (msg.cible == ctx.dstId || msg.extraInfo == Spell.BREATH_WEAPON && (msg.cible2 == ctx.dstId || msg.cible3 == ctx.dstId))) {

          val nomSpell : String = msg.extraInfo

          if(nomSpell == "") return

          val spell : Spell = ctx.srcAttr.spells.filter(a => a.nom == nomSpell)(0)

          if(spell.nom == Spell.CURE_MODERATE_WOUNDS) {

            ctx.sendToDst(Array(MessageFactory.makeMessageRealisationAction(ACTION_SPELL, ctx.srcAttr.name, ctx.srcId, DiceCalculator._x_Dy_plus_z_(spell.valeur1, spell.valeur2, spell.valeur3), spell.nom)))

          } else if(spell.nom == Spell.HEAL) {

            ctx.sendToDst(Array(MessageFactory.makeMessageRealisationAction(ACTION_SPELL, ctx.srcAttr.name, ctx.srcId, spell.valeur1, spell.nom)))

          } else if(spell.nom == Spell.MASS_HEAL) {

            ctx.sendToDst(Array(MessageFactory.makeMessageRealisationAction(ACTION_SPELL, ctx.srcAttr.name, ctx.srcId, spell.valeur1, spell.nom)))

          } else if(spell.nom == Spell.FULL_ATTACK) {

            ctx.srcAttr.attaques.foreach(attaque => {

              attaque.touches.foreach(toucheBonus => {

                val toucheValeur : Int = DiceCalculator.jetToucher(toucheBonus)
                val degatValeur : Int = DiceCalculator._x_Dy_plus_z_(attaque.nbDes, attaque.valeurDes, attaque.degatFixe)

                ctx.sendToDst(Array(MessageFactory.makeMessageRealisationAction(ACTION_ATTAQUER, ctx.srcAttr.name, ctx.srcId, toucheValeur, degatValeur, attaque.nom)))
              })
            })


          } else if(spell.nom == Spell.BREATH_WEAPON) {

            val degat : Int = DiceCalculator._x_Dy_plus_z_(spell.valeur1, spell.valeur2, 0)

            ctx.sendToDst(Array(MessageFactory.makeMessageRealisationAction(ACTION_SPELL, ctx.srcAttr.name, ctx.srcId, spell.DC, degat, spell.nom)))

          } else if(spell.nom == Spell.POWER_WORD_STUN) {

            var duree : Int = DiceCalculator._x_Dy_plus_z_(spell.valeur1, spell.valeur2, 0)

            if(ctx.dstAttr.pvActuel <= 50) {
              duree = DiceCalculator._x_Dy_plus_z_(spell.valeur1 + 3, spell.valeur2, 0)
            }

            else if(ctx.dstAttr.pvActuel <= 100) {
              duree = DiceCalculator._x_Dy_plus_z_(spell.valeur1 + 1, spell.valeur2, 0)
            }

            ctx.sendToDst(Array(MessageFactory.makeMessageRealisationAction(ACTION_SPELL, ctx.srcAttr.name, ctx.srcId, spell.DC, duree, spell.nom)))

          }

          // redondance du test pour éviter d'envoyer le message plusieurs fois lors du BREATH_WEAPON
          if(msg.cible == ctx.dstId){
            ctx.sendToSrc(Array(MessageFactory.makeMessageRealisationAction(ACTION_SPELL_UTILISE, ctx.srcAttr.name, ctx.srcId, spell.nom)))
          }
        }
      })
    }

    def find3AngelAlive() : Array[Long] = {

      var listAngelAlive : Array[Long] = Array()

      for( i <- 0 to 9) {
        if(angels(i).pvActuel > 0){
          listAngelAlive = listAngelAlive :+ i.asInstanceOf[Long]
        }
      }

      val nbAngelAlive = listAngelAlive.length

      if(nbAngelAlive == 0) Array(-1L, -1L, -1L)

      else if(nbAngelAlive == 1) Array(listAngelAlive(0), -1L, -1L)

      else if(nbAngelAlive == 2) Array(listAngelAlive(0), listAngelAlive(1), -1L)

      else if(nbAngelAlive == 3) Array(listAngelAlive(0), listAngelAlive(1), listAngelAlive(2))

      else {

        val angel1 : Int = RandomUtil.getRandomInt(nbAngelAlive)

        val angel2 : Int = (angel1 + RandomUtil.getRandomInt(nbAngelAlive - 1) + 1) % nbAngelAlive

        var angel3 : Int = angel1

        while(angel3 == angel1 || angel3 == angel2){
          angel3 = (angel2 + RandomUtil.getRandomInt(nbAngelAlive - 1) + 1) % nbAngelAlive
        }

        Array(listAngelAlive(angel1), listAngelAlive(angel2), listAngelAlive(angel3))
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

        else if(msg.action == ACTION_ENVOL) {
          plusHautePrioriteAction = ACTION_ENVOL
        }

        else if(plusHautePrioriteAction != ACTION_ENVOL &&
          msg.action == ACTION_ATTERRI) {

          plusHautePrioriteAction = ACTION_ATTERRI
        }

        else if(plusHautePrioriteAction != ACTION_ENVOL &&
          plusHautePrioriteAction != ACTION_ATTERRI &&
          msg.action == ACTION_SPELL) {

          plusHautePrioriteAction = ACTION_SPELL
        }

        else if(plusHautePrioriteAction != ACTION_ENVOL &&
          plusHautePrioriteAction != ACTION_ATTERRI &&
          plusHautePrioriteAction != ACTION_SPELL &&
          msg.action == ACTION_ATTAQUER) {

          plusHautePrioriteAction = ACTION_ATTAQUER
        }

        else if(plusHautePrioriteAction != ACTION_ENVOL &&
          plusHautePrioriteAction != ACTION_ATTERRI &&
          plusHautePrioriteAction != ACTION_SPELL &&
          plusHautePrioriteAction != ACTION_ATTAQUER &&
          msg.action == ACTION_DEPLACEMENT) {

          plusHautePrioriteAction = ACTION_DEPLACEMENT
        }
      })

      plusHautePrioriteAction
    }

    def getPlusHautePrioriteSpell(msgs : Array[MessageChoixAction]) : String = {

      var plusHautePrioriteSpell : String = Spell.CURE_MODERATE_WOUNDS

      // pour tout les message qu'on a à merge
      msgs.foreach(msg => {

        if(msg == null || msg.action != ACTION_SPELL){
          // nothing to do
        }

        else if(msg.extraInfo == Spell.MASS_HEAL) {
          plusHautePrioriteSpell = Spell.MASS_HEAL
        }

        else if(plusHautePrioriteSpell != Spell.MASS_HEAL &&
          msg.extraInfo == Spell.HEAL) {

          plusHautePrioriteSpell = Spell.HEAL
        }

        else if(plusHautePrioriteSpell != Spell.MASS_HEAL &&
          msg.extraInfo != Spell.HEAL &&
          msg.extraInfo == Spell.POWER_WORD_STUN) {

          plusHautePrioriteSpell = Spell.POWER_WORD_STUN
        }

        else if(plusHautePrioriteSpell != Spell.MASS_HEAL &&
          msg.extraInfo != Spell.HEAL &&
          msg.extraInfo != Spell.POWER_WORD_STUN &&
          msg.extraInfo == Spell.BREATH_WEAPON) {

          plusHautePrioriteSpell = Spell.BREATH_WEAPON
        }

        else if(plusHautePrioriteSpell != Spell.MASS_HEAL &&
          msg.extraInfo != Spell.HEAL &&
          msg.extraInfo != Spell.POWER_WORD_STUN &&
          msg.extraInfo != Spell.BREATH_WEAPON &&
          msg.extraInfo == Spell.FULL_ATTACK) {

          plusHautePrioriteSpell = Spell.FULL_ATTACK
        }
      })

      plusHautePrioriteSpell
    }

    def calculeDistance(combattant1: Combattant, combattant2: Combattant) : Float = {

      calculeNorme(combattant1.positionX - combattant2.positionX, combattant1.positionY - combattant2.positionY, combattant1.positionZ - combattant2.positionZ)
    }

    def calculeDistanceAuSol(combattant1: Combattant, combattant2: Combattant) : Float = {

      calculeNorme(combattant1.positionX - combattant2.positionX, 0.0f, combattant1.positionZ - combattant2.positionZ)
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

      angels = Array()

      var localNbOrcGreatAxeDead : Int = 0

      graphToDisplay.vertices.collect.foreach { case (id, combattant: Combattant) => {

        if(id < 10L) {
          angels = angels :+ combattant
        }

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

          if(combattant.name != Combattant.ORC_GREAT_AXE) {
            if(combattant.status == Status.STUNNED){
              println("  - " + Console.BLUE + Console.BOLD + combattant.name + " " + id + Console.WHITE + " : " + Console.GREEN + Console.BOLD + combattant.pvActuel + "/" + combattant.pvMax + " PV" + Console.WHITE + ", " + Console.MAGENTA + Console.BOLD + "STUNNED " + Console.RED + combattant.nbTourStatus + Console.WHITE + " tour(s), position : (" + combattant.positionX + ", " + combattant.positionY + ", " + combattant.positionZ + ")")
            } else {
              println("  - " + Console.BLUE + Console.BOLD + combattant.name + " " + id + Console.WHITE + " : " + Console.GREEN + Console.BOLD + combattant.pvActuel + "/" + combattant.pvMax + " PV" + Console.WHITE + ", position : (" + combattant.positionX + ", " + combattant.positionY + ", " + combattant.positionZ + ")")
            }
          }

        }

        else if(combattant.name == Combattant.ORC_GREAT_AXE) {
          localNbOrcGreatAxeDead = localNbOrcGreatAxeDead + 1
        }
      }
      }

      if(localNbOrcGreatAxeDead < 200) {
        println("  - " + (200 - localNbOrcGreatAxeDead) + " " + Console.BLUE + Console.BOLD + Combattant.ORC_GREAT_AXE + Console.WHITE)
      }

      var affichageMort = true

      graphToDisplay.vertices.collect.foreach { case (id, combattant: Combattant) => {

        if (combattant.pvActuel <= 0) {

          if (affichageMort) {
            affichageMort = false
            println("")
            println("Mort :")
          }

          if(combattant.name != Combattant.ORC_GREAT_AXE){
            println("  - " + Console.RED + Console.BOLD + combattant.name + " " + id + Console.WHITE)
          }
        }
      }
      }

      if(localNbOrcGreatAxeDead > 0) {
        println("  - " + localNbOrcGreatAxeDead + " " + Console.RED + Console.BOLD + Combattant.ORC_GREAT_AXE + Console.WHITE)
      }

      nbOrcGreatAxeDead = localNbOrcGreatAxeDead
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

      myGraph = myGraph.joinVertices(messagesChoixActions)(
        (id, combattant, msgsRetenu) => {

          val combattantResult = CombattantFactory.copyCombattantWithMsg(combattant, msgsRetenu)

          combattantResult
        })

/*
      var affichageChoixActions = true

      myGraph.vertices.collect.foreach { case (id, combattant: Combattant) => {

        if(affichageChoixActions){
          affichageChoixActions = false
          println("")
          println("****************** Choix des Actions ******************")
          println("")
        }

        if(combattant.msgsRetenu != null && combattant.name == Combattant.RED_DRAGON) {
          println(combattant.name + " " + id + " : ")

          var nbAttaquesAffichees : Int = 0
          var nbAttaquesAfficheesMax : Int = 1

          if(combattant.name == Combattant.ANGEL_SOLAR)         nbAttaquesAfficheesMax = 4
          if(combattant.name == Combattant.ANGEL_PLANETAR)      nbAttaquesAfficheesMax = 3
          if(combattant.name == Combattant.ANGEL_MOVANIC_DEVA)  nbAttaquesAfficheesMax = 3
          if(combattant.name == Combattant.ANGEL_ASTRAL_DEVA)   nbAttaquesAfficheesMax = 3
          if(combattant.name == Combattant.RED_DRAGON)          nbAttaquesAfficheesMax = 6
          if(combattant.name == Combattant.ORC_GREAT_AXE)       nbAttaquesAfficheesMax = 1
          if(combattant.name == Combattant.ORC_ANGEL_SLAYER)    nbAttaquesAfficheesMax = 6


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
*/
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
                //println("Attaque ratée de " + nameCombattantMsg + " avec " + arme + " contre " + nameCombattantResult)
              }
            }

            else if(msg.action == ACTION_SPELL) {

              val nomSpell : String = Console.CYAN + Console.BOLD + msg.extraInfo + Console.WHITE

              if(msg.extraInfo == Spell.HEAL || msg.extraInfo == Spell.CURE_MODERATE_WOUNDS || msg.extraInfo == Spell.MASS_HEAL) {

                // si on est déjà mort quand le heal arrive, dommage ...
                if(combattantResult.pvActuel <= 0) {
                  println(nameCombattantResult + " est mort avant l'arrivé du " + nomSpell + " de " + nameCombattantMsg + "...")
                } else {
                  println(nomSpell + " de " + nameCombattantMsg + " pour " + nameCombattantResult + " : " + Console.GREEN + Console.BOLD + "soin de " + msg.valeur1 + Console.WHITE)
                  combattantResult.pvActuel = combattantResult.pvActuel + msg.valeur1.asInstanceOf[Int]
                  if(combattantResult.pvActuel > combattantResult.pvMax){
                    combattantResult.pvActuel = combattantResult.pvMax
                  }

                  if(combattantResult.status == Status.STUNNED && msg.extraInfo != Spell.CURE_MODERATE_WOUNDS) {
                    combattantResult.status = Status.VIVANT
                    combattantResult.nbTourStatus = 0
                    println("Grace au " + nomSpell + " de " + nameCombattantMsg + ", " + nameCombattantResult + " n'est plus stunned !")
                  }
                }
              }

              else if(msg.extraInfo == Spell.POWER_WORD_STUN) {

                val willSave : Int = DiceCalculator._x_Dy_plus_z_(1, 20, combattantResult.Will)

                if(willSave >= msg.valeur1) {
                  println(nameCombattantResult + " réussit un will et évite " + nomSpell + " de " + nameCombattantMsg)
                }

                else {

                  val nbTour : Int = msg.valeur2.asInstanceOf[Int]
                  println(nameCombattantResult + " recoit " + nomSpell + " de " + nameCombattantMsg + " et est stunned pendant " + Console.RED + Console.BOLD + nbTour + " tour(s)" + Console.WHITE)

                  combattantResult.status = Status.STUNNED
                  combattantResult.nbTourStatus = nbTour
                }
              }

              else if(msg.extraInfo == Spell.BREATH_WEAPON) {

                val reflexeSave : Int = DiceCalculator._x_Dy_plus_z_(1, 20, combattantResult.Reflex)

                if(reflexeSave >= msg.valeur1) {
                  println(nameCombattantResult + " réussit un réflexe et évite " + nomSpell + " de " + nameCombattantMsg)
                }

                else {

                  val degat = Math.max(0, msg.valeur2.asInstanceOf[Int] - combattantResult.SR)

                  println(nameCombattantResult + " recoit " + nomSpell + " de " + nameCombattantMsg + " et subit " + Console.RED + Console.BOLD + degat + " dégats" + Console.WHITE)

                  combattantResult.pvActuel = combattantResult.pvActuel - degat
                  if(combattantResult.pvActuel < 0){
                    combattantResult.pvActuel = 0
                  }
                }
              }
            }

            else if(msg.action == ACTION_SPELL_UTILISE) {

              for(i <- 0 to combattantResult.spells.length - 1) {

                if(combattantResult.spells(i).nom == msg.extraInfo && combattantResult.spells(i).disponible) {

                  if(combattantResult.spells(i).utilisationUnique) {
                    println(nameCombattantMsg + " utilise son spell " + Console.CYAN + Console.BOLD + msg.extraInfo + Console.WHITE + ", il ne peut plus l'utiliser.")
                    combattantResult.spells(i).disponible = false
                  } else {
                    println(nameCombattantMsg + " utilise son spell " + Console.CYAN + Console.BOLD + msg.extraInfo + Console.WHITE)
                  }

                  if(msg.extraInfo == Spell.FULL_ATTACK) {
                    combattantResult.status = Status.VIVANT
                  }
                }
              }
            }

            else if(msg.action == ACTION_DEPLACEMENT) {

              if(combattantResult.name != Combattant.ORC_GREAT_AXE)
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

            else if(msg.action == ACTION_ENVOL) {

              println(Console.MAGENTA + Console.BOLD + "Envol" + Console.WHITE + " de " + nameCombattantResult)
              combattantResult.positionY = combattantResult.positionY + 50.0f
            }

            else if(msg.action == ACTION_ATTERRI) {

              println(Console.MAGENTA + Console.BOLD + "Atterrissage" + Console.WHITE + " de " + nameCombattantResult)
              combattantResult.positionY = 0.0f
            }

            else if(msg.action == ACTION_ALTERATION_ETAT_NB_TOUR_MOINS_1 && combattantResult.status == Status.STUNNED) {

              combattantResult.nbTourStatus = combattantResult.nbTourStatus - 1

              if(combattantResult.nbTourStatus == 0){
                combattantResult.status = Status.VIVANT
                println(nameCombattantResult + " n'est plus stunned !")
              } else {
                println(nameCombattantResult + " est encore stunned pour " + Console.RED + Console.BOLD + combattantResult.nbTourStatus + Console.WHITE + " tour(s)")
              }
            }
          })

          combattantResult
        })

      afficherStatus(myGraph)

      tourCombat = tourCombat + 1
    }
  }

  exercice2combat2()
}
