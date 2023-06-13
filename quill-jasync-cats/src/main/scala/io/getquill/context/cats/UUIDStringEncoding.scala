package io.getquill.context.cats

import java.util.UUID

trait UUIDStringEncoding {
  this: CatsJAsyncContext[_, _, _] =>

  implicit val uuidEncoder: Encoder[UUID] = encoder[UUID]((v: UUID) => v.toString, SqlTypes.UUID)

  implicit val uuidDecoder: Decoder[UUID] =
    AsyncDecoder(SqlTypes.UUID)((index: Index, row: ResultRow, session: Session) =>
      row.get(index) match {
        case value: String => UUID.fromString(value)
      }
    )
}