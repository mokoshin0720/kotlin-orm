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

class UserDetail(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<UserDetail>(UserDetails)
    
    var user by User referencedOn UserDetails.userId
    var name by UserDetails.name
    var age by UserDetails.age
}

class User(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<User>(Users) {
        fun create(model: UserDomainModel): User {
            val user = User.new(15) {
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
    val id: Int,
    val name: String,
    val age: Int
) {
    companion object {
        fun create(name: String, age: Int): UserDomainModel {
            return UserDomainModel(id=1, name=name, age=age)
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
