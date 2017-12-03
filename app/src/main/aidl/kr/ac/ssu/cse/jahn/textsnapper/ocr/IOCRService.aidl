// IOCRService.aidl
package kr.ac.ssu.cse.jahn.textsnapper.ocr;
import android.content.Intent;
import kr.ac.ssu.cse.jahn.textsnapper.ocr.IOCRServiceCallback;
// Declare any non-default types here with import statements

interface IOCRService {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString);
    oneway void setCallback(IOCRServiceCallback callback);
    void startOCR(in Intent intent);
}
