package Implementation;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

public class PasswordProcessing {

    private static final String JSON_FILE = "src\\main\\java\\resource\\PublicFile.json";

    /**
    * Validates a username and password against stored credentials in a JSON file.
    *
    * @param username The username.
    * @param password The plaintext password
    * @return returns true if the username exists and the password is valid;
    *          otherwise false.
    */
    public boolean passwordPros(String username, String password){
        try{
            //parsing the PublicFile.json
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(new File(JSON_FILE));
            JsonNode usersNode = rootNode.path("users");

            for (JsonNode userNode : usersNode) {
                String storedUsername = userNode.path("user_id").asText();
                String storedPasswordHash = userNode.path("hash").asText();

                if (storedUsername.equalsIgnoreCase(username)) {
                    Argon2 argon2 = Argon2Factory.create();
                    char[] passwordChars = password.toCharArray();
                    boolean isValid = argon2.verify(storedPasswordHash, passwordChars);

                    argon2.wipeArray(passwordChars);
                    return isValid; 
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;  
    }
}

//    PasswordVer("Alice", "Fall2019");
//    PasswordVer("GeorgeT", "ILoveManchesterUnited");
//    PasswordVer("Cecilia", "AfterAllThisTime?");
//    PasswordVer("David", "Boston1978");
//    PasswordVer("Erica", "IHatePatricia");
//    PasswordVer("Fred", "DefinitelyNotGeorge");
//    PasswordVer("George", "DefinitelyNotFred");
//    PasswordVer("Henry", "Tequilaismylove")
//    PasswordVer("Ida", "youaresayingitwrong")
