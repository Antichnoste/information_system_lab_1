package org.example.lab_1.security;
import java.security.*;
import java.util.*;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
public final class Passwords {
 private static final int ITERATIONS=210000;
 private Passwords() {}
 public static String hash(String password) {
  byte[] salt=new byte[16]; new SecureRandom().nextBytes(salt);
  return ITERATIONS+":"+Base64.getEncoder().encodeToString(salt)+":"+Base64.getEncoder().encodeToString(derive(password,salt,ITERATIONS));
 }
 public static boolean verify(String password,String stored) {
  try {
   String[] parts=stored.split(":");
   return MessageDigest.isEqual(Base64.getDecoder().decode(parts[2]),derive(password,Base64.getDecoder().decode(parts[1]),Integer.parseInt(parts[0])));
  } catch(IllegalArgumentException e) { return false; }
 }
 private static byte[] derive(String password,byte[] salt,int iterations) {
  PBEKeySpec spec=new PBEKeySpec(password.toCharArray(),salt,iterations,256);
  try { return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded(); }
  catch(GeneralSecurityException e) { throw new IllegalStateException(e); }
  finally { spec.clearPassword(); }
 }
}
