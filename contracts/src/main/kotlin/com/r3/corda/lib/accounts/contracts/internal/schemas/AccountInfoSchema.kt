package com.r3.corda.lib.accounts.contracts.internal.schemas

import com.r3.corda.lib.accounts.contracts.states.AccountStatus
import net.corda.core.identity.Party
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import java.time.Instant
import java.util.*
import javax.persistence.*

object AccountsContractsSchema

object AccountsContractsSchemaV1 : MappedSchema(
        schemaFamily = AccountsContractsSchema::class.java,
        version = 1,
        mappedTypes = listOf(PersistentAccountInfo::class.java)
)

@Entity
@Table(name = "accounts", uniqueConstraints = [
    UniqueConstraint(name = "id_constraint", columnNames = ["identifier"]),
    UniqueConstraint(name = "host_and_name_constraint", columnNames = ["host", "name"])
], indexes = [
    Index(name = "accountId_idx", columnList = "identifier"),
    Index(name = "accountHost_idx", columnList = "host"),
    Index(name = "name_idx", columnList = "name")
])
data class PersistentAccountInfo(
        @Column(name = "identifier", unique = true, nullable = false)
        val id: UUID,
        @Column(name = "name", unique = false, nullable = false)
        val name: String,
        @Column(name = "host", unique = false, nullable = false)
        val host: Party,
        //Added extra auth fields
        @Column(name = "firstName", unique = false, nullable = true)
        val firstName: String?,
        @Column(name = "lastName", unique = false, nullable = true)
        val lastName: String?,
        @Column(name = "phone", unique = false, nullable = true)
        val phone: String?,
        @Column(name = "email", unique = false, nullable = true)
        val email: String?,
        @Column(name = "passwordHash", unique = false, nullable = true)
        val passwordHash: String?,
        @Column(name = "accountNumber", unique = true, nullable = true)
        val accountNumber: String?,
        @Column(name = "authId", unique = false, nullable = true)
        val authId: Int = 0,
        @Column(name = "status")
        val status: AccountStatus = AccountStatus.INACTIVE,
        @Column(name = "validEmail")
        val validEmail: Boolean = false,
        @Column(name = "validPhone")
        val validPhone: Boolean = false,
        val createDate: Instant = Instant.now()
        //----------- end of auth extra fields
) : PersistentState()

/*
data class PersistentAccountInfo(
        @Column(name = "identifier", unique = true, nullable = false)
        val id: UUID,
        @Column(name = "name", unique = false, nullable = false)
        val name: String,
        @Column(name = "host", unique = false, nullable = false)
        val host: Party
) : PersistentState()*/
