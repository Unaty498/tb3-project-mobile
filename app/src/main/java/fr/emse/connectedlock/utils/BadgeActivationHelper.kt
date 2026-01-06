package fr.emse.connectedlock.utils

import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.MifareClassic
import android.util.Log
import java.nio.charset.Charset
import java.io.IOException

object BadgeActivationHelper {

    private const val TAG = "BadgeActivationHelper"

    fun writeToNfcTag(tag: Tag, badgeNumber: String): Boolean {
        Log.d(TAG, "Attempting to write badge number: $badgeNumber to tag")
        val mfc = MifareClassic.get(tag)

        if (mfc != null) {
            try {
                mfc.connect()
                val sectorIndex = 1
                val blockIndex = mfc.sectorToBlock(sectorIndex) // Usually block 4 for sector 1

                // Authenticate with Key A (Default Factory Key)
                // In a real secure app, DO NOT use KEY_DEFAULT. Use a custom project key.
                val auth = mfc.authenticateSectorWithKeyA(sectorIndex, MifareClassic.KEY_DEFAULT)

                if (auth) {
                    // Prepare data: Pad with 0s to fill 16 bytes
                    val data = ByteArray(16)
                    val bytes = badgeNumber.toByteArray(Charset.forName("UTF-8"))
                    System.arraycopy(bytes, 0, data, 0, bytes.size.coerceAtMost(16))

                    Log.d(TAG, "Writing to Sector $sectorIndex, Block $blockIndex")
                    mfc.writeBlock(blockIndex, data)
                    mfc.close()
                    Log.d(TAG, "Write successful")
                    return true
                } else {
                    Log.e(TAG, "Authentication failed for Sector $sectorIndex")
                }
            } catch (e: IOException) {
                Log.e(TAG, "Error interacting with MifareClassic tag", e)
            } finally {
                try {
                    if (mfc.isConnected) mfc.close()
                } catch (e: Exception) { /* ignore */ }
            }
        } else {
            Log.e(TAG, "Tag is not Mifare Classic compatible")
        }
        return false
    }
}

