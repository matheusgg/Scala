package br.com.gatling

import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation
import io.gatling.http.Predef._

import scala.concurrent.duration._

/**
 * http://gatling.io/docs/2.2.2/
 * http://gatling.io/docs/2.2.3/session/index.html
 * https://www.amazon.com/Learning-Scala-Practical-Functional-Programming/dp/1449367933/ref=sr_1_3?ie=UTF8&qid=1477871157&sr=8-3&keywords=scala
 */
class SimpleSimulation extends Simulation {

	before {
		println("Before simulation...")
	}

	/**
	 * Feeders sao datasources de dados que podem ser carregados e utilizados durante a execucao da
	 * simulacao
	 */
	var feeder = csv("randomData.csv").random

	val httpConfig = http
	  .baseURL("http://localhost:8082")
	  .acceptHeader("*/*")

	/**
	 * Com os grupos é possível definir comportamentos diferentes dentro de um mesmo cenario
	 */
	val scn = scenario("SimpleSimulation")
	  .feed(feeder)
	  .group("generateMessageGroup") {
		  exec(http("generateMessageReq").get("/generateMessage?raiseErr=${raiseErr}")).pause(1)
	  }
	  .group("generateMessageWithoutErrorsGroup") {
		  exec(http("generateMessageWithoutErrorsReq").get("/generateMessage"))
	  }

	setUp(
		//		scn.inject(atOnceUsers(50))
		scn.inject(rampUsers(18000) over (60 seconds)))
	  .protocols(httpConfig)
	  .assertions(
		  /**
		   * Assertions sao condicoes que devem ser satisfeitas para que a simulacao seja bem sucedida.
		   * Assertions sao definidas atraves de um escopo.estatistica.metrica.condicao:
		   * Ex.: global.responseTime.max.lessThan
		   */
		  global.responseTime.max.lessThan(20), // 20ms
		  forAll.successfulRequests.percent.is(100),

		  /**
		   * É possivel validar o resultado de requisicoes de grupos especificos
		   */
		  details("generateMessageWithoutErrorsGroup" / "generateMessageWithoutErrorsReq").responseTime.max.lessThan(10)
	  )

	after {
		println("After simulation...")
	}

}
