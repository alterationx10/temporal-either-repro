import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import io.temporal.activity.{ ActivityInterface, ActivityMethod }
import io.temporal.common.converter.{
  ByteArrayPayloadConverter,
  DefaultDataConverter,
  JacksonJsonPayloadConverter,
  NullPayloadConverter,
  ProtobufJsonPayloadConverter,
}
import io.temporal.workflow.{ WorkflowInterface, WorkflowMethod }
import zio.temporal.activity.{ ZActivity, ZActivityOptions, ZActivityStub }
import zio.temporal.workflow.{
  ZWorkflow,
  ZWorkflowClientOptions,
  ZWorkflowStub,
}
import zio._
import zio.temporal.ZRetryOptions
import zio.temporal.testkit.{
  ZTestEnvironmentOptions,
  ZTestWorkflowEnvironment,
}
import zio.temporal.worker.{ ZWorker, ZWorkerFactoryOptions }
import zio.test._

import java.util.UUID
import scala.collection.Seq

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
    ZActivityStub.execute(stub.either)
  }
}

object TemporalEitherSpec extends ZIOSpecDefault {
  override def spec: Spec[TestEnvironment with Scope, Any] = suite("repro")(
    test("either") {
      for {
        ao <- ZTestWorkflowEnvironment.activityOptions[Any]
        _  <- ZTestWorkflowEnvironment.newWorker("repro") @@
                ZWorker
                  .addWorkflow[TestWorkflow]
                  .from(TestWorkflowImpl()) @@
                ZWorker.addActivityImplementation[TestActivity](
                  TestActivityImpl()(ao),
                )

        _      <- ZTestWorkflowEnvironment.setup()
        stub   <-
          ZTestWorkflowEnvironment
            .newWorkflowStub[TestWorkflow]
            .withTaskQueue("repro")
            .withWorkflowId(UUID.randomUUID().toString)
            .withRetryOptions(ZRetryOptions.default.withMaximumAttempts(1))
            .build
        result <- ZWorkflowStub.execute(
                    stub.start,
                  )
      } yield assertTrue(result == Right(42))
    },
  ).provide(
    Scope.default,
    ZWorkerFactoryOptions.make,
    ZWorkflowClientOptions.make,
    ZTestEnvironmentOptions.make,
    ZTestWorkflowEnvironment.make[Any],
  )
}
