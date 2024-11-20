package Implementation;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

public class PasswordProcessing {

    private static final String JSON_FILE = "src\\main\\java\\resource\\PublicFile.json";

    public boolean passwordPros(String username, String password){
        System.out.println(username + " ... " + password);
        try{
            //parsing the PublicFile.json
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(new File(JSON_FILE));
            JsonNode usersNode = rootNode.path("users");

            //iterate over the users and check credentials
            for (JsonNode userNode : usersNode) {
                String storedUsername = userNode.path("user_id").asText();
                String storedPasswordHash = userNode.path("hash").asText();

                if (storedUsername.equalsIgnoreCase(username)) {
                    // Verify the password using Argon2
                    Argon2 argon2 = Argon2Factory.create();
                    char[] passwordChars = password.toCharArray();
                    boolean isValid = argon2.verify(storedPasswordHash, passwordChars);

                    // Clean up password array after use
                    argon2.wipeArray(passwordChars);
                    return isValid;  // Return true if the password matches
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;  
    }

    //    PasswordVer("Alice", "Fall2019");
    //    PasswordVer("Bob", "ILoveManchesterUnited");
    //    PasswordVer("Cecilia", "AfterAllThisTime?");
    //    PasswordVer("David", "Boston1978");
    //    PasswordVer("Erica", "IHatePatricia");
    //    PasswordVer("Fred", "DefinitelyNotGeorge");
    //    PasswordVer("George", "DefinitelyNotFred");
}
