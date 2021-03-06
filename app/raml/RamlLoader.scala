package raml

import java.io.File
import javax.inject.{Inject, Singleton}

import models.{RamlNotFoundException, RamlParseException, RamlUnsupportedVersionException}
import org.raml.v2.api.loader._
import org.raml.v2.api.{RamlModelBuilder, RamlModelResult}

import scala.collection.JavaConversions._
import scala.util.{Failure, Success, Try}

trait RamlLoader {

  private val RamlDoesNotExist = "Raml does not exist at:"
  private val unsupportedSpecVersion: Try[RAML] = Failure(RamlUnsupportedVersionException("Only RAML1.0 is supported"))

  def load(resource: String): Try[RAML]

  protected def verify(result: RamlModelResult): Try[RAML] = {
    result.getValidationResults.toSeq match {
      case Nil => Option(result.getApiV10).fold(unsupportedSpecVersion) { api => Success(api) }
      case errors => {
        val msg = errors.map(e => transformError(e.toString)).mkString("; ")
        if (msg.contains(RamlDoesNotExist)) Failure(RamlNotFoundException(msg))
        else Failure(RamlParseException(msg))
      }
    }
  }

  protected def transformError(msg: String) = msg
}

class ClasspathRamlLoader extends RamlLoader {
  override def load(classpath: String) = {
    val builder = new RamlModelBuilder(new CompositeResourceLoader(
      new ClassPathResourceLoader(),
      new UrlResourceLoader()
    ))

    verify(builder.buildApi(classpath))
  }
}

class FileRamlLoader extends RamlLoader {
  override def load(filepath: String) = {
    val file = new File(filepath)
    val ramlRoot = file.getParentFile
    val filename = file.getName
    val builder = new RamlModelBuilder(new FileResourceLoader(ramlRoot))
    verify(builder.buildApi(filename))
  }
}

class StringRamlLoader extends RamlLoader {
  override def load(content: String) = {
    val builder = new RamlModelBuilder()
    val api = builder.buildApi(content, "")
    verify(api)
  }
}

class UrlRamlLoader extends RamlLoader {
  override def load(url: String) = {
    val builder = new RamlModelBuilder(new UrlResourceLoader())
    verify(builder.buildApi(url))
  }
}

class CombinedRamlLoader @Inject()(fileRamlLoader: FileRamlLoader, urlRamlLoader: UrlRamlLoader) extends RamlLoader {
  override def load(url: String) = {
    url match {
      case ramlFileUrl if ramlFileUrl.startsWith("http") => urlRamlLoader.load(ramlFileUrl)
      case ramlFileUrl => fileRamlLoader.load(ramlFileUrl)
    }
  }
}
