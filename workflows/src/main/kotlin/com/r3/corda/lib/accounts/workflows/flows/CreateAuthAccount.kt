package com.r3.corda.lib.accounts.workflows.flows

import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.accounts.contracts.commands.Create
import com.r3.corda.lib.accounts.contracts.states.AccountInfo
import com.r3.corda.lib.accounts.workflows.accountService
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.StartableByRPC
import net.corda.core.flows.StartableByService
import net.corda.core.transactions.TransactionBuilder
import java.util.*

/**
 * A flow to create a new account.
 * The flow will fail if an account already exists with the provided [name] or [identifier].
 * Pass it big account object to create an account.
 *
 * @property ai the proposed account info details for the new account.
 */
@StartableByService
@StartableByRPC
class CreateAuthAccount private constructor(
        private val ai:AccountInfo
) : FlowLogic<StateAndRef<AccountInfo>>() {

    @Suspendable
    override fun call(): StateAndRef<AccountInfo> {
        // There might be another account on this node with the same name... That's OK as long as the host is another
        // node. This can happen because another node shared that account with us. However, there cannot be two accounts
        // with the same name with the same host node.
        require(accountService.accountInfo(ai.name).none { it.state.data.host == ourIdentity }) {
            "There is already an account registered with the specified name ${ai.name}."
        }
        require(accountService.accountInfo(ai.identifier.id) == null) {
            "There is already an account registered with the specified identifier $ai.identifier.id."
        }
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val newAccountInfo = AccountInfo(
                name = ai.name,
                host = ourIdentity,
                identifier = UniqueIdentifier(id = ai.identifier.id)
        )
        newAccountInfo.updateOptionalFields(
                accountNumber = ai.accountNumber,
                firstName = ai.firstName,
                lastName = ai.lastName,
                passwordHash = ai.passwordHash,
                email = ai.email,
                phone = ai.phone,
                displayName = ai.displayName
        )

        val transactionBuilder = TransactionBuilder(notary = notary).apply {
            addOutputState(newAccountInfo)
            addCommand(Create(), ourIdentity.owningKey)
        }
        val signedTransaction = serviceHub.signInitialTransaction(transactionBuilder)
        val finalisedTransaction = subFlow(FinalityFlow(signedTransaction, emptyList()))
        return finalisedTransaction.coreTransaction.outRefsOfType<AccountInfo>().single()
    }
}