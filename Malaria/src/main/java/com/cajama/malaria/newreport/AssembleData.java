package com.cajama.malaria.newreport;

import android.content.Context;
import android.text.format.Time;
import android.util.Base64;
import android.util.Log;

import com.cajama.malaria.encryption.AES;
import com.cajama.malaria.encryption.RSA;

import java.io.File;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by GMGA on 8/5/13.
 */
public class AssembleData {
    ArrayList<String> entryList,fileList, accountData;
    String USERNAME;
    Context c;

    private static final String PATIENT_TXT_FILENAME = "textData.xml";
    private static final String ACCOUNT_TXT_FILENAME = "accountData.xml";
    private static final String PATIENT_ZIP_FILENAME = "entryData.zip";
    private static final String AES_FILENAME = "cipherZipFile.zip";

    public AssembleData(Context c,ArrayList<String> entryList ,ArrayList<String> fileList, ArrayList<String> accountData, String USERNAME){
        this.c = c;
        this.entryList=entryList;
        this.fileList = fileList;
        this.accountData = accountData;
        this.USERNAME = USERNAME;
    }
    private String[] getFirstZipArray(){
        try{
            fileList.add(0,c.getExternalFilesDir(null).getPath() + "/" + PATIENT_TXT_FILENAME);
        }
        catch (Exception e){
            Log.v("Error", "arrayList error");
        }
        String[] entryData = new String[fileList.size()];

        return fileList.toArray(entryData);

    }

    private String[] getSecondZipArray(){
        String[] travelData = new String[2];
        travelData[0] = c.getExternalFilesDir(null).getPath() + "/" + ACCOUNT_TXT_FILENAME;
        travelData[1] = c.getExternalFilesDir(null).getPath() + "/" + AES_FILENAME;
        return travelData;
    }
    public void start(){

        //create patient details file
        File entryFile = new File (c.getExternalFilesDir(null), PATIENT_TXT_FILENAME);
        MakeTextFile patient = new MakeTextFile(entryFile,entryList, false);
        patient.writeTextFile();

        //compress patient data file and images to a zip file
        File zipFile1 = new File (c.getExternalFilesDir(null), PATIENT_ZIP_FILENAME);
        Compress firstZip = new Compress(getFirstZipArray(),zipFile1.getPath());
        firstZip.zip();

        //hash secret key
        try {
            Log.v("AES","Start AES");
            byte[] skByte = accountData.get(1).getBytes("UTF-8");
            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            skByte = sha.digest(skByte);
            skByte = Arrays.copyOf(skByte, 16); // use only first 128 bit
            SecretKeySpec secretKey = new SecretKeySpec(skByte, "AES");

            Log.v("SeckretKeybase64", Base64.encodeToString(skByte,Base64.DEFAULT));

            //AES encrypt patient zip file
            //Log.v("AES","new AES");
            AES aes = new AES(secretKey);
            File AESFile = new File(c.getExternalFilesDir(null),AES_FILENAME);
            aes.encryptAES(zipFile1,AESFile);
            Log.v("AES","end AES");
            //decryption test
            /*File test = new File(c.getExternalFilesDir(null),"clearZip.zip");
            aes.decryptAES(AESFile,test);*/

            //RSA encrypt private key
            Log.v("ENCRYPTION","Start RSA");
            //RSA rsa = new RSA();
            RSA rsa = new RSA(skByte);
            //Log.v("ENCRYPTION","set RSA");
            Log.v("ENCRYPTION", "Private key:" + Base64.encodeToString(skByte,Base64.DEFAULT));
            accountData.set(1, rsa.encryptRSA(skByte));

            Log.v("ENCRYPTION","End RSA");

            //RSA decryption test
            //Log.v("ENCRYPTION", rsa.decryptRSA(Base64.decode(accountData.get(1),Base64.DEFAULT)));
        } catch (Exception e){
            Log.v("Encryption","exception" + e);
        }

        //create private key text file
        File accountFile = new File(c.getExternalFilesDir(null),ACCOUNT_TXT_FILENAME);
        MakeTextFile account = new MakeTextFile(accountFile,accountData, false);
        account.writeTextFile();

        //compress patient zip file and private key text file to a 2nd zip file
        Time today = new Time(Time.getCurrentTimezone());
        today.setToNow();
        File zipFile2 = new File (c.getExternalFilesDir("ZipFiles"), today.format("%m%d%Y_%H%M%S")+"_"+ USERNAME + ".zip");
        Compress secondZip = new Compress(getSecondZipArray(),zipFile2.getPath());
        secondZip.zip();
    }
}