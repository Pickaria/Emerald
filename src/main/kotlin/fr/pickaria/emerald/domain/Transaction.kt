package fr.pickaria.emerald.domain

class Transaction(private val from: Account, private val to: Account, private val amount: Price) {
    /**
     * Carrying out a monetary transaction from one account to another.
     * @throws BalanceInsufficientException
     * @throws UnknownCurrencyException
     * @throws InvalidAmountException
     * @throws SenderCannotBeRecipientException
     */
    fun perform() {
        if (from.ownerUniqueId == to.ownerUniqueId) {
            throw SenderCannotBeRecipientException()
        }

        from.withdraw(amount)
        to.deposit(amount)
    }
}

class SenderCannotBeRecipientException : Throwable()
