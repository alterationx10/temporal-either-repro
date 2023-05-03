import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import io.temporal.activity.{ActivityInterface, ActivityMethod}
import io.temporal.common.converter._
import io.temporal.workflow.{WorkflowInterface, WorkflowMethod}
import zio._
import zio.temporal.ZRetryOptions
import zio.temporal.activity.{ZActivity, ZActivityOptions}
import zio.temporal.testkit.{ZTestEnvironmentOptions, ZTestWorkflowEnvironment}
import zio.temporal.workflow._
import zio.test._

import scala.reflect.ClassTag

@WorkflowInterface
trait TestWorkflow {

  @WorkflowMethod
  def start: Either[String, Int]
}

@ActivityInterface
trait TestActivity {
  @ActivityMethod
  def either: Either[String, Int]

}

case class TestActivityImpl()(implicit options: ZActivityOptions[Any])
    extends TestActivity {
  override def either: Either[String, Int] =
    ZActivity.run {
      ZIO.succeed(Right(42))
    }

}

case class TestWorkflowImpl() extends TestWorkflow {
  override def start: Either[String, Int] = {
    val stub = ZWorkflow
      .newActivityStub[TestActivity]
      .withStartToCloseTimeout(1.seconds)
      .withRetryOptions(ZRetryOptions.default.withMaximumAttempts(1))
      .build
    stub.either
  }
}

object TemporalEitherSpec extends ZIOSpecDefault {

  val dataConverter = {
    val mapper: ObjectMapper = {
      val _mapper = new ObjectMapper()
      _mapper.registerModule(DefaultScalaModule)
      _mapper.registerModule(new JavaTimeModule)
      _mapper.findAndRegisterModules()
      _mapper.configure(
        DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
        false,
      )

      _mapper
    }

    val scalaJacksonJsonPayloadConverter: JacksonJsonPayloadConverter =
      new JacksonJsonPayloadConverter(
        mapper,
      )

    val dataConverter: DefaultDataConverter = DefaultDataConverter
      .newDefaultInstance()
      .withPayloadConverterOverrides(scalaJacksonJsonPayloadConverter)
    dataConverter
  }

  val dataConverter2 = {
    val mapper = JsonMapper.builder().addModule(DefaultScalaModule).build()

    val scalaJacksonJsonPayloadConverter: JacksonJsonPayloadConverter =
      new JacksonJsonPayloadConverter(
        mapper,
      )

    val dataConverter: DefaultDataConverter = DefaultDataConverter
      .newDefaultInstance()
      .withPayloadConverterOverrides(scalaJacksonJsonPayloadConverter)
    dataConverter
  }

  // This helper method helps with IntelliJ/newWorkflowStub
  def workflowStubBuilder[W: ClassTag: IsWorkflow](
      client: ZWorkflowClient,
  ): ZWorkflowStubBuilderTaskQueueDsl[W] =
    client.newWorkflowStub[W]

  override def spec: Spec[TestEnvironment with Scope, Any] = suite("repro")(
    test("either") {
      ZIO
        .serviceWithZIO[ZTestWorkflowEnvironment[Any]] { testEnv =>
          val worker   = testEnv.newWorker("repro")
          worker.addWorkflow[TestWorkflow].from(TestWorkflowImpl())
          val activity = TestActivityImpl()(testEnv.activityOptions)
          worker.addActivityImplementation[TestActivity](activity)

          testEnv.use() {
            for {
              stub <- workflowStubBuilder[TestWorkflow](
                        testEnv.workflowClient,
                      ).withTaskQueue("repro").withWorkflowId("abc123").build
              _    <- ZWorkflowStub.execute(stub.start)
            } yield assertCompletes
          }
        }
    },
  ).provide(
    ZLayer.succeed(
      ZTestEnvironmentOptions.default
        .withWorkflowClientOptions(
          ZWorkflowClientOptions.default.withDataConverter(
            dataConverter, // also passes with dataConverter2
          ),
        ),
    ),
    ZTestWorkflowEnvironment.make[Any],
  )
}
