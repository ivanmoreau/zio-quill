package io.getquill.context.cats

import com.github.jasync.sql.db.postgresql.PostgreSQLConnection
import com.github.jasync.sql.db.{QueryResult => DBQueryResult}
import io.getquill.ReturnAction.{ReturnColumns, ReturnNothing, ReturnRecord}
import io.getquill.context.cats.jasync.{ArrayDecoders, ArrayEncoders}
import io.getquill.util.Messages.fail
import io.getquill.{NamingStrategy, PostgresDialect, ReturnAction}

import scala.jdk.CollectionConverters._
import com.github.jasync.sql.db.pool.ConnectionPool
import com.typesafe.config.Config
import io.getquill.util.LoadConfig

class PostgresCatsJAsyncContext[+N <: NamingStrategy](naming: N, pool: ConnectionPool[PostgreSQLConnection])
    extends CatsJAsyncContext[PostgresDialect, N, PostgreSQLConnection](PostgresDialect, naming, pool)
    with ArrayEncoders
    with ArrayDecoders
    with UUIDObjectEncoding {

  def this(naming: N, config: PostgresJAsyncContextConfig) = this(naming, config.pool)
  def this(naming: N, config: Config) = this(naming, PostgresJAsyncContextConfig(config))
  def this(naming: N, configPrefix: String) = this(naming, LoadConfig(configPrefix))

  override protected def extractActionResult[O](returningAction: ReturnAction, returningExtractor: Extractor[O])(
    result: DBQueryResult
  ): List[O] =
    result.getRows.asScala.toList.map(row => returningExtractor(row, ()))

  override protected def expandAction(sql: String, returningAction: ReturnAction): String =
    returningAction match {
      // The Postgres dialect will create SQL that has a 'RETURNING' clause so we don't have to add one.
      case ReturnRecord => s"$sql"
      // The Postgres dialect will not actually use these below variants but in case we decide to plug
      // in some other dialect into this context...
      case ReturnColumns(columns) => s"$sql RETURNING ${columns.mkString(", ")}"
      case ReturnNothing          => s"$sql"
    }

}