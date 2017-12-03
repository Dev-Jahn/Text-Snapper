// IOCRServiceCallback.aidl
package kr.ac.ssu.cse.jahn.textsnapper.ocr;
import android.os.Message;
// Declare any non-default types here with import statements

interface IOCRServiceCallback {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString);
    void sendResult(in Message msg);
}
