package fr.pickaria.emerald

import fr.pickaria.bookshelf.permissions.PluginPermissions

internal object EconomyPermissions : PluginPermissions("emerald") {
    // Pay
    val payCommand = commandPermission("pay")

    // Balance
    val balanceCommand = commandPermission("balance")

    // Balance top
    val balanceTopCommand = commandPermission("balanceTop")

    // Eco command
    val ecoCommand = adminCommandPermission("eco")
    val ecoGiveCommand = adminCommandPermission("eco.give")
    val ecoGiveVirtualCommand = adminCommandPermission("eco.give.virtual")
    val ecoGivePhysicalCommand = adminCommandPermission("eco.give.physical")
    val ecoBalanceCommand = commandPermission("eco.balance")
}
