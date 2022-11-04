package net.iceyleagons.gatekeeper.sqlite

import org.hibernate.dialect.identity.IdentityColumnSupportImpl

class SQLiteIdentityColumnSupport : IdentityColumnSupportImpl() {

    override fun supportsIdentityColumns() = true

    override fun getIdentitySelectString(table: String?, column: String?, type: Int): String = "select last_insert_rowid()"

    override fun getIdentityColumnString(type: Int): String = "integer"

}