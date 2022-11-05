package net.iceyleagons.gatekeeper.user.converter

import net.iceyleagons.gatekeeper.user.Identity
import org.json.JSONObject
import javax.persistence.AttributeConverter
import javax.persistence.Converter

@Converter
class IdentityConverter : AttributeConverter<Identity, String> {

    override fun convertToDatabaseColumn(attribute: Identity?): String {
        return toJson(attribute!!).toString()
    }

    override fun convertToEntityAttribute(dbData: String?): Identity {
        return fromJson(JSONObject(dbData))
    }

    companion object {
        fun fromJson(json: JSONObject): Identity {
            return Identity(
                    json["providerId"] as String,
                    json["providerAccessToken"] as String,
                    json["providerAccessTokenExpires"] as Long,
                    json["providerRefreshToken"] as String,
                    json.getJSONObject("data").toMap()
            )
        }

        fun toJson(identity: Identity): JSONObject {
            val json = JSONObject()

            json.put("providerId", identity.providerId)
            json.put("providerAccessToken", identity.providerAccessToken)
            json.put("providerAccessTokenExpires", identity.providerAccessTokenExpires)
            json.put("providerRefreshToken", identity.providerRefreshToken)
            json.put("data", identity.data)

            return json
        }
    }
}