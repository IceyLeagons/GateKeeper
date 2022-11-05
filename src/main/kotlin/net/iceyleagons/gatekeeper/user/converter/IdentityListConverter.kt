package net.iceyleagons.gatekeeper.user.converter

import net.iceyleagons.gatekeeper.user.Identity
import org.json.JSONArray
import org.json.JSONObject
import javax.persistence.AttributeConverter
import javax.persistence.Converter

@Converter
class IdentityListConverter : AttributeConverter<MutableList<Identity>, String> {

    override fun convertToDatabaseColumn(attribute: MutableList<Identity>?): String {
        val array = JSONArray()
        attribute?.forEach {
            array.put(IdentityConverter.toJson(it))
        }

        return array.toString()
    }

    override fun convertToEntityAttribute(dbData: String?): MutableList<Identity> {
        val array = JSONArray(dbData)
        val result: MutableList<Identity> = ArrayList()

        array.forEach {
            if (it is JSONObject) {
                result.add(IdentityConverter.fromJson(it))
            }
        }

        return result
    }
}