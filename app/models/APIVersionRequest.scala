package models

case class Scope(key: String, name: String)

case class APIVersionCreateRequest(
                       context: String,
                       apiName: String,
                       apiDescription: String,
                       version: String,
                       serviceBaseUrl: String,
                       status: APIStatus.Value,
                       endpoints: Seq[Endpoint])

case class Endpoint(
                     uriPattern: String,
                     endpointName: String,
                     method: HttpMethod.Value,
                     authType: AuthType.Value,
                     scope: Option[String] = None,
                     queryParameters: Seq[Parameter] = Seq.empty)

case class Parameter(name: String, required: Boolean = false)

object APIStatus extends Enumeration {
  type APIStatus = Value
  val PROTOTYPED, PUBLISHED, DEPRECATED, RETIRED = Value
}

object AuthType extends Enumeration {
  type AuthType = Value
  val NONE, APPLICATION, USER = Value
}

object HttpMethod extends Enumeration {
  type HttpMethod = Value
  val GET, POST, PUT, DELETE, OPTIONS = Value
}
