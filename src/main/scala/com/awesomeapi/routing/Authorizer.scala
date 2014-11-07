package com.awesomeapi.routing

import spray.routing._
import com.awesomeapi._
import com.awesomeapi.domain.{UserRoles}
import scala.util.control.Breaks._
import spray.routing.RequestContext


trait Authorizer extends HttpService {
  type PathWithIds = (String, Array[(String, Int)])

  val action: Map[String, String] =  Map("POST" -> "create",
                                         "GET" -> "read",
                                         "PUT" -> "update",
                                         "DELETE" -> "delete")

  val prefixPath: Array[String] = Array("/v1/auth/",
                                    "/v1/config/")

  def authorize(ctx: RequestContext, user: Int): Directive0 = {
    if (authorized(ctx, user)){
      pass
    } else {
      complete(Error.forbidden.code, Error.forbidden.toJson)
    }
  }

  private def authorized(ctx: RequestContext, user: Int): Boolean = {
    val valuesPath = extractPath(ctx.request.uri.path.toString())
    val path: String = valuesPath._1
    val method: String = action.get(ctx.request.method.toString()).get
    val ids: Array[(String, Int)]  = valuesPath._2

    PermissionAuth.permissions.contains(path) && permissionMatchesWithUsers(path, method, user)
  }

  private def permissionMatchesWithUsers(path: String, method: String, user: Int): Boolean = {
    val permissionActions = PermissionAuth.permissions.get(path).get.get("actions").get
    permissionActions.contains(method) && permissionMatchesWithActions(permissionActions, method, user)

  }

  private def permissionMatchesWithActions(actions: PermissionAuth.ActionType, method: String, userId: Int): Boolean = {
    val permissionRoles = actions.get(method).get.get("roles").get
    val permissionUsers = actions.get(method).get.get("users").get
    val roles: Set[(Int, String)] = UserRoles.findByUser(userId).toSet

    permissionUsers.contains(userId) || roles.map(_._1).subsetOf(permissionRoles)
  }
  private def extractPath(path: String): PathWithIds = {
    var shortPath: String = ""
    var idsWithEntity: Array[(String, Int)] = Array()
    prefixPath.foreach{ s =>
      if(path.startsWith(s)){
        shortPath = path.replace(s, "")
        val path_parts = shortPath.split("/")
        if(path_parts.length % 2 == 0 && path_parts.length > 1 ){
          replacePath(path_parts) match {
            case (path, ids) =>{
              shortPath = path
              idsWithEntity = ids
            }
          }
        }else{
          shortPath = "/" + shortPath
        }
      }
    }
    (shortPath, idsWithEntity)
  }

  private def replacePath(segments: Array[String]): PathWithIds = {
    var path: String = ""
    var idWithEntity: Array[(String, Int)] = Array()

    for(i <- 0 to segments.length-1){
      if(i%2 != 0){
        Uuid.decode(segments(i)) match {
          case (id, signature) => {
            idWithEntity :+ (signature, id)
          }
        }
        segments(i) = "/*"
      }else{
        segments(i) = "/" + segments(i)
      }
      path += segments(i)
    }
    (path, idWithEntity)
  }
}

object PermissionAuth {
  import com.awesomeapi.domain.{Permissions, PermissionUsers, PermissionRoles}
  type Permission = Map[String,Any]

  type IdsType = Map[String, Set[Int]]
  type ActionType = Map[String, IdsType]
  type ActionsType = Map[String, ActionType]
  type PathsType = Map[String, ActionsType]

  var permissions: PathsType = Map()

  def load_permissions: Unit = {
    if(permissions.isEmpty) permissions.empty

    Permissions.all.foreach { x =>
      val users: IdsType = Map("users" -> PermissionUsers.findUserIdsByPermission(x.id.get).toSet)
      val roles: IdsType = Map("roles" -> PermissionRoles.findRoleIdsByPermission(x.id.get).toSet)
      val actions: ActionType = Map(x.action -> (roles ++ users))
      var action: ActionsType = Map()

      if(permissions.contains(x.path)){
        var actionsPermission: ActionType = permissions.get(x.path).get.get("actions").get
        actionsPermission = actionsPermission ++ actions
        action = (permissions.get(x.path).get + ("actions" -> actionsPermission))
      }else{
        action = Map("actions" -> actions)
      }

      permissions = (permissions ++ Map(x.path -> action))
    }
  }

  def reload_permissions: Unit = load_permissions
}
