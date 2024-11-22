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
    private static final List<String> FAIL_MESSAGE2 = new ArrayList<>();

    /**
     * Retrieves the role of a specified user
     * 
     * @param username The username of the user whose role is to be retireved.
     * @return The user's role as a String, or a faliure message
     */
    public String getRole(String username) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(new File(JSON_FILE));
            JsonNode usersNode = rootNode.path("users");

            for (JsonNode userNode : usersNode) {
                String storedUsername = userNode.path("user_id").asText();
                String storedRole = userNode.path("role").asText();

                if (storedUsername.equalsIgnoreCase(username)) {
                    return storedRole;
                }

            }
        } catch (IOException e) {
            System.err.println("Error reading the JSON file: " + e.getMessage());
            e.printStackTrace();
        }
        return FAIL_MESSAGE1;
    }

    /**
     * Retrieves the permissions of a specified user
     * @param username The username of the user whose permissions are to be retrieved
     * @return The user's role as a String, or a faliure message
     */
    public List<String> getPermissions(String username) {
        try {
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
            System.err.println("Error reading the JSON file: " + e.getMessage());
            e.printStackTrace();
        }
        return FAIL_MESSAGE2;
    }

}
