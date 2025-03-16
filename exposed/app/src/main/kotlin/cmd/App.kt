package cmd

import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

object Users : UUIDTable() {
}

object UserDetails : UUIDTable() {
    val userId = uuid("user_id").references(Users.id)
    val name = varchar("name", 50)
    val age = integer("age")
}

class UserDetail(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<UserDetail>(UserDetails)
    
    var user by User referencedOn UserDetails.userId
    var name by UserDetails.name
    var age by UserDetails.age
}

class User(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<User>(Users) {
        fun create(model: UserDomainModel): User {
            val user = User.new(model.id) {
            }
    
            UserDetails.insert {
                it[userId] = user.id.value
                it[name] = model.name
                it[age] = model.age
            }
            
            return user
        }
    }

    val detail by UserDetail.backReferencedOn(UserDetails.userId)
}

data class UserDomainModel(
    val id: UUID,
    val name: String,
    val age: Int
) {
    companion object {
        fun create(name: String, age: Int): UserDomainModel {
            return UserDomainModel(id=UUID.randomUUID(), name=name, age=age)
        }
    }
}

fun main() {
    Database.connect(
        "jdbc:postgresql://localhost:5432/exposed",
        "org.postgresql.Driver",
        "postgres",
        "password"
    )

    transaction {
        addLogger(StdOutSqlLogger)

        SchemaUtils.drop(UserDetails)
        SchemaUtils.drop(Users)
        SchemaUtils.create(Users)
        SchemaUtils.create(UserDetails)

        val user = UserDomainModel.create(name="shinya", age=25)

        User.create(user)
        
        println(User.all().joinToString { it.detail.name })
    }
}
