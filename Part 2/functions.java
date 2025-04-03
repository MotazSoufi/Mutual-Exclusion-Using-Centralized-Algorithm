import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Random;

public class functions {
	
	public static String[] arrayLetters = {"0","1","9","a","b","c","^",
    		"r","s","t","~","&","%"};

	public static ArrayList<String> getRandPasswords(int number, int length) {
		String password = "";
		ArrayList<String> results = new ArrayList<String>();
		Random rand = new Random();
		for (int j=0; j<number; j++) {
			for (int i=0; i<length; i++) {
		    	String randomElement = arrayLetters[rand.nextInt(arrayLetters.length)];
		    	password += randomElement;
		    }
			results.add(password);
			password = "";
		}
		return results;
    }
	
	
	public static String applySha256(String input){
		
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256"); // Use "SHA-256"
	        
			//Applies sha256 to our input, 
			byte[] hash = digest.digest(input.getBytes("UTF-8"));
	        
			StringBuffer hexString = new StringBuffer(); // This will contain hash as hexidecimal
			for (int i = 0; i < hash.length; i++) {
				String hex = Integer.toHexString(0xff & hash[i]);
				if(hex.length() == 1) hexString.append('0');
				hexString.append(hex);
			}
			return hexString.toString();
		}
		catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static String compareHashes(ArrayList<String> results, String hash) {
    	String password = " Cracked password is ";
    	for (int i = 0; i<results.size()-1; i++) {
			String pass = applySha256(results.get(i));
			if(hash.equals(pass)) {
				password += results.get(i);
			}
		}
    	return password;
    }
	
	
	public static ArrayList<String> generatePasswords(String[] array, String current, int length) {
        ArrayList<String> results = new ArrayList<>();
        if (current.length() == length) {
            results.add(current);
        } else {
            for (int i = 0; i < array.length; i++) {
                ArrayList<String> intermediate = generatePasswords(array, current + array[i], length);
                results.addAll(intermediate);
            }
        }
        return results;
    }

}
