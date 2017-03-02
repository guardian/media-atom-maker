import integration.IntegrationTests
import org.scalatest.Suites
import test.ThriftUtilSpec

class UnitTestsSuite extends Suites(new ThriftUtilSpec)

class IntegrationTestSuite extends Suites(new IntegrationTests)
