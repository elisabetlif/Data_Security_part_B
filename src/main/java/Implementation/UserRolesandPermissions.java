package Implementation;


import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UserRolesandPermissions {
    private static final String JSON_FILE = "src\\main\\java\\resource\\PublicFile.json";

    private static final String FAIL_MESSAGE1 = "Failed to get user role";

    private static final List<String> FAIL_MESSAGE2 = new ArrayList<>(); // Return an empty list on failure

    public String getRole(String username){
        try{
        //parsing the PublicFile.json
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(new File(JSON_FILE));
            JsonNode usersNode = rootNode.path("users");

            for (JsonNode userNode : usersNode) {
                String storedUsername = userNode.path("user_id").asText();
                String storedRole = userNode.path("role").asText();
            
                if (storedUsername.equalsIgnoreCase(username)) {
                    return storedRole;
                }
        
    }}
    catch (IOException e) {
        System.err.println("Error reading the JSON file: " + e.getMessage());
        e.printStackTrace();
    }
    return FAIL_MESSAGE1;
}

    public List<String> getPermissions(String username){
        
        try{
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(new File(JSON_FILE));
            JsonNode usersNode = rootNode.path("users");

            for (JsonNode userNode : usersNode) {
                String storedUsername = userNode.path("user_id").asText();
                JsonNode permissionsNode = userNode.path("permissions");

                if (storedUsername.equalsIgnoreCase(username)) {
                    List<String> permissions = new ArrayList<>();
                    for (JsonNode permissionNode : permissionsNode) {
                        permissions.add(permissionNode.asText());
                    }
                    return permissions;
                }
        }
    } catch (IOException e) {
        // Log the error if the file is not found or other IO issues occur
        System.err.println("Error reading the JSON file: " + e.getMessage());
        e.printStackTrace();
    }
    return FAIL_MESSAGE2;
    }

} 







