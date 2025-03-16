package cmd

import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object Users : IntIdTable() {
}

object UserDetails : IntIdTable() {
    val userId = integer("user_id").references(Users.id)
    val name = varchar("name", 50)
    val age = integer("age")
}

class User(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<User>(Users)

    val detail by UserDetail.backReferencedOn(UserDetails.userId)
}

class UserDetail(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<UserDetail>(UserDetails)

    var user by User referencedOn UserDetails.userId
    var name by UserDetails.name
    var age by UserDetails.age
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

        val user = User.new(15) {
        }

        UserDetails.insert {
            it[userId] = user.id.value
            it[name] = "b"
            it[age] = 27
        }

        println(User.all().joinToString { it.detail.name })
    }
}
