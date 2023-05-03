import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.scala.{ClassTagExtensions, DefaultScalaModule}
import zio._
import zio.Console.printLine

object Main extends ZIOAppDefault {

  val mapper = JsonMapper
    .builder()
    .addModule(DefaultScalaModule)
    .build() :: ClassTagExtensions



  val mapper2 = new JsonMapper() with ClassTagExtensions

  val lala: Either[String, Int] = Right(42)
  println("---")
  val a = mapper.writeValueAsString(lala)
  println(a)
  val b = mapper.readValue[Either[String, Int]](a)
  println(b)
  println("---")

  override def run: ZIO[Environment with ZIOAppArgs with Scope, Any, Any] =
    printLine("Welcome to your first ZIO app!")
}