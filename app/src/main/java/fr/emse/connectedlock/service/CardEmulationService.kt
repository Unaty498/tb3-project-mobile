package fr.emse.connectedlock.service

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log
import java.nio.charset.Charset

class CardEmulationService : HostApduService() {

    override fun processCommandApdu(commandApdu: ByteArray, extras: Bundle?): ByteArray {
        Log.d(TAG, "Received APDU: ${toHex(commandApdu)}")

        if (!BadgeEmulationState.isEmulating) {
             Log.d(TAG, "Not emulating. Ignoring.")
             return hexStringToByteArray(STATUS_FAILED)
        }

        val command = toHex(commandApdu)

        if (command == SELECT_AID) {
            Log.d(TAG, "Selected AID. Responding with badge ID.")
            val badgeId = BadgeEmulationState.activeBadgeId
            if (badgeId != null) {
                Log.d(TAG, "Responding with badge ID: $badgeId")
                val payload = badgeId.toByteArray(Charset.forName("UTF-8"))
                return concatArrays(payload, hexStringToByteArray(STATUS_SUCCESS))
            }
        }

        return hexStringToByteArray(STATUS_FAILED)
    }

    override fun onDeactivated(reason: Int) {
        Log.d(TAG, "Deactivated: $reason")
    }

    companion object {
        private const val TAG = "CardEmulationService"
        private const val STATUS_SUCCESS = "9000"
        private const val STATUS_FAILED = "6F00"
        private const val SELECT_AID = "00A4040007F0010203040506"

        fun toHex(bytes: ByteArray): String {
            val sb = StringBuilder()
            for (b in bytes) {
                sb.append(String.format("%02X", b))
            }
            return sb.toString()
        }

        fun hexStringToByteArray(s: String): ByteArray {
            val len = s.length
            val data = ByteArray(len / 2)
            for (i in 0 until len step 2) {
                data[i / 2] = ((Character.digit(s[i], 16) shl 4) + Character.digit(s[i + 1], 16)).toByte()
            }
            return data
        }

        fun concatArrays(first: ByteArray, second: ByteArray): ByteArray {
            val result = first.copyOf(first.size + second.size)
            System.arraycopy(second, 0, result, first.size, second.size)
            return result
        }
    }
}

